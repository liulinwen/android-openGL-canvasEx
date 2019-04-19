package cn.nubia.mediastudio.mediaeditor.virtualvideo;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

class PositionController {
    private int mViewWidth;
    private int mViewHeight;

    private int mImageWidth = -1;
    private int mImageHeight = -1;

    private float mCurrentScale, mMinScale, mMaxScale;
    private int mCenterX;
    private int mCenterY;
    private Matrix mMatrix;
    private RectF mRect;


    public PositionController(int viewWidth, int viewHeight){
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        mMatrix = new Matrix();
        mRect = new RectF(0, 0, viewWidth, viewHeight);
    }

    public void setImageSize(int w, int h){
        if(mImageWidth != w && mImageHeight != h){
            mImageWidth = w;
            mImageHeight = h;
            init(mImageWidth, mImageHeight);
        }
    }

    public RectF getPostion(){
        mRect.left = (int)(mViewWidth - mImageWidth*mCurrentScale)/2;
        mRect.top = (int)(mViewHeight - mImageHeight*mCurrentScale)/2;
        mRect.right = mViewWidth - mRect.left;
        mRect.bottom = mViewHeight - mRect.top;
        mRect.offset(mCenterX, mCenterY);
        //mMatrix.mapRect(mRect);
        return mRect;
    }

    public void zoomIn(float tapX, float tapY, float targerScale){
        Log.i("llw", "zoomIn targerScale is: "+ targerScale);
        //mMatrix.reset();
        mCurrentScale *= targerScale;
        mCurrentScale = clamp(mCurrentScale, mMinScale, mMaxScale);
        //mMatrix.setScale(mCurrentScale, mCurrentScale, mViewWidth/2, mViewHeight/2);
        Log.i("llw", "zoomIn mCurrentScale is: "+ mCurrentScale+ ", tapx is: "+ tapX+ ", tapY is: "+ tapY);
    }

    public void move(float x, float y){
        mCenterX += x;
        mCenterY += y;
        //mMatrix.setTranslate(x, y);
        Log.i("llw", "move x is: "+ x+ ", y is: "+ y);
    }

    private void init(int w, int h){
        mCurrentScale = (w > h)
                ? (float)mViewWidth / w
                : (float)mViewHeight / h;
        mMinScale = mCurrentScale;
        mMaxScale = mCurrentScale * 4;
        mCenterX = 0;
        mCenterY = 0;

        mRect.left = (int)(mViewWidth - mImageWidth*mCurrentScale)/2;
        mRect.top = (int)(mViewHeight - mImageHeight*mCurrentScale)/2;
        mRect.right = mViewWidth - mRect.left;
        mRect.bottom = mViewHeight - mRect.top;

        Log.i("llw", "init mCurrentScale is: "+ mCurrentScale);
    }

    private float clamp(float x, float min, float max){
        if(x > max){
            return max;
        }else{
            return x < min ? min : x;
        }
    }

}
