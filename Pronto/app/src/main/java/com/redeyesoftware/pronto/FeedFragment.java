package com.redeyesoftware.pronto;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;


public class FeedFragment extends Fragment {


    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        LinearLayout linear = new LinearLayout(getActivity());
        linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linear.setOrientation(LinearLayout.VERTICAL);//needed to explicitly say this for it to work
        for (int i = 0; i < 10; i++) {
            Comment cmt = new Comment(getActivity(), "Message #"+i, "George Eisa", "Today at 8:32 am",5,10);
            cmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linear.addView(cmt);
        }
        scroll.addView(linear);
        fragmentContent.addView(scroll);


        return fragmentContent;
    }



}
