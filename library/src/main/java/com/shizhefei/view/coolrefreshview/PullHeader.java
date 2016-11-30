package com.shizhefei.view.coolrefreshview;
/*
*  Copyright 2016 shizhefei（LuckyJayce）
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.AbsListView;


/**
 * Created by LuckyJayce on 2016/11/27.
 */

public interface PullHeader extends OnPullListener {

    View createHeaderView(CoolRefreshView refreshView);

    Config getConfig();

    abstract class Config {
        /**
         * 超出这个偏移量，松开手指就会触发刷新。
         */
        public abstract int offsetToRefresh(CoolRefreshView refreshView, View headerView);

        /**
         * 显示刷新的位置的偏移量
         */
        public abstract int offsetToKeepHeaderWhileLoading(CoolRefreshView refreshView, View headerView);

        /**
         * 刷新控件总共可以下拉拖动的距离
         */
        public abstract int totalDistance(CoolRefreshView refreshView, View headerView);

        /**
         * headView 在布局中的偏移量
         */
        public abstract int headerViewLayoutOffset(CoolRefreshView refreshView, View headerView);

        /**
         * contentView 是否可以向上滚动，用来判断是否可以下拉刷新，如果可以向上滚动就不做下拉刷新动作
         */
        public abstract boolean contentCanScrollUp(CoolRefreshView refreshView, View contentView);

        /**
         * 拦截滑动事件
         *
         * @param refreshView
         * @param dy              触摸滑动的偏移量
         * @param currentDistance 当前的滑动的距离
         * @param totalDistance   总的可下拉距离
         * @return
         */
        public abstract int dispatchTouchMove(CoolRefreshView refreshView, int dy, int currentDistance, int totalDistance);

    }

    class DefaultConfig extends Config {

        @Override
        public int offsetToRefresh(CoolRefreshView refreshView, View headerView) {
            return (int) (headerView.getMeasuredHeight() * 1.2f);
        }

        @Override
        public int offsetToKeepHeaderWhileLoading(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight();
        }

        @Override
        public int totalDistance(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight() * 3;
        }

        @Override
        public int headerViewLayoutOffset(CoolRefreshView refreshView, View headerView) {
            return headerView.getMeasuredHeight();
        }

        @Override
        public boolean contentCanScrollUp(CoolRefreshView refreshView, View contentView) {
            if (android.os.Build.VERSION.SDK_INT < 14) {
                if (contentView instanceof AbsListView) {
                    final AbsListView absListView = (AbsListView) contentView;
                    return absListView.getChildCount() > 0
                            && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                            .getTop() < absListView.getPaddingTop());
                } else {
                    return ViewCompat.canScrollVertically(contentView, -1) || contentView.getScrollY() > 0;
                }
            } else {
                return ViewCompat.canScrollVertically(contentView, -1);
            }
        }

        @Override
        public int dispatchTouchMove(CoolRefreshView refreshView, int dy, int currentDistance, int totalDistance) {
            float hDataY = dy / 2;
            float ps = (int) (-hDataY * Math.abs(currentDistance) / (float) totalDistance) - hDataY;
            float resultDy;
            resultDy = dy + ps * 0.9f;
            return (int) resultDy;
        }
    }
}
