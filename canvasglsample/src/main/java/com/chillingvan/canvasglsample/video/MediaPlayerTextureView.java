package com.chillingvan.canvasglsample.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLSurfaceTextureProducerView;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.chillingvan.canvasgl.textureFilter.ColorMatrixFilter;
import com.chillingvan.canvasgl.textureFilter.PixelationFilter;
import com.chillingvan.canvasgl.textureFilter.TextureFilter;
import com.chillingvan.canvasglsample.R;

/**
 * Created by Chilling on 2017/12/16.
 */

public class MediaPlayerTextureView extends GLSurfaceTextureProducerView {

    private TextureFilter textureFilter = new PixelationFilter(12);
    private Bitmap mBitmap;
    public MediaPlayerTextureView(Context context) {
        super(context);
    }

    public MediaPlayerTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaPlayerTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        float[] matrix4 = {
                1.0f, 0.0f, 0.0f, 0.3f,
                0.0f, 1.0f, 0.0f, 0.4f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };
        ColorMatrixFilter colorMatrixFilter = new ColorMatrixFilter(0.3f, matrix4);
        //PixelationFilter pixelationFilter = new PixelationFilter(36);
        textureFilter = colorMatrixFilter;
    }

    public void setTextureFilter(TextureFilter textureFilter) {
        this.textureFilter = textureFilter;
    }

    @Override
    protected void onGLDraw(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture, @Nullable SurfaceTexture sharedSurfaceTexture, @Nullable BasicTexture sharedTexture) {
        canvas.save();
        canvas.rotate(90, 500, 500);
        producedRawTexture.setIsFlippedVertically(true);
        canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture, 0, 0, producedRawTexture.getWidth(), producedRawTexture.getHeight(), textureFilter);
        if(mBitmap == null){
            mBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_robot);
        }
        if(mBitmap != null) {
            canvas.invalidateTextureContent(mBitmap);
            canvas.drawBitmap(mBitmap, 0, 0, textureFilter);
        }
        canvas.restore();
    }
}
