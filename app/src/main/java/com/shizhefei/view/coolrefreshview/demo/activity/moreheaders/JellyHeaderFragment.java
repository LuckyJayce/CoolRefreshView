package com.shizhefei.view.coolrefreshview.demo.activity.moreheaders;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shizhefei.fragment.LazyFragment;
import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.demo.BooksLoador;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.adapters.BooksRecyclerAdapter;
import com.shizhefei.view.coolrefreshview.demo.events.RefreshEvent;
import com.shizhefei.view.coolrefreshview.header.JellyHeader;


/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class JellyHeaderFragment extends LazyFragment implements RefreshEvent {
    private CoolRefreshView coolRefreshView;
    private RecyclerView recyclerView;
    private BooksRecyclerAdapter adapter;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_recyclerview);
        coolRefreshView = (CoolRefreshView) findViewById(R.id.recyclerview_funnyRefreshView);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_recyclerView);

        //这个header要白色才能突出效果
        coolRefreshView.setBackgroundColor(Color.WHITE);

        JellyHeader jellyHeader = new JellyHeader(getContext());
        jellyHeader.setDragLayoutColor(ContextCompat.getColor(getContext(), R.color.primary));
        jellyHeader.setLoadingView(R.layout.layout_jelley_header);
        coolRefreshView.setPullHeader(jellyHeader);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new BooksRecyclerAdapter(BooksLoador.getBooks(0)));

    }

    @Override
    public void setRefreshing(boolean refreshing) {
        coolRefreshView.setRefreshing(refreshing);
    }

}
