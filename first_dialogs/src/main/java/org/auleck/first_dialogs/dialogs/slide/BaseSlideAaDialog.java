package org.auleck.first_dialogs.dialogs.slide;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

public abstract class BaseSlideAaDialog extends Dialog {

    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_TOP = 3;
    public static final int DIRECTION_BOTTOM = 4;

    private FrameLayout mRootLayout;
    private View mContentView;

    private int mDirection;
    private int mWidth;
    private int mHeight;
    private float mWidthPercent;
    private float mHeightPercent;
    private Drawable mBackground;
    private boolean mCancelableOutside;
    private int mAnimDuration;

    private boolean mIsAnimating = false;

    protected BaseSlideAaDialog(@NonNull Context context, Builder builder) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mDirection = builder.direction;
        this.mWidth = builder.mWidth;
        this.mHeight = builder.mHeight;
        this.mWidthPercent = builder.mWidthPercent;
        this.mHeightPercent = builder.mHeightPercent;
        this.mBackground = builder.background;
        this.mCancelableOutside = builder.cancelableOutside;
        this.mAnimDuration = builder.animDuration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0f);
            window.getDecorView().setPadding(0, 0, 0, 0);

            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setStatusBarColor(Color.TRANSPARENT);

            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }

        mRootLayout = new FrameLayout(getContext());
        mRootLayout.setBackgroundColor(Color.parseColor("#80000000"));
        mRootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mContentView = getContentView();
        if (mContentView == null) {
            throw new NullPointerException("getContentView() cannot return null");
        }

        // 计算最终宽高
        int finalWidth, finalHeight;
        if (mWidth != Integer.MIN_VALUE) {
            finalWidth = mWidth;
        } else {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int screenWidth;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                screenWidth = wm.getCurrentWindowMetrics().getBounds().width();
            } else {
                Point size = new Point();
                wm.getDefaultDisplay().getSize(size);
                screenWidth = size.x;
            }
            finalWidth = (int) (screenWidth * mWidthPercent);
        }

        if (mHeight != Integer.MIN_VALUE) {
            finalHeight = mHeight;
        } else {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int screenHeight;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                screenHeight = wm.getCurrentWindowMetrics().getBounds().height();
            } else {
                Point size = new Point();
                wm.getDefaultDisplay().getSize(size);
                screenHeight = size.y;
            }
            finalHeight = (int) (screenHeight * mHeightPercent);
        }

        FrameLayout.LayoutParams contentLp = new FrameLayout.LayoutParams(finalWidth, finalHeight);
        applyGravity(contentLp);
        if (mBackground != null) {
            ViewCompat.setBackground(mContentView, mBackground);
        }
        mRootLayout.addView(mContentView, contentLp);

        setContentView(mRootLayout);

        mRootLayout.setAlpha(0f);
        mContentView.setAlpha(0f);

        mRootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mCancelableOutside && !mIsAnimating) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (!isPointInsideView(event.getRawX(), event.getRawY(), mContentView)) {
                            dismiss();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        setCancelable(mCancelableOutside);
    }

    private void applyGravity(FrameLayout.LayoutParams lp) {
        switch (mDirection) {
            case DIRECTION_LEFT:
                lp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                break;
            case DIRECTION_RIGHT:
                lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                break;
            case DIRECTION_TOP:
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
                break;
            case DIRECTION_BOTTOM:
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                break;
        }
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getWidth();
        int bottom = top + view.getHeight();
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    @Override
    public void show() {
        super.show();
        mContentView.post(this::startEnterAnimation);
    }

    @Override
    public void dismiss() {
        if (mIsAnimating) return;
        startExitAnimation();
    }

    private void startEnterAnimation() {
        if (mContentView == null) return;
        mIsAnimating = true;

        mContentView.setAlpha(1f);
        mRootLayout.animate().alpha(1f).setDuration(mAnimDuration).start();

        float startX = 0, startY = 0;
        int width = mContentView.getWidth();
        int height = mContentView.getHeight();

        switch (mDirection) {
            case DIRECTION_LEFT: startX = -width; break;
            case DIRECTION_RIGHT: startX = width; break;
            case DIRECTION_TOP: startY = -height; break;
            case DIRECTION_BOTTOM: startY = height; break;
        }

        mContentView.setTranslationX(startX);
        mContentView.setTranslationY(startY);

        mContentView.animate()
                .translationX(0)
                .translationY(0)
                .setDuration(mAnimDuration)
                .withEndAction(() -> mIsAnimating = false)
                .start();
    }

    private void startExitAnimation() {
        if (mContentView == null) return;
        mIsAnimating = true;

        mRootLayout.animate().alpha(0f).setDuration(mAnimDuration).start();

        float endX = 0, endY = 0;
        int width = mContentView.getWidth();
        int height = mContentView.getHeight();

        switch (mDirection) {
            case DIRECTION_LEFT: endX = -width; break;
            case DIRECTION_RIGHT: endX = width; break;
            case DIRECTION_TOP: endY = -height; break;
            case DIRECTION_BOTTOM: endY = height; break;
        }

        mContentView.animate()
                .translationX(endX)
                .translationY(endY)
                .setDuration(mAnimDuration)
                .withEndAction(() -> {
                    mIsAnimating = false;
                    BaseSlideAaDialog.super.dismiss();
                })
                .start();
    }

    protected abstract View getContentView();

    public static class Builder {
        private int direction = DIRECTION_LEFT;
        private int mWidth = Integer.MIN_VALUE;
        private int mHeight = Integer.MIN_VALUE;
        private float mWidthPercent = 1.0f;
        private float mHeightPercent = 1.0f;
        private Drawable background = null;
        private boolean cancelableOutside = true;
        private int animDuration = 300;

        public Builder() {}

        public Builder setDirection(int direction) {
            this.direction = direction;
            return this;
        }

        /**
         * 设置宽度（像素值或布局常量，如 {@link ViewGroup.LayoutParams#MATCH_PARENT}）
         * 调用此方法后，宽度百分比设置将被忽略。
         */
        public Builder setWidth(int width) {
            this.mWidth = width;
            return this;
        }

        /**
         * 设置宽度占屏幕百分比（0~1）
         * 调用此方法后，任何之前设置的像素宽度将被清除，使用百分比。
         */
        public Builder setWidth(float percent) {
            this.mWidthPercent = Math.max(0f, Math.min(1f, percent));
            this.mWidth = Integer.MIN_VALUE; // 清除像素设置
            return this;
        }

        /**
         * 设置高度（像素值或布局常量，如 {@link ViewGroup.LayoutParams#MATCH_PARENT}）
         * 调用此方法后，高度百分比设置将被忽略。
         */
        public Builder setHeight(int height) {
            this.mHeight = height;
            return this;
        }

        /**
         * 设置高度占屏幕百分比（0~1）
         * 调用此方法后，任何之前设置的像素高度将被清除，使用百分比。
         */
        public Builder setHeight(float percent) {
            this.mHeightPercent = Math.max(0f, Math.min(1f, percent));
            this.mHeight = Integer.MIN_VALUE; // 清除像素设置
            return this;
        }

        // 保留百分比方法（别名，向后兼容）
        public Builder setWidthPercent(float percent) {
            return setWidth(percent);
        }
        public Builder setHeightPercent(float percent) {
            return setHeight(percent);
        }

        public Builder setBackground(Drawable background) {
            this.background = background;
            return this;
        }

        public Builder setCancelableOutside(boolean cancelable) {
            this.cancelableOutside = cancelable;
            return this;
        }

        public Builder setAnimDuration(int duration) {
            if (duration > 0) this.animDuration = duration;
            return this;
        }
    }
}