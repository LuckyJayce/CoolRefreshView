package com.shizhefei.view.coolrefreshview.demo.activity.moreviews;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.shizhefei.fragment.LazyFragment;
import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.demo.BooksLoador;
import com.shizhefei.view.coolrefreshview.demo.DisplayUtil;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.adapters.BannerAdapter;
import com.shizhefei.view.coolrefreshview.demo.adapters.BooksRecyclerAdapter;
import com.shizhefei.view.coolrefreshview.demo.events.RefreshEvent;
import com.shizhefei.view.indicator.BannerComponent;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.slidebar.ColorBar;


/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class RecyclerViewFragment extends LazyFragment implements RefreshEvent {
    private CoolRefreshView coolRefreshView;
    private RecyclerView recyclerView;
    private BooksRecyclerAdapter adapter;
    private BannerComponent bannerComponent;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_recyclerview);
        coolRefreshView = (CoolRefreshView) findViewById(R.id.recyclerview_funnyRefreshView);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new BooksRecyclerAdapter(BooksLoador.getBooks(0)));
        View headLayout = inflater.inflate(R.layout.main_head, recyclerView, false);
        adapter.addHeader(headLayout);

        ViewPager viewPager = (ViewPager) headLayout.findViewById(R.id.viewPager);
        Indicator indicator = (Indicator) headLayout.findViewById(R.id.indicatorView);
        indicator.setScrollBar(new ColorBar(getContext(), ContextCompat.getColor(getContext(), R.color.primary), DisplayUtil.dipToPix(getContext(), 3)));
        bannerComponent = new BannerComponent(indicator, viewPager, false);
        bannerComponent.setAdapter(new BannerAdapter());
        bannerComponent.startAutoPlay();
    }

    @Override
    protected void onFragmentStopLazy() {
        super.onFragmentStopLazy();
        bannerComponent.stopAutoPlay();
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        coolRefreshView.setRefreshing(refreshing);
    }

}
