package com.redeyesoftware.pronto;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * Created by George on 19/11/2016.
 */

public class HomePagesAdapter extends FragmentPagerAdapter {


    public HomePagesAdapter(FragmentManager fm) {
        super(fm);
    }


    //doesnt need to be overridden, using to always update fragment
    //This method is called when you call notifyDataSetChanged()
    @Override
    public int getItemPosition(Object object) {
        Log.d("Debug","updating");
        /*Implicitly this method returns POSITION_UNCHANGED value that means something like this: "Fragment is where it should be so change anything."
        So if you need to update Fragment you can do it with one of teh following:
        -Always return POSITION_NONE from getItemPosition() method. It which means: "Fragment must be always recreated"
        -You can create some update() method that will update your Fragment(fragment will handle updates itself)*/
        return POSITION_NONE;
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
