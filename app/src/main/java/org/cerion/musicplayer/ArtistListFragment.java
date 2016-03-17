package org.cerion.musicplayer;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ArtistListFragment extends NavigationFragment {

    private static final String TAG = ArtistListFragment.class.getSimpleName();
    private NavigationListAdapter mAdapter;
    private boolean mRoot = true;
    private String mArtist;

    public ArtistListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mNavListener = (OnNavigationListener)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.artist_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, getItems());
        mAdapter = new NavigationListAdapter(getContext());
        //mAdapter.addAll( getItems() );
        setListAdapter(mAdapter);
        fillWithArtists();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                NavigationListItem item = mAdapter.getItem(position);

                if (item.audioFile == null) { //artist
                    fillWithArtist(item.title);
                } else { //file
                    onFileSelected( item.audioFile );
                }

            }
        });

    }

    private void onFileSelected(AudioFile file) {

        Intent intent = new Intent(getContext(), AudioService.class);

        PlayList pl = getPlayList( file );
        intent.putExtra(AudioService.EXTRA_PLAYLIST, pl);
        getContext().startService(intent);

        intent = new Intent(getContext(), NowPlayingActivity.class);
        startActivity(intent);

    }

    private PlayList getPlayList(AudioFile currentFile) {
        Database db = Database.getInstance(getContext());
        List<AudioFile> files = db.getFilesForArtist(mArtist);

        PlayList result = new PlayList();
        result.addAll(files);

        //Randomize and put first song at the beginning
        result.shuffle();
        result.setCurrentFile(currentFile);

        return result;
    }


    private void fillWithArtists() {
        mRoot = true;
        mAdapter.empty();
        mNavListener.onNavChanged(isRoot());

        UpdateListTask updateTask = new UpdateListTask();
        updateTask.execute();
    }

    private void fillWithArtist(String artist) {
        mRoot = false;
        mArtist = artist;
        mNavListener.onNavChanged(isRoot());

        UpdateListTask updateTask = new UpdateListTask(artist);
        updateTask.execute();
    }


    @Override
    public void onNavigateUp() {
        fillWithArtists();
    }

    @Override
    public String getTitle() {
        if(isRoot())
            return "";
        else
            return mArtist;
    }

    @Override
    public boolean isRoot() {
        return mRoot;
    }



    private class UpdateListTask extends AsyncTask<Void,Void,Void> {

        List<NavigationListItem> items = new ArrayList<>();
        String mArtist = null;

        public UpdateListTask() {
            //Update list with artists
        }

        public UpdateListTask(String artist) {
            mArtist = artist;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if(mArtist == null)
                setArtists();
            else
                setArtist(mArtist);

            Collections.sort(items);
            return null;
        }

        public void setArtists() {

            Database db = Database.getInstance(getContext());
            Map<String,Integer> map = db.getArtists();

            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                //String name = pair.getKey() + " - " + pair.getValue();

                NavigationListItem item = new NavigationListItem("" + pair.getKey(), "" + pair.getValue());
                items.add(item);
                it.remove();
            }
        }

        public void setArtist(String artist) {

            Database db = Database.getInstance(getContext());
            List<AudioFile> files = db.getFilesForArtist(artist);

            for(AudioFile file : files) {
                String title = file.getTitle();
                String info = file.getAlbum();
                if(title.length() == 0)
                    title = file.getFilename();

                NavigationListItem item = new NavigationListItem(title, info, file);
                items.add(item);
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.setData(items);
        }
    }

}
