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

public interface OnPullListener {

    void onPullBegin(CoolRefreshView refreshView);

    void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance);

    void onRefreshing(CoolRefreshView refreshView);

    void onReset(CoolRefreshView refreshView, boolean pullRelease);

    void onPullRefreshComplete(CoolRefreshView refreshView);
}
