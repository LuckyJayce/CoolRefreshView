package com.shizhefei.view.coolrefreshview.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shizhefei.view.coolrefreshview.demo.R;


/**
 * Created by LuckyJayce on 2016/11/27.
 */

public class MainActivity extends AppCompatActivity {
    private View moreViewsButton;
    private View moreHeadersButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moreViewsButton = findViewById(R.id.main_moreViews_button);
        moreHeadersButton = findViewById(R.id.main_moreHeaders_button);

        moreViewsButton.setOnClickListener(onClickListener);
        moreHeadersButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == moreViewsButton) {
                startActivity(new Intent(getApplicationContext(), MoreViewsActivity.class));
            } else if (v == moreHeadersButton) {
                startActivity(new Intent(getApplicationContext(), MoreHeadersActivity.class));
            }
        }
    };
}
