package org.auleck.first_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {

    private Paint mPaint;
    private int mCircleColor;
    private float mCircleRadius;
    private float mBorderWidth;
    private int mBorderColor;
    private float mCornerRadius;

    // 构造函数1：在Java代码中直接 new 时调用
    public CircleView(Context context) {
        this(context, null);
    }

    // 构造函数2：在XML布局中使用时调用
    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // 构造函数3：带有 defStyleAttr 时调用（主要用于样式）
    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 设置抗锯齿

        // 设置默认值
        mCircleColor = Color.RED;
        mCircleRadius = 50f;
        mBorderWidth = 0f;
        mBorderColor = Color.BLACK;
        mCornerRadius = 0f;

        // 如果是从XML解析来的，提取自定义属性
        if (attrs != null) {
            // 获取属性数组
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleView);

            // 读取 XML 中配置的属性值，如果没配置则使用默认值
            mCircleColor = typedArray.getColor(R.styleable.CircleView_circle_color, Color.RED);
            mCircleRadius = typedArray.getDimension(R.styleable.CircleView_circle_radius, 50f);
            mBorderWidth = typedArray.getDimension(R.styleable.CircleView_border_width, 0f);
            mBorderColor = typedArray.getColor(R.styleable.CircleView_border_color, Color.BLACK);
            mCornerRadius = typedArray.getDimension(R.styleable.CircleView_corner_radius, 0f);

            // 必须调用 recycle() 释放资源，以便 TypedArray 被缓存复用
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算圆形所需的最小尺寸（直径 + padding）
        int desiredSize = (int) (mCircleRadius * 2) + getPaddingLeft() + getPaddingRight();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // 根据测量模式选择宽度
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredSize, widthSize);
        } else {
            width = desiredSize;
        }

        // 根据测量模式选择高度
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredSize, heightSize);
        } else {
            height = desiredSize;
        }

        // 设置测量后的尺寸
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 获取组件的中心点
        int width = getWidth();
        int height = getHeight();
        float cx = width / 2f;
        float cy = height / 2f;

        // 绘制圆角矩形背景（如果设置了圆角半径）
        if (mCornerRadius > 0) {
            mPaint.setColor(mCircleColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(0, 0, width, height, mCornerRadius, mCornerRadius, mPaint);
            
            // 绘制圆角矩形边框
            if (mBorderWidth > 0) {
                mPaint.setColor(mBorderColor);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mBorderWidth);
                canvas.drawRoundRect(mBorderWidth / 2, mBorderWidth / 2, 
                    width - mBorderWidth / 2, height - mBorderWidth / 2, 
                    mCornerRadius, mCornerRadius, mPaint);
            }
        } else {
            // 绘制圆形
            mPaint.setColor(mCircleColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, mCircleRadius, mPaint);

            // 绘制圆形边框
            if (mBorderWidth > 0) {
                mPaint.setColor(mBorderColor);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mBorderWidth);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
            }
        }
    }
}
