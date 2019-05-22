package com.example.demo.appdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class RequestDetailsActivity extends AppCompatActivity {

    private static final String TAG = ".RequestDetailsActivity";

    private FirebaseFirestore db;

    FirebaseUser user;

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    TextView userName, playlistNum;

    String requestType;
    String requestId;
    Request request;

    LinearLayout details, response;
    TextView notAvailableTxt, userFieldTxt, textFieldTxt, noResponseTxt, playlistNameTxt;
    ImageButton playlistButton;
    Button choosePlaylistButton, saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // UI Components
        details = findViewById(R.id.details);
        response = findViewById(R.id.response);

        notAvailableTxt = findViewById(R.id.norequest_txt);
        userFieldTxt = findViewById(R.id.userField);
        textFieldTxt = findViewById(R.id.textField);
        noResponseTxt = findViewById(R.id.noresponse_txt);
        playlistNameTxt = findViewById(R.id.playlist_name);

        playlistButton = findViewById(R.id.playlist_image);

        choosePlaylistButton = findViewById(R.id.chooseplaylist_btn);
        saveButton = findViewById(R.id.save_btn);


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

        // get request information
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            requestType = (String) bd.get("type");
            requestId = (String) bd.get("id");
        }

        DocumentReference requestRef = db.collection("requests").document(requestId);
        requestRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);

                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    request = documentSnapshot.toObject(Request.class);
                    presentRequest();
                } else {
                    Log.d(TAG, "Current data: null");
                    details.setVisibility(View.GONE);
                    response.setVisibility(View.GONE);
                    notAvailableTxt.setVisibility(View.VISIBLE);
                }
            }
        });

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
                startActivity(new Intent(RequestDetailsActivity.this, HomeActivity.class));
                finish();
                break;

            case R.id.search:
                Intent intent = new Intent(RequestDetailsActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
                finish();
                break;

            case R.id.my_playlists:
                startActivity(new Intent(RequestDetailsActivity.this, MyPlaylistsActivity.class));
                finish();
                break;

            case R.id.community:
                startActivity(new Intent(RequestDetailsActivity.this, CommunityActivity.class));
                finish();
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(RequestDetailsActivity.this, MainActivity.class));
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
     * presentRequest presents the chosen request
     */
    void presentRequest()
    {
        notAvailableTxt.setVisibility(View.GONE);
        details.setVisibility(View.VISIBLE);
        response.setVisibility(View.VISIBLE);
        String text = "Message: " + request.getText();
        textFieldTxt.setText(text);

        // check if the user is the sender or receiver
        if (requestType.equals("My Requests"))
        {
            text = "From: " + request.getReceiver();
            userFieldTxt.setText(text);
        }
        else {
            text = "To: " + request.getSender();
            userFieldTxt.setText(text);
        }

        // check if there is a response
        // no response
        if (request.getResponsePlaylist().equals(""))
        {
            // if the request was sent by the user
            if (requestType.equals("My Requests"))
            {
                noResponseTxt.setVisibility(View.VISIBLE);
                playlistNameTxt.setVisibility(View.GONE);
                playlistButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                choosePlaylistButton.setVisibility(View.GONE);
            }

            // if the request was sent to the user
            else
            {
                noResponseTxt.setVisibility(View.GONE);
                playlistNameTxt.setVisibility(View.GONE);
                playlistButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                choosePlaylistButton.setVisibility(View.VISIBLE);

                choosePlaylistButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: add send playlist dialog
                    }
                });
            }
        }

        // there is a response
        else
        {
            noResponseTxt.setVisibility(View.GONE);
            playlistNameTxt.setVisibility(View.VISIBLE);
            playlistButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            choosePlaylistButton.setVisibility(View.GONE);

            playlistNameTxt.setText(request.getResponsePlaylist());

            playlistButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: switch to PlaylistActivity
                }
            });

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: save playlist to my playlists
                }
            });
        }
    }
}
