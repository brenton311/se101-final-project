package com.redeyesoftware.pronto;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by George on 19/11/2016.
 */

public class HomePagesAdapter extends FragmentPagerAdapter {


    public HomePagesAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FeedFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new BookmarksFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Feed";
            case 1:
                return "Chat";
            case 2:
                return "Bookmarks";
            default:
                return null;
        }
    }

}
