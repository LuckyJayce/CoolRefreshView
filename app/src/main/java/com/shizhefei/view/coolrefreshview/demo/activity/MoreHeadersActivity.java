package com.shizhefei.view.coolrefreshview.demo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.shizhefei.view.coolrefreshview.demo.DisplayUtil;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.activity.moreheaders.JellyHeaderFragment;
import com.shizhefei.view.coolrefreshview.demo.activity.moreheaders.MaterialHeaderFragment;
import com.shizhefei.view.coolrefreshview.demo.activity.moreheaders.PinContentMaterialHeaderFragment;
import com.shizhefei.view.coolrefreshview.demo.activity.moreheaders.StateHeaderFragment;
import com.shizhefei.view.coolrefreshview.demo.activity.moreheaders.TestHeaderFragment;
import com.shizhefei.view.coolrefreshview.demo.adapters.PagesAdapter;
import com.shizhefei.view.coolrefreshview.demo.events.RefreshEvent;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

public class MoreHeadersActivity extends AppCompatActivity {


    private IndicatorViewPager indicatorViewPager;
    private View refreshButton;
    private View completeButton;
    private TextView headTextView;
    private PagesAdapter pagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_views);
        Indicator indicator = (Indicator) findViewById(R.id.moreviews_indicatorView);
        ViewPager viewPager = (ViewPager) findViewById(R.id.moreviews_viewPager);
        refreshButton = findViewById(R.id.moreviews_refresh_button);
        completeButton = findViewById(R.id.moreviews_complete_button);
        headTextView = (TextView) findViewById(R.id.moreviews_headtext_textView);

        headTextView.setText("MoreHeaders");

        Class[] fragments = {
                StateHeaderFragment.class,
                JellyHeaderFragment.class,
                MaterialHeaderFragment.class,
                PinContentMaterialHeaderFragment.class,
                TestHeaderFragment.class
        };

        indicator.setScrollBar(new ColorBar(this, ContextCompat.getColor(this, R.color.primary), DisplayUtil.dipToPix(this, 3)));
        indicator.setOnTransitionListener(new OnTransitionTextListener().setColor(Color.BLACK, Color.GRAY));
        viewPager.setOffscreenPageLimit(2);
        indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
        indicatorViewPager.setAdapter(pagesAdapter = new PagesAdapter(getSupportFragmentManager(), fragments));

        refreshButton.setOnClickListener(onClickListener);
        completeButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment fragment = pagesAdapter.getCurrentFragment();
            RefreshEvent refreshEvent = (RefreshEvent) fragment;
            if (view == refreshButton) {
                refreshEvent.setRefreshing(true);
            } else if (view == completeButton) {
                refreshEvent.setRefreshing(false);
            }
        }
    };
}
