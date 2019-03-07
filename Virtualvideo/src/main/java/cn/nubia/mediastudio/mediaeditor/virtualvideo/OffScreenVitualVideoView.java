package cn.nubia.mediastudio.mediaeditor.virtualvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.util.Log;
import android.view.Surface;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.MultiTexOffScreenCanvas;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLMultiTexProducerView;
import com.chillingvan.canvasgl.glview.texture.GLTexture;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.chillingvan.canvasgl.textureFilter.TextureFilter;

import java.util.List;

public class OffScreenVitualVideoView implements IVirtualVideoView {
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
    private TextureFilter textureFilter = new BasicTextureFilter();

    private MultiTexOffScreenCanvas multiTexOffScreenCanvas;
    private MyRecorder mRecorder = null;

    private int mViewWidth;
    private int mViewHeight;
    public OffScreenVitualVideoView(Context context, int width, int hegiht) {
        mViewWidth = width;
        mVideoHeight = hegiht;
        initTextureView();
    }

    @Override
    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    @Override
    public Surface getProduceSurface() {
        return mProducerSurface;
    }

    @Override
    public void drawBitmap(Bitmap bitmap, int left, int top) {
        mBitmap = bitmap;
        mBitmapLeft = left;
        mBitmapTop = top;
        multiTexOffScreenCanvas.requestRender();
    }

    @Override
    public void rotate(float degree, float px, float py) {
        mDegree = degree;
        mRotateCenterX = px;
        mRotateCenterY = py;
        multiTexOffScreenCanvas.requestRender();
    }

    @Override
    public void setTextureFilter(TextureFilter textureFilter) {
        this.textureFilter = textureFilter;
        multiTexOffScreenCanvas.requestRender();
    }

    private void initTextureView() {
        mRecorder = new MyRecorder();
        int width = mViewWidth;
        int height = mViewHeight;
        Surface recordSurface = mRecorder.start(mViewWidth, mViewHeight);
        multiTexOffScreenCanvas = new MultiTexOffScreenCanvas(width, height, recordSurface) {
            {
                setProducedTextureTarget(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            }

            @Override
            protected int getInitialTexCount() {
                return 1;
            }

            @Override
            protected int getRenderMode() {
                return GLThread.RENDERMODE_WHEN_DIRTY;
            }

            @Override
            protected void onGLDraw(ICanvasGL canvas, List<GLTexture> producedTextures, List<GLTexture> consumedTextures) {
                GLTexture glTexture = producedTextures.get(0);
                Log.i("llw", "multiTexOffScreenCanvas onGLDraw");
                if (!consumedTextures.isEmpty()) {
                    GLTexture consumeTexture = consumedTextures.get(0);
                    render(canvas, glTexture.getSurfaceTexture(), glTexture.getRawTexture());
                } else {
                    render(canvas, glTexture.getSurfaceTexture(), glTexture.getRawTexture());
                }
                mRecorder.swapBuffers();
            }
        };

        multiTexOffScreenCanvas.setSurfaceTextureCreatedListener(new GLMultiTexProducerView.SurfaceTextureCreatedListener() {
            @Override
            public void onCreated(List<GLTexture> producedTextureList) {
                SurfaceTexture surfaceTexture = producedTextureList.get(0).getSurfaceTexture();
                surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        Log.i("llw", "Recored SurfaceTexture onFrameAvailable");
                        multiTexOffScreenCanvas.requestRender();
                    }
                });
                mProducerSurface = new Surface(surfaceTexture);
            }
        });
        multiTexOffScreenCanvas.start();
        multiTexOffScreenCanvas.onResume();
    }

    private void render(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture) {
        canvas.save();
        canvas.rotate(mDegree, mRotateCenterX, mRotateCenterY);
        producedRawTexture.setIsFlippedVertically(true);
        Rect tect = computerTargetRect(mVideoWidth, mVideoHeight);
        Log.i("llw", "OffScreenVitualVideoView render rect is:"+tect);
        canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture, tect.left, tect.top, tect.right, tect.bottom, textureFilter);
        //canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture, 0, (int)(producedRawTexture.getHeight()*0.128f), producedRawTexture.getWidth(), (int)(producedRawTexture.getHeight()*0.744f), textureFilter);
        if(mBitmap != null && !mBitmap.isRecycled()) {
            canvas.invalidateTextureContent(mBitmap);
            canvas.drawBitmap(mBitmap, mBitmapLeft, mBitmapTop, textureFilter);
        }
        canvas.restore();
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

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {
        mRecorder.stop();
        multiTexOffScreenCanvas.onPause();
    }

    @Override
    public int getWidth() {
        return mViewWidth;
    }

    @Override
    public int getHeight() {
        return mViewHeight;
    }
}
