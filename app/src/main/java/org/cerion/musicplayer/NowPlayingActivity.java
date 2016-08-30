package org.cerion.musicplayer;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.service.AudioService;
import org.cerion.musicplayer.service.AudioStateReceiver;

public class NowPlayingActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    //private static final String TAG = NowPlayingActivity.class.getSimpleName();

    private GestureDetectorCompat mDetector;
    private TextView mTitle;
    private TextView mArtist;
    private TextView mAlbum;
    private TextView mLength;

    private final AudioStateReceiver mBroadcastReceiver = new AudioStateReceiver() {
        @Override
        public void onStateChange() {
            update();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitle = (TextView)findViewById(R.id.title);
        mArtist = (TextView)findViewById(R.id.artist);
        mAlbum = (TextView)findViewById(R.id.album);
        mLength = (TextView)findViewById(R.id.length);

        //onFling to change track
        mDetector = new GestureDetectorCompat(this,this);

        //TODO, if track is paused position shows incorrectly, possibly bug in ControlBar
        update();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private boolean mUpdated = false;
    private void update() {

        //Finish activity is music is stopped, on create this gets called too soon so ignore state the first time
        if(mUpdated && AudioService.getState() == AudioService.STATE_STOPPED) {
            finish();
            return;
        }

        mUpdated = true;
        AudioFile af = AudioService.getActiveFile();

        if(af != null) {
            mTitle.setText(af.getTitle());
            mArtist.setText(af.getArtist());
            mAlbum.setText(af.getAlbum());

            mLength.setText( Utils.formatSeconds(AudioService.getLength() / 1000));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, AudioStateReceiver.BROADCAST_FILTER_ALL);

        update(); //in case track changed since last pause
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        AudioService.togglePlay();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float dist = e1.getX() - e2.getX();
        //Log.d(TAG,"onFling: x=" + dist + " v=" + velocityX);

        //TODO, change distance/velocity to standard best practices
        if(dist < -100) {
            //swipe left/back
            AudioService.playPrev();
        } else if(dist > 100) {
            //swipe right/next
            AudioService.playNext();
        }

        return false;
    }
}
