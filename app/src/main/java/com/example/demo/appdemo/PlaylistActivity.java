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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


public class PlaylistActivity extends AppCompatActivity {

    private static final String TAG = ".PlaylistActivity";

    // UI components
    ListView listview;
    TextView playlistNameTxt, currentSongTxt, currentArtistTxt, timerTxt;
    ImageButton playButton, rewindButton, forwardButton;
    SeekBar seekBar;
    SearchView searchView;

    Playlist playlist;
    String playlistName;
    String[] songs;
    String[] artists;

    SearchResults results;

    Song currentSong;
    MediaPlayer mediaPlayer;
    ProgressDialog mDialog;
    int mediaFileLength;
    int realtimeLength;

    private FirebaseFirestore db;

    final Handler handler = new Handler();
    Runnable updater;


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

        timerTxt = findViewById(R.id.textTimer);
        seekBar = findViewById(R.id.seekbar);
        seekBar.setMax(99);

        currentSong = new Song();

        // set playlist name
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            playlistName = (String) bd.get("name");
        }

        // Search page
        if (playlistName.equals("Search"))
        {
            searchView = findViewById(R.id.search_view);
            searchView.setVisibility(View.VISIBLE);
            playlistNameTxt.setVisibility(View.GONE);
            results = new SearchResults();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    getSearchResults(query);
                    currentSong = new Song();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }

        // Static playlist
        else
        {
            playlistNameTxt.setText(playlistName);
            // display the selected playlist songs
            getCurrentPlaylist();
        }

        Log.w(TAG, "!!!playlist name" + playlistName);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mDialog = new ProgressDialog(PlaylistActivity.this);

        // play button onClickListener
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if a song has been chosen
                if (currentSong.getName() != null) {
                    // if a song is currently playing, pause it and change the icon to play icon
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playButton.setImageResource(R.drawable.ic_play);
                        handler.removeCallbacks(updater);
                    }
                    // if a song is not currently playing, play it and change the icon to pause icon
                    else {
                        mediaPlayer.start();
                        playButton.setImageResource(R.drawable.ic_pause);
                        updateSeekBar();
                    }
                }
            }
        });

        // rewind to the previous song
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSong.getName()!=null) {
                    if (playlist!=null) {
                        playPreviosSong();
                    }
                    else {
                        if (!results.getResults().isEmpty())
                        {
                            currentSong = results.getPrevSong(currentSong);
                            playCurrentSong();
                        }

                    }
                }
            }
        });

        // forward to the next song
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSong.getName()!=null) {
                    if (playlist!=null) {
                        playNextSong();
                    }
                    else {
                        if (!results.getResults().isEmpty())
                        {
                            currentSong = results.getNextSong(currentSong);
                            playCurrentSong();
                        }

                    }
                }
            }
        });

        // update seek bar according to user's touch
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if (mediaPlayer.isPlaying()){
                SeekBar seekBar = (SeekBar) v;
                int playPosition = mediaFileLength / 100 * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                realtimeLength = playPosition;
                updateTimer();
                //}
                return false;
            }
        });

    }

    /**
     * getSearchResults function reads the matching results of the current search from the database and
     * displays them
     * @param searchText (String) the text that the user searched
     */
    public void getSearchResults(String searchText) {

        results = new SearchResults();

        // read songs data from the database
        CollectionReference songsRef = db.collection("songs");

        // matching song names
        Query resultSongs = songsRef.whereEqualTo("name", searchText);
        resultSongs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Song resultSong;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        resultSong = document.toObject(Song.class);
                        results.addResult(resultSong);
                    }
                }
            }
        });

        // matching artists
        resultSongs = songsRef.whereEqualTo("artist", searchText);
        resultSongs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Song resultSong;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        resultSong = document.toObject(Song.class);
                        results.addResult(resultSong);
                    }
                    if (!results.getResults().isEmpty())
                        listview.setAdapter(new ResultsAdapter(PlaylistActivity.this, results.getResults()));
                }
            }
        });

    }


    void playSelectedResult(int index)
    {
        currentSong = results.getResults().get(index);
        playCurrentSong();
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
                        playlist.setSongNames();
                        // an array of the songs' names
                        songs = playlist.getSongsNames();
                        //an array of the artists'
                        artists = playlist.getArtistsNames();
                        // call the SongListAdapter of the listview with the songs details
                        listview.setAdapter(new SongListAdapter(PlaylistActivity.this, songs, artists));
                        Log.w(TAG, "@@@songs map: " + playlist.getSongs());
                        Log.w(TAG, "@@@Songs array " + Arrays.toString(songs));

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
     *
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
                realtimeLength = 0;
                updateTimer();
                playButton.setImageResource(R.drawable.ic_pause);
            }

            /**
             * prepare the media player
             */
            @Override
            protected String doInBackground(String... param) {
                try {
                    // pause and reset media player before changing the source
                    if (mediaPlayer.isPlaying()) {
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
                mediaFileLength = mediaPlayer.getDuration();
                mediaPlayer.start();
                updateSeekBar();
                mDialog.dismiss();

                // check if the song has ended
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // reset media bar an play next song
                        realtimeLength = 0;
                        handler.removeCallbacks(updater);
                        updateTimer();
                        seekBar.setProgress(0);
                        if (playlist!=null)
                            playNextSong();
                        else
                        {
                            currentSong = results.getNextSong(currentSong);
                            playCurrentSong();
                        }
                    }
                });
            }
        };

        mp3Play.execute(currentSong.getLink()); //direct link mp3 file
    }


    /**
     * playNextSong function plays the next song on the playlist
     */
    void playNextSong() {
        readSelectedSong(playlist.getNextSong(currentSong.getName()));
    }


    /**
     * playPreviosSong function plays the previous song on the playlist
     */
    void playPreviosSong() {
        readSelectedSong(playlist.getPrevSong(currentSong.getName()));
    }


    /**
     * updateSeekBar function updates the seek bar and timer every second
     */
    void updateSeekBar() {
        seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLength) * 100));
        if (mediaPlayer.isPlaying()) {
            updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    realtimeLength += 1000; //1 second
                    updateTimer();
                }
            };
            handler.postDelayed(updater, 1000);
        }
    }


    /**
     * updateTimer function updates the timer according to the real time position
     */
    void updateTimer() {
        timerTxt.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(realtimeLength),
                TimeUnit.MILLISECONDS.toSeconds(realtimeLength)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(realtimeLength))));
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
