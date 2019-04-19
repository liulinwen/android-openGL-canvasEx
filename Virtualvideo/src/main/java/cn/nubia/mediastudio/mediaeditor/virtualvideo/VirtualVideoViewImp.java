package cn.nubia.mediastudio.mediaeditor.virtualvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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

    private PositionController mPositionController;

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

    ICanvasGL.BitmapMatrix matrix = new ICanvasGL.BitmapMatrix();

    @Override
    public void rotate(float degree, float px, float py) {
        mDegree = degree;
        mRotateCenterX = px;
        mRotateCenterY = py;
        matrix.rotateX(degree);
        matrix.rotateY(degree);
        this.requestRender();
    }

    @Override
    public void setVideoSize(int width, int height){
        mVideoWidth = width;
        mVideoHeight = height;
        mPositionController.setImageSize(width, height);
    }

    @Override
    protected void onGLDraw(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture, @Nullable SurfaceTexture sharedSurfaceTexture, @Nullable BasicTexture sharedTexture) {
        if(mPositionController == null){
            mPositionController = new PositionController(getWidth(), getHeight());
        }
        render(canvas, producedSurfaceTexture, producedRawTexture);
    }

    private void render(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture) {
        canvas.save();
        canvas.rotate(mDegree, mRotateCenterX, mRotateCenterY);
        //canvas.scale(scaledRatio, scaledRatio);
        //canvas.translate(lastXMove, lastYMove);
        producedRawTexture.setIsFlippedVertically(true);

        //Rect tect = computerTargetRect(mVideoWidth, mVideoHeight);
        RectF tect = mPositionController.getPostion();
        //Log.i("llw", "VirtualVideoView render rect is:"+tect);
        canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture,
                (int)tect.left, (int)tect.top, (int)tect.right, (int)tect.bottom, textureFilter);
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

    /**
     * 记录当前操作的状态，可选值为STATUS_INIT、STATUS_ZOOM_OUT、STATUS_ZOOM_IN和STATUS_MOVE
     */
    private int currentStatus;
    /**
     * 初始化状态常量
     */
    public static final int STATUS_INIT = 1;
    /**
     * 图片放大状态常量
     */
    public static final int STATUS_ZOOM_OUT = 2;
    /**
     * 图片缩小状态常量
     */
    public static final int STATUS_ZOOM_IN = 3;
    /**
     * 图片拖动状态常量
     */
    public static final int STATUS_MOVE = 4;
    /**
     * 记录上次手指移动时的横坐标
     */
    private float lastXMove = -1;
    /**
     * 记录上次手指移动时的纵坐标
     */
    private float lastYMove = -1;
    /**
     * 记录上次两指之间的距离
     */
    private double lastFingerDis;
    /**
     * 记录手指移动的距离所造成的缩放比例
     */
    private float scaledRatio = 1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    // 当有两个手指按在屏幕上时，计算两指之间的距离
                    lastFingerDis = distanceBetweenFingers(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    // 只有单指按在屏幕上移动时，为拖动状态
                    float xMove = event.getX();
                    float yMove = event.getY();
                    if (lastXMove == -1 && lastYMove == -1) {
                        lastXMove = xMove;
                        lastYMove = yMove;
                    }
                    currentStatus = STATUS_MOVE;
                    // ------------拖动数值----------------
                    //after(xMove,yMove),befor(lastXMove,lastYMove)
                    //lastXMove -= xMove;
                    //lastYMove -= yMove;
                    mPositionController.move(xMove - lastXMove, yMove -lastYMove);
                    lastXMove = xMove;
                    lastYMove = yMove;

                } else if (event.getPointerCount() == 2) {
                    // 有两个手指按在屏幕上移动时，为缩放状态
                    double fingerDis = distanceBetweenFingers(event);
                    if (fingerDis > lastFingerDis) {
                        currentStatus = STATUS_ZOOM_OUT;
                    } else {
                        currentStatus = STATUS_ZOOM_IN;
                    }
                    // ------------缩放倍数----------------
                    scaledRatio =  (float) (fingerDis / lastFingerDis);
                    mPositionController.zoomIn(event.getX(), event.getY(), scaledRatio);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                     //手指离开屏幕时将临时值还原
                    lastXMove = -1;
                    lastYMove = -1;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 手指离开屏幕时将临时值还原
                lastXMove = -1;
                lastYMove = -1;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 计算两个手指之间的距离。
     *
     * @param event
     * @return 两个手指之间的距离
     */
    private double distanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }
}
