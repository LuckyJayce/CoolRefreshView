package com.shizhefei.view.coolrefreshview.demo.activity.moreviews;

import android.os.Bundle;

import com.shizhefei.fragment.LazyFragment;
import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.events.RefreshEvent;

/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class TextViewFragment extends LazyFragment implements RefreshEvent {
    private CoolRefreshView coolRefreshView;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_textview);
        coolRefreshView = (CoolRefreshView) findViewById(R.id.textview_funnyRefreshView);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        coolRefreshView.setRefreshing(refreshing);
    }

}
