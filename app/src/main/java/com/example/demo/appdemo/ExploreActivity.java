package com.example.demo.appdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

public class ExploreActivity extends AppCompatActivity {

    GridView gridview;

    String[] playlistNameList = {
            "Trending",
            "Party On!",
            "Summer Vibes",
            "Chill",
            "Pop",
            "Rock",
    };

    int[] playlistImages = {
            R.mipmap.trend_icon,
            R.mipmap.party_icon,
            R.mipmap.summer_icon,
            R.mipmap.chill_icon,
            R.mipmap.pop_icon,
            R.mipmap.rock_icon,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        gridview = findViewById(R.id.customgrid);
        gridview.setAdapter(new CustomAdapter(this, playlistNameList, playlistImages));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
            getMenuInflater().inflate(R.menu.menu, menu);
            return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.home:
                startActivity(new Intent(ExploreActivity.this, ExploreActivity.class));
                finish();
                break;

            case R.id.my_favorites:
                // TODO change to FavoritesActivity
                startActivity(new Intent(ExploreActivity.this, SignUpActivity.class));
                finish();
                break;

            case R.id.my_playlists:
                // TODO change to FavoritesActivity
                startActivity(new Intent(ExploreActivity.this, SignUpActivity.class));
                finish();
                break;

            case R.id.logout:
                startActivity(new Intent(ExploreActivity.this, MainActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
