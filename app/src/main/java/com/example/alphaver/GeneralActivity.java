package com.example.alphaver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GeneralActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
    }

    public void cameraTest(View view) {
        Intent si = new Intent(GeneralActivity.this,CameraActivity.class);
        startActivity(si);
    }

    public void galleryTest(View view) {
        Intent si = new Intent(GeneralActivity.this,GalleryActivity.class);
        startActivity(si);
    }

    public void readTest(View view) {
        Intent si = new Intent(GeneralActivity.this,TextToSpeechActivity.class);
        startActivity(si);
    }
}