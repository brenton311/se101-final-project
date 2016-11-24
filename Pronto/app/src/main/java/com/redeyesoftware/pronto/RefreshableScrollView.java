package com.redeyesoftware.pronto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import static android.content.Context.MODE_PRIVATE;


/**
 * Created by George on 24/11/2016.
 */

public class RefreshableScrollView extends ScrollView implements View.OnTouchListener {

    private static RefreshableScrollView me;
    private float startY = 10000f, lastY = 10000f;
    private int dragLength = 0, topMargin = 0;
    private boolean refreshing = false;
    private ProgressBar progress;;
    private RelativeLayout content_rel_layout;
    private LinearLayout linear;
    private Context parentAcivity;

    public RefreshableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parentAcivity = context;
        init();
    }

    public RefreshableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parentAcivity = context;
        init();
    }

    public RefreshableScrollView(Context context) {
        super(context);
        parentAcivity = context;
        init();
    }

    private void init() {
        setOnTouchListener(this);
        me = this;
    }

    public void setUpLayout(RelativeLayout content_rel_layout, LinearLayout linear) {
        this.content_rel_layout = content_rel_layout;
        this.linear = linear;
        createProgressBarLayout();
    }


    public static void addCommentsToFeed() {
        for (int i = 0; i < NetworkingUtility.comments.length; i++) {
            //Log.d("long timsetamp",NetworkingUtility.comments[i][3]);
            String time = TimeStampConverter.getDate(Long.parseLong(NetworkingUtility.comments[i][3]));
            boolean iLiked = NetworkingUtility.comments[i][4].indexOf(LoginActivity.getId()) != -1;
            boolean iBookmarked = NetworkingUtility.comments[i][5].indexOf(LoginActivity.getId()) != -1;
            int numLikes = NetworkingUtility.comments[i][4].length() - NetworkingUtility.comments[i][4].replace(",", "").length();
            int numBookmarks =  NetworkingUtility.comments[i][5].length() - NetworkingUtility.comments[i][5].replace(",", "").length();
            if (numLikes>0) numLikes++;
            if (numBookmarks>0) numBookmarks++;
            if (numLikes==0 && NetworkingUtility.comments[i][4].length()>4) numLikes=1;
            if (numBookmarks==0 && NetworkingUtility.comments[i][5].length()>4) numBookmarks=1;
            Comment cmt = new Comment(me.parentAcivity, NetworkingUtility.comments[i][1], NetworkingUtility.comments[i][2], NetworkingUtility.comments[i][0], time, numLikes,iLiked,numBookmarks, iBookmarked, false);
            cmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            me.linear.addView(cmt);
        }
        me.finishRefresh();
    }

    public static void removeCommentFromFeed(final int index) {
        if (index<0||index >= me.linear.getChildCount()) {
            Log.d("ERROR", "Tried to delete element at invalid index");
            return;
        }
        ((MainPage)me.parentAcivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                me.linear.removeView(me.linear.getChildAt(index));//index needed to be final to be used by this inner class
            }
        });
    }

    public static void refresh() {
        SharedPreferences prefs = me.parentAcivity.getSharedPreferences("PrefsFile", MODE_PRIVATE);
        String token = prefs.getString("accessToken", "ERROR: DID NOT READ");
        //Log.d("got prefs accesstoken",token);
        NetworkingUtility.getComments("/inbox/main/", token, 30, "1127396163964738", "fillFeed", new String[]{
                "author_id", "msg_id", "text", "timestamp", "likes", "bookmarks"
        });
        me.linear.removeAllViews();
    }

   /* @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = (View) getChildAt(getChildCount()-1);
        int diff = (view.getBottom()-(getHeight()+getScrollY()));

        if (diff == 0) {
            //mListener.onBottomReached();
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }*/

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ScrollView scroll = (ScrollView) v;
        if (scroll.getScrollY() == 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    lastY = startY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!refreshing && event.getY() > lastY) {
                        lastY = event.getY();
                        if (event.getY() - startY <= dragLength) {
                            double percent = 1 - (event.getY() - startY) / dragLength;
                            double weight;
                            weight = 2 * Math.pow(percent, 0.8);
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progress.getLayoutParams();
                            params.weight = (float) weight;
                            progress.setLayoutParams(params);
                            progress.setIndeterminate(false);
                            progress.setPadding(0, 0, 0, 0);
                            return true;
                        } else {
                            refreshing = true;

                            refresh();

                            startY = 100000f;
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progress.getLayoutParams();
                            params.weight = 0;
                            progress.setIndeterminate(true);
                            progress.postInvalidate();
                            progress.setLayoutParams(params);
                        }
                    }
                case MotionEvent.ACTION_UP:
                    startY = 100000f;
                    if (!refreshing) {
                        Log.i("Debug", "action up " + event.getY());
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progress.getLayoutParams();
                        params.weight = 2;
                        progress.setLayoutParams(params);
                    }
            }
        }
        return false;
    }

    public void finishRefresh() {
        progress.setIndeterminate(false);
        progress.postInvalidate();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progress.getLayoutParams();
        params.weight = 2;
        progress.setLayoutParams(params);
        refreshing = false;
    }

    private void createProgressBarLayout() {
        topMargin = 100;//-Math.round(6 * act.metrics.density);
        dragLength = 100;//Math.round(act.screen_size.y / 2.5f);

        LinearLayout top = new LinearLayout(parentAcivity);
        top.setGravity(Gravity.TOP);
        top.setOrientation(LinearLayout.HORIZONTAL);

        content_rel_layout.addView(top);
        ViewGroup.LayoutParams topParams = top.getLayoutParams();
        topParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        topParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        top.setLayoutParams(topParams);

        FrameLayout left = new FrameLayout(parentAcivity);
        progress = new ProgressBar(parentAcivity);//, null, android.R.attr.progressBarStyleHorizontal);
        progress.setProgress(100);
        progress.setIndeterminate(false);
        // progress.setBackgroundResource(R.drawable.progress_bar);
        FrameLayout right = new FrameLayout(parentAcivity);

        top.addView(left);
        top.addView(progress);
        top.addView(right);

        LinearLayout.LayoutParams leftParams = (LinearLayout.LayoutParams) left.getLayoutParams();
        leftParams.weight = 1;
        leftParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        leftParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        leftParams.topMargin = topMargin;
        left.setLayoutParams(leftParams);

        LinearLayout.LayoutParams progressParams = (LinearLayout.LayoutParams) progress.getLayoutParams();
        progressParams.weight = 2;
        progressParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        progressParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        progressParams.topMargin = topMargin;

        progress.setLayoutParams(progressParams);

        LinearLayout.LayoutParams rightParams = (LinearLayout.LayoutParams) right.getLayoutParams();
        rightParams.weight = 1;
        rightParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        rightParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        rightParams.topMargin = topMargin;

        right.setLayoutParams(rightParams);
    }

}