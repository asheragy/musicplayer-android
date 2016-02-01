package org.cerion.musicplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class HeadSetReceiver extends BroadcastReceiver {

    private static final String TAG = HeadSetReceiver.class.getSimpleName();
    private boolean mStartOnPlug = false;

    public HeadSetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"onReceive " + intent.getAction());

        if(intent.getAction().contentEquals(Intent.ACTION_HEADSET_PLUG))
            onHeadsetPlug(intent);
        else if(intent.getAction().contentEquals(Intent.ACTION_MEDIA_BUTTON))
            onMediaButton(context, intent);
        else {
            Toast.makeText(context,"Unknown broadcast " + intent.getAction(),Toast.LENGTH_LONG).show();
            logIntentData(intent);
        }
    }

    private void onHeadsetPlug(Intent intent) {
        int state = intent.getIntExtra("state", -1);
        if(state == 0) {
            Log.d(TAG,"onUnplugged");
            if(AudioService.getState() == AudioService.STATE_PLAYING) {
                AudioService.togglePlay(); //pause
                mStartOnPlug = true; //only trigger play if this broadcast caused it to pause
            }
        } else if(state == 1) {
            Log.d(TAG, "onPlugged");
            if(mStartOnPlug && AudioService.getState() == AudioService.STATE_PAUSED) {
                AudioService.togglePlay(); //play
                mStartOnPlug = false;
            }
        }
    }

    private void onMediaButton(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        int action = event.getAction();

        switch(action) {
            case KeyEvent.ACTION_UP:
                AudioService.togglePlay();
                break;
            default: {
                Toast.makeText(context, "Unknown media button action", Toast.LENGTH_SHORT).show();
                logIntentData(intent);
            }
        }

    }


    private void logIntentData(Intent intent) {
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if(value != null)
                Log.d(TAG, String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
        }
    }

}
