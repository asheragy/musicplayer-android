package org.cerion.musicplayer;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.data.PlayList;
import org.cerion.musicplayer.service.AudioService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ArtistListFragment extends NavigationFragment {

    private static final String TAG = ArtistListFragment.class.getSimpleName();
    private ArtistListAdapter mAdapter;
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
        mAdapter = new ArtistListAdapter(getContext());
        //mAdapter.addAll( getItems() );
        setListAdapter(mAdapter);
        fillWithArtists();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListItem item = mAdapter.getItem(position);

                if (item.file == null) { //artist
                    fillWithArtist(item.title);
                } else { //file
                    onFileSelected(item.file);
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
        Database db = Database.getInstance(getContext());

        List<ListItem> items = new ArrayList<>();
        Map<String,Integer> map = db.getArtists();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            //String name = pair.getKey() + " - " + pair.getValue();

            ListItem item = new ListItem("" + pair.getKey(), "" + pair.getValue());
            items.add(item);
            it.remove();
        }

        Collections.sort(items);

        mAdapter.setData(items);
        mNavListener.onNavChanged(isRoot());
    }

    private void fillWithArtist(String artist) {
        mRoot = false;
        mArtist = artist;

        Database db = Database.getInstance(getContext());
        List<AudioFile> files = db.getFilesForArtist(artist);

        List<ListItem> items = new ArrayList<>();
        for(AudioFile file : files) {
            ListItem item = new ListItem(file.getTitle(), file.getAlbum(), file);
            items.add(item);
        }

        //already sorted by title Collections.sort(items);
        mAdapter.setData(items);
        mNavListener.onNavChanged(isRoot());
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


    //TODO convert to view holder
    private class ListItem implements Comparable<ListItem> {

        ListItem(String title, String info) {
            this.title = title;
            this.info = info;
        }

        ListItem(String title, String info, AudioFile file) {
            this(title,info);
            this.file = file;
        }

        AudioFile file;
        String title;
        String info;

        @Override
        public int compareTo(@NonNull ListItem another) {
            int comp = this.title.compareTo(another.title);
            if(comp == 0)
                comp = this.info.compareTo(another.info);

            return comp;
        }
    }

    //TODO use this
    private static class ViewHolder {
        TextView title;
        TextView info;
    }

    private class ArtistListAdapter extends ArrayAdapter<ListItem> {

        public ArtistListAdapter(Context context) {
            super(context, R.layout.list_item);
        }

        public void setData(List<ListItem> items) {
            clear();
            addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.list_item, parent, false);

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView info = (TextView) view.findViewById(R.id.info);

            ListItem item = getItem(position);
            title.setText(item.title);
            info.setText(item.info);

            return view;
        }

    }
}
