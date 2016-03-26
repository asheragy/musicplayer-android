package org.cerion.musicplayer.navigation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.cerion.musicplayer.Database;
import org.cerion.musicplayer.data.AudioFile;

import java.io.File;

public abstract class NavigationFragment extends ListFragment {

    private static final String TAG = NavigationFragment.class.getSimpleName();
    protected OnNavigationListener mNavListener;
    protected NavigationListAdapter mAdapter;

    public abstract void onNavigateUp();
    public abstract String getTitle();
    public abstract boolean isRoot();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mNavListener != null)
            mNavListener.onNavChanged(isRoot());


    }


    private static final int MENU_DELETE = 0;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ListView lv = (ListView) v;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        NavigationListItem item = (NavigationListItem) lv.getItemAtPosition(info.position);

        if(item.isAudioFile()) {
            menu.setHeaderTitle(item.getAudioFile().getFilename());
            menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        //The context menu is triggered on the first viewPager fragment unless this is used
        if(getUserVisibleHint()) {
            switch (item.getItemId()) {
                case MENU_DELETE:
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                    final NavigationListItem listItem = mAdapter.getItem(info.position);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Are you sure?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.remove(listItem);
                            deleteFile(listItem.getAudioFile());
                            dialog.dismiss();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();





                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } else
            return false; //Most likely triggered from another fragment that will handle it
    }


    private void deleteFile(AudioFile file) {

        //Delete physical file
        File f = new File(file.getPath());
        f.delete();

        //Remove entry in database
        Database db = Database.getInstance(getContext());
        db.delete(file);

        //TODO, remove this file from the playlist
        //If current file need to switch tracks
    }
}
