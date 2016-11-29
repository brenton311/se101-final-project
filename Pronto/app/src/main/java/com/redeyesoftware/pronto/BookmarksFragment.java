package com.redeyesoftware.pronto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static bolts.Task.delay;


public class BookmarksFragment extends Fragment {

    private static BookmarksFragment me;

    private LinearLayout linear;

    public BookmarksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
    }

    //returns what goes inside the fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_feed, container, false);

        ArrayList<SerializableBookmark> bookmarkList = new ArrayList<SerializableBookmark>();
        File bookmarksFile = new File(getActivity().getFilesDir().getPath().toString() + "/SavedProntoBookmarks.txt");
        if (bookmarksFile.exists()) {
            Log.d("Debug", "bookmark frag found file");
            try {
                FileInputStream fileIn = new FileInputStream(bookmarksFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                bookmarkList = (ArrayList<SerializableBookmark>) in.readObject();
                //Log.i("palval", "dir.exists()");
                in.close();
                fileIn.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Debug", "bookmark frag did not find file");
            try {
                bookmarksFile.createNewFile(); // if file already exists will do nothing
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("Debug:", bookmarkList.size() + " bookmarks found");

        FrameLayout fragmentContent = new FrameLayout(getActivity());
        ScrollView scroll = new ScrollView(getActivity());
        scroll.setPadding(0,0,0,120);
        scroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        linear = new LinearLayout(getActivity());
        linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linear.setOrientation(LinearLayout.VERTICAL);//needed to explicitly say this for it to work
        for (int i=bookmarkList.size()-1; i>=0; i--) {
            //public Comment(Context context, String messageID, String message, String author, String date, int likes, boolean iLiked, int bookmarks, boolean iBookmarked, boolean commentIsBookmark
            Comment cmt = new Comment(
                    me.getActivity(), bookmarkList.get(i).getMessageID(), bookmarkList.get(i).getMessage(), bookmarkList.get(i).getAuthor(),
                    bookmarkList.get(i).getDate(),bookmarkList.get(i).getLikes(),bookmarkList.get(i).isiLiked(),bookmarkList.get(i).getBookmarks(),
                    true, true, bookmarkList.get(i).getAttachment(), false
            );
            cmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            me.linear.addView(cmt);
        }
        scroll.addView(linear);
        fragmentContent.addView(scroll);

        // setUpSwipeToDelete();

        return fragmentContent;
    }

    public static void removeCommentFromBookmarks(final int index) {
        if (index<0||index >= me.linear.getChildCount()) {
            Log.d("ERROR", "Tried to delete element at invalid index");
            return;
        }
        me.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                me.linear.removeView(me.linear.getChildAt(index));//index needed to be final to be used by this inner class
            }
        });
    }



}
