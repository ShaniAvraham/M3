package com.example.demo.appdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    GridView gridview;

    String[] playlistNameList = {
            "Trending",
            "Party On!",
            "Summer Vibes",
            "Chill",
            "Pop",
            "Rock",
    };

    int[] playlistImages = {
            R.mipmap.trend_icon,
            R.mipmap.party_icon,
            R.mipmap.summer_icon,
            R.mipmap.chill_icon,
            R.mipmap.pop_icon,
            R.mipmap.rock_icon,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        gridview = findViewById(R.id.customgrid);
        gridview.setAdapter(new CustomAdapter(this, playlistNameList, playlistImages));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);

        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nv);

        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(nvDrawer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (mToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void selectIterDrawer(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.home:
                break;

            case R.id.logout:
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                break;

            default:
                ;
        }

        mDrawerLayout.closeDrawers();
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectIterDrawer(menuItem);
                return true;
            }
        });
    }
}
