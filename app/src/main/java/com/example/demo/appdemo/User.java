package com.example.demo.appdemo;

import android.support.annotation.NonNull;

import java.util.Arrays;

public class User {

    private int playlistNumber;
    private String[] playlistNames;

    public User()
    {}

    public User(int playlistNum, String[] playArr)
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

    public String[] getPlaylistNames() {
        return playlistNames;
    }

    public void setPlaylistNames(String[] playlists) {
        this.playlistNames = playlists;
    }

    public void increasePlaylistNumber()
    {
        playlistNumber++;
    }

    /*@NonNull
    @Override
    public String toString() {
        return "playlists number: " + playlistNumber + ", playlists names: " + Arrays.toString(playlistNames);
    }*/
}
