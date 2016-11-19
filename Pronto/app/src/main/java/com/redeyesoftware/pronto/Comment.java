package com.redeyesoftware.pronto;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by George on 19/11/2016.
 */

public class Comment extends FrameLayout implements View.OnClickListener {

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

        setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(parentActivity, MainPage.class);
       // intent.putExtra("index", index);
        parentActivity.startActivity(intent);
    }
}