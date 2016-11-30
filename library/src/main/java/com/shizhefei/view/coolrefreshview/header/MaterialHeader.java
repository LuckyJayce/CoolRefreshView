package com.shizhefei.view.coolrefreshview.header;
/*
 * Copyright (C) 2016 LuckyJayce
 * Copyright (C) 2016 liaohuqiu
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.PullHeader;

/**
 * Created by LuckyJayce on 2016/11/27.
 *
 * 这个header来源于 https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh
 */

public class MaterialHeader extends View implements PullHeader {

    private final MaterialProgressDrawable mDrawable;
    private float mScale = 1f;

    public MaterialHeader(Context context) {
        this(context, null);
    }

    public MaterialHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = new MaterialProgressDrawable(getContext(), this);
        mDrawable.setBackgroundColor(Color.WHITE);
        mDrawable.setCallback(this);
        mDrawable.showArrow(true);
        mDrawable.setAlpha(255);
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr == mDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    public void setColorSchemeColors(int[] colors) {
        mDrawable.setColorSchemeColors(colors);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int padding = getPaddingTop() + getPaddingBottom();
        if (padding == 0) {
            padding = Utils.dipToPix(getContext(), 16);
        }
        int height = mDrawable.getIntrinsicHeight() + padding;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int saveCount = canvas.save();
        Rect rect = mDrawable.getBounds();
        int l = getPaddingLeft() + (getMeasuredWidth() - mDrawable.getIntrinsicWidth()) / 2;
        int offset = getMeasuredHeight() / 2 - mDrawable.getIntrinsicHeight() / 2;
        canvas.translate(l, getPaddingTop() + offset);
        canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
        mScale = 1f;
        mDrawable.stop();
    }

    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
        mDrawable.setAlpha(255);
    }

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
        int totalDistance = getConfig().totalDistance(refreshView, this);
        float percent = Math.min(1f, 1.0f * currentDistance / totalDistance);
        if (status == CoolRefreshView.PULL_STATUS_TOUCH_MOVE) {
//            mDrawable.setAlpha((int) (255 * percent));
            mDrawable.showArrow(true);

            float strokeStart = ((percent) * .8f);
            mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
            mDrawable.setArrowScale(Math.min(1f, percent));

            // magic
            float rotation = (-0.25f + .4f * percent + percent * 2) * .5f;
            mDrawable.setProgressRotation(rotation);
            invalidate();
        }
    }

    @Override
    public void onRefreshing(CoolRefreshView refreshView) {
        mDrawable.start();
    }


    @Override
    public void onPullRefreshComplete(CoolRefreshView refreshView) {
        mDrawable.stop();
    }

    @Override
    public View createHeaderView(CoolRefreshView refreshView) {
        return this;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    private DefaultConfig config = new DefaultConfig();
}
