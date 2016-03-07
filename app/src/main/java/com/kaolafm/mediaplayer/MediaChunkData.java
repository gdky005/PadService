package com.kaolafm.mediaplayer;

class MediaChunkData {
    private short[] mChunkData = null;
    private int mSampleCount = 0;
    private boolean mEndOfStream = false;
    private long mTimestamp = 0;
    public MediaChunkData(short[] chunk, int sampleCount, boolean eos, long timestamp) {
        mChunkData = chunk;
        mSampleCount = sampleCount;
        mEndOfStream = eos;
        mTimestamp = timestamp;
    }

    public short[] getData() {
        return mChunkData;
    }

    public int getSampleCount() {
        return mSampleCount;
    }

    public boolean isEndofStream() {
        return mEndOfStream;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
