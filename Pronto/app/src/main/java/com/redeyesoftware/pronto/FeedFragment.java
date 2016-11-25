package com.redeyesoftware.pronto;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import static android.content.Context.MODE_PRIVATE;
import static bolts.Task.delay;


public class FeedFragment extends Fragment {

    private final boolean TESTING_MODE  =false;

    private static FeedFragment me;

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
        RefreshableScrollView scroll = new RefreshableScrollView(getActivity());
        scroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        RelativeLayout relLayout = new RelativeLayout(getActivity());
        LinearLayout linear = new LinearLayout(getActivity());
        linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linear.setOrientation(LinearLayout.VERTICAL);//needed to explicitly say this for it to work

        relLayout.addView(linear);
        scroll.addView(relLayout);
        fragmentContent.addView(scroll);
        scroll.setUpLayout(relLayout, linear);

        if (TESTING_MODE) {
            for (int i=0; i<2; i++) {
                //public Comment(Context context, String messageID, String message, String author, String date, int likes, boolean iLiked, int bookmarks, boolean isBookmarked, boolean commentIsBookmark
                Comment cmt = new Comment(getActivity(), i+"000", "Message #"+i, "George Eisa", "Today at 8:32 am",5,false,10,false, false);
                cmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linear.addView(cmt);
            }
        } else {
            scroll.refresh();
        }

        return fragmentContent;
    }



}
