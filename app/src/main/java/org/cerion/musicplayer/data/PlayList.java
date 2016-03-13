package org.cerion.musicplayer.data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayList implements Serializable {

    private List<String> mFileList = new ArrayList<>();
    private int mPosition;

    public PlayList() {


    }

    public void addFile(String path) {
        mFileList.add(path);
    }

    public void addAll(Collection<String> paths) {
        mFileList.addAll(paths);
    }

}
