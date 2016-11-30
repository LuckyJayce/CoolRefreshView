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

/**
 * Created by LuckyJayce on 2016/11/27.
 */

/**
 * 下拉监听
 */
public interface OnPullListener {

    /**
     * 开始拖动
     */
    void onPullBegin(CoolRefreshView refreshView);

    /**
     * 位置变化
     *
     * @param refreshView
     * @param status          状态 /没有任何操作
     *                        public final static byte PULL_STATUS_INIT = 1;
     *                        //开始下拉
     *                        public final static byte PULL_STATUS_TOUCH_MOVE = 2;
     *                        //回到原始位置
     *                        public final static byte PULL_STATUS_RESET = 3;
     *                        //刷新中
     *                        public final static byte PULL_STATUS_REFRESHING = 4;
     *                        //刷新完成
     *                        public final static byte PULL_STATUS_COMPLETE = 5;
     * @param dy              下拉事件的位移
     * @param currentDistance 当前位移的距离
     */
    void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance);

    /**
     * 刷新中
     */
    void onRefreshing(CoolRefreshView refreshView);

    /**
     * 没有刷新的释放回去
     */
    void onReset(CoolRefreshView refreshView, boolean pullRelease);

    /**
     * 设置刷新完成，并且释放回去
     */
    void onPullRefreshComplete(CoolRefreshView refreshView);
}
