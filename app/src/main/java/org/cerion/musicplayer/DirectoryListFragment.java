package org.cerion.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.data.PlayList;
import org.cerion.musicplayer.navigation.NavigationFragment;
import org.cerion.musicplayer.navigation.NavigationListAdapter;
import org.cerion.musicplayer.navigation.NavigationListItem;
import org.cerion.musicplayer.navigation.OnNavigationListener;
import org.cerion.musicplayer.service.AudioService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class DirectoryListFragment extends NavigationFragment {

    private static final String TAG = DirectoryListFragment.class.getSimpleName();
    private String mRootPath;
    private String mCurrentPath;

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
        mAdapter = new NavigationListAdapter(getContext());
        setListAdapter(mAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavigationListItem item = mAdapter.getItem(position);
                //String path = file.getAbsolutePath();
                //Log.d(TAG, "onClick " + path);

                if (item.isFolder()) {
                    setDirectory(item.file.getAbsolutePath());
                } else {
                    onFileSelected(item.file);
                }

            }
        });

        registerForContextMenu(getListView());
        setDirectory(mRootPath);
    }

    private void onNavChanged() {
        mNavListener.onNavChanged(isRoot());
    }

    private void onFileSelected(File file) {
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
            File file = new File(mCurrentPath);
            setDirectory(file.getParent());
        }
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
        mCurrentPath = path;

        //Clear current contents
        mAdapter.empty();
        onNavChanged();

        //Update in background
        UpdateListTask updateTask = new UpdateListTask();
        updateTask.execute();
    }

    private List<File> getFiles() {
        List<File> result = new ArrayList<>();

        for(int i = 0; i < mAdapter.getCount(); i++) {
            File f = mAdapter.getItem(i).file;
            if(f.isFile())
                result.add(f);
        }

        return result;
    }


    private class UpdateListTask extends AsyncTask<Void,Void,Void> {

        private final List<NavigationListItem> items = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... params) {

            List<File> files = getDirectoryListing(mCurrentPath);
            Database db = Database.getInstance(getContext());
            Map<String,AudioFile> map = db.getFilesInPath(mCurrentPath);

            for(File file : files) {
                String info = "";
                String title = file.getName();

                if(file.isDirectory()) {
                    info = file.listFiles().length + " files";
                } else if(map.containsKey(file.getPath())) {
                    AudioFile audioFile = map.get(file.getPath());
                    if(audioFile.getTitle().length() > 0)
                        title = audioFile.getTitle();

                    if(audioFile.getAlbum().length() == 0)
                        info = audioFile.getArtist();
                    else if(audioFile.getArtist().length() == 0)
                        info = audioFile.getAlbum();
                    else
                        info = String.format("%s - %s", audioFile.getArtist(), audioFile.getAlbum());
                }

                NavigationListItem item = new NavigationListItem(title,info,file);
                items.add(item);
            }

            Collections.sort(items);
            return null;
        }

        private List<File> getDirectoryListing(String directory) {
            List<File> result = new ArrayList<>();

            File dir = new File(directory);
            File files[] = dir.listFiles();
            //Arrays.sort(files);

            //Log.d("Files", "Size: " + length + " " + f.getAbsolutePath());
            result.addAll(Arrays.asList(files));
            return result;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.setData(items);
        }


    }

    /*
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
    */
}
