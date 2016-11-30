package com.shizhefei.view.coolrefreshview.header;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by LuckyJayce on 2016/11/30.
 */

class Utils {

    public static int dipToPix(Context context, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }
}
