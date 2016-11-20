package com.redeyesoftware.pronto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by George on 19/11/2016.
 */

public class Comment extends FrameLayout implements View.OnTouchListener {

    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 50;


    String message = "";
    String author;
    String date;
    int likes = 0;
    int bookmarks = 0;
    Context parentActivity;

    public Comment(Context context) {
        super(context);
        init();
    }

    public Comment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Comment(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Comment(Context context, String message, String author, String date, int likes, int bookmarks) {
        super(context);
        parentActivity = context;
        this.message = message;
        this.author = author;
        this.date = date;
        this.likes = likes;
        this.bookmarks = bookmarks;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.comment_template, this);

        setOnTouchListener(this);

        ((TextView)(findViewById(R.id.message))).setText(message);
        ((TextView)(findViewById(R.id.author))).setText(author);
        ((TextView)(findViewById(R.id.date))).setText(date);
        ((TextView)(findViewById(R.id.numLikes))).setText("" + likes);
        ((TextView)(findViewById(R.id.numBookmarks))).setText("" + bookmarks);
        /*
        String eventName = "";
        double longi = 0;
        double lat = 0;

        int totalNeeded = 0;
        String desc = "";
        String poster = "";
        String participants = "";
        String eventTime = "";
        eventId = 0;


        (TextView)(findViewById(R.id.eventName)).setText(eventName);
        (TextView)(findViewById(R.id.time)).setText(""+eventTime);
        (TextView)(findViewById(R.id.desc)).setText(desc);
        (TextView)(findViewById(R.id.loc)).setText(longi+", "+lat);
        (TextView)(findViewById(R.id.peeps)).setText((participants.split(",").length+"/"+totalNeeded));
        (TextView)(findViewById(R.id.poster)).setText(poster);
        ProgressBar p = (ProgressBar)(findViewById(R.id.progressBar));
        p.setProgress(100*( (participants.split(",").length) / (float)(totalNeeded) );




        Code from AI Labs

        RelativeLayout hpdown1  = (RelativeLayout)findViewById(R.id.codefile1);
        Resources res = getResources();
        if (type==0) {
            hpdown1.setBackground((res.getDrawable(R.drawable.gradient)));
        }else {
            hpdown1.setBackground((res.getDrawable(R.drawable.gradient2)));
        }
        //request server data

        setOnClickListener(this);

        ((TextView)(findViewById(R.id.typeTitle))).setText(((type==1)?"TANKS AI # ":"PONG AI # ") + index);
        ((TextView)(findViewById(R.id.dateTitle))).setText(date);
        */

    }

   /* @Override
    public void onClick(View view) {
        Intent intent = new Intent(parentActivity, MainPage.class);
       // intent.putExtra("index", index);
        parentActivity.startActivity(intent);
    }*/

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Log.d("Debug", "" + event.getAction());
       // Log.d("Debug", "" + MotionEvent.ACTION_UP);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                historicX = event.getX();
                historicY = event.getY();
                return true;//was oringally returning false here and in default and only ACTION_DOWN were regestering
                 //You need to return true to get the following events after a down.

            case MotionEvent.ACTION_CANCEL://this was originally ACTION_UP (1), but through debugging
                //I saw that event.getAction was never 1. Instead, gestured ended with (3), ACTION_CANCEL

                //Log.d("Debug", "up"));
                if (event.getX() - historicX < -DELTA) {
                    return true;
                } else if (event.getX() - historicX > DELTA) {

                    //Start animation with 500 miliseconds of time
                    this.startAnimation(outToRightAnimation(500));
                    //after 500 miliseconds remove from linear layout
                    //ie the animation takes 500 and after, it is deleted.
                    // if delete immediately, the views below rise before the view being deleting is gone and they overlap

                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    //FeedFragment.linear.removeView(FeedFragment.linear.getChildAt(0));
                                    //the above resulted in an error because Only the original thread that created a view hierarchy can touch its views.
                                    FeedFragment.removeCommentFromFeed(((LinearLayout)Comment.this.getParent()).indexOfChild(Comment.this));
                                    //simply running from Frag Class didnt fix this; likely because it doesnt change the thread
                                    //the real reason why this works is I used Activity.runInUIThread to get the removal to run in original thread
                                    //couldnt have used it here, inside comment, because it runs from an Activity (which the fragment can reference with getActivity() )

                                    //Log.d("Debug", "removed");
                               }
                            },
                            500
                    );
                    return true;

                }

                return true;
            default:
                return true;

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
}