package com.example.demo.appdemo;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {

    private static final String TAG = ".PlaylistActivity";

    private FirebaseFirestore db;

    final Handler handler = new Handler();

    // UI components
    ListView listview;
    TextView playlistNameTxt;

    Playlist playlist;
    String playlistName;
    String[] songs;
    String[] artists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        db = FirebaseFirestore.getInstance();

        playlistNameTxt = findViewById(R.id.playlist_name);
        listview = findViewById(R.id.listview);

        // set playlist name
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if(bd != null)
        {
            playlistName = (String) bd.get("name");
            playlistNameTxt.setText(playlistName);
        }

        Log.w(TAG, "!!!playlist name" + playlistName);

        getCurrentPlaylist();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listview.setAdapter(new SongListAdapter(PlaylistActivity.this, songs, artists));
            }
        }, 1000);

        // TODO: initial the listview exactly after the data has been read, not after defined amount of time

    }

    public void getCurrentPlaylist()
    {
        Log.w(TAG, "entered getCurrentPlaylist!!!");
        DocumentReference playlistsRef = db.collection("static playlists").document(playlistName);
        playlistsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document!=null) {
                        playlist = document.toObject(Playlist.class);
                        songs = playlist.getSongsNames();
                        artists = playlist.getArtistsNames();
                    }
                    Log.d(TAG, "DocumentSnapshot data: !!!" + playlist);
                }
                else
                {
                    Log.d(TAG, "get failed with !!!", task.getException());
                }
            }
        });
    }
}
