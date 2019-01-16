package com.example.demo.appdemo;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class PlaylistActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener{

    private ImageButton btn_play_pause;
    private SeekBar seekbar;
    private TextView textView;

    private MediaPlayer mediaPlayer;
    private int mediaFileLength;
    private int realtimeLength;
    final Handler handler = new Handler();

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
        
        textView = findViewById(R.id.textTimer);

        seekbar = findViewById(R.id.seekbar);
        seekbar.setMax(99);
        seekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mediaPlayer.isPlaying()){
                    SeekBar seekBar = (SeekBar)v;
                    int playPosition = mediaFileLength/100 * seekBar.getProgress();
                    mediaPlayer.seekTo(playPosition);
                }
                return false;
                // TODO update seekbar according to position after touch
            }
        });

        btn_play_pause = findViewById(R.id.btn_play_pause);
        btn_play_pause.setOnClickListener(new View.OnClickListener() {

            final ProgressDialog mDialog = new ProgressDialog(PlaylistActivity.this);


            @Override
            public void onClick(View v) {
                AsyncTask<String, String, String> mp3Play = new AsyncTask<String, String, String>() {

                    @Override
                    protected void onPreExecute() {
                        mDialog.setMessage("please wait");
                        mDialog.show();
                    }

                    @Override
                    protected String doInBackground(String... param) {
                        try {
                            mediaPlayer.setDataSource(param[0]);
                            mediaPlayer.prepare();
                        } catch (Exception e) {
                        }
                        return "";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        mediaFileLength = mediaPlayer.getDuration();
                        realtimeLength = mediaFileLength;

                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            btn_play_pause.setImageResource(R.drawable.ic_pause);
                        } else {
                            mediaPlayer.pause();
                            btn_play_pause.setImageResource(R.drawable.ic_play);
                        }

                        updateSeekBar();
                        mDialog.dismiss();

                    }
                };

                mp3Play.execute("http://docs.google.com/uc?export=open&id=1NR3h2VPdIbSN2gXaYzttqsQHJ1PZHsY5"); //direct link mp3 file

            }
        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    private void updateSeekBar() {
        seekbar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLength) * 100));
        if (mediaPlayer.isPlaying()) {
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    realtimeLength -= 1000; //1 second
                    textView.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(realtimeLength),
                            TimeUnit.MILLISECONDS.toSeconds(realtimeLength)
                                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(realtimeLength))));
                    //TODO reverse text (count to not from)
                }
            };
            handler.postDelayed(updater, 1000);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekbar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play_pause.setImageResource(R.drawable.ic_play);
    }
}
