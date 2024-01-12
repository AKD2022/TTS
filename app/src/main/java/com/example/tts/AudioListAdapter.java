package com.example.tts;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AudioListAdapter extends ArrayAdapter<String> {
    private int expandedPosition = -1;
    private Handler mHandler = new Handler();

    private MediaPlayer mediaPlayer;
    private LayoutInflater inflater;
    private List<String> audioFileNames;

    public AudioListAdapter(Context context, List<String> audioFileNames) {
        super(context, R.layout.list_item_layout, audioFileNames);
        this.audioFileNames = audioFileNames;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_layout, null);
            holder = new ViewHolder();
            holder.textItem = convertView.findViewById(R.id.textItem);
            holder.progressBar = convertView.findViewById(R.id.progressBarListItem);
            holder.textElapsed = convertView.findViewById(R.id.textElapsed);
            holder.textRemaining = convertView.findViewById(R.id.textRemaining);
            holder.elapsedTimeLayout = convertView.findViewById(R.id.elapsedTimeLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textItem.setText(audioFileNames.get(position));

        // Handle progress bar visibility
        if (position == expandedPosition) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.elapsedTimeLayout.setVisibility(View.VISIBLE); // Show elapsed time layout
            updateProgressAndTime(holder);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.elapsedTimeLayout.setVisibility(View.GONE); // Hide elapsed time layout
        }

        holder.textItem.setOnClickListener(view -> {
            if (position == expandedPosition) {
                expandedPosition = -1; // Collapse if already expanded
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            } else {
                expandedPosition = position;
                playAudio(holder, "YOUR_AUDIO_FILE_PATH"); // Replace with your audio file path
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void setExpandedPosition(int position) {
        expandedPosition = position;
        notifyDataSetChanged();
    }

    private void playAudio(ViewHolder holder, String audioFilePath) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            updateProgressAndTime(holder);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        updateProgressAndTime(holder);
                        mHandler.postDelayed(this, 1000); // Update every second
                    }
                }
            }, 1000); // Start updating progress after 1 second
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProgressAndTime(ViewHolder holder) {
        if (mediaPlayer != null && holder.progressBar.getVisibility() == View.VISIBLE) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int totalDuration = mediaPlayer.getDuration();

            holder.progressBar.setProgress((int) (((double) currentPosition / totalDuration) * 100));

            long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);
            long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration);

            holder.textElapsed.setText(String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60));
            holder.textRemaining.setText(String.format("/ %02d:%02d", totalSeconds / 60, totalSeconds % 60));
        }
    }

    private static class ViewHolder {
        TextView textItem;
        ProgressBar progressBar;
        TextView textElapsed;
        TextView textRemaining;
        View elapsedTimeLayout;
    }
}
