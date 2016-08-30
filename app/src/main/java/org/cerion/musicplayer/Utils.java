package org.cerion.musicplayer;

public class Utils {

    public static String formatSeconds(int seconds) {
        int secs = seconds % 60;
        int mins = seconds / 60;

        return String.format("%s:%s", mins, secs);
    }
}
