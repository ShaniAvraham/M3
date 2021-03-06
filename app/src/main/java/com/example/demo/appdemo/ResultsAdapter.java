package com.example.demo.appdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ResultsAdapter extends BaseAdapter {
    String [] result;
    Context context;
    String [] artists;

    private static LayoutInflater inflater=null;
    ResultsAdapter(PlaylistActivity playlistActivity, List<Song> songsList) {
        String[] songNames;
        String[] artistNames;
        if (songsList.isEmpty())
        {
            songNames = new String[1];
            songNames[0] = "No results";
            artistNames = new String[1];
            artistNames[0] = "";
        }
        else {
            songNames = new String[songsList.size()];
            artistNames = new String[songsList.size()];

            int index = 0;

            for (Song s : songsList) {
                songNames[index] = s.getName();
                artistNames[index] = s.getArtist();
                index++;
            }
        }

            result = songNames;
            context = playlistActivity;
            artists = artistNames;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView song_txt;
        TextView artist_txt;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Auto-generated method stub
        ResultsAdapter.Holder holder=new ResultsAdapter.Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.song_list, null);
        holder.song_txt = rowView.findViewById(R.id.songs);
        holder.artist_txt = rowView.findViewById(R.id.artists);

        holder.song_txt.setText(result[position]);
        holder.artist_txt.setText(artists[position]);

        ImageButton moreButton = (ImageButton) rowView.findViewById(R.id.more_btn);

        if (!artists[0].equals("")) {
            // suppose a button id in rawlayout is btn1
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PlaylistActivity)context).popAddDialog(result[position]);
                }
            });
            rowView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // play the selected song
                    ((PlaylistActivity) context).playSelectedResult(position);
                }
            });
        }

        else
            moreButton.setVisibility(View.GONE);



        return rowView;

    }

}
