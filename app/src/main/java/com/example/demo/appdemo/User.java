package com.example.demo.appdemo;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class User {

    private int playlistNumber;
    private List<String> playlistNames;

    public User()
    {}

    public User(int playlistNum, List<String> playArr)
    {
        playlistNumber = playlistNum;
        playlistNames = playArr;
    }

    public int getPlaylistNumber() {
        return playlistNumber;
    }

    public void setPlaylistNumber(int playlistNumber) {
        this.playlistNumber = playlistNumber;
    }

    public List<String> getPlaylistNames() {
        return playlistNames;
    }

    public void setPlaylistNames(List<String> playlists) {
        this.playlistNames = playlists;
    }

    /**
     * addPlaylist function adds a new playlist to user
     * @param name (String) the playlist's name
     */
    public void addPlaylist(String name)
    {
        if (playlistNames!=null) {
            playlistNames.add(name);
        }

        else {
            playlistNames = new ArrayList<>();
            playlistNames.add(name);
        }
        playlistNumber++;
    }

    @NonNull
    @Override
    public String toString() {
        return "playlists number: " + playlistNumber + ", playlists names: " + playlistNames;
    }
}
