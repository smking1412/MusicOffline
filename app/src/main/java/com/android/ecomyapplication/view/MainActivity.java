package com.android.ecomyapplication.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.ecomyapplication.R;
import com.android.ecomyapplication.model.Song;
import com.android.ecomyapplication.musicplayer.SongListLoader;
import com.android.ecomyapplication.service.MusicService;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private SongListAdapter adapter;
    private Cursor listCursor;
    private ListView list;

    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (ListView) findViewById(R.id.listView);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pick a Song");
        getSupportActionBar().setLogo(R.drawable.music_widget_icon);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            new loadSongTask().execute();
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Song song = ((SongListAdapter) parent.getAdapter()).getSong(position);
                    Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
                    serviceIntent.setAction(WidgetProvider.ACTION_PLAY_PAUSE);
                    serviceIntent.putExtra("song", song);
                    ContextCompat.startForegroundService(MainActivity.this, serviceIntent);

                }
            });
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0) {
                    boolean isReadPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean isWritePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (isReadPermission && isWritePermission) {
                        new loadSongTask().execute();
                    } else {
                        Toast.makeText(this, "Please Grant Permissions", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private class loadSongTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            listCursor = SongListLoader.getInstance(MainActivity.this).getCursor();
            String result = "";
            if (listCursor != null && listCursor.getCount() > 0) {
                result = "success";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equalsIgnoreCase("success")) {
                progressBar.setVisibility(View.GONE);
                adapter = new SongListAdapter(MainActivity.this, listCursor);
                list.setAdapter(adapter);
            }
        }
    }
}
