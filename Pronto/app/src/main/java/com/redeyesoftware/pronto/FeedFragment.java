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


public class FeedFragment extends Fragment {

    private static LinearLayout linear;
public static FeedFragment me;
    public FeedFragment() {
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

        FrameLayout fragmentContent = new FrameLayout(getActivity());
        ScrollView scroll = new ScrollView(getActivity());
        scroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        linear = new LinearLayout(getActivity());
        linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linear.setOrientation(LinearLayout.VERTICAL);//needed to explicitly say this for it to work
        for (int i = 0; i < 10; i++) {
            Comment cmt = new Comment(getActivity(), "Message #"+i, "George Eisa", "Today at 8:32 am",5,10);
            cmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linear.addView(cmt);
        }
        scroll.addView(linear);
        fragmentContent.addView(scroll);

       // setUpSwipeToDelete();

        return fragmentContent;
    }

    public void removeCommentFromFeed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
             linear.removeView(linear.getChildAt(0));

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
