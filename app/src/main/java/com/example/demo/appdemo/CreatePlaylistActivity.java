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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;

public class CreatePlaylistActivity extends AppCompatActivity {

    private static final String TAG = ".CreatePlaylistActivity";

    private FirebaseFirestore db;

    private User currentUser;
    FirebaseUser user;

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    TextView userName, playlistNum;

    EditText nameField;
    Button nextButton;

    String defaultName = "New Playlist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

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

        nameField = findViewById(R.id.playlist_name_text);
        nextButton = findViewById(R.id.next_button);

        // check when the "next" button is selected and create a playlist with he typed name
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the text from the name field (in the fitting database format)
                String playlistName = getFittingSearchKey(nameField.getText().toString());
                if (playlistName.equals(""))
                    playlistName = defaultName;
                else {
                    if (currentUser.getPlaylistNumber() > 0 && currentUser.getPlaylistNames().contains(playlistName))
                        playlistName = checkForDouble(playlistName, 1);
                }

                addNewPlaylist(playlistName);

                Intent intent = new Intent(CreatePlaylistActivity.this, EmptyPlaylistActivity.class);
                intent.putExtra("name", playlistName);
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
                startActivity(new Intent(CreatePlaylistActivity.this, HomeActivity.class));
                break;

            case R.id.search:
                Intent intent = new Intent(CreatePlaylistActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
                break;

            case R.id.my_playlists:
                startActivity(new Intent(CreatePlaylistActivity.this, MyPlaylistsActivity.class));
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CreatePlaylistActivity.this, MainActivity.class));
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
                            nameField.setText(defaultName);
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
     * checkForDouble function is called when the playlist's name already exists, and returns the
     * correct name
     *
     * @param name (String) the original name
     * @param num  (String) the number to begin with
     * @return (String) the correct playlist's name
     */
    String checkForDouble(String name, int num) {
        Log.w(TAG, "!@! entered checkForDouble");
        if (currentUser.getPlaylistNumber() > 0 && currentUser.getPlaylistNames().contains(name + String.valueOf(num)))
            return checkForDouble(name, num + 1);
        Log.w(TAG, "!@! finale name " + name + String.valueOf(num));
        return name + String.valueOf(num);
    }

    /**
     * getFittingSearchKey function returns the search key in a fitting database search format
     *
     * @return (String) the fitting search format key
     */
    public String getFittingSearchKey(String key) {
        char[] chars = key.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i])) {
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * addNewPlaylist function adds the new playlist to the database - it updates the user fields
     * and creates / updates the user's playlist collection
     *
     * @param name the playlist's name
     */
    void addNewPlaylist(String name) {
        // change details locally
        currentUser.addPlaylist(name);

        // update database user data
        DocumentReference userRef = db.collection("users").document(user.getUid());

        // add playlist name
        userRef.update("playlistNames", FieldValue.arrayUnion(name))
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
        userRef.update("playlistNumber", currentUser.getPlaylistNumber())
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
        Playlist newPlaylist = new Playlist(name, new HashMap<String, String>());
        userRef.collection("Playlists").document(name).set(newPlaylist)
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
