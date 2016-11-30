package com.shizhefei.view.coolrefreshview.demo.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.indicator.IndicatorViewPager;

public class BannerAdapter extends IndicatorViewPager.IndicatorViewPagerAdapter {

    private int[] resIds = {R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4};

    @Override
    public int getCount() {
        return resIds.length;
    }

    @Override
    public View getViewForTab(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = new View(container.getContext());
        }
        return convertView;
    }

    @Override
    public View getViewForPage(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = new ImageView(container.getContext());
        }
        ImageView textView = (ImageView) convertView;
        textView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        textView.setImageResource(resIds[position]);
        return convertView;
    }
}