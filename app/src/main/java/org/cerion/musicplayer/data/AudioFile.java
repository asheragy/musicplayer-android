package org.cerion.musicplayer.data;


import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.io.Serializable;

public class AudioFile implements Serializable {
    //private static final String TAG = AudioFile.class.getSimpleName();
    private static final MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    private final File mFile; //TODO, change to path
    private MetaData mMetaData;

    public AudioFile(File file) {
        mFile = file;
    }

    public AudioFile(String filePath) {
        mFile = new File(filePath);
        //mMetaData = new MetaData(mFile);
    }

    public AudioFile(String filePath, String artist, String album, String title) {
        mFile = new File(filePath);
        mMetaData = new MetaData(artist, album, title);
    }

    public static boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return (name.contains(".mp3") || name.contains(".flac"));
    }

    public String getPath() {
        return mFile.getAbsolutePath();
    }

    public String getFilename() {
        return mFile.getName();
    }

    public String getTitle() {
        if(mMetaData == null)
            mMetaData = new MetaData(mFile);

        return mMetaData.title;
    }

    public String getArtist() {
        if(mMetaData == null)
            mMetaData = new MetaData(mFile);

        return mMetaData.artist;
    }

    public String getAlbum() {
        if(mMetaData == null)
            mMetaData = new MetaData(mFile);

        return mMetaData.album;
    }

    @Override
    public String toString() {

        if(mMetaData != null) {
            return "File: " + getTitle();
        }

        return "File: null";
    }

    public boolean isEqual(AudioFile o) {
        Log.d("temp", getPath() + "   " + o.getPath());
        return getPath().contentEquals(o.getPath());
    }

    private class MetaData implements Serializable {

        private final String artist;
        private final String title;
        private final String album;

        MetaData(File file) {
            mmr.setDataSource(file.getAbsolutePath());

            String s = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            artist = (s == null ? "" : s);
            s = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            album = (s == null ? "" : s);
            s = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            title = (s == null ? "" : s);

            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        }

        MetaData(String artist, String album, String title) {
            this.artist = (artist != null ? artist : "");
            this.album = (album != null ? album : "");
            this.title = (title != null ? title : "");
        }

    }

}
