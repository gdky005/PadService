package com.kaolafm.mediaplayer;

import com.gdky005.padservice.utils.KL;

import java.io.IOException;

class OpusDecoder extends KaolaDecoder {
    static {
        System.loadLibrary("opusdecoder");
    }

    private int mCurrentPageIndex = 0;
    private int mPreSkip = 0;
    private long mDataStartPosition = 0;
    private long mTotalChunks = 0;
    private long mCurrentFilePosition = 0;
    private long mCurrentGranulePosition = 0;
    private static final int OGG_HEADER_SIZE = 27;
    private static final int FIXED_SAMPLE_RATE = 48000;
    private final int CHUNK_SIZE;
    private MediaFile mMediaFile = null;

    public native static int initOpusDecoder(int channels, int sampleRate);

    public native static void destroyDecoder();

    public native static int decodeOpusData(byte[] data, int length, short[] pcm, int sampleCount);

    public native static int decodeOpusPageData(byte[] lacingData, int lacingLength, byte[] pageData, int length, short[] pcm, int pcmLength);

    public native static int resetDecoder();

    public OpusDecoder() {
        CHUNK_SIZE = getDefaultChunkSize();
        int result = initOpusDecoder(2, FIXED_SAMPLE_RATE);
        KL.i("init opus deocder, result = {}", result);
        reset();
    }

    public void reset() {
        mCurrentFilePosition = mDataStartPosition;
        mCurrentGranulePosition = 0;
    }

    public void release() {
        reset();
        mTotalChunks = 0;
        mPreSkip = 0;
        mDataStartPosition = 0;
        mCurrentFilePosition = 0;
        mCurrentPageIndex = 0;
        mMediaFile = null;
        destroyDecoder();
    }

