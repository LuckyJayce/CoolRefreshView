package com.shizhefei.view.coolrefreshview.demo.activity.moreviews;

import android.os.Bundle;
import android.widget.ListView;

import com.shizhefei.fragment.LazyFragment;
import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.demo.BooksLoador;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.adapters.BooksListAdapter;
import com.shizhefei.view.coolrefreshview.demo.events.RefreshEvent;

/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class ListViewFragment extends LazyFragment implements RefreshEvent {
    private CoolRefreshView coolRefreshView;
    private ListView listView;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_listview);
        coolRefreshView = (CoolRefreshView) findViewById(R.id.listview_funnyRefreshView);
        listView = (ListView) findViewById(R.id.listview_listView);

        listView.setAdapter(new BooksListAdapter(BooksLoador.getBooks(0)));
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        coolRefreshView.setRefreshing(refreshing);
    }
}
