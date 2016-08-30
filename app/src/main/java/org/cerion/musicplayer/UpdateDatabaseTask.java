package org.cerion.musicplayer;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.cerion.musicplayer.data.AudioFile;

import java.io.File;
import java.util.Map;
import java.util.Set;


class UpdateDatabaseTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = UpdateDatabaseTask.class.getSimpleName();

    private String mStatus;
    //Context mContext;
    private final Database mDb;
    private final ProgressDialog m_pd;
    private final String mRootPath;

    public UpdateDatabaseTask(Context context, String rootPath) {
        //mContext = context;
        mDb = Database.getInstance(context);
        mRootPath = rootPath;

        m_pd = new ProgressDialog(context);
        m_pd.setMessage("Initializing database...");
        m_pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_pd.show();
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        update("Resetting database...");
        mDb.reset();
        addFilesInDirectory(new File(mRootPath));
        mergeSimilarNames();
        update("Done");
        mDb.log();

        return null;
    }

    private void addFilesInDirectory(File dir) {
        File files[] = dir.listFiles();

        Log.d(TAG, "Adding " + dir.getName());
        update("Adding " + dir.getName());

        if(files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    addFilesInDirectory(f);
                else if (AudioFile.isAudioFile(f)) {
                    AudioFile af = new AudioFile(f);
                    mDb.add(af);
                }
            }
        }

    }

    //Find similar artist names and merge to most common version, Example "The Beatles" and "Beatles
    private void mergeSimilarNames() {
        Log.d(TAG, "checking for names to merge");

        Map<String,Integer> map = mDb.getArtists();
        Set<String> artists = map.keySet();

        for(String s : artists) {
            for(String t : artists) {
                if(isSimilarArtist(s, t)) {
                    int c1 = map.get(s);
                    int c2 = map.get(t);
                    if(c2 >= c1)
                        mDb.replaceArtist(s,t);
                }
            }
        }
    }

    private boolean isSimilarArtist(String a1, String a2) {
        if(a1.contentEquals(a2)) //Exact match is just the same entry
            return false;

        if(a1.equalsIgnoreCase(a2))
            return true;

        return false;
    }

    private void update(String status)
    {
        mStatus = status;
        publishProgress();
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
        super.onProgressUpdate(values);
        //m_pd.setProgress(mPos);
        m_pd.setMessage(mStatus);
    }

    @Override
    protected void onPostExecute(Void v)
    {
        m_pd.dismiss();
    }

}
