package com.example.demo.appdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class RequestsActivity extends AppCompatActivity {

    private static final String TAG = ".RequestsActivity";

    private FirebaseFirestore db;

    FirebaseUser user;

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    TextView userName, playlistNum;

    String titleType;
    TextView titleRequests;
    ImageButton newRequestButton;
    Query requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

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

        userName.setText(user.getEmail());
        playlistNum.setText((String.valueOf("Playlists: " + CurrentUser.currentUser.getPlaylistNumber())));

        titleRequests = findViewById(R.id.titleRequest);

        // set playlist name
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            titleType = (String) bd.get("title");
        }

        CollectionReference requestsRef = db.collection("requests");

        // set UI according to the chosen option
        // My requests
        if (titleType.equals("my"))
        {
            titleType = "My Requests";
            titleRequests.setText(titleType);
            newRequestButton = findViewById(R.id.plus_btn);
            newRequestButton.setVisibility(View.VISIBLE);

            newRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: switch to new request activity
                }
            });

            requests = requestsRef.whereEqualTo("sender", CurrentUser.currentUser.getUsername());
        }

        // Community requests
        else
        {
            titleType = "Community Requests";
            titleRequests.setText(titleType);
            requests = requestsRef.whereEqualTo("receiver", CurrentUser.currentUser.getUsername());
        }

        getRequests(requests);
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
                startActivity(new Intent(RequestsActivity.this, HomeActivity.class));
                finish();
                break;

            case R.id.search:
                Intent intent = new Intent(RequestsActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
                finish();
                break;

            case R.id.my_playlists:
                startActivity(new Intent(RequestsActivity.this, MyPlaylistsActivity.class));
                finish();
                break;

            case R.id.community:
                startActivity(new Intent(RequestsActivity.this, CommunityActivity.class));
                finish();
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(RequestsActivity.this, MainActivity.class));
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

    void getRequests(Query query)
    {
    }
}
