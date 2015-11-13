package com.example.android.team4p2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NotesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Receive the Intent
        Intent intent = getIntent();
        String user_input = intent.getStringExtra(MainActivity.USER_INPUT);

        // Code here...
    }
}
