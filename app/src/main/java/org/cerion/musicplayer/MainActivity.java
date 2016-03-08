package org.cerion.musicplayer;

import android.os.Bundle;
import android.os.Environment;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import org.cerion.musicplayer.data.AudioFile;

import java.io.File;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    public static final String mRootPath = Environment.getExternalStorageDirectory().toString()+"/Music";
    //private List<File> mItems = getFiles();

    //private ArrayAdapter<File> mAdapter;
    //private DirectoryListAdapter mAdapter;
    //private MenuItem mMenuUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);


        //Add tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //mMenuUp = menu.findItem(R.id.action_up);
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
        } else if(id == R.id.action_refresh) {
            onRefresh();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onRefresh() {
        Log.d(TAG,"onRefresh");
        Database db = Database.getInstance(this);
        db.reset();

        addFilesInDirectory(new File(mRootPath),db);

        db.log();
    }

    private void addFilesInDirectory(File dir, Database db) {
        File files[] = dir.listFiles();

        for(File f : files) {
            if(f.isDirectory())
                addFilesInDirectory(f,db);
            else if(AudioFile.isAudioFile(f)) {
                AudioFile af = new AudioFile(f);
                db.add(af);
            }

        }

    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_PAGES = 2;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {

            switch(position) {
                case 0: return new DirectoryListFragment();
                case 1: return new ArtistListFragment();
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch(position) {
                case 0: return "Folders";
                case 1: return "Artists";
            }

            return null;
        }

        /* TODO find what this does
        public Fragment getActiveFragment(ViewPager container, int position) {
            String name = makeFragmentName(container.getId(), position);
            return  mFragmentManager.findFragmentByTag(name);
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }
        */
    }

}
