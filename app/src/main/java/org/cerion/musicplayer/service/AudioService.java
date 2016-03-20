package org.cerion.musicplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import org.cerion.musicplayer.R;
import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.data.PlayList;

import java.io.IOException;

public class AudioService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = AudioService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 49724;
    public static final String EXTRA_PLAYLIST = "playList";
    private static final String ACTION_STOP = "actionStop";
    private static final String ACTION_BACK = "actionBack";
    private static final String ACTION_NEXT = "actionNext";
    private static final String ACTION_TOGGLE = "actionToggle";

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


    private Notification getNotification() {
        String title = mPlayList.getCurrentAudioFile().getFilename();

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.now_playing)  // the status icon
                .setTicker(title)
                .setWhen(System.currentTimeMillis())  // the time stamp
                //.setContentTitle(title)  // the label of the entry
                //.setContentText(text)  // the contents of the entry
        //.setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        notification.contentView = new RemoteViews(getPackageName(), R.layout.now_playing_notification);
        notification.contentView.setTextViewText(R.id.title, title);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.contentView.setImageViewResource(R.id.toggle, getState() == STATE_PAUSED ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);
        notification.contentView.setOnClickPendingIntent(R.id.stop, getActionIntent(ACTION_STOP));
        notification.contentView.setOnClickPendingIntent(R.id.back, getActionIntent(ACTION_BACK));
        notification.contentView.setOnClickPendingIntent(R.id.next, getActionIntent(ACTION_NEXT));
        notification.contentView.setOnClickPendingIntent(R.id.toggle, getActionIntent(ACTION_TOGGLE));
        //notification.deleteIntent = PendingIntent.getService(this, 0, new Intent(this, AudioService.class).setAction(ACTION_STOP), 0);

        return notification;
    }

    private PendingIntent getActionIntent(String action) {
        Intent intent = new Intent(this, AudioService.class).setAction(action);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        sService = this;
        Log.d(TAG, "onStart State=" + getState() + " action=" + intent.getAction());

        String action = (intent.getAction() != null ? intent.getAction() : "");

        if(action.contentEquals(ACTION_STOP)) { //TODO, make function like back/next
            //mNotification.cancel();
            mMediaPlayer.stop(); //stopSelf does not seem to end this one
            broadcastUpdate();
            stopForeground(true);
            stopSelf();
        } else if(action.contentEquals(ACTION_BACK)) {
            sService.playPrevFile();
        } else if(action.contentEquals(ACTION_NEXT)) {
            sService.playNextFile();
        } else if(action.contentEquals(ACTION_TOGGLE)) {
            sService.toggle();
        } else {

            //Every time a new track starts register as the media button receiver in case another app has it
            mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);

            //Log.d(TAG,"reading intent");
            //mPlayListOLD = intent.getStringArrayListExtra(PLAYLIST_FILES);
            //mPlayListPosition = -1;
            mPlayList = (PlayList)intent.getSerializableExtra(EXTRA_PLAYLIST);
            playCurrentFile();
        }


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

        String file = mPlayList.getCurrentFilePath();
        Log.d(TAG, "playing = " + mPlayList);

        try {
            mMediaPlayer.setDataSource(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();
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
        i.putExtra(AudioStateReceiver.EXTRA_MESSAGE, "Finished onPrepared");
        sendBroadcast(i);

        //Reset notification with current title/status
        startForeground(NOTIFICATION_ID, getNotification());
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
            stopForeground(false);
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
            return new AudioFile(sService.mPlayList.getCurrentFilePath());

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