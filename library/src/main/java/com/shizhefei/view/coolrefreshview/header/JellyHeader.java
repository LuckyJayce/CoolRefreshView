package com.shizhefei.view.coolrefreshview.header;
/*
 * Copyright (C) 2016 LuckyJayce
 * Copyright (C) 2015 imallan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.PullHeader;

/**
 * Created by LuckyJayce on 2016/11/28.
 * 这个header 来源于https://github.com/imallan/JellyRefreshLayout
 * 我做了部分改动
 */

public class JellyHeader extends ViewGroup implements PullHeader {

    private final Paint mPaint;
    private final Path mPath;
    private ViewOutlineProvider mViewOutlineProvider;
    private int currentDistance;
    private int status;
    private float mPointX;
    private int mColor = Color.GRAY;

    private int defaultMinHeight;

    public JellyHeader(Context context) {
        this(context, null);
    }

    public JellyHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private Animation showLoadingAnimation;
    private Animation hideLoadingAnimation;

    public JellyHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        defaultMinHeight = Utils.dipToPix(context, 208);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();

        showLoadingAnimation = new AlphaAnimation(0, 1);
        showLoadingAnimation.setDuration(300);
        showLoadingAnimation.setInterpolator(new AccelerateInterpolator());

        hideLoadingAnimation = new AlphaAnimation(1, 0);
        hideLoadingAnimation.setDuration(300);
        hideLoadingAnimation.setInterpolator(new DecelerateInterpolator());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mViewOutlineProvider = new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    if (mPath.isConvex()) outline.setConvexPath(mPath);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        outline.offset(0, totalDistance() - currentDistance);
                    }
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setElevation(Utils.dipToPix(context, 4));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedMiniHeight = getSuggestedMinimumHeight();
        suggestedMiniHeight = Math.max(defaultMinHeight, suggestedMiniHeight);
        int childState = 0;
        int height = ViewCompat.resolveSizeAndState(suggestedMiniHeight, heightMeasureSpec,
                childState << MEASURED_HEIGHT_STATE_SHIFT);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);

        if (loadingView != null) {
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    offsetToKeepHeaderWhileLoading(), MeasureSpec.EXACTLY);
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight(), LayoutParams.MATCH_PARENT);
            loadingView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop() + totalDistance() - offsetToKeepHeaderWhileLoading();
        int measureHeight = loadingView.getMeasuredHeight();
        int measuredWidth = loadingView.getMeasuredWidth();
        loadingView.layout(childLeft, childTop, measuredWidth + childLeft, childTop + measureHeight);
    }

    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
        this.loadingView.setVisibility(View.GONE);
    }

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
        this.currentDistance = currentDistance;
        this.status = status;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void onRefreshing(CoolRefreshView refreshView) {
        this.loadingView.setVisibility(View.VISIBLE);
        loadingView.clearAnimation();
        loadingView.startAnimation(showLoadingAnimation);
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
        this.loadingView.setVisibility(View.GONE);
    }

    @Override
    public void onPullRefreshComplete(CoolRefreshView refreshView) {
        loadingView.clearAnimation();
        loadingView.startAnimation(hideLoadingAnimation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int totalDistance = totalDistance();
        int headerHeight;
        int pullHeight;
        int offsetToKeepHeaderWhileLoading = offsetToKeepHeaderWhileLoading();
        if (status == CoolRefreshView.PULL_STATUS_REFRESHING) {
            headerHeight = offsetToKeepHeaderWhileLoading;
            pullHeight = currentDistance;
        } else {
            headerHeight = Math.min(currentDistance / 2, offsetToKeepHeaderWhileLoading);
            pullHeight = currentDistance;
        }
        if (headerHeight == 0) {
            return;
        }

        int saveCount = canvas.save();
        canvas.translate(0, totalDistance - currentDistance);

        final int width = canvas.getWidth();
        //原本可以根据touch的x 来决定拖拽的贝赛尔曲线的弧度位置，不过没有touch的x就不处理，默认在中间
        mPointX = width / 2;

        final float mDisplayX = (mPointX - width / 2f) * 0.5f + width / 2f;
        mPaint.setColor(mColor);

        mPath.rewind();
        // 贝赛尔曲线的起始点
        mPath.moveTo(0, 0);
        mPath.lineTo(0, headerHeight);
        // 设置贝赛尔曲线的操作点以及终止点
        mPath.quadTo(mDisplayX, pullHeight, width, headerHeight);
        mPath.lineTo(width, 0);
        mPath.close();
        canvas.drawPath(mPath, mPaint);
        canvas.restoreToCount(saveCount);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(mViewOutlineProvider);
        }
    }

    @Override
    public View createHeaderView(CoolRefreshView refreshView) {
        return this;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    private int totalDistance() {
        return getMeasuredHeight();
    }

    private int offsetToRefresh() {
        return getMeasuredHeight() / 2;
    }

    private int offsetToKeepHeaderWhileLoading() {
        return offsetToRefresh() / 2;
    }

    private DefaultConfig config = new DefaultConfig() {

        @Override
        public int headerViewLayoutOffset(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight();
        }

        @Override
        public int offsetToRefresh(CoolRefreshView refreshView, View headerView) {
            return JellyHeader.this.offsetToRefresh();
        }

        @Override
        public int totalDistance(CoolRefreshView refreshView, View headerView) {
            return JellyHeader.this.totalDistance();
        }

        @Override
        public int offsetToKeepHeaderWhileLoading(CoolRefreshView refreshView, View headerView) {
            return JellyHeader.this.offsetToKeepHeaderWhileLoading();
        }
    };

    private View loadingView;

    /**
     * 设置刷新中的布局
     *
     * @param view
     */
    public void setLoadingView(View view) {
        this.loadingView = view;
        LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        addView(loadingView, layoutParams);
    }

    /**
     * 设置刷新中的布局
     *
     * @param layoutId
     */
    public void setLoadingView(int layoutId) {
        this.loadingView = LayoutInflater.from(getContext()).inflate(layoutId, this, false);
        addView(loadingView);
    }

    /**
     * 设置拖拽的背景色
     *
     * @param color
     */
    public void setDragLayoutColor(int color) {
        mColor = color;
        ViewCompat.postInvalidateOnAnimation(this);
    }

}
