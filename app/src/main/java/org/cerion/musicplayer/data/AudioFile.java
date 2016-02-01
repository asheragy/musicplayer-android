package org.cerion.musicplayer.data;


import android.media.MediaMetadataRetriever;

import java.io.File;

public class AudioFile {

    //private static final String TAG = AudioFile.class.getSimpleName();
    private static final MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    private final File mFile;
    private MetaData mMetaData;

    public AudioFile(String filePath) {
        mFile = new File(filePath);
        //mMetaData = new MetaData(mFile);
    }

    public static boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return (name.contains(".mp3") || name.contains(".flac"));
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

    private class MetaData {

        private final String artist;
        private final String title;
        private final String album;

        MetaData(File file) {
            mmr.setDataSource(file.getAbsolutePath());

            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }

    }

}
