package com.example.demo.appdemo;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * getSongsNames function returns an array of the playlist's songs names
     * @return an array of songs' names
     */
    String[] getSongsNames()
    {
        List<String> temp = new ArrayList<>();
        for (Map.Entry<String, String> entry: songs.entrySet())
        {
            temp.add(entry.getKey());
        }
        String[] names = new String[temp.size()];
        temp.toArray(names);
        return names;
    }


    /**
     * getArtistsNames function returns an array of the playlist's artists names
     * @return an array of artists' names
     */
    String[] getArtistsNames()
    {
        List<String> temp = new ArrayList<>();
        for (Map.Entry<String, String> entry: songs.entrySet())
        {
            temp.add(entry.getValue());
        }
        String[] artists = new String[temp.size()];
        temp.toArray(artists);
        return artists;
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

