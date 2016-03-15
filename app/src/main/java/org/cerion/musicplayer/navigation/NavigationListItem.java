package org.cerion.musicplayer.navigation;


import android.support.annotation.NonNull;

import org.cerion.musicplayer.data.AudioFile;

import java.io.File;

public class NavigationListItem implements Comparable<NavigationListItem> {

    public AudioFile audioFile;
    public File file;
    public String title;
    public String info;

    public NavigationListItem(String title, String info) {
        this.title = title;
        this.info = info;
    }

    public NavigationListItem(String title, String info, AudioFile file) {
        this(title,info);
        this.audioFile = file;
    }

    public NavigationListItem(String title, String info, File file) {
        this(title,info);
        this.file = file;
    }

    public boolean isFolder() {
        if(file != null && !file.isDirectory())
            return false;
        if(audioFile != null)
            return false;

        return true;
    }
    @Override
    public int compareTo(@NonNull NavigationListItem another) {
        int comp = this.title.compareTo(another.title);
        if(comp == 0)
            comp = this.info.compareTo(another.info);

        return comp;
    }
}
