package com.shizhefei.view.coolrefreshview.header;
/*
 * Copyright (C) 2016 LuckyJayce
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
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.PullHeader;
import com.shizhefei.view.coolrefreshview.R;

/**
 * Created by LuckyJayce on 2016/11/28.
 */

public class DefaultHeader implements PullHeader {

    private ImageView imageView;
    private TextView textView;
    private MaterialProgressDrawable materialProgressDrawable;
    private ImageView progressImageView;
    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private int mRotateAniTime = 150;
    private View headerView;
    private int backgroundColor = Color.parseColor("#989898");

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        if (headerView != null) {
            headerView.setBackgroundColor(color);
        }
    }

    @Override
    public View createHeaderView(CoolRefreshView refreshView) {
        Context context = refreshView.getContext();
        headerView = LayoutInflater.from(context).inflate(R.layout.coolrefreshview_defaultheader, refreshView, false);
        imageView = (ImageView) headerView.findViewById(R.id.coolrefresh_defaultheader_imageView);
        textView = (TextView) headerView.findViewById(R.id.coolrefresh_defaultheader_textView);
        progressImageView = (ImageView) headerView.findViewById(R.id.coolrefresh_defaultheader_progress_imageView);

        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setFillAfter(true);

        imageView.setAnimation(mFlipAnimation);
        materialProgressDrawable = new MaterialProgressDrawable(context, progressImageView);
        materialProgressDrawable.setColorSchemeColors(Color.WHITE);
        materialProgressDrawable.setAlpha(255);
        progressImageView.setImageDrawable(materialProgressDrawable);
        headerView.setBackgroundColor(backgroundColor);
        return headerView;
    }

    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
        progressImageView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        textView.setText(getResources().getString(R.string.coolrefreshview_pull_down_to_refresh));
        isDownArrow = true;
    }

    private boolean isDownArrow = true;

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
        int offsetToRefresh = getConfig().offsetToRefresh(refreshView, headerView);
        if (status == CoolRefreshView.PULL_STATUS_TOUCH_MOVE) {
            if (currentDistance < offsetToRefresh) {
                if (!isDownArrow) {
                    textView.setText(getResources().getString(R.string.coolrefreshview_pull_down_to_refresh));
                    imageView.clearAnimation();
                    imageView.startAnimation(mReverseFlipAnimation);
                    isDownArrow = true;
                }
            } else {
                if (isDownArrow) {
                    textView.setText(getResources().getString(R.string.coolrefreshview_release_to_refresh));
                    imageView.clearAnimation();
                    imageView.startAnimation(mFlipAnimation);
                    isDownArrow = false;
                }
            }
        }
    }

    @Override
    public void onRefreshing(CoolRefreshView refreshView) {
        textView.setText(getResources().getString(R.string.coolrefreshview_refreshing));
        imageView.clearAnimation();
        materialProgressDrawable.start();
        imageView.setVisibility(View.GONE);
        progressImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
        textView.setText(getResources().getString(R.string.coolrefreshview_pull_down_to_refresh));
        materialProgressDrawable.stop();
        imageView.setVisibility(View.GONE);
        progressImageView.setVisibility(View.GONE);
        imageView.clearAnimation();
    }

    @Override
    public void onPullRefreshComplete(CoolRefreshView refreshView) {
        textView.setText(getResources().getString(R.string.coolrefreshview_complete));
        materialProgressDrawable.stop();
        imageView.setVisibility(View.GONE);
        progressImageView.setVisibility(View.GONE);
        imageView.clearAnimation();
    }

    private Resources getResources() {
        return headerView.getResources();
    }


    @Override
    public Config getConfig() {
        return config;
    }

    private DefaultConfig config = new DefaultConfig() {
        @Override
        public int offsetToRefresh(CoolRefreshView refreshView, View headerView) {
            return (int) (headerView.getMeasuredHeight() / 3 * 1.2f);
        }

        @Override
        public int offsetToKeepHeaderWhileLoading(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight() / 3;
        }

        @Override
        public int totalDistance(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight();
        }
    };
}
