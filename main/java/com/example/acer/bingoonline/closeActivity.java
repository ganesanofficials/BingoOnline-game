package com.example.acer.bingoonline;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class closeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close);
        finish();
        System.exit(0);
    }
}
