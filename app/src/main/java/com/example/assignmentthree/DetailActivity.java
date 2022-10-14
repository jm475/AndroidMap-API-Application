package com.example.assignmentthree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * DetailActivity class that displays thumbnail of the webcam image and various details
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Hide the navigation bar by enabling Immersive Sticky mode
        int uiOptionsSticky = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptionsSticky);

        //Get the intent sent along with its contents
        Intent intent = getIntent();

        //Access the views by their id's
        ImageView imageView = (ImageView) findViewById(R.id.iv_thumbnail);
        TextView textViewTitle = (TextView) findViewById(R.id.tv_title);
        TextView textViewSnippet = (TextView) findViewById(R.id.tv_city);

        //set the image (using Glide) and text views with the data sent
        Glide.with(imageView).load(intent.getStringExtra("markerImage")).into(imageView);
        textViewTitle.setText(intent.getStringExtra("markerTitle"));
        textViewSnippet.setText(intent.getStringExtra("markerSnippet"));

    }
}