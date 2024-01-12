package com.example.tts;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioFragment extends Fragment {

    private ListView listView;
    private MediaPlayer mediaPlayer;
    private AudioListAdapter adapter;

    public AudioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_audio, container, false);

        listView = v.findViewById(R.id.listView);

        List<String> audioFileNames = getAudioFileNames();
        adapter = new AudioListAdapter(requireContext(), audioFileNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setExpandedPosition(position);
                String selectedAudioFileName = audioFileNames.get(position);
                playAudio(selectedAudioFileName);
            }
        });


        return v;
    }


    private void playAudio(String audioFileName) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();

        try {
            File audioFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Text To Speech Audio/" + audioFileName);
            if (audioFile.exists()) {
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } else {
                Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error playing audio", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private List<String> getAudioFileNames() {
        List<String> audioFileNames = new ArrayList<>();

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Text To Speech Audio");

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".wav");
                }
            });

            if (files != null) {
                for (File file : files) {
                    audioFileNames.add(file.getName());
                }
            }
        }

        return audioFileNames;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
