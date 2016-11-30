package com.shizhefei.view.coolrefreshview.demo.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shizhefei.mvc.IDataAdapter;
import com.shizhefei.recyclerview.HFAdapter;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.entry.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class BooksRecyclerAdapter extends HFAdapter implements IDataAdapter<List<Book>> {
    private List<Book> list = new ArrayList<>();

    public BooksRecyclerAdapter() {
    }

    public BooksRecyclerAdapter(List<Book> list) {
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolderHF(ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false)) {
        };
    }

    @Override
    public void onBindViewHolderHF(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(list.get(position).name);
    }

    @Override
    public int getItemCountHF() {
        return list.size();
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

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
