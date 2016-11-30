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
import android.view.View;

import java.util.HashSet;

/**
 * Created by LuckyJayce on 2016/11/27.
 */

class ProxyPullHeader implements PullHeader {

    private HashSet<OnPullListener> listeners = new HashSet<>(3);

    private PullHeader reaPullHeader;

    public ProxyPullHeader(PullHeader reaPullHeader) {
        this.reaPullHeader = reaPullHeader;
    }

    public void setPullHandler(PullHeader pullHeader) {
        reaPullHeader = pullHeader;
    }

    @Override
    public View createHeaderView(CoolRefreshView refreshView) {
        return reaPullHeader.createHeaderView(refreshView);
    }

    @Override
    public Config getConfig() {
        return reaPullHeader.getConfig();
    }

    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
        reaPullHeader.onPullBegin(refreshView);
        for (OnPullListener listener : listeners) {
            listener.onPullBegin(refreshView);
        }
    }

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
        reaPullHeader.onPositionChange(refreshView, status, dy, currentDistance);
        for (OnPullListener listener : listeners) {
            listener.onPositionChange(refreshView, status, dy, currentDistance);
        }
    }

    @Override
    public void onRefreshing(CoolRefreshView refreshView) {
        reaPullHeader.onRefreshing(refreshView);
        for (OnPullListener listener : listeners) {
            listener.onRefreshing(refreshView);
        }
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
        reaPullHeader.onReset(refreshView, pullRelease);
        for (OnPullListener listener : listeners) {
            listener.onReset(refreshView, pullRelease);
        }
    }

    @Override
    public void onPullRefreshComplete(CoolRefreshView refreshView) {
        reaPullHeader.onPullRefreshComplete(refreshView);
        for (OnPullListener listener : listeners) {
            listener.onPullRefreshComplete(refreshView);
        }
    }

    public void addListener(OnPullListener onPullListener) {
        listeners.add(onPullListener);
    }

    public void removeListener(OnPullListener onPullListener) {
        listeners.remove(onPullListener);
    }
}
