package com.example.demo.appdemo;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Playlist implements PlayableList{

    /**
     * Playlist class is a class which represents a playlist object
     * attributes :
     * name (String) - the playlist's name
     * songs (Map<String, String>) - the songs which the playlist contains,
     *                               the key is the song's name and the value is the artist
     */

    private String name;
    private Map<String, String> songs;
    private List<String> songNames;

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

    public void setName(String name) {
        this.name = name;
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

    public void setSongNames() {
        songs = new TreeMap<>(songs);
        songNames = new ArrayList<>();
        for (Map.Entry<String, String> entry: songs.entrySet())
        {
            songNames.add(entry.getKey());
        }
        Collections.sort(songNames);
    }

    /**
     * getNextSong recieves the name of the current song and returns the name of the next song
     *
     * @param currentSong (String) the name of the current song
     * @return (String) the name of the next song
     */
    @Override
    public String getNextSong(String currentSong) {
        int indexCurrent = songNames.indexOf(currentSong);
        int indexNext = indexCurrent + 1;
        if (indexNext==songNames.size())
            indexNext = 0;
        return songNames.get(indexNext);
    }

    /**
     * getPrevSong receives the name of the current song and returns the name of the previous song
     *
     * @param currentSong (String) the name of the current song
     * @return (String) the name of the previous song
     */
    @Override
    public String getPrevSong(String currentSong) {
        int indexCurrent = songNames.indexOf(currentSong);
        if (indexCurrent==-1)
        {
            indexCurrent=1;
        }
        int indexPrev = indexCurrent - 1;
        if (indexPrev==-1)
            indexPrev = songNames.size() - 1;
        return songNames.get(indexPrev);
    }

    /**
     * removeSong removes a song from this playlist
     * @param songName the song to remove
     */
    void removeSong(String songName)
    {
        songs.remove(songName);
        songNames.remove(songName);
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

