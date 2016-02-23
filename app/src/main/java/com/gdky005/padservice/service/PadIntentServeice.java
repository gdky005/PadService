package com.gdky005.padservice.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by WangQing on 16/2/19.
 */
public class PadIntentServeice extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PadIntentServeice(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