    public boolean prepare() {
        try {
            if (parseHeader()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean parseHeader() throws Exception {
        if (mMediaFile == null) return false;
        mMediaFile.seek(0);
        byte[] opusHeader = getPageHeader(0);
        long pos = 0;
        if (opusHeader != null && isValidHeader(opusHeader)) {
            int laceLen = ((int) opusHeader[OGG_HEADER_SIZE - 1]) & 0x00FF;
            byte[] lacing = new byte[laceLen];
            mMediaFile.read(lacing, 0, laceLen);
            long pageSize = getPageDataSize(lacing);
            pos += OGG_HEADER_SIZE;
            pos += laceLen;
            pos += pageSize;
            byte[] id = new byte[(int) pageSize];
            mMediaFile.read(id, 0, (int) pageSize);
            mPreSkip = ((id[11] & 0x00FF) << 8) + (id[10] & 0x00FF);
            mMediaFile.seek(pos);
        }
        byte[] commentHeader = getPageHeader(pos);
        if (commentHeader != null && isValidHeader(commentHeader)) {
            int laceLen = ((int) commentHeader[OGG_HEADER_SIZE - 1]) & 0x00FF;
            byte[] lacing = new byte[laceLen];
            mMediaFile.read(lacing, 0, laceLen);
            long pageSize = getPageDataSize(lacing);
            pos += OGG_HEADER_SIZE;
            pos += laceLen;
            pos += pageSize;
            mMediaFile.seek(pos);
            mDataStartPosition = pos;
            mCurrentFilePosition = mDataStartPosition;
            mMediaFile.buildChunks(mDataStartPosition);
            int len = (int) (mMediaFile.length() - mDataStartPosition);
            int i = (int) (len % CHUNK_SIZE);
            int j = (int) (len / CHUNK_SIZE);
            mTotalChunks = j + (i == 0 ? 0 : 1);
            KL.i("Opus header parse result mCurrentFilePosition = {}", mCurrentFilePosition);
            return true;
        }
        return false;
    }

    private class OpusSeekHelper {
        long mTarget = 0;
        long mLeftChunkIndex = 0;
        long mRightChunkIndex = mTotalChunks;
        byte[] mSeekChunk = new byte[CHUNK_SIZE];
        byte[] mSeekHeader = new byte[OGG_HEADER_SIZE];
        long mSeekGranulePosition = 0;
        long mSeekResult = -1;
        boolean mSeekDone = false;
        long mSeekStartOffset = -1;
        long mSeekRange = -1;
    }

    private OpusSeekHelper mSeekHelper = null;

    public void startSeek(long ms) {
        mSeekHelper = new OpusSeekHelper();
        mSeekHelper.mTarget = ms * 48;
    }

    public boolean processSeek() {
        if (mMediaFile == null) {
            return false;
        }
        try {
            if (mSeekHelper != null) {
                KL.i("Search between chunk = {} and chunk = {}", mSeekHelper.mLeftChunkIndex, mSeekHelper.mRightChunkIndex);
                long chunkIndex = mSeekHelper.mLeftChunkIndex + (mSeekHelper.mRightChunkIndex - mSeekHelper.mLeftChunkIndex) / 2;
                long pos = chunkIndex * CHUNK_SIZE + mDataStartPosition;
                long fileLength = mMediaFile.length();
                int len = CHUNK_SIZE;
                if (pos + CHUNK_SIZE > fileLength) {
                    len = (int) (fileLength - pos);
                }
                mSeekHelper.mSeekStartOffset = pos;
                mSeekHelper.mSeekRange = len;
                try {
                    mMediaFile.seek(pos);
                    mMediaFile.read(mSeekHelper.mSeekChunk, 0, len);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                //Seek in chunk to get first granule and last granule
                long first_gran = -1, last_gran = -1;
                int i, j;
                for (i = len - 1; i >= 0; i--) {
                    if (mSeekHelper.mSeekChunk[i] == 'S' && (i - 3) >= 0) {
                        if (mSeekHelper.mSeekChunk[i - 1] == 'g'
                                && mSeekHelper.mSeekChunk[i - 2] == 'g'
                                && mSeekHelper.mSeekChunk[i - 3] == 'O') {
                            i -= 3;
                            if (i + OGG_HEADER_SIZE > len) {
                                continue;
                            } else {
                                System.arraycopy(mSeekHelper.mSeekChunk, i, mSeekHelper.mSeekHeader, 0, OGG_HEADER_SIZE);
                                last_gran = getGranulePostion(mSeekHelper.mSeekHeader);
                            }
                            break;
                        }
                    }
                }
                for (j = 0; j < len; j++) {
                    if (mSeekHelper.mSeekChunk[j] == 'O' && (j + 3) < len) {
                        if (mSeekHelper.mSeekChunk[j + 1] == 'g'
                                && mSeekHelper.mSeekChunk[j + 2] == 'g'
                                && mSeekHelper.mSeekChunk[j + 3] == 'S') {
                            if (j + OGG_HEADER_SIZE > len) {
                                continue;
                            } else {
                                System.arraycopy(mSeekHelper.mSeekChunk, j, mSeekHelper.mSeekHeader, 0, OGG_HEADER_SIZE);
                                first_gran = getGranulePostion(mSeekHelper.mSeekHeader);
                                break;
                            }
                        }
                    }
                }
                KL.i("Get first granule = {} last granule = {} in chunk = {}", first_gran, last_gran, chunkIndex);
                if (first_gran == -1 || last_gran == -1 || first_gran > last_gran) {
                    //TODO: We need to return here for error situation.
                    mSeekHelper.mSeekDone = true;
                    return true;
                }
                if (mSeekHelper.mTarget < first_gran) {
                    long tempIndex = mSeekHelper.mRightChunkIndex;
                    mSeekHelper.mRightChunkIndex = chunkIndex - 1;
                    if (mSeekHelper.mRightChunkIndex == -1) {
                        mSeekHelper.mSeekResult = j + pos;
                        mSeekHelper.mSeekGranulePosition = 0;
                        KL.i("The right chunk is located at {} ,result = {} ,gran = {}" + chunkIndex, mSeekHelper.mSeekResult, mSeekHelper.mSeekGranulePosition);
                        mSeekHelper.mSeekDone = true;
                        return true;
                    }
                    if (mSeekHelper.mLeftChunkIndex > mSeekHelper.mRightChunkIndex) {
                        //We are getting the result
                        KL.i("The right chunk is located between {} and {}", mSeekHelper.mRightChunkIndex, mSeekHelper.mLeftChunkIndex);
                        long pos_s = mSeekHelper.mRightChunkIndex * CHUNK_SIZE + mDataStartPosition;
                        int len_s = CHUNK_SIZE * 2;
                        if (pos_s + CHUNK_SIZE * 2 > fileLength) {
                            len_s = (int) (fileLength - pos_s);
                        }
                        mSeekHelper.mSeekStartOffset = pos_s;
                        mSeekHelper.mSeekRange = len_s;
                        byte[] chunk_s = new byte[len_s];
                        try {
                            mMediaFile.seek(pos_s);
                            mMediaFile.read(chunk_s, 0, len_s);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            mSeekHelper.mRightChunkIndex = tempIndex;
                            return false;
                        }
                        int x = 0;
                        for (int k = j + CHUNK_SIZE; k >= 0; k--) {
                            if (chunk_s[k] == 'S'
                                    && chunk_s[k - 1] == 'g'
                                    && chunk_s[k - 2] == 'g'
                                    && chunk_s[k - 3] == 'O') {
                                x++;
                                k -= 3;
                                if (x == 1) {
                                    mSeekHelper.mSeekResult = k + pos_s;
                                } else {
                                    System.arraycopy(chunk_s, k, mSeekHelper.mSeekHeader, 0, OGG_HEADER_SIZE);
                                    mSeekHelper.mSeekGranulePosition = getGranulePostion(mSeekHelper.mSeekHeader);
                                }
                            }
                            if (x >= 2) {
                                KL.i("Got result = {} ,granule = {}", mSeekHelper.mSeekResult, mSeekHelper.mSeekGranulePosition);
                                break;
                            }
                        }
                        mSeekHelper.mSeekDone = true;
                        return true;
                    }
                } else if (mSeekHelper.mTarget == first_gran) {
                    KL.i("The right chunk is located between {} and {}", (chunkIndex - 1), chunkIndex);

                    if (chunkIndex == 0) {
                        mSeekHelper.mSeekResult = j + pos;
                        mSeekHelper.mSeekGranulePosition = 0;
                    } else {
                        long pos_s = (chunkIndex - 1) * CHUNK_SIZE + mDataStartPosition;
                        int len_s = CHUNK_SIZE * 2;
                        if (pos_s + CHUNK_SIZE * 2 > fileLength) {
                            len_s = (int) (fileLength - pos_s);
                        }
                        byte[] chunk_s = new byte[len_s];
                        mSeekHelper.mSeekStartOffset = pos_s;
                        mSeekHelper.mSeekRange = len_s;
                        try {
                            mMediaFile.seek(pos_s);
                            mMediaFile.read(chunk_s, 0, len_s);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return false;
                        }
                        for (int k = j + CHUNK_SIZE; k >= 0; k--) {
                            if (chunk_s[k] == 'S'
                                    && chunk_s[k - 1] == 'g'
                                    && chunk_s[k - 2] == 'g'
                                    && chunk_s[k - 3] == 'O') {
                                k -= 3;
                                mSeekHelper.mSeekResult = j + pos_s;
                                System.arraycopy(chunk_s, k, mSeekHelper.mSeekHeader, 0, OGG_HEADER_SIZE);
                                mSeekHelper.mSeekGranulePosition = getGranulePostion(mSeekHelper.mSeekHeader);
                                KL.i("Got result = {} ,granule = {}", mSeekHelper.mSeekResult, mSeekHelper.mSeekGranulePosition);
                                break;
                            }
                        }
                    }

                    mSeekHelper.mSeekDone = true;
                    return true;
                } else if (mSeekHelper.mTarget > first_gran && mSeekHelper.mTarget <= last_gran) {
                    KL.i("Get the right chunk = {} target = {}" + chunkIndex, mSeekHelper.mTarget);
                    while (j <= i) {
                        System.arraycopy(mSeekHelper.mSeekChunk, j, mSeekHelper.mSeekHeader, 0, OGG_HEADER_SIZE);

                        KL.i("Valid header = {}", isValidHeader(mSeekHelper.mSeekHeader));

                        long gran = getGranulePostion(mSeekHelper.mSeekHeader);
                        if (mSeekHelper.mTarget <= gran) {
                            mSeekHelper.mSeekResult = j + pos;
                            KL.i("Result file position = {} ,granule = {}", mSeekHelper.mSeekResult, mSeekHelper.mSeekGranulePosition);
                            break;
                        } else {
                            int laceLen = mSeekHelper.mSeekHeader[OGG_HEADER_SIZE - 1] & 0x00FF;
                            byte[] lacing = new byte[laceLen];
                            System.arraycopy(mSeekHelper.mSeekChunk, j + OGG_HEADER_SIZE, lacing, 0, laceLen);
                            mSeekHelper.mSeekGranulePosition = gran;
                            int pageSize = getPageDataSize(lacing);
                            j += (OGG_HEADER_SIZE + laceLen + pageSize);
                        }
                    }
                    mSeekHelper.mSeekDone = true;
                    return true;
                } else {
                    long tempIndex = mSeekHelper.mLeftChunkIndex;
                    mSeekHelper.mLeftChunkIndex = chunkIndex + 1;
                    if (mSeekHelper.mLeftChunkIndex >= mTotalChunks) {
                        mSeekHelper.mSeekDone = true;
                        return true;
                    }
                    if (mSeekHelper.mLeftChunkIndex > mSeekHelper.mRightChunkIndex) {
                        //we are getting the result
                        KL.i("The right chunk is located between {} and {}", mSeekHelper.mRightChunkIndex, mSeekHelper.mLeftChunkIndex);
                        long pos_s = mSeekHelper.mRightChunkIndex * CHUNK_SIZE + mDataStartPosition;
                        int len_s = CHUNK_SIZE * 2;
                        if (pos_s + CHUNK_SIZE * 2 > fileLength) {
                            len_s = (int) (fileLength - pos_s);
                        }
                        mSeekHelper.mSeekStartOffset = pos_s;
                        mSeekHelper.mSeekRange = len_s;
                        byte[] chunk_s = new byte[len_s];
                        try {
                            mMediaFile.seek(pos_s);
                            mMediaFile.read(chunk_s, 0, len_s);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            mSeekHelper.mLeftChunkIndex = tempIndex;
                            return false;
                        }

                        for (int k = i; k < len_s; ) {
                            System.arraycopy(chunk_s, k, mSeekHelper.mSeekHeader, 0, OGG_HEADER_SIZE);

                            KL.i("Valid header = {}", isValidHeader(mSeekHelper.mSeekHeader));

                            long gran = getGranulePostion(mSeekHelper.mSeekHeader);
                            if (mSeekHelper.mTarget <= gran) {
                                mSeekHelper.mSeekResult = k + pos_s;
                                KL.i("Result file position = {} ,granule = {}", mSeekHelper.mSeekResult, mSeekHelper.mSeekGranulePosition);
                                break;
                            } else {
                                int laceLen = mSeekHelper.mSeekHeader[OGG_HEADER_SIZE - 1] & 0x00FF;
                                byte[] lacing = new byte[laceLen];
                                System.arraycopy(chunk_s, k + OGG_HEADER_SIZE, lacing, 0, laceLen);
                                mSeekHelper.mSeekGranulePosition = gran;
                                int pageSize = getPageDataSize(lacing);
                                k += (OGG_HEADER_SIZE + laceLen + pageSize);
                            }
                        }
                        mSeekHelper.mSeekDone = true;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean isSeekComplete() {
        return mSeekHelper != null ? mSeekHelper.mSeekDone : true;
    }

    @Override
    public int getSeekOffset() {
        return (int) mSeekHelper.mSeekStartOffset;
    }

    @Override
    public int getSeekRange() {
        return (int) mSeekHelper.mSeekRange;
    }

    @Override
    public int endSeek() {
        if (mSeekHelper != null && mSeekHelper.mSeekDone && mSeekHelper.mSeekResult != -1) {
            mCurrentFilePosition = mSeekHelper.mSeekResult;
            mCurrentGranulePosition = mSeekHelper.mSeekGranulePosition;
        }
        mSeekHelper = null;
        return (int) mCurrentFilePosition;
    }

    private byte[] getPageHeader(long position) throws IOException {
        mMediaFile.seek(position);
        byte[] header = new byte[OGG_HEADER_SIZE];
        mMediaFile.read(header, 0, OGG_HEADER_SIZE);
        return header;
    }

    private boolean isValidHeader(byte[] header) {
        if (header[0] == 'O'
                && header[1] == 'g'
                && header[2] == 'g'
                && header[3] == 'S') {
            return true;
        }
        return false;
    }

    private int getPageDataSize(byte[] lacing) {
        int length = lacing.length;
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += ((int) lacing[i] & 0x00FF);
        }
        if (sum <= 65025) {
            return sum;
        }
        return 0;
    }

    private synchronized MediaChunkData getAudioDataFromCurrentPage() throws DecoderDataException {
        byte[] header;
        try {
            header = getPageHeader(mCurrentFilePosition);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new DecoderDataException(mCurrentFilePosition + OGG_HEADER_SIZE);
        }
        mCurrentFilePosition += OGG_HEADER_SIZE;
        if (isValidHeader(header)) {
            long granulePos = getGranulePostion(header);
            int laceLen = header[OGG_HEADER_SIZE - 1];
            byte[] lacing = new byte[laceLen];
            try {
                mMediaFile.read(lacing, 0, laceLen);
            } catch (Exception e) {
                e.printStackTrace();
                mCurrentFilePosition -= OGG_HEADER_SIZE;
                throw new DecoderDataException(mCurrentFilePosition + OGG_HEADER_SIZE + laceLen);
            }
            int pageSize = getPageDataSize(lacing);
            byte[] data = new byte[pageSize];
            try {
                mMediaFile.read(data, 0, pageSize);
                mCurrentFilePosition = mMediaFile.getFilePointer();
            } catch (Exception e) {
                e.printStackTrace();
                mCurrentFilePosition -= OGG_HEADER_SIZE;
                throw new DecoderDataException(mCurrentFilePosition + OGG_HEADER_SIZE + laceLen + pageSize);
            }
            long sampleCount = granulePos - mCurrentGranulePosition;
            mCurrentGranulePosition = granulePos;
            mCurrentPageIndex++;


            int sysVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
            int pLen = 0;
//            KL.i("sysVersion:" + sysVersion);
            if (sysVersion < 11) {
                pLen = (int) (480 * 6 * 4) * laceLen;
            } else {
                pLen = (int) (960 * 6 * 4) * laceLen;
            }

            short[] decodedData = new short[pLen];
            int outputPos = 0;
            outputPos = decodeOpusPageData(lacing, laceLen, data, pageSize, decodedData, (int) (960 * 6 * 4));
            data = null;
            lacing = null;

            if (outputPos < 0) {
                KL.i("Oops! native decoder returns null");
                return null;
            }
            boolean eos = (getHeaderType(header) & 0x04) != 0 ? true : false;
            return new MediaChunkData(decodedData, outputPos, eos, getCurrentPosition());
        }
        return null;
    }

    private long getGranulePostion(byte[] header) {
        int granule_msb = 13;
        int granule_lsb = 6;
        long granule_value = 0;
        for (int i = granule_msb; i >= granule_lsb; i--) {
            granule_value = granule_value << 8;
            granule_value += ((int) header[i] & 0x00FF);
        }
        return granule_value;
    }

    private int getHeaderType(byte[] header) {
        return header[5] & 0x00FF;
    }

    @Override
    public boolean setup(MediaFile file) {
        mMediaFile = file;
        return true;
    }

    @Override
    public MediaChunkData getAudioChunkData() throws DecoderDataException {
        return getAudioDataFromCurrentPage();
    }

    @Override
    public long getCurrentPosition() {
        long result = (mCurrentGranulePosition - mPreSkip) / 48;
        return result >= 0 ? result : 0;
    }

}
