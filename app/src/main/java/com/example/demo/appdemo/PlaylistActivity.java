package com.example.demo.appdemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    FirebaseUser user;

    final Handler handler = new Handler();
    Runnable updater;

    ImageButton playlistOptionsButton;
    String[] playlistOptions = {"Change playlist name", "Delete playlist"};

    String playlistType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //TODO: add drawer menu

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
        if (playlistName.equals("Search")) {
            searchView = findViewById(R.id.search_view);

            // set UI to search page
            searchView.setVisibility(View.VISIBLE);
            playlistNameTxt.setVisibility(View.GONE);

            results = new SearchResults();

            // set search listener
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                // search when usr presses search key
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
        else {
            playlistNameTxt.setText(playlistName);
            DocumentReference playlistRef;
            // set database path
            playlistType = (String) bd.get("type");
            if (playlistType.equals("static"))
                playlistRef = db.collection("static").document(playlistName);
            else {
                if (playlistType.equals("request"))
                {
                    String recId = (String) bd.get("recId");
                    playlistRef = db.collection("users").document(recId).collection("Playlists").document(playlistName);
                }
                else{
                    playlistRef = db.document("users/" + user.getUid() + "/Playlists/" + playlistName);
                    // make playlist option button visible
                    playlistOptionsButton = findViewById(R.id.option_btn);
                    playlistOptionsButton.setVisibility(View.VISIBLE);
                    playlistOptionsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popOptionDialog();
                        }
                    });
                }
            }

            // display the selected playlist songs
            getCurrentPlaylist(playlistRef);

            // update playlist if changes
            updatePlaylist(playlistRef);
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
                if (currentSong.getName() != null) {
                    if (playlist != null) {
                        playPreviosSong();
                    } else {
                        if (!results.getResults().isEmpty()) {
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
                if (currentSong.getName() != null) {
                    if (playlist != null) {
                        playNextSong();
                    } else {
                        if (!results.getResults().isEmpty()) {
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
     *
     * @param searchText (String) the text that the user searched
     */
    public void getSearchResults(final String searchText) {
        // reset the media player UI
        resetMediaPlayerUI();
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();

        results = new SearchResults(searchText);

        // read songs data from the database
        CollectionReference songsRef = db.collection("songs");

        // matching song names
        Query resultSongs = songsRef.whereEqualTo("name", results.getFittingSearchKey());
        resultSongs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Song resultSong;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        resultSong = document.toObject(Song.class);
                        results.addResult(resultSong);
                    }
                    getSearchResultsArtists();
                }
            }
        });

    }

    void getSearchResultsArtists()
    {
        // matching artists
        CollectionReference songsRef = db.collection("songs");

        Query resultSongs = songsRef.whereEqualTo("artist", results.getFittingSearchKey());
        resultSongs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Song resultSong;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        resultSong = document.toObject(Song.class);
                        results.addResult(resultSong);
                    }
                    listview.setAdapter(new ResultsAdapter(PlaylistActivity.this, results.getResults()));

                }
            }
        });
    }


    void playSelectedResult(int index) {
        currentSong = results.getResults().get(index);
        playCurrentSong();
    }

    /**
     * getCurrentPlaylist function reads the songs of the current playlist from the database and
     * displays them
     */
    public void getCurrentPlaylist(DocumentReference playlistRef) {
        // read songs data from the database
        Log.w(TAG, "entered getCurrentPlaylist!!!");
        playlistRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        playlist = document.toObject(Playlist.class);
                        playlist.setSongNames();
                        // an array of the songs' names
                        songs = playlist.getSongsNames();
                        //an array of the artists
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
                mDialog.setCancelable(false);
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
                        resetMediaPlayerUI();
                        if (playlist != null)
                            playNextSong();
                        else {
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
     * resetMediaPlayerUI function resets the UI of the media player
     */
    void resetMediaPlayerUI() {
        realtimeLength = 0;
        handler.removeCallbacks(updater);
        updateTimer();
        seekBar.setProgress(0);
        currentSongTxt.setText("");
        currentArtistTxt.setText("");
    }

    /**
     * popTypeDialog pops an add dialog according to the playlist type (
     * personal - which includes delete option, and static - which doesn't)
     * @param songName the song's name
     */
    void popTypeDialog(final String songName) {
        if (playlistType.equals("static") || playlistType.equals("request"))
            popAddDialog(songName);
        else
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Options");
            builder.setItems(new String[]{"Remove song from playlist", "Add song to another playlist"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on options[which]
                    switch (which) {
                        // Remove song
                        case 0:
                            popRemoveSongDialog(songName);
                            break;

                        // Add to different
                        case 1:
                            popAddDialog(songName);
                            break;

                    }
                }
            });
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
    }

    /**
     * popAddDialog function pops the add song option dialog, which presents the user's playlists
     *
     * @param songName (String) the chosen song's name
     */
    void popAddDialog(final String songName) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(CurrentUser.currentUser.getPlaylistNumber()==0){
            builder.setTitle("There are no personal playlists");
        }

            else{
            final String[] options = CurrentUser.currentUser.getPlaylistNames().toArray(new String[0]);
            builder.setTitle("Add to playlist:");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on options[which]

                    // song added from search
                    if (playlistName.equals("Search"))
                        addResultToPlaylist(songName, options[which]);

                        // song added from static playlist
                    else
                        addSongToPlaylist(songName, options[which]);
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

    /**
     * popOptionDialog function pops the add song option dialog, which presents the user's playlists
     */
    void popOptionDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");
        builder.setItems(playlistOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on options[which]

                switch (which) {
                    // Change playlist name
                    case 0:
                        openNameDialog();
                        break;

                    case 1:
                        popDeleteDialog();
                        break;
                }
            }
        });
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

    /**
     * popRemoveSongDialog pops a dialog for song removal confirmation
     * @param songName
     */
    void popRemoveSongDialog(final String songName)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove song from playlist");
        builder.setMessage(String.format("Are you sure you want to remove %s from this playlist?", songName));

        // dismiss dialog if cancel was pressed
        // (Negative and Positive are reversed)
        builder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the user clicked on Cancel
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the user clicked on Delete
                removeSongFromPlaylist(songName);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * popDeleteDialog function pops delete dialog, which asks the user if he wants to delete the playlist
     */
    void popDeleteDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(playlistOptions[1]);
        builder.setMessage(String.format("Are you sure you want to delete %s?", playlist.getName()));

        // dismiss dialog if cancel was pressed
        // (Negative and Positive are reversed)
        builder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the user clicked on Cancel
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the user clicked on Delete
                deletePlaylist(playlist.getName());
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    void openNameDialog() {
        LayoutInflater inflater = LayoutInflater.from(PlaylistActivity.this);
        View subView = inflater.inflate(R.layout.dialog_name, null);
        final EditText nameEditText = (EditText) subView.findViewById(R.id.dialogEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(playlistOptions[0]);
        nameEditText.setText(playlist.getName());
        builder.setView(subView);
        AlertDialog alertDialog = builder.create();

        builder.setNegativeButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // change the playlist's name according to the edittext text
                String newName = getFittingSearchKey(nameEditText.getText().toString());
                if (!newName.toLowerCase().equals(playlist.getName().toLowerCase())) {
                    if (CurrentUser.currentUser.getPlaylistNumber() > 1 && CurrentUser.currentUser.getPlaylistNames().contains(newName))
                        newName = checkForDouble(playlistName, 1);
                    changePlaylistName(playlist.getName(), newName);
                    playlistNameTxt.setText(newName);

                }
            }
        });

        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
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
     * changePlaylistName function changes the current playlist's name - it updates the user fields
     * updates the user's playlist collection
     *
     * @param newName the playlist's name
     */
    void changePlaylistName(String oldName, String newName) {

        // change user details locally
        CurrentUser.currentUser.changePlaylistName(oldName, newName);

        // change playlist details locally
        playlist.setName(newName);

        // update database user data
        DocumentReference userRef = db.collection("users").document(user.getUid());

        // remove the old name from user's playlists
        userRef.update("playlistNames", FieldValue.arrayRemove(oldName))
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

        // add the new name to user's playlist
        userRef.update("playlistNames", FieldValue.arrayUnion(newName))
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

        // create a new document for the changed name
        userRef.collection("Playlists").document(newName).set(playlist)
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

        // delete the old document
        userRef.collection("Playlists").document(oldName).delete()
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
     * deletePlaylist receives a playlist's name and deletes it
     *
     * @param playlistName the playlist's name
     */
    void deletePlaylist(String playlistName) {
        // delete locally
        CurrentUser.currentUser.deletePlaylist(playlistName);

        // update database user data
        DocumentReference userRef = db.collection("users").document(user.getUid());

        // remove the name from user's playlists
        userRef.update("playlistNames", FieldValue.arrayRemove(playlistName))
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

        // update playlist number
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

        // delete the old document
        userRef.collection("Playlists").document(playlistName).delete()
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
     * removeSongFromPlaylist removes a song from the playlist
     * @param songName the song's name to remove
     */
    void removeSongFromPlaylist(final String songName) {
        // remove the song from the current playlist
        playlist.removeSong(songName);

        DocumentReference playlistRef = db.collection("users").document(user.getUid()).collection("Playlists").document(playlist.getName());


        playlistRef.update("songs", playlist.getSongs())
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
        getCurrentPlaylist(playlistRef);

    }


    /**
     * addSongToPlaylist receives a song's name and a playlist's name and adds the song to te playlist
     *
     * @param songName     (String) the song (name) to add
     * @param playlistName (String) the playlist (name) to add the song to
     */
    void addSongToPlaylist(final String songName, String playlistName) {
        Log.w(TAG, "!@! entered addSongToPlaylist!!!");
        final DocumentReference destPlaylistsRef = db.document("users/" + user.getUid() + "/Playlists/" + playlistName);
        destPlaylistsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Playlist destPlaylist = document.toObject(Playlist.class);
                        destPlaylist.setSongNames();
                        // an array of the songs' names
                        List<String> destSongNames = Arrays.asList(destPlaylist.getSongsNames());

                        Log.w(TAG, "!@! " + destSongNames);

                        if (!destSongNames.contains(songName)) {
                            Log.w(TAG, "!@! song " + songName);
                            Log.w(TAG, "!@! artist " + playlist.getSongs().get(songName));
                            destPlaylistsRef.update("songs." + songName, playlist.getSongs().get(songName))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "!@! added song to playlist!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "!@! Error while adding song", e);
                                        }
                                    });
                        }


                    }
                    Log.d(TAG, "DocumentSnapshot data: !!!" + playlist);
                } else {
                    Log.d(TAG, "get failed with !!!", task.getException());
                }
            }
        });
    }

    /**
     * addResultToPlaylist receives a result song's name and a playlist's name and adds the song to te playlist
     *
     * @param songName     (String) the result song (name) to add
     * @param playlistName (String) the playlist (name) to add the song to
     */
    void addResultToPlaylist(final String songName, String playlistName) {
        Log.w(TAG, "!@! entered addSongToPlaylist!!!");
        final DocumentReference destPlaylistsRef = db.document("users/" + user.getUid() + "/Playlists/" + playlistName);
        destPlaylistsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Playlist destPlaylist = document.toObject(Playlist.class);
                        destPlaylist.setSongNames();
                        // an array of the songs' names
                        List<String> destSongNames = Arrays.asList(destPlaylist.getSongsNames());

                        Log.w(TAG, "!@! " + destSongNames);

                        if (!destSongNames.contains(songName)) {
                            Log.w(TAG, "!@! song " + songName);
                            Log.w(TAG, "!@! artist " + results.getSongArtist(songName));
                            destPlaylistsRef.update("songs." + songName, results.getSongArtist(songName))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "!@! added song to playlist!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "!@! Error while adding song", e);
                                        }
                                    });
                        }


                    }
                    Log.d(TAG, "DocumentSnapshot data: !!!" + playlist);
                } else {
                    Log.d(TAG, "get failed with !!!", task.getException());
                }
            }
        });
    }


    /**
     * updatePlaylist function updates the playlist according to changes
     * @param playlistRef the playlist's database reference
     */
    void updatePlaylist(final DocumentReference playlistRef) {
        playlistRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "!@!Current data: " + snapshot.getData());
                    getCurrentPlaylist(playlistRef);
                } else {
                    Log.d(TAG, "!@!Current data: null");
                }
            }
        });
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
