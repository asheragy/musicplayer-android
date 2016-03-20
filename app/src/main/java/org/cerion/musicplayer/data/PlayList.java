package org.cerion.musicplayer.data;


import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlayList implements Serializable {

    private static final String TAG = PlayList.class.getSimpleName();
    private final List<AudioFile> mFileList = new ArrayList<>();
    private int mPosition = 0;

    public PlayList() {

    }

    public void add(AudioFile file) {
        mFileList.add(file);
    }

    public void addAll(Collection<AudioFile> audioFiles) {
        mFileList.addAll(audioFiles);
    }

    public String getCurrentFilePath() {
        return mFileList.get(mPosition % mFileList.size()).getPath();
    }

    public AudioFile getCurrentAudioFile() {
        return mFileList.get(mPosition % mFileList.size());
    }

    public void next() {
        mPosition = (mPosition + 1) % mFileList.size();
    }

    public void prev() {
        if(mPosition == 0)
            mPosition = mFileList.size() - 1;
        else
            mPosition = (mPosition - 1) % mFileList.size();
    }

    public void shuffle() {
        Collections.shuffle(mFileList);
    }

    public void setCurrentFile(AudioFile file) {
        for(int i = 0; i < mFileList.size(); i++) {
            if(mFileList.get(i).isEqual(file)) {
                mPosition = i;
                Log.d(TAG, "current position = " + i);
                break;
            }
        }
    }
}
