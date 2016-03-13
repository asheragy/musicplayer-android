package org.cerion.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.data.PlayList;
import org.cerion.musicplayer.service.AudioService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DirectoryListFragment extends NavigationFragment {

    private static final String TAG = DirectoryListFragment.class.getSimpleName();
    private String mRootPath;
    private String mCurrentPath;
    private DirectoryListAdapter mAdapter;

    public DirectoryListFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mNavListener = (OnNavigationListener)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.directory_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRootPath = MainActivity.mRootPath;
        mAdapter = new DirectoryListAdapter(getContext());
        setListAdapter(mAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = mAdapter.getItem(position);
                //String path = file.getAbsolutePath();
                //Log.d(TAG, "onClick " + path);

                if (file.isDirectory()) {
                    mCurrentPath = file.getAbsolutePath();
                    setDirectory(mCurrentPath);

                } else {
                    onFileSelected(file);
                }

            }
        });

        setDirectory(mRootPath);
    }

    public void onNavChanged() {
        mNavListener.onNavChanged(isRoot());
    }


    public void onFileSelected(File file) {
        if (AudioFile.isAudioFile(file)) {
            Intent intent = new Intent(getContext(), AudioService.class);

            intent.putExtra(AudioService.EXTRA_PLAYLIST, getPlayList(file));

            getContext().startService(intent);

            intent = new Intent(getContext(), NowPlayingActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Not a valid audio file", Toast.LENGTH_SHORT).show();
        }
    }

    private PlayList getPlayList(File currentFile) {

        PlayList result = new PlayList();

        List<File> files = getFiles();
        for(File file : files) {
            if(AudioFile.isAudioFile(file)) {
                result.add( new AudioFile(file) );
            }
        }

        //Randomize and set current song
        result.shuffle();
        result.setCurrentFile(new AudioFile(currentFile));

        return result;
    }

    @Override
    public void onNavigateUp() {
        if(isRoot())
            Log.e(TAG, "cannot navigate up on root");
        else {
            getListView().performItemClick(getListView(), 0, 0);
        }

        onNavChanged();
    }

    @Override
    public String getTitle() {
        if(isRoot())
            return "/";
        else
            return mCurrentPath.substring(mCurrentPath.lastIndexOf('/') + 1);
    }

    public boolean isRoot() {
        return (mCurrentPath == null || mRootPath.contentEquals(mCurrentPath));
    }

    private void setDirectory(String path) {
        Log.d(TAG, "nav -> " + path);
        mAdapter.setData(getDirectoryListing(path));

        onNavChanged();
    }

    private List<File> getDirectoryListing(String directory) {
        List<File> result = new ArrayList<>();

        File dir = new File(directory);
        File files[] = dir.listFiles();
        Arrays.sort(files);

        //Log.d("Files", "Size: " + length + " " + f.getAbsolutePath());
        result.addAll(Arrays.asList(files));
        return result;
    }

    public List<File> getFiles() {
        List<File> result = new ArrayList<>();

        for(int i = 0; i < mAdapter.getCount(); i++) {
            File f = mAdapter.getItem(i);
            if(f.isFile())
                result.add(f);
        }

        return result;
    }


    private class DirectoryListAdapter extends ArrayAdapter<File> {

        public DirectoryListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
        }

        public void setData(List<File> files) {
            clear();

            if(mCurrentPath != null && !mRootPath.contentEquals(mCurrentPath))
                files.add(0, new File(mCurrentPath).getParentFile());

            addAll(files);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);

            File file = getItem(position);
            String name = file.getName();
            if(!isRoot() && position == 0)
                name = "..";

            text1.setText(name);

            return view;
        }

    }
}
