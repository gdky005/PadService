package com.kaolafm.mediaplayer;

class DecoderDataException extends Exception {
    private long mBytes = 0;
    public DecoderDataException(long bytes) {
        super();
        mBytes = bytes;
    }
    public long getExpectBytes() {
        return mBytes;
    }
}
