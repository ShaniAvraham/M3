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
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = ".HomeActivity";

    private FirebaseFirestore db;

    final Handler handler = new Handler();

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    GridView gridview;

    String[] playlistNameList;
    List<String> temp = new ArrayList<>();

    int[] playlistImages = {
            R.mipmap.chill_icon,
            R.mipmap.party_icon,
            R.mipmap.pop_icon,
            R.mipmap.rock_icon,
            R.mipmap.summer_icon,
            R.mipmap.trend_icon,};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        readPlaylist();

        mDrawerLayout = findViewById(R.id.activity_main);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);

        NavigationView nvDrawer = findViewById(R.id.nv);

        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(nvDrawer);

        // create the gridview after the data has been read from the database
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playlistNameList = new String[temp.size()];
                temp.toArray(playlistNameList);
                gridview = findViewById(R.id.customgrid);
                gridview.setAdapter(new CustomAdapter(HomeActivity.this, playlistNameList, playlistImages));
            }
        }, 1750);

        // TODO: initial the gridview exactly after the data has been read, not after defined amount of time

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (mToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    /**
     * selectedIterItem function receives a menu item and launches the matching activity
     * @param menuItem the selected menu item
     */
    public void selectIterDrawer(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.home:
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
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

    /**
     * readPlaylist function reads the name of the home page playlists from the database
     */
    public void readPlaylist()
    {
        Task<QuerySnapshot> playlists = db.collection("static playlists")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult()!=null)
                            {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String name = document.getId();
                                    temp.add(name);
                                    Log.d(TAG, document.getId() + " => !!!" + document.getData() + document.getData());
                                }
                            }
                        }
                        else
                        {
                            Log.w(TAG, "Error getting documents.!!!", task.getException());
                        }
                    }
                });
    }
}
