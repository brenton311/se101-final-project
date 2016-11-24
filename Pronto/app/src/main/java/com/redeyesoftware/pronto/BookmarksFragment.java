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
        scroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        linear = new LinearLayout(getActivity());
        linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linear.setOrientation(LinearLayout.VERTICAL);//needed to explicitly say this for it to work
        for (int i=0; i<bookmarkList.size(); i++) {
            //public Comment(Context context, String messageID, String message, String author, String date, int likes, boolean iLiked, int bookmarks
            Comment cmt = new Comment(
                    me.getActivity(), bookmarkList.get(i).getMessageID(), bookmarkList.get(i).getMessage(), bookmarkList.get(i).getAuthor(),
                    bookmarkList.get(i).getDate(),bookmarkList.get(i).getLikes(),bookmarkList.get(i).isiLiked(),bookmarkList.get(i).getBookmarks(), true, true
            );
            cmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            me.linear.addView(cmt);
        }
        scroll.addView(linear);
        fragmentContent.addView(scroll);

        // setUpSwipeToDelete();

        return fragmentContent;
    }

    //Todo: change to bookmarks
    public static void removeCommentFromFeed(final int index) {
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


    private void setUpSwipeToDelete() {
        Log.d("Debug","func called");
        linear.setOnTouchListener(new View.OnTouchListener() {

            float historicX = Float.NaN, historicY = Float.NaN;
            static final int DELTA = 50;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("Debug","xpos: "+event.getX());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        historicX = event.getX();
                        historicY = event.getY();
                        Log.d("Debug","xpos: "+event.getX());
                        return false;

                    case MotionEvent.ACTION_UP:
                        if (linear.getChildAt(0) != null) {
                            int heightOfEachItem = linear.getChildAt(0).getHeight();
                            int heightOfFirstItem = linear.getChildAt(0).getTop();
                            //IF YOU HAVE CHILDS IN LIST VIEW YOU START COUNTING
                            //listView.getChildAt(0).getTop() will see top of child showed in screen
                            //Dividing by height of view, you get how many views are not in the screen
                            //It needs to be Math.ceil in this case because it sometimes only shows part of last view
                            final int firstPosition = (int) Math.ceil(heightOfFirstItem / heightOfEachItem); // This is the same as child #0

                            //Here you get your List position, use historic Y to get where the user went first
                            final int wantedPosition = (int) Math.floor((historicY - linear.getChildAt(0).getTop()) / heightOfEachItem) + firstPosition;

                            Log.d("Debug","first pos: "+firstPosition + " second pos: "+wantedPosition);

                            //Here you get the actually position in the screen
                            final int wantedChild = wantedPosition - firstPosition;
                            //Depending on delta, go right or left
                            if (event.getX() - historicX < -DELTA) {
                                return true;
                            } else if (event.getX() - historicX > DELTA) {
                                //If something went wrong, we stop it now
                                if (wantedChild < 0 || wantedChild >= linear.getChildCount()) {

                                    return true;
                                }
                                //Start animation with 500 miliseconds of time
                                linear.getChildAt(wantedChild).startAnimation(outToRightAnimation(500));
                                //after 500 miliseconds remove from List the item and update the adapter.
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                linear.removeView(linear.getChildAt(wantedChild));
                                            }
                                        },
                                        500
                                );
                                return true;

                            }
                        }
                        return true;
                    default:
                        return false;

                }
            }

            private Animation outToRightAnimation(int duration) {
                Animation outtoRight = new TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, +1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                outtoRight.setDuration(duration);
                outtoRight.setInterpolator(new AccelerateInterpolator());
                return outtoRight;
            }

        });
    }


}