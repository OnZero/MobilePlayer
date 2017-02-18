package com.example.mobileplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by lenovo on 2017/2/2.
 */

public class MarqueeTextview extends TextView {
    public MarqueeTextview(Context context) {
        super(context);
    }

    public MarqueeTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
