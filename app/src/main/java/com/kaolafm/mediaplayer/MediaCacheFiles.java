package com.kaolafm.mediaplayer;

import android.content.Context;
import android.support.v4.util.LruCache;

import java.io.File;
import java.io.IOException;

public class MediaCacheFiles extends LruCache<String, MediaFile> {
    private static MediaCacheFiles mInstance;

    private MediaCacheFiles(int maxSize, Context context) {
        super(maxSize);
        File file = new File(MediaFile.getTemporaryFolderPath(context));
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file2 : files) {
                    file2.delete();
                }
            }
        }
    }

    protected void entryRemoved(boolean evicted, String key, MediaFile oldFile, MediaFile newFile) {
        if (evicted) {
            oldFile.close();
            File file = new File(oldFile.getLocalFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static MediaFile getMediaFile(Context context, String source) {
        if (mInstance == null) {
            mInstance = new MediaCacheFiles(3, context);
        }
        MediaFile file = mInstance.get(source);
        if (file == null) {
            try {
                file = new MediaFile(context, source);
                mInstance.put(source, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
