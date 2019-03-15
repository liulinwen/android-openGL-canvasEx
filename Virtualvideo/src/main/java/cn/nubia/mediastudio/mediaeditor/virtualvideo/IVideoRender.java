package cn.nubia.mediastudio.mediaeditor.virtualvideo;

import android.graphics.Bitmap;
import android.view.Surface;

import com.chillingvan.canvasgl.textureFilter.TextureFilter;

public interface IVideoRender {
    //Video
    public void setVideoSize(int width, int height);
    public Surface getProduceSurface();

    //Bitmap
    public void drawBitmap(Bitmap bitmap, int left, int top);


    //gloab method
    public void rotate(float degree, float px, float py);
    public void setTextureFilter(TextureFilter textureFilter);

    public void onResume();
    public void onPause();

    public int getWidth();
    public int getHeight();
}
