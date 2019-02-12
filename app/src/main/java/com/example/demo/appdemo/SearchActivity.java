package com.example.demo.appdemo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = ".PlaylistActivity";

    private FirebaseFirestore db;

    // UI components
    SearchView searchView;
    ListView resltslistView;

    List<Song> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();

        searchView = findViewById(R.id.search_view);
        resltslistView = findViewById(R.id.results);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getSearchResults(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    /**
     * getSearchResults function reads the matching results of the current search from the database and
     * displays them
     */
    public void getSearchResults(String searchText) {

        results = new ArrayList<>();

        // read songs data from the database
        CollectionReference songsRef = db.collection("songs");

        // matching song names
        Query resultSongs = songsRef.whereEqualTo("name", searchText);
        resultSongs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Song currentSong;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        currentSong = document.toObject(Song.class);
                        results.add(currentSong);
                    }
                }
            }
        });

        // matching artists
        resultSongs = songsRef.whereEqualTo("artist", searchText);
        resultSongs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Song currentSong;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        currentSong = document.toObject(Song.class);
                        results.add(currentSong);
                    }
                    //resltslistView.setAdapter(new ResultsAdapter(SearchActivity.this, results));

                }
            }
        });

    }
}
