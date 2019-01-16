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
                "Apologize",
                "How Long",
                "Stay",
                "Do I Wanna Know",
                "Nevermind",
                "Sweater Weather",
        };

        String[] artists = {
                "Timbaland ft. One Republic",
                "Charlie Puth",
                "Zedd ft. Alessia Cara",
                "Arctic Monkeys",
                "Dennis Lloyd",
                "The Neighborhood",
        };
        ListView listview;

        // TODO send a request with playlist name to server and present the songs
        listview = findViewById(R.id.listview);
        listview.setAdapter(new SongListAdapter(this, songs, artists));

        // TODO after pressing a button - send a request with the song's name and receive a link to play
        // TODO play the song using the link
    }
}
