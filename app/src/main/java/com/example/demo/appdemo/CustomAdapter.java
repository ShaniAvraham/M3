package com.example.demo.appdemo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class CustomAdapter extends BaseAdapter{

    String [] result;
    Context context;
    int [] imageId;

    private static LayoutInflater inflater=null;

    public CustomAdapter(HomeActivity homeActivity, String[] playlistNameList, int[] playlistImages) {
        // Auto-generated constructor stub
        result=playlistNameList;
        context=homeActivity;
        imageId=playlistImages;
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
        TextView playlist_txt;
        ImageView playlist_img;
    }
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // Auto-generated method stub
        Holder holder=new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.sample_gridlayout, null);
        holder.playlist_txt =(TextView) rowView.findViewById(R.id.playlist_texts);
        holder.playlist_img =(ImageView) rowView.findViewById(R.id.playlist_images);

        holder.playlist_txt.setText(result[position]);
        holder.playlist_img.setImageResource(imageId[position]);

        rowView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Auto-generated method stub
                Intent intent= new Intent(context, PlaylistActivity.class);
                intent.putExtra("name", result[position]);
                context.startActivity(intent);
            }
        });

        return rowView;
    }

}