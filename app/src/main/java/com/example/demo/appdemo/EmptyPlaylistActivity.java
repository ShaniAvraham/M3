package com.example.demo.appdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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


public class EmptyPlaylistActivity extends AppCompatActivity {

    private static final String TAG = ".EmptyPlaylistActivity";

    private FirebaseFirestore db;

    private User currentUser;
    FirebaseUser user;

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    TextView userName, playlistNum;

    String playlistName;

    TextView playlistNameTxt;

    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_playlist);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        playlistNameTxt = findViewById(R.id.playlist_name);
        searchButton = findViewById(R.id.search_button);

        // set playlist name
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            playlistName = (String) bd.get("name");
            playlistNameTxt.setText(playlistName);
        }

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

        // when the search button start SearchActivity
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyPlaylistActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
            }
        });

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
                startActivity(new Intent(EmptyPlaylistActivity.this, HomeActivity.class));
                break;

            case R.id.search:
                Intent intent = new Intent(EmptyPlaylistActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
                break;

            case R.id.my_playlists:
                startActivity(new Intent(EmptyPlaylistActivity.this, MyPlaylistsActivity.class));
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(EmptyPlaylistActivity.this, MainActivity.class));
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
                            currentUser = document.toObject(User.class);
                            userName.setText(user.getEmail());
                            playlistNum.setText((String.valueOf("Playlists: " + currentUser.getPlaylistNumber())));
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
