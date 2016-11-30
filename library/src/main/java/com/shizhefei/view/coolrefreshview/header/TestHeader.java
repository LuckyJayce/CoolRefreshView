package com.shizhefei.view.coolrefreshview.header;
/*
 * Copyright (C) 2016 LuckyJayce
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.PullHeader;
import com.shizhefei.view.coolrefreshview.R;

/**
 * 用于测试的Header
 * Created by LuckyJayce on 2016/11/27.
 */

public class TestHeader implements PullHeader {
    private View view;
    private TextView mTextView;
    private TextView mTextView2;
    private DefaultConfig config = new DefaultConfig();

    @Override
    public View createHeaderView(final CoolRefreshView refreshView) {
        view = LayoutInflater.from(refreshView.getContext()).inflate(R.layout.coolrecyclerview_testhead, refreshView, false);
        mTextView = (TextView) view.findViewById(R.id.coolrecyclerview_testhead_text1_textView);
        mTextView2 = (TextView) view.findViewById(R.id.coolrecyclerview_testhead_text2_textView);
        mTextView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshView.setRefreshing(false);
            }
        });
        return view;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
        mTextView.setText("onPullBegin :");
    }

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
        mTextView2.setText("onPositionChange" + " status:" + status + " dy:" + dy + " currentDistance:" + currentDistance);
    }

    @Override
    public void onRefreshing(CoolRefreshView refreshView) {
        mTextView.setText("onRefreshing");
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
        mTextView.setText("onReset pullRelease:" + pullRelease);
    }

    @Override
    public void onPullRefreshComplete(CoolRefreshView frame) {
        mTextView.setText("onPullRefreshComplete");
    }

}
