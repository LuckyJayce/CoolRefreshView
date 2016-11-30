package com.shizhefei.view.coolrefreshview.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shizhefei.mvc.IDataAdapter;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.entry.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class BooksListAdapter extends BaseAdapter implements IDataAdapter<List<Book>> {
    private List<Book> list = new ArrayList<>();

    public BooksListAdapter() {
        list = new ArrayList<>();
    }

    public BooksListAdapter(List<Book> list) {
        this.list = list;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        }
        TextView textView = (TextView) view;
        textView.setText(list.get(position).name);
        return view;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public void notifyDataChanged(List<Book> books, boolean isRefresh) {
        if (isRefresh) {
            list.clear();
        }
        list.addAll(books);
        notifyDataSetChanged();
    }

    @Override
    public List<Book> getData() {
        return list;
    }
}
