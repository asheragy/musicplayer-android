package org.cerion.musicplayer.navigation;


import android.support.annotation.NonNull;

import org.cerion.musicplayer.data.AudioFile;

import java.io.File;

public class NavigationListItem implements Comparable<NavigationListItem> {

    public AudioFile audioFile;
    public File file;
    public String title;
    public String info;
    private boolean isFolder;

    public NavigationListItem(String title, String info) {
        this.title = title;
        this.info = info;
        isFolder = true; //folder since no file associated
    }

    public NavigationListItem(String title, String info, AudioFile file) {
        this(title,info);
        this.audioFile = file;
        isFolder = false; //audio file
    }

    public NavigationListItem(String title, String info, File file) {
        this(title,info);
        this.file = file;
        isFolder = file.isDirectory();
    }

    public boolean isFolder() {
        return isFolder;
        /*
        if(file != null && !file.isDirectory())
            return false;
        if(audioFile != null)
            return false;

        return true;
        */
    }
    @Override
    public int compareTo(@NonNull NavigationListItem another) {
        int comp = 0;
        if(isFolder() != another.isFolder()) //list folders first
            comp = (isFolder() ? -1 : 1);
        if(comp == 0)
            comp = this.title.compareTo(another.title);
        if(comp == 0)
            comp = this.info.compareTo(another.info);

        return comp;
    }
}
