package com.example.demo.appdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;


public class PlaylistActivity extends AppCompatActivity {

    private static final String TAG = ".PlaylistActivity";

    // UI components
    ListView listview;
    TextView playlistNameTxt , currentSongTxt, currentArtistTxt, timerTxt;
    ImageButton playButton, rewindButton, forwardButton;
    SeekBar seekBar;

    Playlist playlist;
    String playlistName;
    String[] songs;
    String[] artists;
    NavigableMap<String,String> navigatePlaylist;

    Song currentSong;
    MediaPlayer mediaPlayer;
    ProgressDialog mDialog;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        db = FirebaseFirestore.getInstance();

        playlistNameTxt = findViewById(R.id.playlist_name);
        listview = findViewById(R.id.listview);

        currentSongTxt = findViewById(R.id.textSong);
        currentArtistTxt = findViewById(R.id.textArtist);

        playButton = findViewById(R.id.play_pause_btn);
        rewindButton = findViewById(R.id.rewind_btn);
        forwardButton = findViewById(R.id.forward_btn);


        // set playlist name
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            playlistName = (String) bd.get("name");
            playlistNameTxt.setText(playlistName);
        }

        Log.w(TAG, "!!!playlist name" + playlistName);

        // display the selected playlist songs
        getCurrentPlaylist();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mDialog = new ProgressDialog(PlaylistActivity.this);

        // play button onClickListener
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if a song has been chosen
                if(currentSong.getName()!= null)
                {
                    // if a song is currently playing, pause it and change the icon to play icon
                    if (mediaPlayer.isPlaying())
                    {
                        mediaPlayer.pause();
                        playButton.setImageResource(R.drawable.ic_play);
                    }
                    // if a song is not currently playing, play it and change the icon to pause icon
                    else
                    {
                        mediaPlayer.start();
                        playButton.setImageResource(R.drawable.ic_pause);
                    }
                }
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG,"@@@"+navigatePlaylist.toString());
                Map.Entry<String, String> previosSongEntry = navigatePlaylist.lowerEntry(currentSong.getName());
                Log.w(TAG, "@@@is map empty " + navigatePlaylist.isEmpty());
                if (previosSongEntry == null) {
                        previosSongEntry = navigatePlaylist.lastEntry();
                    }
                Log.w(TAG, "@@@prevsong " + previosSongEntry);
                readSelectedSong(previosSongEntry.getKey());
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG,"@@@"+navigatePlaylist.toString());
                Map.Entry<String, String> nextSongEntry = navigatePlaylist.higherEntry(currentSong.getName());
                Log.w(TAG, "@@@is map empty " + navigatePlaylist.isEmpty());
                if (nextSongEntry == null) {
                    nextSongEntry = navigatePlaylist.firstEntry();
                }
                Log.w(TAG, "@@@prevsong " + nextSongEntry);
                readSelectedSong(nextSongEntry.getKey());
            }
        });

    }

    /**
     * getCurrentPlaylist function reads the songs of the current playlist from the database and
     * displays them
     */
    public void getCurrentPlaylist() {
        // read songs data from the database
        Log.w(TAG, "entered getCurrentPlaylist!!!");
        DocumentReference playlistsRef = db.collection("static playlists").document(playlistName);
        playlistsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        playlist = document.toObject(Playlist.class);
                        // an array of the songs' names
                        songs = playlist.getSongsNames();
                        //an array of the artists'
                        artists = playlist.getArtistsNames();
                        // call the SongListAdapter of the listview with the songs details
                        listview.setAdapter(new SongListAdapter(PlaylistActivity.this, songs, artists));
                        Log.w(TAG, "@@@songs map: " + playlist.getSongs());
                        Log.w(TAG, "@@@Songs array " + Arrays.toString(songs));
                        navigatePlaylist = new TreeMap<>();
                        for (Map.Entry<String, String> entry: playlist.getSongs().entrySet()) {
                            navigatePlaylist.put(entry.getKey(),entry.getValue());
                            Log.w(TAG, "@@@ copy: key: " + entry.getKey() + " value: " + entry.getValue());
                        }
                        currentSong = new Song();
                    }
                    Log.d(TAG, "DocumentSnapshot data: !!!" + playlist);
                } else {
                    Log.d(TAG, "get failed with !!!", task.getException());
                }
            }
        });
    }

    /**
     * readSelectedSong function receives a song's name and queries it from he database
     * Then, it calls playCurrentSong and plays the current song
     * @param songName (String) the selected song's name
     */
    public void readSelectedSong(String songName) {
        CollectionReference songsRef = db.collection("songs");
        Query selectedSong = songsRef.whereEqualTo("name", songName).limit(1);
        selectedSong.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        currentSong = document.toObject(Song.class);
                        playCurrentSong();
                    }
                }
            }
        });
        Log.w(TAG, "@@@ " + selectedSong.get());
    }

    /**
     * playCurrentSong runs an asynctask which plays the current song
     */
    void playCurrentSong() {
        AsyncTask<String, String, String> mp3Play = new AsyncTask<String, String, String>() {

            /**
             * show dialog of loading song
             */
            @Override
            protected void onPreExecute() {
                mDialog.setMessage("please wait\n" + currentSong.getName());
                mDialog.show();
                currentSongTxt.setText(currentSong.getName());
                currentArtistTxt.setText(currentSong.getArtist());
                playButton.setImageResource(R.drawable.ic_pause);
            }

            /**
             * prepare the media player
             */
            @Override
            protected String doInBackground(String... param) {
                try {
                    // pause and reset media player before changing the source
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    mediaPlayer.reset();

                    Log.w(TAG, "@@@param - " + param[0]);
                    // set the link as data source
                    mediaPlayer.setDataSource(param[0]);
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    Log.w(TAG, "@@@Exception (mediaPlayer)" + e.toString() + " current: " + param[0]);
                }
                return "";
            }

            /**
             * play the song & dismiss dialog
             * @param s (String)
             */
            @Override
            protected void onPostExecute(String s) {
                mediaPlayer.start();
                mDialog.dismiss();
            }
        };

        mp3Play.execute(currentSong.getLink()); //direct link mp3 file


    }

    /**
     * onStop function stops the played song (if a song is played) when the activity is stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }
}

