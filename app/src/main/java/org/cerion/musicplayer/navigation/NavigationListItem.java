package org.cerion.musicplayer.navigation;


import android.support.annotation.NonNull;

import org.cerion.musicplayer.data.AudioFile;

import java.io.File;

public class NavigationListItem implements Comparable<NavigationListItem> {

    public AudioFile audioFile;
    public File file;
    public final String title;
    public final String info;
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

    public boolean isAudioFile() {
        if(audioFile != null)
            return true;
        if(!isFolder() && AudioFile.isAudioFile(file))
            return true;

        return false;
    }

    public AudioFile getAudioFile() {
        if(audioFile != null)
            return audioFile;
        if(!isFolder() && AudioFile.isAudioFile(file))
            return new AudioFile(file);

        return null;
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
        //if(comp == 0 && !isFolder() && !another.isFolder() && ((audioFile == null && another.audioFile != null) || (audioFile != null && another.audioFile == null)))
        //    comp = (audioFile != null ? -1 : 1);

        //IgnoreCase is putting [Unknown] at the top of the list
        if(comp == 0 && (title.length() > 0 && title.charAt(0) == '[') || (another.title.length() > 0 && another.title.charAt(0) == '['))
            comp = this.title.compareTo(another.title);
        if(comp == 0)
            comp = this.title.compareToIgnoreCase(another.title);
        if(comp == 0)
            comp = this.info.compareToIgnoreCase(another.info);

        return comp;
    }
}
