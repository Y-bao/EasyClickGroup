package com.ybao.easyclickgroup;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

public class ClickAnimGroupL extends FrameLayout {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    Rect clickedchildRect;

    private int mTargetWidth;
    private int mTargetHeight;
    int downX;
    int maxAlpha = 35;
    int mTouchSlop;
    private boolean mIsPressed = false;
    private View mTouchTarget;

    int alpha;


    private int none = 0;
    private int show = 1;
    private int hide = 2;
    private int animType = none;

    private int mMaxRevealRadius;
    private int mRevealRadiusGapS;
    private int mRevealRadiusGapL;
    private int mRevealRadius = 0;
    private int mCenterX;
    private int mCenterY;


    private DispatchUpTouchEventRunnable mDispatchUpTouchEventRunnable = new DispatchUpTouchEventRunnable();
    int[] mTouchTargetLocation;

    public ClickAnimGroupL(Context context) {
        this(context, null);
    }

    public ClickAnimGroupL(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickAnimGroupL(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mPaint.setColor(0xff000000);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setCenter(MotionEvent event) {
        mCenterX = (int) event.getRawX();
        mCenterY = (int) event.getRawY();
    }


    private void initParametersForChild(View view) {
        mTargetWidth = view.getMeasuredWidth();
        mTargetHeight = view.getMeasuredHeight();

        alpha = maxAlpha;

//        mMinBetweenWidthAndHeight = (mTargetWidth + mTargetHeight) / 2;

        mRevealRadius = 0;
        animType = show;
        mIsPressed = true;

        int mMinBetweenWidthAndHeight = Math.min(mTargetWidth, mTargetHeight);
        mRevealRadiusGapS = mMinBetweenWidthAndHeight / 80;
        mRevealRadiusGapL = mMinBetweenWidthAndHeight / 20;


        mTouchTargetLocation = new int[2];
        mTouchTarget.getLocationOnScreen(mTouchTargetLocation);
        mMaxRevealRadius = (int) Math.sqrt(mTargetWidth * mTargetWidth + mTargetHeight * mTargetHeight);
        mDispatchUpTouchEventRunnable.event = null;
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (animType == none || mTargetWidth <= 0 || mTouchTarget == null) {
            mIsPressed = false;
            return;
        }
        int[] location = new int[2];
        int[] mLocationInScreen = new int[2];
        this.getLocationOnScreen(mLocationInScreen);
        mTouchTarget.getLocationOnScreen(location);
        if (animType != hide && Math.abs(location[0] - mTouchTargetLocation[0]) > mTouchSlop || Math.abs(location[1] - mTouchTargetLocation[1]) > mTouchSlop) {
            mIsPressed = false;
        }
        clickedchildRect = new Rect();
        clickedchildRect.left = location[0] - mLocationInScreen[0];
        clickedchildRect.top = location[1] - mLocationInScreen[1];
        clickedchildRect.right = clickedchildRect.left + mTargetWidth;
        clickedchildRect.bottom = clickedchildRect.top + mTargetHeight;
        int cX = mCenterX - mLocationInScreen[0];
        int cY = mCenterY - mLocationInScreen[1];
        if (animType == show) {
            if (mRevealRadius <= mMaxRevealRadius) {
                if (mIsPressed) {
                    mRevealRadius += mRevealRadiusGapS;
                } else {
                    mRevealRadius += mRevealRadiusGapL;
                    alpha = (int) (maxAlpha - ((maxAlpha - 5.0f) * mRevealRadius / mMaxRevealRadius));
                    if (alpha < 0) {
                        alpha = 0;
                    }
                }
            }
        } else if (animType == hide) {
            alpha -= 1;
            if (alpha < 0) {
                alpha = 0;
            }
        } else {
            alpha = 0;
        }

        mPaint.setAlpha(alpha);
        canvas.save();
        canvas.clipRect(clickedchildRect);
        canvas.drawCircle(cX, cY, mRevealRadius, mPaint);
        canvas.restore();

        if (mRevealRadius <= mMaxRevealRadius) {
            postInvalidate(clickedchildRect.left, clickedchildRect.top, clickedchildRect.right, clickedchildRect.bottom);
        } else if (!mIsPressed) {
            if (alpha == 0 && animType == hide) {
                animType = none;
            } else {
                animType = hide;
                mDispatchUpTouchEventRunnable.run();
            }
            postInvalidate(clickedchildRect.left, clickedchildRect.top, clickedchildRect.right, clickedchildRect.bottom);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        setCenter(event);
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downX = (int) event.getX();
            View touchTarget = getTouchTarget(this, x, y);
            if (touchTarget != null && touchTarget.isClickable() && touchTarget.isEnabled()) {
                mTouchTarget = touchTarget;
                initParametersForChild(touchTarget);
                invalidate();
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
//            int moveX = (int) event.getX();
//            if (mIsPressed && Math.abs(moveX - downX) > mTouchSlop) {
//                mIsPressed = false;
//                invalidate();
//            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (mIsPressed) {
                mIsPressed = false;
                invalidate();
                mDispatchUpTouchEventRunnable.event = event;
                return true;
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            if (mIsPressed) {
                mIsPressed = false;
                invalidate();
                super.dispatchTouchEvent(event);
                return false;
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private View getTouchTarget(View view, int x, int y) {
        View target = null;
        ArrayList<View> TouchableViews = view.getTouchables();
        for (View child : TouchableViews) {
            if (isTouchPointInView(child, x, y)) {
                target = child;
                break;
            }
        }

        return target;
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (view.isClickable() && y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }
        return false;
    }

    private class DispatchUpTouchEventRunnable implements Runnable {
        public MotionEvent event;

        @Override
        public void run() {
            if (mTouchTarget == null || !mTouchTarget.isEnabled() || event == null) {
                return;
            }

            if (isTouchPointInView(mTouchTarget, (int) event.getRawX(), (int) event.getRawY())) {
                mTouchTarget.performClick();
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != VISIBLE) {
            animType = none;
            invalidate();
        }
    }
}
