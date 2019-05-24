package com.example.demo.appdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RequestsAdapter extends BaseAdapter {
    String[] usernames;
    Context context;
    String[] texts;

    private static LayoutInflater inflater = null;

    RequestsAdapter(RequestsActivity requestsActivity, String[] user, String[] text) {
        // Auto-generated constructor stub
        usernames = user;
        context = requestsActivity;
        texts = text;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // Auto-generated method stub
        return usernames.length;
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
        TextView username_txt;
        TextView text_txt;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Auto-generated method stub
        RequestsAdapter.Holder holder = new RequestsAdapter.Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.request_list, null);
        holder.username_txt = rowView.findViewById(R.id.usernames);
        holder.text_txt = rowView.findViewById(R.id.texts);

        holder.username_txt.setText(usernames[position]);
        holder.text_txt.setText(texts[position]);

        if (!usernames[0].equals("No Requests"))
        rowView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // play the selected song
                ((RequestsActivity) context).openRequest(position);
            }
        });

        return rowView;
    }
}
