package com.chillingvan.canvasglsample.virtualvideo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.chillingvan.canvasglsample.R;
import com.chillingvan.canvasglsample.video.MediaPlayerHelper;

import cn.nubia.mediastudio.mediaeditor.virtualvideo.BasicFitTextureFilter;
import cn.nubia.mediastudio.mediaeditor.virtualvideo.IVirtualVideoView;
import cn.nubia.mediastudio.mediaeditor.virtualvideo.OffScreenVitualVideoView;

public class VirtualVideoActivity extends AppCompatActivity {

    private MediaPlayerHelper mediaPlayer = new MediaPlayerHelper();
    private Surface mediaSurface = null;
    private IVirtualVideoView mediaPlayerTextureView;
    private IVirtualVideoView mOff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player1);
        mediaPlayerTextureView = findViewById(R.id.media_player_texture_view);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayerTextureView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayerTextureView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.release();
        }
    }

    public void onClickStart(View view) {
        if ((mediaPlayer.isPlaying() || mediaPlayer.isLooping())) {
            mediaPlayer.start();
            return;
        }
        if(mediaSurface == null){
            mediaSurface = mediaPlayerTextureView.getProduceSurface();
        }
        BasicFitTextureFilter f = new BasicFitTextureFilter();
        mediaPlayerTextureView.setTextureFilter(f);
        playMedia(mediaSurface);
        ((TextView)view).setText( ((TextView)view).getText()+ mediaPlayer.getWH());

    }

    public void ondrawBitmap(View view) {
        if ((mediaPlayer.isPlaying() || mediaPlayer.isLooping())) {
            mediaPlayer.pause();
        }
        Bitmap b = BitmapFactory.decodeResource(this.getResources(), R.drawable.lenna);
        mediaPlayerTextureView.drawBitmap(b,500, 500);

    }

    private int mDegree = 0;
    public void rotate(View view) {
        float px = mediaPlayerTextureView.getWidth()/2;
        float py = mediaPlayerTextureView.getHeight()/2;
        mDegree += 90;
        if(mDegree > 360){
            mDegree = 0;
        }
        mediaPlayerTextureView.rotate(mDegree, px, py);
    }

    public void startRecord(View view){
        mOff = new OffScreenVitualVideoView(this, mediaPlayerTextureView.getWidth(), mediaPlayerTextureView.getHeight());
        Surface surface = mOff.getProduceSurface();
        playMedia(surface);
    }

    public void stopRecord(View view){
        mOff.onPause();
        mediaPlayer.stop();
    }

    private void playMedia(Surface surface) {
        mediaPlayer.playMedia(this, surface);
        mediaPlayerTextureView.setVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
    }

    public void viewHeight(View view){

        ((TextView)view).setText( mediaPlayerTextureView.getWidth() + "*" + mediaPlayerTextureView.getHeight());

    }
}
