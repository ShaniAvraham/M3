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

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    GridView gridview;

    String[] playlistNameList;
    List<String> temp = new ArrayList<>();

    // TODO: add photos to server (instead of using res)
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

        // Darwer menu
        mDrawerLayout = findViewById(R.id.activity_main);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);

        NavigationView nvDrawer = findViewById(R.id.nv);

        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(nvDrawer);

        // display the home page playlists
        gridview = findViewById(R.id.customgrid);
        readPlaylists();

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

            case R.id.search:
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
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
     * readPlaylist function reads the names of the home page playlists from the database and
     * displays them
     */
    public void readPlaylists()
    {
        // read playlists data from the database
        Task<QuerySnapshot> playlists = db.collection("static playlists")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult()!=null)
                            {
                                // iterate the names of the playlists
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String name = document.getId();
                                    temp.add(name);
                                    Log.d(TAG, document.getId() + " => !!!" + document.getData() + document.getData());
                                }
                                // call the CostumeAdapter of the gridview with the playlists details
                                playlistNameList = new String[temp.size()];
                                temp.toArray(playlistNameList);
                                gridview.setAdapter(new CustomAdapter(HomeActivity.this, playlistNameList, playlistImages));
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
