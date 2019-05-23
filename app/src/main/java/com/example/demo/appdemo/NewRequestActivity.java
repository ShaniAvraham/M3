package com.example.demo.appdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewRequestActivity extends AppCompatActivity {

    private static final String TAG = ".NewRequestActivity";

    private FirebaseFirestore db;

    FirebaseUser user;

    // UI components
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    TextView userName, playlistNum;

    ListView usersList;
    SearchView searchView;
    List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        usersList = findViewById(R.id.list_view);
        searchView = findViewById(R.id.search_view);
        users = new ArrayList<>();

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

        // set search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // search when usr presses search key
            @Override
            public boolean onQueryTextSubmit(String query) {
                getUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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
                startActivity(new Intent(NewRequestActivity.this, HomeActivity.class));
                finish();
                break;

            case R.id.search:
                Intent intent = new Intent(NewRequestActivity.this, PlaylistActivity.class);
                intent.putExtra("name", "Search");
                startActivity(intent);
                finish();
                break;

            case R.id.my_playlists:
                startActivity(new Intent(NewRequestActivity.this, MyPlaylistsActivity.class));
                finish();
                break;

            case R.id.community:
                startActivity(new Intent(NewRequestActivity.this, CommunityActivity.class));
                finish();
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(NewRequestActivity.this, MainActivity.class));
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

    //TODO: search if text contains the key
    void getUsers(String searchKey)
    {
        users.clear();
        CollectionReference usersRef = db.collection("users");

        // prevent the user from sending a request to himself
        if (!searchKey.equals(CurrentUser.currentUser.getUsername())) {
            Query queryUsers = usersRef.whereEqualTo("username", searchKey);
            queryUsers.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    User resultUser;

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            resultUser = document.toObject(User.class);
                            users.add(resultUser);
                        }
                        presentUsers(false);
                    }
                }
            });
        }

        else
        {
            presentUsers(true);
        }
    }

    /**
     * presentUsers presents a list of the found users
     */
    void presentUsers(boolean self)
    {
        String[] usernames;

        if (self)
        {
            usernames = new String[1];
            usernames[0] = "You can't send a request to yourself";
        }
        else {

            // No requests
            if (users.isEmpty()) {
                usernames = new String[1];
                usernames[0] = "No Users";
            }
            else {
                usernames = new String[users.size()];
                for (int i = 0; i < users.size(); i++) {
                    usernames[i] = users.get(i).getUsername();
                    Log.w(TAG, "@@@@" + usernames[i]);
                }
            }
        }

        usersList.setAdapter(new UsersAdapter(NewRequestActivity.this, usernames));
    }

    /**
     * createRequest continues to the next step in request creation
     *
     * @param username the username that the request is sent to
     */
    void createRequest(String username)
    {
        Intent intent = new Intent(NewRequestActivity.this, CreateRequestActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
