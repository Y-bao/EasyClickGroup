package com.ybao.library;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class ClickAnimGroupL2 extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    Rect clickedchildRect;

    private int mTargetWidth;
    private int mTargetHeight;
    private int mMinBetweenWidthAndHeight;

    int alpha;
    int mTouchSlop;

    private View mTouchTarget;
    Animator animator;

    private DispatchUpTouchEventRunnable mDispatchUpTouchEventRunnable = new DispatchUpTouchEventRunnable();
    int[] mLocationInScreen;

    public ClickAnimGroupL2(Context context) {
        this(context, null);
    }

    public ClickAnimGroupL2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickAnimGroupL2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mPaint.setColor(0xff000000);
        mPaint.setAlpha(10);
        mLocationInScreen = new int[2];
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

//    private void initParametersForChild(MotionEvent event, View view) {
//        mCenterX = event.getX();
//        mCenterY = event.getY();
//        mTargetWidth = view.getMeasuredWidth();
//        mTargetHeight = view.getMeasuredHeight();
//
//        alpha = 30;
//
//        mMinBetweenWidthAndHeight = Math.min(mTargetWidth, mTargetHeight);
//        mMinBetweenWidthAndHeight = (mTargetWidth + mTargetHeight) / 2;
//
//        mRevealRadius = 0;
//        mShouldDoAnimation = true;
//        mIsPressed = true;
//        isUp = false;
//        mRevealRadiusGap = mMinBetweenWidthAndHeight / 60;
//
//        mMaxRevealRadius = (int) Math.sqrt(mTargetWidth * mTargetWidth + mTargetHeight * mTargetHeight);
//    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mPaint.setAlpha((int) a);
        canvas.drawCircle(pX, pY, r, mPaint);
    }

    boolean startAnim = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        startX = event.getX();
        startY = event.getY();
        startR = 0;
        endX = getMeasuredWidth() / 2.0f;
        endY = getMeasuredHeight() / 2.0f;
        endR = (float) Math.sqrt(endX * endX + endY * endY);
        if (action == MotionEvent.ACTION_DOWN) {
            stopOldAnim();
            View touchTarget = getTouchTarget(this, (int) event.getRawX(), (int) event.getRawY());
            if (touchTarget != null && touchTarget.isClickable() && touchTarget.isEnabled()) {
                mTouchTarget = touchTarget;
                a = 20;
                ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                animator.addUpdateListener(this);
                animator.setDuration(1500);
                animator.start();
                this.animator = animator;
                startAnim = true;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            stopOldAnim();
            if (startAnim) {
                AnimatorSet animatorSet = new AnimatorSet();
                startA = 20;
                endA = 0;
                ValueAnimator animator1 = ValueAnimator.ofFloat(0, 1);
                animator1.addUpdateListener(animatorUpdateListener);
                if (r < endR) {
                    animator1.setStartDelay(200);
                    animator1.setDuration(200);
                    startX = pX;
                    startY = pY;
                    startR = r;
                    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                    animator.addUpdateListener(this);
                    animator.setDuration(300);
                    animatorSet.playTogether(animator, animator1);
                } else {
                    animator1.setDuration(400);
                    animatorSet.play(animator1);
                }
                animatorSet.start();
                this.animator = animatorSet;
                startAnim = false;
            }

        }
        return super.dispatchTouchEvent(event);
    }

    private void stopOldAnim() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
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

    float startX = 0;
    float startY = 0;
    float startR = 0;
    float endX = 0;
    float endY = 0;
    float endR = 0;

    float pX = 0;
    float pY = 0;
    float r = 0;


    float startA = 0;
    float endA = 0;

    float a = 0;

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float f = animation.getAnimatedFraction();
        pX = f * (endX - startX) + startX;
        pY = f * (endY - startY) + startY;
        r = f * (endR - startR) + startR;
        postInvalidate(clickedchildRect.left, clickedchildRect.top, clickedchildRect.right, clickedchildRect.bottom);
//        postInvalidate();
    }

    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float f = animation.getAnimatedFraction();
            a = f * (endA - startA) + startA;
            postInvalidate(clickedchildRect.left, clickedchildRect.top, clickedchildRect.right, clickedchildRect.bottom);
//            postInvalidate();
        }
    };
}
