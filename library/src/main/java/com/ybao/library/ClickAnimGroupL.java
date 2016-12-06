package com.ybao.library;

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
    private int mMinBetweenWidthAndHeight;
    private int mMaxRevealRadius;
    private int mRevealRadiusGap;
    private int mRevealRadius = 0;
    private float mCenterX;
    private float mCenterY;
    int downX;
    private boolean isUp = false;

    private boolean mShouldDoAnimation = false;
    private boolean mIsPressed = false;

    int alpha;
    int mTouchSlop;

    private View mTouchTarget;

    private DispatchUpTouchEventRunnable mDispatchUpTouchEventRunnable = new DispatchUpTouchEventRunnable();
    int[] mLocationInScreen;

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
        mLocationInScreen = new int[2];
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private void initParametersForChild(MotionEvent event, View view) {
        mCenterX = event.getX();
        mCenterY = event.getY();
        mTargetWidth = view.getMeasuredWidth();
        mTargetHeight = view.getMeasuredHeight();

        alpha = 30;

        mMinBetweenWidthAndHeight = Math.min(mTargetWidth, mTargetHeight);
        mMinBetweenWidthAndHeight = (mTargetWidth + mTargetHeight) / 2;

        mRevealRadius = 0;
        mShouldDoAnimation = true;
        mIsPressed = true;
        isUp = false;
        mRevealRadiusGap = mMinBetweenWidthAndHeight / 60;

        mMaxRevealRadius = (int) Math.sqrt(mTargetWidth * mTargetWidth + mTargetHeight * mTargetHeight);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!mShouldDoAnimation || mTargetWidth <= 0 || mTouchTarget == null) {
            return;
        }
        int[] location = new int[2];
        this.getLocationOnScreen(mLocationInScreen);
        mTouchTarget.getLocationOnScreen(location);
        clickedchildRect = new Rect();
        clickedchildRect.left = location[0] - mLocationInScreen[0];
        clickedchildRect.top = location[1] - mLocationInScreen[1];
        clickedchildRect.right = clickedchildRect.left + mTargetWidth;
        clickedchildRect.bottom = clickedchildRect.top + mTargetHeight;
        if (mRevealRadius > mMaxRevealRadius) {
            if (alpha >= 5) {
                alpha -= 5;
            }
        } else {
            mRevealRadius += mRevealRadiusGap;
        }

        mPaint.setAlpha(alpha);
        canvas.save();
        canvas.clipRect(clickedchildRect);
        canvas.drawCircle(mCenterX, mCenterY, mRevealRadius, mPaint);
        canvas.restore();

        if (mRevealRadius <= mMaxRevealRadius) {
            postInvalidate(clickedchildRect.left, clickedchildRect.top, clickedchildRect.right, clickedchildRect.bottom);
        } else if (!mIsPressed) {
            if (alpha < 5) {
                mShouldDoAnimation = false;
                if (isUp) {
                    mDispatchUpTouchEventRunnable.run();
                }
                alpha = 0;
                //				postDelayed(mDispatchUpTouchEventRunnable, 10);
            }
            postInvalidate(clickedchildRect.left, clickedchildRect.top, clickedchildRect.right, clickedchildRect.bottom);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downX = (int) event.getX();
            View touchTarget = getTouchTarget(this, x, y);
            if (touchTarget != null && touchTarget.isClickable() && touchTarget.isEnabled()) {
                mTouchTarget = touchTarget;
                initParametersForChild(event, touchTarget);
                invalidate();
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            int moveX = (int) event.getX();
            if (Math.abs(moveX - downX) > mTouchSlop) {
                mIsPressed = false;
                invalidate();
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (mIsPressed) {
                mIsPressed = false;
                invalidate();
                mDispatchUpTouchEventRunnable.event = event;
                isUp = true;
                return true;
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            if (mIsPressed) {
                mIsPressed = false;
                invalidate();
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
            if (mTouchTarget == null || !mTouchTarget.isEnabled()) {
                return;
            }

            if (event != null && isTouchPointInView(mTouchTarget, (int) event.getRawX(), (int) event.getRawY())) {
                mTouchTarget.performClick();
            }
        }
    }
}
