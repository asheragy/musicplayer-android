package org.cerion.musicplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public abstract class AudioStateReceiver extends BroadcastReceiver {

    private static final String TAG = AudioStateReceiver.class.getSimpleName();
    public static final String ACTION_BROADCAST = "action_broadcast";
    public static final String EXTRA_MESSAGE = "message";

    public static final IntentFilter BROADCAST_FILTER_ALL = new IntentFilter(AudioStateReceiver.ACTION_BROADCAST);

    public AudioStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        //String message = intent.getStringExtra(EXTRA_MESSAGE);
        //Toast.makeText(context, "BroadcastReceiver: " + message, Toast.LENGTH_SHORT).show();
        onStateChange();
    }


    public abstract void onStateChange();

}
