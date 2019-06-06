package com.example.demo.appdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

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
    DocumentReference requestRef;

    String recieverId;

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

        requestRef = db.collection("requests").document(requestId);
        requestRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);

                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    request = documentSnapshot.toObject(Request.class);
                    getUserId(request.getReceiver());
                    presentRequest();
                } else {
                    Log.d(TAG, "Current data: null");
                    details.setVisibility(View.GONE);
                    response.setVisibility(View.GONE);
                    notAvailableTxt.setVisibility(View.VISIBLE);
                }
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
                startActivity(new Intent(RequestDetailsActivity.this, HomeActivity.class));
                finish();
                break;

            case R.id.search:
                Intent intent = new Intent(RequestDetailsActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
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
            text = "To: " + request.getReceiver();
            userFieldTxt.setText(text);
        }
        else {
            text = "From: " + request.getSender();
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
                        // pop the playlist options dialog
                        popPlaylistsdDialog();
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
                    Intent intent = new Intent(RequestDetailsActivity.this, PlaylistActivity.class);
                    intent.putExtra("name", request.getResponsePlaylist());
                    intent.putExtra("type", "request");
                    intent.putExtra("recId", recieverId);
                    startActivity(intent);
                }
            });

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w(TAG, "!@! onClick of save");
                    savePlaylist();
                }
            });
        }
    }

    /**
     * popPlaylistsdDialog function pops an option dialog, which presents the user's playlists
     * the user chooses which playlist he wants to send to the request author
     */
    void popPlaylistsdDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(CurrentUser.currentUser.getPlaylistNumber()==0){
            builder.setTitle("There are no personal playlists");
        }

        else{
            final String[] options = CurrentUser.currentUser.getPlaylistNames().toArray(new String[0]);
            builder.setTitle("Send playlist:");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on options[which]
                    requestRef.update("responsePlaylist", options[which]);
                    finish();
                }
            });
        }
        // dismiss dialog if cancel was pressed
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the user clicked on Cancel
                dialog.dismiss();
            }
        });
        builder.show();
    }

    void savePlaylist()
    {
        Log.w(TAG, "!@! inside savePlaylist()");
        // check if there is a response
        if(!request.getResponsePlaylist().equals(""))
        {
            Log.w(TAG, "!@! after empty");

            getUserId(request.getReceiver());
            getResPlaylist();
        }
    }

    /**
     * getUserId receives a username and finds it's document id
     *
     * @param username the username
     */
    void getUserId(String username)
    {
        CollectionReference usersRef = db.collection("users");

            Query queryUsers = usersRef.whereEqualTo("username", username);
            queryUsers.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            recieverId = document.getId();
                        }
                    }
                }
            });

    }

    /**
     * getResPlaylist reads the response playlist from the database, then copies the playlist
     */
    void getResPlaylist()
    {
        DocumentReference docRef = db.collection("users").document(recieverId).collection("Playlists").document(request.getResponsePlaylist());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        // get the playlist to save
                        Playlist playlistToSave = document.toObject(Playlist.class);

                        // check if the playlist's name already exists
                        if (CurrentUser.currentUser.getPlaylistNumber() > 0 && CurrentUser.currentUser.getPlaylistNames().contains(playlistToSave.getName()))
                            playlistToSave.setName(checkForDouble(playlistToSave.getName(), 1));

                        // copy the playlist
                        copyPlaylist(playlistToSave);

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such playlist");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * checkForDouble function is called when the playlist's name already exists, and returns the
     * correct name
     *
     * @param name (String) the original name
     * @param num  (String) the number to begin with
     * @return (String) the correct playlist's name
     */
    String checkForDouble(String name, int num) {
        Log.w(TAG, "!@! entered checkForDouble");
        if (CurrentUser.currentUser.getPlaylistNumber() > 0 && CurrentUser.currentUser.getPlaylistNames().contains(name + String.valueOf(num)))
            return checkForDouble(name, num + 1);
        Log.w(TAG, "!@! finale name " + name + String.valueOf(num));
        return name + String.valueOf(num);
    }

    /**
     * copyPlaylist receives a playlist and copies it to the user's personal playlists
     *
     * @param playlist the playlist to copy
     */
    void copyPlaylist(Playlist playlist)
    {
        // change details locally
        CurrentUser.currentUser.addPlaylist(playlist.getName());

        // update database user data
        DocumentReference userRef = db.collection("users").document(user.getUid());

        // add playlist name
        userRef.update("playlistNames", FieldValue.arrayUnion(playlist.getName()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "!@! DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "!@! Error updating document", e);
                    }
                });

        // increase playlists number
        userRef.update("playlistNumber", CurrentUser.currentUser.getPlaylistNumber())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "!@! DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "!@! Error updating document", e);
                    }
                });

        // create a new collection for the new playlist or update existing one
        userRef.collection("Playlists").document(playlist.getName()).set(playlist)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "!@! Document successfully created!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "!@! Error creating document", e);
                    }
                });
    }

    /**
     * updateDetails function updates the screen and user info according to user details changes
     */
    void updateDetails() {
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@android.support.annotation.Nullable DocumentSnapshot snapshot,
                                @android.support.annotation.Nullable FirebaseFirestoreException e) {
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
}
