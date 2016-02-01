package org.cerion.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.service.AudioService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DirectoryListView.OnNavigationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private DirectoryListView mDirectoryListView;
    private static final String mRootPath = Environment.getExternalStorageDirectory().toString()+"/Music";
    //private List<File> mItems = getFiles();


    //private ArrayAdapter<File> mAdapter;
    //private DirectoryListAdapter mAdapter;
    private MenuItem mMenuUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDirectoryListView = (DirectoryListView) findViewById(android.R.id.list);

        if(verifyPermissions())
            initList();
    }

    private void initList() {
        mDirectoryListView.init(mRootPath, this);
    }

    private static final int PERMISSION_READ_STORAGE = 0;
    private boolean verifyPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
           return true;
        else
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_STORAGE);

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_READ_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            initList();
        else
            Toast.makeText(this,"External storage permission required", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileSelected(File file) {
        if (AudioFile.isAudioFile(file)) {
            Intent intent = new Intent(MainActivity.this, AudioService.class);
            intent.putStringArrayListExtra(AudioService.PLAYLIST_FILES, getPlayListFilePaths(file.getAbsolutePath()));
            startService(intent);

            intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "Not a valid audio file", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> getPlayListFilePaths(String pathOfFirst) {
        List<File> files = mDirectoryListView.getFiles();
        ArrayList<String> result = new ArrayList<>();

        for(File file : files) {
            if(AudioFile.isAudioFile(file)) {
                String path = file.getAbsolutePath();
                if(!path.contentEquals(pathOfFirst))
                    result.add(path);
            }
        }

        //Randomize and put first song at the beginning
        Collections.shuffle(result);
        result.add(0,pathOfFirst);

        return result;
    }

    @Override
    public void onDirectoryChanged(boolean bIsRoot) {
        mMenuUp.setVisible(!bIsRoot);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(mBroadcastReceiver, AudioStateReceiver.BROADCAST_FILTER_ALL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mBroadcastReceiver);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptions");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenuUp = menu.findItem(R.id.action_up);
        //mMenuUp.setVisible(!mAdapter.isRoot());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_up) {
            mDirectoryListView.navigateUp();
            mMenuUp.setVisible(!mDirectoryListView.isRoot());
        }

        return super.onOptionsItemSelected(item);
    }

}
