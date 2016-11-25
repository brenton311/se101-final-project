package com.redeyesoftware.pronto;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkingUtility.setUpRequestQueue(this);//must be before login becuase login posts token

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        if (LoginActivity.isLoggedIn()) {
            LoginActivity.setFacebookData();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);

            //deletes all prev activities from the back stack (otherwise pressing back access feed without logging in)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_main_page);



        HomePagesAdapter mFragPagerAdapter = new HomePagesAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mFragPagerAdapter);

        //the below would be necessary if tablayout was defined inside the view pager in the xml
        //in this case, instead, it is part of the toolbar to create  cleaner app bar
        TabLayout tablayout = (TabLayout)findViewById(R.id.tab_layout);
        tablayout.setupWithViewPager(mViewPager);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.animate().translationY(-myToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        //This method sets the toolbar as the app bar for the activity
        //By default, the action bar contains just the name of the app and an overflow menu.
        // The options menu initially contains just the Settings item.
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_main, menu);

        // Get the MenuItem for the action item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        //when the user clicks an action view's icon, the view's UI fills the toolbar
        //If you need to configure the action, do so in your activity's onCreateOptionsMenu() callback by calling the static getActionView()
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), SearchResultsActivity.class)));
        searchView.setSubmitButtonEnabled(true);
        /*
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                //Todo: doSearchForQuery(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //doSearchForQuery(newText);
                return true;
            }
        });


        // Assign the listener to that action item if you want to do something when the action is expanded or collapsed
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when action item collapses
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });*/

        return true;
    }

    //define result of buttons on toolbar being pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_search:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case R.id.action_tiva_mode:
                Intent intent = new Intent(this, BluetoothActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_logout:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
