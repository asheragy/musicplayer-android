package org.cerion.musicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DirectoryListView extends ListView {

    private static final String TAG = DirectoryListAdapter.class.getSimpleName();
    private OnNavigationListener mListener;
    private String mRootPath;
    private String mCurrentPath;
    private DirectoryListAdapter mAdapter;

    public interface OnNavigationListener {
        void onFileSelected(File file);
    }

    public DirectoryListView(Context context) {
        super(context);
    }

    public DirectoryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DirectoryListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(String rootPath, OnNavigationListener listener) {
        mRootPath = rootPath;
        mListener = listener;

        mAdapter = new DirectoryListAdapter(getContext());
        setAdapter(mAdapter);

        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = mAdapter.getItem(position);
                //String path = file.getAbsolutePath();
                //Log.d(TAG, "onClick " + path);

                if (file.isDirectory()) {
                    mCurrentPath = file.getAbsolutePath();
                    setDirectory(mCurrentPath);

                } else {
                    mListener.onFileSelected(file);
                }

            }
        });

        setDirectory(rootPath);
    }


    public boolean isRoot() {
        return (mCurrentPath == null || mRootPath.contentEquals(mCurrentPath));
    }

    private void setDirectory(String path) {
        Log.d(TAG, "nav -> " + path);
        mAdapter.setData(getDirectoryListing(path));
    }

    private List<File> getDirectoryListing(String directory) {
        List<File> result = new ArrayList<>();

        File dir = new File(directory);
        File files[] = dir.listFiles();
        Arrays.sort(files);

        //Log.d("Files", "Size: " + length + " " + f.getAbsolutePath());
        result.addAll( Arrays.asList(files) );
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
