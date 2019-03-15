package cn.nubia.mediastudio.mediaeditor.virtualvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.MultiTexOffScreenCanvas;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLMultiTexProducerView;
import com.chillingvan.canvasgl.glview.texture.GLSurfaceTextureProducerView;
import com.chillingvan.canvasgl.glview.texture.GLTexture;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.chillingvan.canvasgl.textureFilter.PixelationFilter;
import com.chillingvan.canvasgl.textureFilter.TextureFilter;

import java.io.IOException;
import java.util.List;

/**
 * Created by Chilling on 2017/12/16.
 */

public class VirtualVideoViewImp extends GLSurfaceTextureProducerView implements IVideoRender {

    private TextureFilter textureFilter = new BasicTextureFilter();

    //Video
    protected Surface mProducerSurface;
    private int mVideoWidth;
    private int mVideoHeight;
    // rotate
    private float mDegree;
    private float mRotateCenterX;
    private float mRotateCenterY;

    //draw bitmap
    private Bitmap mBitmap;
    private int mBitmapLeft;
    private int mBitmapTop;
    private MultiTexOffScreenCanvas multiTexOffScreenCanvas;
    private MyRecorder mRecorder = null;

    public VirtualVideoViewImp(Context context) {
        super(context);
    }
    public VirtualVideoViewImp(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public VirtualVideoViewImp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        initTextureView();
    }

    public void setTextureFilter(TextureFilter textureFilter) {
        this.textureFilter = textureFilter;
        this.requestRender();
    }

    @Override
    public Surface getProduceSurface(){
        return mProducerSurface;
    }

    @Override
    public void drawBitmap(Bitmap bitmap, int left, int top){
        mBitmap = bitmap;
        mBitmapLeft = left;
        mBitmapTop = top;
        this.requestRender();
    }

    @Override
    public void rotate(float degree, float px, float py) {
        mDegree = degree;
        mRotateCenterX = px;
        mRotateCenterY = py;
        this.requestRender();
    }

    @Override
    public void setVideoSize(int width, int height){
        mVideoWidth = width;
        mVideoHeight = height;
    }

    @Override
    protected void onGLDraw(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture, @Nullable SurfaceTexture sharedSurfaceTexture, @Nullable BasicTexture sharedTexture) {
        render(canvas, producedSurfaceTexture, producedRawTexture);
        Log.i("llw", "VirtualVideoView onGLDraw");
    }

    private void render(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture) {
        canvas.save();
        canvas.rotate(mDegree, mRotateCenterX, mRotateCenterY);
        producedRawTexture.setIsFlippedVertically(true);
        Rect tect = computerTargetRect(mVideoWidth, mVideoHeight);
        Log.i("llw", "VirtualVideoView render rect is:"+tect);
        canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture, tect.left, tect.top, tect.right, tect.bottom, textureFilter);
        //canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture, 0, (int)(producedRawTexture.getHeight()*0.128f), producedRawTexture.getWidth(), (int)(producedRawTexture.getHeight()*0.744f), textureFilter);
        if(mBitmap != null && !mBitmap.isRecycled()) {
            canvas.invalidateTextureContent(mBitmap);
            canvas.drawBitmap(mBitmap, mBitmapLeft, mBitmapTop, textureFilter);
        }
        canvas.restore();
    }

    private void initTextureView() {
        final GLSurfaceTextureProducerView view = this;
        view.setOnSurfaceTextureSet(new GLSurfaceTextureProducerView.OnSurfaceTextureSet() {
            @Override
            public void onSet(SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture) {
                surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        view.requestRenderAndWait();
                    }
                });
                mProducerSurface = new Surface(surfaceTexture);
            }
        });
    }

    private Rect computerTargetRect(int width, int height){
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Rect rect = new Rect(0,0,viewWidth, viewHeight);
        float imgAspectRatio = (float)width / (float)height;
        float viewAspectRatio = (float)viewWidth / (float)viewHeight;
        float xScale = 1.0f;
        float yScale = 1.0f;
        if (imgAspectRatio > viewAspectRatio) {
            yScale = viewAspectRatio / imgAspectRatio;
        } else {
            xScale = imgAspectRatio / viewAspectRatio;
        }
        int offx = (int)(viewWidth * (1 - xScale))/2;
        int offy = (int)(viewHeight * (1 - yScale))/2;
        rect.left = offx;
        rect.top = offy;
        rect.right = viewWidth - offx;
        rect.bottom = viewHeight - offy;
        return rect;
    }

}
