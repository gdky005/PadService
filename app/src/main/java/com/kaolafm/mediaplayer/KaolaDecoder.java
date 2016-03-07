package com.kaolafm.mediaplayer;

abstract class KaolaDecoder {

    public abstract boolean setup(MediaFile file);

    public abstract void release();

    public abstract MediaChunkData getAudioChunkData() throws DecoderDataException;

    public abstract long getCurrentPosition();

    public abstract void reset();

    public abstract boolean prepare();

    public abstract void startSeek(long ms);

    public abstract boolean processSeek();

    public abstract boolean isSeekComplete();

    public abstract int getSeekOffset();

    public abstract int getSeekRange();

    public abstract int endSeek();

    public int getDefaultChunkSize() {
        return CHUNK_SIZE;
    }

    private static final int CHUNK_SIZE = 65536;
}
