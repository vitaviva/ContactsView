package com.vitaviva.contactsview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vitaviva.contactsview.R;

public class SearchBar extends FrameLayout {
    private TextView vTint;

    private String hint;
    private int tintGravity = Gravity.CENTER;

    public SearchBar(Context context) {
        super(context);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        vTint = (TextView) findViewById(R.id.tint);

        refreshUI();
    }

    private void refreshUI() {
        if (vTint == null) {
            return;
        }
        vTint.setText(hint);
        ((LayoutParams) vTint.getLayoutParams()).gravity = tintGravity;
    }

    public void setHint(int strId) {
        hint = getContext().getString(strId);
        refreshUI();
    }

    public void setHint(String hit) {
        this.hint = hit;
        refreshUI();
    }

    public void setHintGravity(int gravity) {
        tintGravity = gravity;
        refreshUI();
    }

}
