package com.example.demo.appdemo;

import android.support.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

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

    public Playlist(String n, Map<String,String> s)
    {
        name = n;
        songs = s;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getSongs() {
        return songs;
    }

    /**
     * getSongsNames function returns an array of the playlist's songs names
     * @return an alphabetically sorted array of songs' names
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
        Arrays.sort(names);
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

    /**
     * getSongEntry function receives a song's name and returns it's entry from the songs map
     * @param name (String) the song's name
     * @return (Map.Entry<String,String>) the requested entry
     */
    Map.Entry<String,String> getSongEntry(String name)
    {
        for (Map.Entry<String, String> entry: songs.entrySet()) {
            if(entry.getKey().equals(name))
                return entry;
        }
        return null;
    }

    NavigableMap<String,String> getNavigationMap()
    {
        NavigableMap<String,String> navMap = new TreeMap<>();
        for (Map.Entry<String, String> entry: songs.entrySet()) {
            navMap.put(entry.getKey(),entry.getValue());
        }
        return navMap;
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

