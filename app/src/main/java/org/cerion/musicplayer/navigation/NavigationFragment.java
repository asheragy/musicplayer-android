package org.cerion.musicplayer.navigation;


import android.support.v4.app.ListFragment;

import org.cerion.musicplayer.navigation.OnNavigationListener;

public abstract class NavigationFragment extends ListFragment {

    protected OnNavigationListener mNavListener;

    public abstract void onNavigateUp();
    public abstract String getTitle();
    public abstract boolean isRoot();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mNavListener != null)
            mNavListener.onNavChanged(isRoot());


    }
}
