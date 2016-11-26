package com.redeyesoftware.pronto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by George on 19/11/2016.
 */

public class Comment extends FrameLayout implements View.OnTouchListener {

    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 80;

    String messageID = "";
    String message = "";
    String author;
    String date;
    int likes = 0;
    boolean iLiked = false;
    int bookmarks = 0;
    boolean iBookmarked = false;
    boolean commentIsBookmark = false;
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

    public Comment(Context context, String messageID, String message, String author, String date, int likes, boolean iLiked, int bookmarks, boolean iBookmarked, boolean commentIsBookmark) {
        super(context);
        parentActivity = context;
        this.messageID = messageID;
        this.message = message;
        this.author = author;
        this.date = date;
        this.likes = likes;
        this.iLiked = iLiked;
        this.bookmarks = bookmarks;
        this.iBookmarked = iBookmarked;
        this.commentIsBookmark = commentIsBookmark;
        init();
    }

    private void remove() {

        //Log.d("Debug", "deleted");
        //Todo: send messageID to server what was deleted
        //NetworkingUtility.get("/inbox/main/", new String[] {"max_messages","group_id"}, new String[] {"20","mid.1479427826988:c661492721"});
        //Start animation with 500 miliseconds of time
        this.startAnimation(outToRightAnimation(500));
        //after 500 miliseconds remove from linear layout
        //ie the animation takes 500 and after, it is deleted.
        // if delete immediately, the views below rise before the view being deleting is gone and they overlap

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        //if ()
                        //FeedFragment.linear.removeView(FeedFragment.linear.getChildAt(0));
                        //the above resulted in an error because Only the original thread that created a view hierarchy can touch its views.
                        RefreshableScrollView.removeCommentFromFeed(((LinearLayout)Comment.this.getParent()).indexOfChild(Comment.this));
                        //simply running from Frag Class didnt fix this; likely because it doesnt change the thread
                        //the real reason why this works is I used Activity.runInUIThread to get the removal to run in original thread
                        //couldnt have used it here, inside comment, because it runs from an Activity (which the fragment can reference with getActivity() )

                        //Log.d("Debug", "removed");
                    }
                },
                500
        );
    }

    private void init() {
        inflate(getContext(), R.layout.comment_template, this);

        setOnTouchListener(this);

        ((TextView)(findViewById(R.id.message))).setText(message);
        ((TextView)(findViewById(R.id.author))).setText(author);
        ((TextView)(findViewById(R.id.date))).setText(date);
        ((TextView)(findViewById(R.id.numBookmarks))).setText("" + bookmarks);

        TextView numLikes =  ((TextView)(findViewById(R.id.numLikes)));
        numLikes.setText("" + likes);
        if (iLiked) {
            numLikes.setTextColor(getResources().getColor(R.color.liked));
            numLikes.setTypeface(null, Typeface.BOLD);//deafult is Typeface.NORMAL
        }

        TextView numBookmarks =  ((TextView)(findViewById(R.id.numBookmarks)));
        numBookmarks.setText("" + bookmarks);
        if (iBookmarked) {
            numBookmarks.setTextColor(getResources().getColor(R.color.liked));
            numBookmarks.setTypeface(null, Typeface.BOLD);//deafult is Typeface.NORMAL
        }

        if (commentIsBookmark) {

        }

        ((ImageButton) findViewById(R.id.viewInNewBtn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        ((ImageButton) findViewById(R.id.likeBtn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences prefs = parentActivity.getSharedPreferences("PrefsFile", MODE_PRIVATE);
                String token = prefs.getString("accessToken", "ERROR: DID NOT READ");
                NetworkingUtility.post("/msg/like/", new String[]{"access_token","msg_id"}, new String[]{token,messageID});
                TextView numLikes =  ((TextView)(findViewById(R.id.numLikes)));
                if (iLiked) {
                    iLiked = false;
                    likes--;
                    numLikes.setText("" + likes);
                    numLikes.setTextColor(getResources().getColor(R.color.offWhite));
                    numLikes.setTypeface(null, Typeface.NORMAL);
                } else {
                    iLiked = true;
                    likes++;
                    numLikes.setText("" + likes);
                    numLikes.setTextColor(getResources().getColor(R.color.liked));
                    numLikes.setTypeface(null, Typeface.BOLD);
                }
                Log.d("Debug", "Message liked");
            }
        });

        ((ImageButton) findViewById(R.id.bookmarkBtn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SerializableBookmark newBookmark = new SerializableBookmark(messageID, message, author, date, likes, iLiked, bookmarks);
                ArrayList<SerializableBookmark> bookmarkList = new ArrayList<SerializableBookmark>();

                File bookmarksFile = new File(parentActivity.getFilesDir().getPath().toString() + "/SavedProntoBookmarks.txt");
                if (bookmarksFile.exists()) {
                    Log.d("Debug", "comment found file");
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
                    Log.d("Debug", "comment did not find file");
                    try {
                        bookmarksFile.createNewFile(); // if file already exists will do nothing
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                bookmarkList.add(newBookmark);

                try {
                    FileOutputStream fileOut = new FileOutputStream(bookmarksFile);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(bookmarkList);//this is only possible becuase SerializableBookmark implements Serializable
                    out.close();
                    fileOut.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d("Debug", "Bookmark successfully added");
            }
        });


        /*
        --Code from Sportigo--
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


        --Code from AI Labs--
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
                setPressed(true);
                return true;//was oringally returning false here and in default and only ACTION_DOWN were regestering
                 //You need to return true to get the following events after a down.

            case MotionEvent.ACTION_UP:
                setPressed(false);
            case MotionEvent.ACTION_CANCEL://this was originally ACTION_UP (1), but through debugging
                //I saw that event.getAction was never 1. Instead, gestured ended with (3), ACTION_CANCEL
                //Log.d("Debug", "up"));
                if (event.getX() - historicX < -DELTA) {
                    return true;
                } else if (event.getX() - historicX > DELTA) {
                    remove();
                    return true;

                }
                setPressed(false);
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