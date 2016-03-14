package org.cerion.musicplayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.cerion.musicplayer.data.AudioFile;

import java.io.File;


public class UpdateDatabaseTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = UpdateDatabaseTask.class.getSimpleName();

    String mStatus;
    Context mContext;
    Database mDb;
    ProgressDialog m_pd;
    String mRootPath;

    public UpdateDatabaseTask(Context context, String rootPath) {
        mContext = context;
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
        update("Done");
        mDb.log();

        return null;
    }

    private void addFilesInDirectory(File dir) {
        File files[] = dir.listFiles();

        Log.d(TAG, "Adding " + dir.getName());
        update("Adding " + dir.getName());

        for(File f : files) {
            if(f.isDirectory())
                addFilesInDirectory(f);
            else if(AudioFile.isAudioFile(f)) {
                AudioFile af = new AudioFile(f);
                mDb.add(af);
            }
        }

    }

    protected void update(String status)
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
