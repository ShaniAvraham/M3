package com.example.demo.appdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        String[] songs = {
                "Trending",
                "Party On!",
                "Summer Vibes",
                "Chill",
                "Pop",
                "Rock",
        };

        String[] artists = {
                "Trending",
                "Party On!",
                "Summer Vibes",
                "Chill",
                "Pop",
                "Rockjrkghfhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhjjjjjjjjjjjj",
        };
        ListView listview;

        // TODO send a request with playlist name to server and present the songs
        listview = findViewById(R.id.listview);
        listview.setAdapter(new SongListAdapter(this, songs, artists));
    }
}
