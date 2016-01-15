package com.example.hotels;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FullScreenImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Intent intent = getIntent();
        String name = intent.getStringExtra("Name");

        File picture = new File(getBaseContext().getCacheDir(), name);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(picture);
        }catch (FileNotFoundException e)
        {
            Log.e("No file", e.getMessage() + " name: " + name);
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        ImageView view = (ImageView) findViewById(R.id.fullscreen_photo);
        view.setImageBitmap(bitmap);
    }
}
