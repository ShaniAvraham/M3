package com.example.demo.appdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = ".HomeActivity";

    private FirebaseFirestore db;

    FirebaseUser user;

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    GridView gridview;
    TextView userName, playlistNum;

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
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Drawer menu
        mDrawerLayout = findViewById(R.id.activity_main);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);
        mDrawerLayout.addDrawerListener(mToggle);

        NavigationView nvDrawer = findViewById(R.id.nv);

        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(nvDrawer);


        View header = nvDrawer.getHeaderView(0);
        userName = header.findViewById(R.id.name);
        playlistNum = header.findViewById(R.id.playlist_num);
        getUserDetails();

        // display the home page playlists
        gridview = findViewById(R.id.customgrid);
        readPlaylists();

        updateDetails();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    /**
     * selectedIterItem function receives a menu item and launches the matching activity
     *
     * @param menuItem the selected menu item
     */
    public void selectIterDrawer(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.home:
                break;

            case R.id.search:
                Intent intent = new Intent(HomeActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
                finish();
                break;

            case R.id.my_playlists:
                startActivity(new Intent(HomeActivity.this, MyPlaylistsActivity.class));
                finish();
                break;

            case R.id.community:
                startActivity(new Intent(HomeActivity.this, CommunityActivity.class));
                finish();
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
                break;

            default:
                break;
        }

        mDrawerLayout.closeDrawers();
    }

    private void setupDrawerContent(NavigationView navigationView) {
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
    public void readPlaylists() {
        // read playlists data from the database
        Task<QuerySnapshot> playlists = db.collection("static playlists")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
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
                        } else {
                            Log.w(TAG, "Error getting documents.!!!", task.getException());
                        }
                    }
                });
    }

    void getUserDetails() {
        if (user != null) {
            // read current user user details data from the database
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            CurrentUser.setCurrentUser(document.toObject(User.class));
                            userName.setText(user.getEmail());
                            playlistNum.setText((String.valueOf("Playlists: " + CurrentUser.currentUser.getPlaylistNumber())));
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    /**
     * updateDetails function updates the screen and user info according to user details changes
     */
    void updateDetails() {
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "!@!Current data: " + snapshot.getData());
                    getUserDetails();
                } else {
                    Log.d(TAG, "!@!Current data: null");
                }
            }
        });
    }
}
