package com.example.demo.appdemo;

import android.support.annotation.NonNull;

public class Song {

    /**
     * Song class is a class which represents a song object
     * attributes :
     * name (String) - the song's name
     * artist (String) - the artist who performs the song
     * link (String) - a direct link to the .mp3 file of this song
     */

    private String name;
    private String artist;
    private String link;

    public Song()
    {

    }

    public Song(String n, String a, String l)
    {
        name = n;
        artist = a;
        link = l;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getLink() {
        return link;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("name: %s\nartist: %s", name, artist);
    }
}

