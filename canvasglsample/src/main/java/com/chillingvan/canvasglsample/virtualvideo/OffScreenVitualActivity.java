package com.chillingvan.canvasglsample.virtualvideo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Surface;
import android.view.View;

import com.chillingvan.canvasglsample.R;
import com.chillingvan.canvasglsample.video.MediaPlayerHelper;

import cn.nubia.mediastudio.mediaeditor.virtualvideo.IVideoRender;
import cn.nubia.mediastudio.mediaeditor.virtualvideo.OffScreenVitualVideoView;

public class OffScreenVitualActivity extends AppCompatActivity {
    private MediaPlayerHelper mediaPlayer = new MediaPlayerHelper();
    private IVideoRender mOff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_screen_vitual);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecord();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void startRecord(){
        Surface surface = mOff.getProduceSurface();
        playMedia(surface);
    }

    private void playMedia(Surface surface) {
        mediaPlayer.playMedia(this, surface);
        mOff.setVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
    }


    @Override
    protected void onResume(){
        super.onResume();
        mOff = new OffScreenVitualVideoView(this, 1920, 1088);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOff.onPause();
        mediaPlayer.stop();
    }
}
