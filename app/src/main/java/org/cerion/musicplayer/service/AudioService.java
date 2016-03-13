package org.cerion.musicplayer.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.data.PlayList;

import java.io.IOException;

public class AudioService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = AudioService.class.getSimpleName();

    public static final String EXTRA_PLAYLIST = "playList";

    private MediaPlayer mMediaPlayer = null;
    private boolean mPaused;
    private static AudioService sService;
    private PlayList mPlayList;

    private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;

    private final HeadSetReceiver mHeadSetReceiver = new HeadSetReceiver();

    public static AudioService getInstance() {
        return sService;
    }

    public AudioService() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(this, HeadSetReceiver.class);

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadSetReceiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        sService = this;
        Log.d(TAG, "onStart State=" + getState());

        //Every time a new track starts register as the media button receiver in case another app has it
        mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);

        //Log.d(TAG,"reading intent");
        //mPlayListOLD = intent.getStringArrayListExtra(PLAYLIST_FILES);
        //mPlayListPosition = -1;
        mPlayList = (PlayList)intent.getSerializableExtra(EXTRA_PLAYLIST);
        playCurrentFile();

        return START_NOT_STICKY;
    }

    private void playNextFile() {
        mPlayList.next();
        playCurrentFile();
    }

    private void playPrevFile() {
        mPlayList.prev();
        playCurrentFile();
    }

    private void playCurrentFile() {
        //Stop/reset if needed
        if(mMediaPlayer.isPlaying() || mPaused) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        mPaused = false;

        String file = getCurrentFilePath();
        Log.d(TAG, "playing = " + file);

        try {
            mMediaPlayer.setDataSource(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();
    }

    @Deprecated
    private String getCurrentFilePath() {
        //return mPlayListOLD.get(mPlayListPosition % mPlayListOLD.size());
        return mPlayList.getCurrentFilePath();
    }

    /**
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer player) {
        player.start();

        broadcastUpdate();
    }

    private void broadcastUpdate() {
        Intent i = new Intent(AudioStateReceiver.ACTION_BROADCAST);
        i.putExtra(AudioStateReceiver.EXTRA_MESSAGE,"Finished onPrepared");
        sendBroadcast(i);
    }



    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onComplete");
        mMediaPlayer.stop();
        mMediaPlayer.reset();

        //Start next file
        playNextFile();

        //Broadcast update so we can switch to it
        broadcastUpdate();
    }

    private void toggle() {
        if(mMediaPlayer.isPlaying() && !mPaused) {
            mMediaPlayer.pause();
            mPaused = true;
            Log.d(TAG, "toggle() paused");
        }
        else if(mPaused) {
            Log.d(TAG,"toggle() resumed");
            mPaused = false;
            mMediaPlayer.start();
        }
        else
            Log.d(TAG,"toggle() not playing");

        broadcastUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHeadSetReceiver);
        sService = null;
        Log.d(TAG,"onDestroy");
    }

    //----------------- Static control methods -----------------

    public static final int STATE_STOPPED = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_PLAYING = 2;

    public static int getState() {
        if(sService == null || (!sService.mMediaPlayer.isPlaying() && !sService.mPaused))
            return STATE_STOPPED;
        else if(sService.mPaused)
            return STATE_PAUSED;
        else
            return STATE_PLAYING;
    }

    public static void setPosition(int position) {
        if(sService != null)
            sService.mMediaPlayer.seekTo(position);
    }

    public static int getPosition() {
        if(sService == null)
            return -1;
        else
            return sService.mMediaPlayer.getCurrentPosition();
    }

    public static int getLength() {
        if(sService == null)
            return -1;
        else
            return sService.mMediaPlayer.getDuration();
    }

    public static AudioFile getActiveFile() {
        if(sService != null)
            return new AudioFile(sService.getCurrentFilePath());

        return null;
    }

    public static void togglePlay() {
        if(sService != null)
            sService.toggle();
    }

    public static void playNext() {
        if(sService != null)
            sService.playNextFile();
    }

    public static void playPrev() {
        if(sService != null)
            sService.playPrevFile();
    }
}