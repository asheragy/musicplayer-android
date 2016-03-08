package org.cerion.musicplayer;


import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.service.AudioService;
import org.cerion.musicplayer.service.AudioStateReceiver;

public class ControlBar extends LinearLayout {

    private static final String TAG = ControlBar.class.getSimpleName();

    private Button mButtonPrimary;
    private TextView mTitle;
    private SeekBar mSeekBar;


    private final AudioStateReceiver mBroadcastReceiver = new AudioStateReceiver() {
        @Override
        public void onStateChange() {
            updateStatus();
        }
    };

    public ControlBar(Context context) {
        super(context);
        init();
    }

    public ControlBar(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        init();
    }

    public ControlBar(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();

        mTitle = (TextView)findViewById(R.id.title);
        mButtonPrimary = (Button)findViewById(R.id.button_primary);
        Button mButtonBack = (Button)findViewById(R.id.button_back);
        Button mButtonNext = (Button)findViewById(R.id.button_next);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);


        mButtonPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioService.togglePlay();
                updateStatus();
            }
        });

        mButtonBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioService.playPrev();
            }
        });

        mButtonNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioService.playNext();
            }
        });


        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int lastUserProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    lastUserProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.d(TAG,"onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioService.setPosition(lastUserProgress);
            }
        });

        updateStatus();
        context.registerReceiver(mBroadcastReceiver, AudioStateReceiver.BROADCAST_FILTER_ALL);
    }

    private void init() {
        //LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //inflater.inflate(R.layout.control_bar, this);

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        mInflater.inflate(R.layout.control_bar  , this);

    }



    private void updateStatus() {
        if(AudioService.getState() == AudioService.STATE_STOPPED) {
            setVisibility(View.GONE);
            stopSeekBar();
        }
        else {
            setVisibility(View.VISIBLE);
            if(AudioService.getState() == AudioService.STATE_PAUSED) {
                mButtonPrimary.setText(getContext().getString(R.string.action_play));
                stopSeekBar();
            }
            else {
                mButtonPrimary.setText(getContext().getString(R.string.action_pause));
                startSeekBar();
            }

            AudioFile current = AudioService.getActiveFile();
            if(current != null) {
                String title = current.getArtist() + " - " + current.getTitle();
                mTitle.setText(title);
            }
        }
    }

    //Seek Bar
    private final Handler mHandler = new Handler();
    private boolean mUpdateSeekBar = false;
    private void startSeekBar() {
        mUpdateSeekBar = true;

        new Thread(new Runnable() {

            public void run() {

                mSeekBar.setMax(AudioService.getLength());
                Log.d(TAG, "Seek Length = " + AudioService.getLength());
                while (mUpdateSeekBar && AudioService.getInstance() != null) {

                    // Update the position
                    update();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                update();
            }

            private void update() {
                mHandler.post(new Runnable() {
                    public void run() {
                        //Log.d(TAG, "" + AudioService.getPosition() + " state=" + AudioService.getState());
                        if(AudioService.getState() == AudioService.STATE_STOPPED) {
                            mSeekBar.setProgress(0);
                            mUpdateSeekBar = false;
                        }
                        else
                            mSeekBar.setProgress(AudioService.getPosition());

                    }
                });
            }

        }).start();
    }

    private void stopSeekBar()
    {
        mUpdateSeekBar = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        stopSeekBar();
        getContext().unregisterReceiver(mBroadcastReceiver);
    }
}
