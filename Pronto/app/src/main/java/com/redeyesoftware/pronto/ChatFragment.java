package com.redeyesoftware.pronto;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.SendButton;

import static com.facebook.FacebookSdk.getApplicationContext;


public class ChatFragment extends Fragment {


    public ChatFragment() {
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
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    //originally had button in onCreate and onCreateView but neither worked becuase id would not be found until layout was inflated
    //looked at the fragment life cycle and onStart is called after both
    @Override
    public void onStart() {
         super.onStart();
        ((Button) getActivity().findViewById(R.id.send_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(""))
                        .build();

                //MessageDialog.show(getActivity(), content);

                //  SendButton sendButton = new SendButton(getActivity());
                // sendButton.setShareContent(shareContent);
                //sendButton.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() { ... });
            }
        });
    }

}
