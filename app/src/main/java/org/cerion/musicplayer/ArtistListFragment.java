package org.cerion.musicplayer;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistListFragment extends Fragment {

    ListView mListView;

    public ArtistListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.artist_list_fragment, container, false);

        mListView = (ListView)view.findViewById(R.id.list);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, getItems());
        mListView.setAdapter(adapter);

        return view;
    }

    private List<String> getItems() {

        Database db = Database.getInstance(getContext());

        List<String> items = new ArrayList<>();
        Map<String,Integer> map = db.getArtists();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String name = pair.getKey() + " - " + pair.getValue();
            items.add(name);
            it.remove();
        }

        Collections.sort(items);

        return items;
    }

}
