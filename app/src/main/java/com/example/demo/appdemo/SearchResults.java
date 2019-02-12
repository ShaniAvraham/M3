package com.example.demo.appdemo;

import java.util.ArrayList;
import java.util.List;
import android.support.annotation.NonNull;


public class SearchResults{

    private List<Song> results;

    public SearchResults()
    {
        results = new ArrayList<>();
    }

    public SearchResults(List<Song> r)
    {
        results = r;
    }

    public List<Song> getResults() {
        return results;
    }

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
