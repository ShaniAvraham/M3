package com.example.demo.appdemo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SongListAdapter extends BaseAdapter{

    String [] result;
    Context context;
    String [] artists;

    private static LayoutInflater inflater=null;
    public SongListAdapter(PlaylistActivity playlistActivity, String[] songNameList, String[] artistNameList) {
        // Auto-generated constructor stub
        result=songNameList;
        context=playlistActivity;
        artists=artistNameList;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // Auto-generated method stub
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
        Holder holder=new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.song_list, null);
        holder.song_txt =(TextView) rowView.findViewById(R.id.songs);
        holder.artist_txt =(TextView) rowView.findViewById(R.id.artists);

        holder.song_txt.setText(result[position]);
        holder.artist_txt.setText(artists[position]);

        rowView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Auto-generated method stub

                // shows a message with the grid clicked
                Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_SHORT).show();
                // TODO send message to server with the song's name, open play bar with the song's details and play the song
            }
        });

        return rowView;
    }

}
