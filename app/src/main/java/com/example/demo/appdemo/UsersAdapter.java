package com.example.demo.appdemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class UsersAdapter extends BaseAdapter {

    String[] users;
    Context context;

    private static LayoutInflater inflater = null;

    UsersAdapter(NewRequestActivity newRequestActivity, String[] usernamesList) {
        // Auto-generated constructor stub
        users = usernamesList;
        context = newRequestActivity;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // Auto-generated method stub
        return users.length;
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
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Auto-generated method stub
        UsersAdapter.Holder holder = new UsersAdapter.Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.user_list, null);
        try {

            holder.username_txt = rowView.findViewById(R.id.username_txt);

            holder.username_txt.setText(users[position]);

            if (!users[0].equals("No Users") && !users[0].equals("You can't send a request to yourself"))
                rowView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // play the selected song
                        ((NewRequestActivity) context).createRequest(users[position]);
                    }
                });
        } catch (Exception e)
        {
            Log.w("UsersAdapter", "@@@@" + e.getMessage());
        }

        return rowView;
    }
}
