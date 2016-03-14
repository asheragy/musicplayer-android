package org.cerion.musicplayer;


import android.os.Bundle;
import android.os.Environment;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

public class MainActivity extends FragmentActivity implements OnNavigationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private MyPagerAdapter mPagerAdapter;

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

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick");
                //getActionBar().setDisplayHomeAsUpEnabled(false);
                //NavigationFragment navFrag  = (NavigationFragment)mPagerAdapter.getActiveFragment(mViewPager, 0);

                NavigationFragment navFrag = getCurrentFragment();
                //Log.d(TAG,f.toString());
                navFrag.onNavigateUp();
            }
        });


        //TODO, verify permissions before showing any fragments

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);



        //Add tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public void onBackPressed() {
        if(getCurrentFragment().isRoot())
            super.onBackPressed();
        else
            getCurrentFragment().onNavigateUp();
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

        Log.d(TAG,"selected menu");
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_refresh) {
            onUpdateDatabase();
        }

        return super.onOptionsItemSelected(item);
    }

    public NavigationFragment getCurrentFragment() {
        int position = mViewPager.getCurrentItem();
        NavigationFragment navFrag  = (NavigationFragment)mPagerAdapter.getActiveFragment(mViewPager, position);
        return navFrag;
    }

    @Override
    public void onNavChanged(boolean bRoot) {
        getActionBar().setDisplayHomeAsUpEnabled(!bRoot);

        NavigationFragment frag = getCurrentFragment();
        if(frag != null)
            getActionBar().setTitle(frag.getTitle());
        else
            Log.d(TAG,"null fragment");
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private FragmentManager mFragmentManager;
        private static final int NUM_PAGES = 2;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
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



        public Fragment getActiveFragment(ViewPager container, int position) {
            String name = makeFragmentName(container.getId(), position);
            //List<Fragment> list = mFragmentManager.getFragments();
            //Log.d(TAG,"tag = " + list.get(0).getTag());
            //Log.d(TAG,"size = " + list.size());

            return  mFragmentManager.findFragmentByTag(name);
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }

    }


    public void onUpdateDatabase()
    {
        Log.d(TAG, "onUpdateDatabase");
        UpdateDatabaseTask mTask = new UpdateDatabaseTask(this,mRootPath);
        mTask.execute();
    }

    /*
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
            //proceed loading app
        else
            Toast.makeText(this, "External storage permission required", Toast.LENGTH_SHORT).show();
    }
    */

}
