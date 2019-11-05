package com.example.treasurehunt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button create_game_button;
    Button join_game_button;
    ArrayAdapter<String> adapter;
    ListView listView;
    String root;
    File myDir;
    private ArrayList<String> fileArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        create_game_button = (Button) findViewById(R.id.create_game_button);
        create_game_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        join_game_button = (Button) findViewById(R.id.join_game_button);
        join_game_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root = android.os.Environment.getExternalStorageDirectory().toString();
                myDir = new File(root + "/TreasureHunt");

                fileArray = new ArrayList<String>();
                File directory = new File(String.valueOf(myDir));
                File[] files = directory.listFiles();
                Log.d("Files", "Size: " + files.length);

                for (int i = files.length - 1; i >= 0; i--) {
                    Log.d("Files", "FileName:" + files[i].getName());
                    fileArray.add(files[i].getName());
                }
                //ArrayAdapter<String>
                adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_list, fileArray);

                final ListView listView = (ListView) findViewById(R.id.mobile_list);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {

                        String itemValue = (String) listView.getItemAtPosition(position);
                        Intent intent = new Intent(MainActivity.this, MapsActivity2.class);
                        intent.putExtra("ITEM_SELECTED", itemValue);
                        startActivity(intent);
                    }
                });
                listView.setAdapter(adapter);

            }
        });

    }
}
