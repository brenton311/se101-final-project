package com.redeyesoftware.pronto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    static private String firstName="";
    static private String lastName="";
    static private String id="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainPage.class);

            //deletes all prev activities from the back stack (otherwise pressing back brings login page)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            LoginActivity.this.startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        //loginButton.setReadPermissions("email");
        //loginButton.setReadPermissions("read_mailbox");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("Login Success", loginResult.getAccessToken().getToken());

                NetworkingUtility.post("/login/", new String[]{"access_token"}, new String[]{loginResult.getAccessToken().getToken()});

                SharedPreferences.Editor editor = getSharedPreferences("PrefsFile", MODE_PRIVATE).edit();
                editor.putString("accessToken", loginResult.getAccessToken().getToken());
                editor.commit();

                Intent intent = new Intent(LoginActivity.this, MainPage.class);
                //deletes all prev activities from the back stack (otherwise pressing back brings login page)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                LoginActivity.this.startActivity(intent);
            }

            @Override
            public void onCancel() {
                Log.e("Login Canceled", "Canceled Facebook Login");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("Login Error", exception.getMessage());
                //before adding onActivityResult and this Log I was blind as to why pressing button did nothing
            }
        });
    }

    public static boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        //Todo: remove the posting of access token each time
       // NetworkingUtility.post("/login/", "access_token", AccessToken.getCurrentAccessToken().getToken());
        return accessToken != null;
    }

    public static void setFacebookData()
    {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            //Log.i("Graph API Response",response.toString());
                            //email = response.getJSONObject().getString("email");
                            firstName = response.getJSONObject().getString("first_name");
                            lastName = response.getJSONObject().getString("last_name");
                            //String gender = response.getJSONObject().getString("gender");
                            //String bday= response.getJSONObject().getString("birthday");

                            Profile profile = Profile.getCurrentProfile();
                            id = profile.getId();
                            /*String link = profile.getLinkUri().toString();
                            Log.i("Link",link);
                            if (Profile.getCurrentProfile()!=null)
                            {
                                Log.i("Login", "ProfilePic" + Profile.getCurrentProfile().getProfilePictureUri(200, 200));
                            }*/
                            Log.i("Login "+ "FirstName", firstName);
                            Log.i("Login " + "LastName", lastName);
                            Log.i("Login " + "ID", id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public static String getId() {
        return id;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
