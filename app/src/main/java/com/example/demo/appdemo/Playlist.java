package com.example.demo.appdemo;

import android.support.annotation.NonNull;

import java.util.Map;

public class Playlist {

    /**
     * Playlist class is a class which represents a playlist object
     * attributes :
     * name (String) - the playlist's name
     * songs (Map<String, String>) - the songs which the playlist contains,
     *                               the key is the song's name and the value is the artist
     */

    private String name;
    private Map<String, String> songs;

    public Playlist()
    {

    }

    public String getName() {
        return name;
    }

    public Map<String, String> getSongs() {
        return songs;
    }

    private String playlistString()
    {
        StringBuilder songsString = new StringBuilder();
        for (Map.Entry<String, String> entry: songs.entrySet())
        {
            songsString.append(entry.getKey());
            songsString.append(", ");
            songsString.append(entry.getValue());
            songsString.append("\n");
        }
        return songsString.toString();
    }

    @NonNull
    @Override
    public String toString()
    {
        return name + ":\n" + playlistString();
    }
}

