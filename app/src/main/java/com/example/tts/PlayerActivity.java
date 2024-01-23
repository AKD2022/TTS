package com.example.tts;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class PlayerActivity extends AppCompatActivity {
    Button btnPlay, btnNext, btnPrev, btnff, btnfb;
    TextView txtsname, tctsstart, tctsstop;
    SeekBar seekmusic;

    String aname;
    public static final String EXTRA_NAME = "audio_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> myAudios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnPrev = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnPlay = findViewById(R.id.playBtn);
        btnff = findViewById(R.id.btnFastForward);
        btnfb = findViewById(R.id.btnFastBackwards);
        txtsname = findViewById(R.id.txtAudioName);

    }
}