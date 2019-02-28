package com.example.demo.appdemo;

import java.util.ArrayList;
import java.util.List;
import android.support.annotation.NonNull;


public class SearchResults{

    private String searchKey;
    private List<Song> results;

    public SearchResults()
    {
        searchKey = "";
        results = new ArrayList<>();
    }

    public SearchResults(String sk)
    {
        searchKey = sk;
        results = new ArrayList<>();
    }

    public SearchResults(String sk, List<Song> r)
    {
        searchKey = sk;
        results = r;
    }

    public List<Song> getResults() {
        return results;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    /**
     * addResult function adds a song to the results list
     *
     * @param result (Song) the song to add
     */
    public void addResult(Song result)
    {
        results.add(result);
    }

    /**
     * getNextSong receives the current song and returns the name of the next song
     *
     * @param currentSong the current song
     * @return (Song) the next song
     */
    public Song getNextSong(Song currentSong) {
        int indexCurrent = results.indexOf(currentSong);
        int indexNext = indexCurrent + 1;
        if (indexNext==results.size())
            indexNext = 0;
        return results.get(indexNext);
    }

    /**
     * getPrevSong receives the current song and returns the name of the previous song
     *
     * @param currentSong the current song
     * @return (Song) the previous song
     */
    public Song getPrevSong(Song currentSong) {
        int indexCurrent = results.indexOf(currentSong);
        int indexPrev = indexCurrent - 1;
        if (indexPrev==-1)
            indexPrev = results.size() - 1;
        return results.get(indexPrev);
    }

    String getSongArtist(String songName)
    {
        for (Song s: results)
        {
            if(s.getName().equals(songName))
                return s.getArtist();
        }
        return "";
    }

    /**
     * getFittingSearchKey function returns the search key in a fitting database search format
     *
     * @return (String) the fitting search format key
     */
    public String getFittingSearchKey()
    {
        char[] chars = searchKey.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            }
            else if (Character.isWhitespace(chars[i]))
            {
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    private String resultsString()
    {
        StringBuilder songsString = new StringBuilder();
        for (Song result: results)
        {
            songsString.append(result.toString());
            songsString.append("\n");
        }
        return songsString.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return resultsString();
    }
}
