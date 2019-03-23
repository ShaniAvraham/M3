package com.example.demo.appdemo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyPlaylistsAdapter extends BaseAdapter {

    String[] result;
    Context context;
    int[] imageId;

    private static LayoutInflater inflater = null;

    public MyPlaylistsAdapter(MyPlaylistsActivity myPlaylistsActivity, String[] playlistNameList, int[] playlistImages) {
        // Auto-generated constructor stub
        if (playlistNameList != null) {
            result = new String[playlistNameList.length + 1];
            imageId = new int[playlistNameList.length + 1];
            System.arraycopy(playlistNameList, 0, result, 1, playlistNameList.length);
            System.arraycopy(playlistImages, 0, imageId, 1, playlistImages.length);


            for (int i = 0; i < imageId.length; i++) {
                if (imageId[i] == 0)
                    imageId[i] = R.mipmap.pop_icon;
            }
        } else {
            result = new String[1];
            imageId = new int[1];
        }

        context = myPlaylistsActivity;

        result[0] = "Create Playlist";
        imageId[0] = R.mipmap.add_icon;

        inflater = (LayoutInflater) context.
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

    public class Holder {
        TextView playlist_txt;
        ImageView playlist_img;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // Auto-generated method stub
        MyPlaylistsAdapter.Holder holder = new MyPlaylistsAdapter.Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.sample_gridlayout, null);
        holder.playlist_txt = (TextView) rowView.findViewById(R.id.playlist_texts);
        holder.playlist_img = (ImageView) rowView.findViewById(R.id.playlist_images);

        holder.playlist_txt.setText(result[position]);
        holder.playlist_img.setImageResource(imageId[position]);

        rowView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if create playlist was pressed
                if (position == 0) {
                    Intent intent = new Intent(context, CreatePlaylistActivity.class);
                    intent.putExtra("name", result[position]);
                    context.startActivity(intent);
                } else {
                    // Auto-generated method stub
                    Intent intent = new Intent(context, PlaylistActivity.class);
                    intent.putExtra("name", result[position]);
                    intent.putExtra("type", "personal");
                    context.startActivity(intent);
                }
            }
        });

        return rowView;
    }
}
