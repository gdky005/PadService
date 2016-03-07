package com.kaolafm.mediaplayer;

import android.content.Context;

import com.gdky005.padservice.utils.KL;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

class MediaFile {
    private final int CHUNK_SIZE;
    private Context mContext;
    private String mLocalFilePath = null;
    private String mUrl = null;
    private RandomAccessFile mAccessFile = null;
    private RandomAccessFile mWriteFile = null;
    private long mContentLength = -1;
    private boolean mIsStreaming = false;
    private int mTotalChunks = 0;
    private BitSet mContentTrackBitSet = null;

    private static final int HEADER_SIZE = 64 * 1024;

    public MediaFile(Context ctx, String source) throws IOException {
        mContext = ctx;
        CHUNK_SIZE = 1024;
        if (source.startsWith("http://")) {
            mUrl = source;
            createLocalFilePath(source);
            File file = new File(mLocalFilePath);
            file.createNewFile();
            mIsStreaming = true;
        } else {
            mLocalFilePath = source;
        }
        mAccessFile = new RandomAccessFile(mLocalFilePath, "r");
        mWriteFile = new RandomAccessFile(mLocalFilePath, "rw");
        if (!mIsStreaming) {
            mContentLength = mAccessFile.length();
        }
    }

    public void read(byte[] dst, int offset, int byteCount) throws IOException {
        if (mAccessFile != null) {
            int pos = (int) mAccessFile.getFilePointer();
            if (!isChunkReady(pos, byteCount)) {
                throw new IOException("Chunk pos = " + pos + " range = " + byteCount + " is not ready" + " file length = " + mContentLength);
            }
            mAccessFile.readFully(dst, offset, byteCount);
        }
    }

    public void seek(long position) throws IOException {
        if (mAccessFile != null) {
            mAccessFile.seek(position);
        }
    }

    public synchronized void write(byte[] data, int offset, int byteCount, long pos) throws IOException {
        if (mWriteFile != null) {
            mWriteFile.seek(pos);
            mWriteFile.write(data, offset, byteCount);
        }
    }

    public String getLocalFilePath() {
        return mLocalFilePath;
    }

    public String getUrl() {
        return mUrl;
    }

    public long getFilePointer() throws IOException {
        return mAccessFile.getFilePointer();
    }

    public long length() {
        return mContentLength;
    }

    public void setLength(long length) {
        mContentLength = length;
    }

    public long getCompletedBytes() {
        if (mAccessFile == null) {
            return 0L;
        }
        try {
            return mAccessFile.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public long buildChunks(long offset) {
        if (mTotalChunks > 0) return mTotalChunks;
        int i = (int) (mContentLength % CHUNK_SIZE);
        int j = (int) (mContentLength / CHUNK_SIZE);
        mTotalChunks = j + (i == 0 ? 0 : 1);
        KL.d("New chunk built = {} size = {}", mTotalChunks, mContentLength);
        try {
            if (mIsStreaming) {
                mContentTrackBitSet = new BitSet(mTotalChunks + 1);
                mContentTrackBitSet.set(mTotalChunks, true);
                markChunks(0, (int) mAccessFile.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mTotalChunks;
    }

    private boolean isChunkReady(int start, int range) {
        if (mContentTrackBitSet != null) {
            int total = start + range;
            int pos = start / CHUNK_SIZE * CHUNK_SIZE;
            while (pos < total) {
                int index = pos / CHUNK_SIZE;
                boolean isIndexSet = mContentTrackBitSet.get(index);
                if (!isIndexSet) {
                    return false;
                }
                pos += CHUNK_SIZE;
            }
        }
        return true;
    }

    public int markChunks(int start, int range) {
        if (mContentTrackBitSet != null) {
            int pos = (start % CHUNK_SIZE == 0) ? start : ((start / CHUNK_SIZE + 1) * CHUNK_SIZE);
            int total = start + range;
            while (pos < total) {
                if (pos + CHUNK_SIZE <= total || pos + CHUNK_SIZE > mContentLength) {
                    mContentTrackBitSet.set(pos / CHUNK_SIZE, true);
                } else {
                    return total - pos;
                }
                pos += CHUNK_SIZE;
            }
            return 0;
        }
        return range;
    }

    public int findNextMissedPosition(int pos) {
        synchronized (mContentTrackBitSet) {
            if (mContentTrackBitSet == null) {
                return 0;
            }
            int index = mContentTrackBitSet.nextClearBit(pos / CHUNK_SIZE);
            return index * CHUNK_SIZE;
        }
    }

    public int findNextMissedRange(int pos) {
        synchronized (mContentTrackBitSet) {
            if (mContentTrackBitSet == null) {
                return 0;
            }
            int nPos = mContentTrackBitSet.nextClearBit(pos / CHUNK_SIZE);
            int target = mContentTrackBitSet.nextSetBit(nPos);
            if (target == -1) {
                return 0;
            } else {
                target = target * CHUNK_SIZE;
                if (target > mContentLength) {
                    target = (int) mContentLength;
                }
            }
            return target - nPos * CHUNK_SIZE;
        }
    }

    public int alignRangeBytes(int start, int range) {
        if (start + range > mContentLength) {
            return (int) (mContentLength - start);
        } else {
            int i = start - start / CHUNK_SIZE * CHUNK_SIZE;
            return ((i + range) / CHUNK_SIZE + 1) * CHUNK_SIZE;
        }
    }

    public boolean parsed() {
        try {
            long len = mAccessFile.length();
            return len >= HEADER_SIZE ? true : false;
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        if (mAccessFile != null) {
            try {
                mAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mAccessFile = null;
        }
        if (mWriteFile != null) {
            try {
                mWriteFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mWriteFile = null;
        }
    }

    private void createLocalFilePath(String url) {
        File directory = new File(getTemporaryFolderPath(mContext));
        if (!directory.exists()) {
            if (!directory.isDirectory()) {
                directory.delete();
            }
            directory.mkdirs();
        }
        mLocalFilePath = getTemporaryFolderPath(mContext) + url.substring(url.lastIndexOf("/"));
    }

    public static String getTemporaryFolderPath(Context ctx) {
        File path = ((ctx.getExternalFilesDir(null) != null) ? ctx.getExternalFilesDir(null) : ctx.getFilesDir());
        return path + "/temp";
    }
}
