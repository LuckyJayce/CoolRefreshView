package com.shizhefei.view.coolrefreshview.demo;

import com.shizhefei.view.coolrefreshview.demo.entry.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class BooksLoador {

    public static List<Book> getBooks(int page) {
        List<Book> list = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            list.add(new Book("page:" + page + " position:" + i));
        }
        return list;
    }

}
