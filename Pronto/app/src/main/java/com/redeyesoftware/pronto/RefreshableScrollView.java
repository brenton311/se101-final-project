package com.redeyesoftware.pronto;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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


    private static String idOfChatTarget = "";

    public static void setIdOfChatTarget(String idOfChatTarget) {
        RefreshableScrollView.idOfChatTarget = idOfChatTarget;
    }

    private boolean offsetAfter = false;

    private static RefreshableScrollView meFeed;
    private static RefreshableScrollView meChat;
    private boolean isChat = false; //feed if not chat
    private String urlEnd = "/inbox/feed/";
    private String methodKeyEnd = "Feed";
    private float startY = -1, lastY = -1;
    private final int dragLength = 300;//min length of drag to reload
    private final int topMargin = 400;//top margin of progress
    private final int dragOffset = 200;//amount of pixels list moves down
    private boolean refreshing = false;
    private ProgressBar progress;
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

    public RefreshableScrollView(Context context, boolean isChat) {
        super(context);
        parentAcivity = context;
        this.isChat = isChat;
        if (isChat) {
            Log.d("Deebug","chatt");
            urlEnd = "/inbox/main/";
            methodKeyEnd = "Chat";
        }else {
            Log.d("Deebug","feed");
            urlEnd = "/inbox/feed/";
            methodKeyEnd = "Feed";
        }
        init();
    }

    private void init() {
        setOnTouchListener(this);
        if (isChat) {
            meChat =this;
        } else {
            meFeed = this;
        }
        if (idOfChatTarget == "wasCreatedFromChatTarget" && isChat) {//from prev run
            idOfChatTarget = "";
        }
    }


    public void setUpLayout(RelativeLayout content_rel_layout, LinearLayout linear) {
        this.content_rel_layout = content_rel_layout;
        this.linear = linear;
        createProgressBarLayout();
    }


    public static void addCommentsToFeed(boolean addingMore, boolean chat, boolean thenAddMore, boolean before) {
        RefreshableScrollView me = (chat)?meChat:meFeed;
        if (addingMore) {
            me.linear.removeView(me.linear.getChildAt(me.linear.getChildCount() - 1));
        }
        Comment targetView  = (Comment) me.linear.getChildAt(0);//used to focus if before
        if (me.offsetAfter)
            targetView  = (Comment) me.linear.getChildAt(me.linear.getChildCount()-1);
        if (before && NetworkingUtility.comments.length == 1) {//reached top
            idOfChatTarget = "";
        }
        for (int i = (addingMore||before)?1:0; i < NetworkingUtility.comments.length; i++) {
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
            Comment cmt = new Comment(me.parentAcivity, NetworkingUtility.comments[i][1], NetworkingUtility.comments[i][2], NetworkingUtility.comments[i][0], time, numLikes,iLiked,numBookmarks, iBookmarked, false, NetworkingUtility.comments[i][6], me.isChat);
            cmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            if (before || thenAddMore)//the former b/c need to apend to front; the latter b/c need to add in reverse order
                me.linear.addView(cmt,0);
            else
                me.linear.addView(cmt);
        }
        if (me.offsetAfter) {
            int newIndexOfOldInitial = me.linear.indexOfChild(targetView);
            targetView  = (Comment) me.linear.getChildAt(newIndexOfOldInitial+4);
            targetView.getParent().requestChildFocus(targetView, targetView);
        }
        if (before) {
            int newIndexOfOldInitial = me.linear.indexOfChild(targetView);
            targetView  = (Comment) me.linear.getChildAt(newIndexOfOldInitial+4);
            targetView.getParent().requestChildFocus(targetView, targetView);
        }else
             me.createProgressBarLayoutBottom();
        me.finishRefresh();
        if (me.offsetAfter)
            me.offsetAfter = false;
        if (thenAddMore) {
            Comment cmt = (Comment) me.linear.getChildAt(me.linear.getChildCount() - 2);
            me.addMore(cmt.messageID);
            me.offsetAfter = true;
        }
    }

    public static void removeCommentFromFeed(final int index) {
        //Todo: make this workfor chat if want to implement deleting
        if (index<0||index >= meFeed.linear.getChildCount()) {
            Log.d("ERROR", "Tried to delete element at invalid index");
            return;
        }
        ((MainPage)meFeed.parentAcivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                meFeed.linear.removeView(meFeed.linear.getChildAt(index));//index needed to be final to be used by this inner class
            }
        });
    }

    public void refresh() {
        //Log.e("called", "yup");
        SharedPreferences prefs = parentAcivity.getSharedPreferences("PrefsFile", MODE_PRIVATE);
        String token = prefs.getString("accessToken", "ERROR: DID NOT READ");
        //Log.d("got prefs accesstoken",token);
        //smartest in canada 1127396163964738
        if (idOfChatTarget == "" || !isChat) {
            //Log.d("normal refresh", "is " +idOfChatTarget);
            NetworkingUtility.getComments(urlEnd, token, true, 30, 20, "1150546131643551", "", "fill" + methodKeyEnd, new String[]{
                    "author_id", "msg_id", "text", "timestamp", "likes", "bookmarks", "attachments"
            });
            linear.removeAllViews();
        } else if (idOfChatTarget == "wasCreatedFromChatTarget") {
            Comment cmt = (Comment) linear.getChildAt(0);
            NetworkingUtility.getComments(urlEnd, token, false, 30, 20, "1150546131643551", cmt.messageID, "fillChatBefore", new String[]{
                    "author_id", "msg_id", "text", "timestamp", "likes", "bookmarks", "attachments"
            });
        } else {
            NetworkingUtility.getComments(urlEnd, token, false, 10, 5, "1150546131643551", idOfChatTarget, "fillChatThenAddMore", new String[]{
                    "author_id", "msg_id", "text", "timestamp", "likes", "bookmarks", "attachments"
            });
            linear.removeAllViews();
            idOfChatTarget = "wasCreatedFromChatTarget";
        }
    }

    private void addMore(String start) {
        SharedPreferences prefs = parentAcivity.getSharedPreferences("PrefsFile", MODE_PRIVATE);
        String token = prefs.getString("accessToken", "ERROR: DID NOT READ");
        NetworkingUtility.getComments(urlEnd, token, (idOfChatTarget == ""), 30, 20, "1150546131643551",start, "addMoreTo"+methodKeyEnd, new String[]{
                "author_id", "msg_id", "text", "timestamp", "likes", "bookmarks", "attachments"
        });
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
        if (scroll.getScrollY() == 0) {//if at top of screen
            //Log.i("Touch action", ""+event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //Log.i("Touch action", "down");
                    startY = event.getY();
                    lastY = startY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //Log.i("Touch action", "move");
                    if (startY == -1) {
                        //ACTION_DOWN Doesnt seem to be called when comments fill screen (when not in testable mode)
                        //so had to implement this if block and reset startY to -1 ater drag finished
                        startY = event.getY();
                        lastY = startY;
                        break;
                    }
                    if (!refreshing && event.getY() > lastY) {
                        lastY = event.getY();
                        //Log.i("Drag length", ""+(event.getY() - startY));
                        if (event.getY() - startY <= dragLength) {
                            double percent = (event.getY() - startY) / dragLength;
                            double weight;
                            /*weight = 2 * Math.pow(percent, 0.8);
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progress.getLayoutParams();
                            params.weight = (float) weight;
                            progress.setLayoutParams(params);
                            progress.setIndeterminate(false);
                            progress.setPadding(0, 0, 0, 0);*/

                            RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) linear.getLayoutParams();
                            linearParams.topMargin = (int)(percent*dragOffset);
                            linear.setLayoutParams(linearParams);

                            return true;
                        } else {
                            refreshing = true;

                            refresh();

                            startY = -1;
                            //show loading in middle
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progress.getLayoutParams();
                            params.weight = 0;
                            progress.setIndeterminate(true);
                            progress.postInvalidate();
                            progress.setLayoutParams(params);

                            RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) linear.getLayoutParams();
                            linearParams.topMargin = 0;
                            linear.setLayoutParams(linearParams);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //Log.i("Touch action", "up");
                    //if finger up and not refreshing, hide load and reset linear height
                    startY = -1;
                    if (!refreshing) {
                        //Log.i("Debug", "action up " + event.getY());
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progress.getLayoutParams();
                        params.weight = 2;
                        progress.setLayoutParams(params);

                        RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) linear.getLayoutParams();
                        linearParams.topMargin = 0;
                        linear.setLayoutParams(linearParams);
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

        RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) linear.getLayoutParams();
        linearParams.topMargin = 0;
        linear.setLayoutParams(linearParams);
        refreshing = false;
    }

    private void createProgressBarLayout() {

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
        //In indeterminate mode, the progress bar shows a cyclic animation without an indication of progress.
        progress.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary),android.graphics.PorterDuff.Mode.MULTIPLY);
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
        progressParams.weight = 0;
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

    private void createProgressBarLayoutBottom() {
        ProgressBar progressBottom = new ProgressBar(parentAcivity);//, null, android.R.attr.progressBarStyleHorizontal);
        progressBottom.setProgress(100);
        progressBottom.setIndeterminate(true);
        //In indeterminate mode, the progress bar shows a cyclic animation without an indication of progress.
        progressBottom.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary),android.graphics.PorterDuff.Mode.MULTIPLY);
        linear.addView(progressBottom);

        LinearLayout.LayoutParams progressParams = (LinearLayout.LayoutParams) progressBottom.getLayoutParams();
        progressParams.weight = 0;
        progressParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        progressParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //progressParams.topMargin = topMargin;
        progressBottom.setLayoutParams(progressParams);

    }

    @Override
    public  void onScrollChanged (int l, int t, int oldl, int oldt) {
        if (!refreshing) {
            super.onScrollChanged(l, t, oldl, oldt);
            View view = getChildAt(getChildCount() - 1);
            int diff = (view.getBottom() - (getHeight() + getScrollY()));
            // if diff is zero, then the bottom has been reached
            //Log.d("Debug", "" + diff);
            if (diff < 20)//bottom is at-120 instead of 0 b/c bottom padding is 120
            {
                refreshing = true;
                Log.d("Debug", "Bottom has been reached");
                Comment cmt = (Comment) linear.getChildAt(linear.getChildCount() - 2);
                //-2 because last child is loading bar
                addMore(cmt.messageID);
            }
        }
    }

}