package org.cerion.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.cerion.musicplayer.data.AudioFile;
import org.cerion.musicplayer.service.AudioService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DirectoryListFragment extends Fragment implements DirectoryListView.OnNavigationListener {

    private DirectoryListView mDirectoryListView;

    public DirectoryListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.directory_list_fragment, container, false);

        mDirectoryListView = (DirectoryListView) view.findViewById(android.R.id.list);

        if(verifyPermissions())
            initList();

        return view;
    }

    private void initList() {
        mDirectoryListView.init(MainActivity.mRootPath, this);
    }

    @Override
    public void onFileSelected(File file) {
        if (AudioFile.isAudioFile(file)) {
            Intent intent = new Intent(getContext(), AudioService.class);
            intent.putStringArrayListExtra(AudioService.PLAYLIST_FILES, getPlayListFilePaths(file.getAbsolutePath()));
            getContext().startService(intent);

            intent = new Intent(getContext(), NowPlayingActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Not a valid audio file", Toast.LENGTH_SHORT).show();
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
        result.add(0, pathOfFirst);

        return result;
    }

    //TODO, move to main activity
    private static final int PERMISSION_READ_STORAGE = 0;
    private boolean verifyPermissions() {
        if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
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
            Toast.makeText(getContext(), "External storage permission required", Toast.LENGTH_SHORT).show();
    }


}
