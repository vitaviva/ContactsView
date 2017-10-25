package com.vitaviva.contactsview.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vitaviva.contactsview.R;
import com.vitaviva.contactsview.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IndexBar extends LinearLayout implements ShadeVisible {
    private static final int DEF_TEXT_COLOR = 0xffbababa;

    public static final String SORT_VALUE_HEAD = "!";
    public static final String SORT_VALUE_TAIL = "{";

    private static final String[] INDEXES = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
            "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
    };

    private final View mDialog;

    // current index view
    private View currentIndexView;

    private final WindowManager windowManager;
    private final WindowManager.LayoutParams lp;

    // list for Callback
    private ArrayList<OnIndexBar> mCallbackList;

    private final Drawable backGround;

    protected int textColor;
    protected float textSize;

    /**
     * 带有 头 A-Z # 的IndexItem List
     */
    public static List<IndexItem> getHeadAZTailIndexItemList() {
        List<IndexItem> headAZTailIndexItemList = new ArrayList<>();
        headAZTailIndexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, "↑",
                SORT_VALUE_HEAD));
        for (String index : INDEXES) {
            headAZTailIndexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, index,
                    index.toLowerCase()));
        }
        headAZTailIndexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, "#",
                SORT_VALUE_TAIL));
        return headAZTailIndexItemList;
    }

    /**
     * 带有 A-Z # 的IndexItem List
     */
    public static List<IndexItem> getContactAZTailIndexItemList() {
        List<IndexItem> headAZTailIndexItemList = new ArrayList<>();
        for (String index : INDEXES) {
            headAZTailIndexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, index,
                    index.toLowerCase()));
        }
        headAZTailIndexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, "#",
                SORT_VALUE_TAIL));
        return headAZTailIndexItemList;
    }

    /**
     * 带有 A-Z # 的IndexItem List
     */
    public static List<IndexItem> getAZTailIndexItemList() {
        List<IndexItem> AZTailIndexItemList = new ArrayList<>();
        for (String index : INDEXES) {
            AZTailIndexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, index,
                    index.toLowerCase()));
        }
        AZTailIndexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, "#",
                SORT_VALUE_TAIL));
        return AZTailIndexItemList;
    }

    @Override
    public void shadeInvisible() {
        animate().alpha(ShadeVisible.INVISIBLE).setDuration(SHADEDURATION);
    }

    @Override
    public void shadeVisible() {
        animate().alpha(ShadeVisible.VISIBLE).setDuration(SHADEDURATION);
    }


    public interface OnIndexBar {
        /**
         * 索引被触摸
         *
         * @param indexView 被触摸的索引
         * @param event     触摸事件
         */
        void onIndexBarTouch(View indexView, MotionEvent event);

        /**
         * 搜索图片被选中
         */
        void onSearchSelect();

        void onIndexChange(View original, View present);
    }

    public boolean registerCallback(OnIndexBar callback) {
        if (mCallbackList == null) {
            mCallbackList = new ArrayList<>();
        }

        return mCallbackList.add(callback);
    }

    public boolean unregisterCallback(OnIndexBar callback) {
        boolean ret = false;

        if (mCallbackList != null) {
            ret = mCallbackList.remove(callback);
        }
        return ret;
    }

    private void onIndexBarTouch(View indexView, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Object oTag = indexView.getTag();
                if (oTag instanceof IndexItem) {
                    IndexItem indexItem = (IndexItem) oTag;
                    switch (indexItem.getType()) {
                        case Text:
                            ((TextView) mDialog.findViewById(R.id.text))
                                    .setText((CharSequence) indexItem.getShowIndexValue());
                            mDialog.findViewById(R.id.text).setVisibility(View.VISIBLE);
//                            mDialog.findViewById(R.id.image).setVisibility(View.GONE);
                            mDialog.setVisibility(View.VISIBLE);
                            break;
//                        case Image:
//                            ((ImageView) mDialog.findViewById(R.id.image))
//                                    .setImageResource((Integer) indexItem.getShowIndexValue());
//                            mDialog.findViewById(R.id.text).setVisibility(View.GONE);
//                            mDialog.findViewById(R.id.image).setVisibility(View.VISIBLE);
//                            mDialog.setVisibility(View.VISIBLE);
//                            break;
                        default:
                            mDialog.setVisibility(View.GONE);
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
                indexView.setBackgroundDrawable(null);
                mDialog.setVisibility(View.INVISIBLE);
                break;
            case MotionEvent.ACTION_UP:
                indexView.setBackgroundDrawable(null);
                mDialog.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
        if (mCallbackList != null) {
            int size = mCallbackList.size();
            for (int i = 0; i < size; i++) {
                mCallbackList.get(i).onIndexBarTouch(indexView, event);
            }
        }
    }

    private void onIndexChange(View original, View present) {
        if (original != null) {
            original.setBackgroundDrawable(null);
        }
        if (present == null) {
            mDialog.setVisibility(View.INVISIBLE);
        }
        if (mCallbackList != null) {
            int size = mCallbackList.size();
            for (int i = 0; i < size; i++) {
                mCallbackList.get(i).onIndexChange(original, present);
            }
        }
    }

    private void onSearchSelect() {
        if (mCallbackList != null) {
            int size = mCallbackList.size();
            for (int i = 0; i < size; i++) {
                mCallbackList.get(i).onSearchSelect();
            }
        }
    }

    /**
     * constructor
     */
    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        backGround = getBackground();
        setBackgroundDrawable(null);
        setOnTouchListener(layoutIndexTouchListener);
        mDialog = View.inflate(context, R.layout.view_index_position, null);
        mDialog.setVisibility(View.INVISIBLE);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IndexBar);
        if (null != a) {
            textColor = a.getColor(R.styleable.IndexBar_indexItemTextColor, DEF_TEXT_COLOR);
            textSize = a.getDimensionPixelSize(R.styleable.IndexBar_indexItemTextSize, 0);
            a.recycle();
        }

        lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    private final OnTouchListener layoutIndexTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int touchX = (int) event.getX();
            int touchY = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_CANCEL:
                    setBackgroundDrawable(null);
                    if (mDialog.getParent() != null) {
                        windowManager.removeView(mDialog);
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    requestFocus();
                    clearFocus();
                    setBackgroundDrawable(backGround);
                    if (mDialog.getParent() == null) {
                        windowManager.addView(mDialog, lp);
                    }
                    break;
                default:

            }
            View tempView = getIndexView(touchX, touchY);
            if (tempView != null) {
                //                Object oTag = tempView.getTag();
                //                if (oTag instanceof IndexItem) {
                //                    IndexItem indexItem = (IndexItem) oTag;
                //                    if (indexItem.getType() == IndexItemType.Search) {
                //                        if (event.getAction() == MotionEvent.ACTION_UP) {
                //                            onSearchSelect();
                //                            if (currentIndexView != null) {
                //                                onIndexChange(currentIndexView, null);
                //                                currentIndexView = null;
                //                            }
                //                        }
                //                        return true;
                //                    }
                //                }
                onIndexBarTouch(tempView, event);
            }
            if (tempView != currentIndexView) {
                onIndexChange(currentIndexView, tempView);
                currentIndexView = tempView;
            }
            return true;
        }
    };

    /**
     * getIndexView
     */
    private View getIndexView(float touchX, float touchY) {
        View ret = null;
        if (getChildCount() > 0) {
            int length = getChildCount();
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                if (touchX > 0 && touchX < getWidth()) {
                    ret = getChildAt((int) Math.floor(touchX
                            / getWidth() * length));
                }
            } else if (getOrientation() == LinearLayout.VERTICAL) {
                if (touchY > 0 && touchY < getHeight()) {
                    ret = getChildAt((int) Math.floor(touchY
                            / getHeight() * length));
                }
            }
            if (ret == null) {
                ret = currentIndexView;
            }
        }
        return ret;
    }

    public static String getIndexValue(CharSequence pinYin) {
        String head = SORT_VALUE_TAIL;
        if (!TextUtils.isEmpty(pinYin)) {
            char c = pinYin.charAt(0);
            if (c <= 'z' && c >= 'a' || c <= 'Z' && c >= 'A') {
                head = String.valueOf(c).toLowerCase();
            } else if (c == SORT_VALUE_HEAD.charAt(0)) {
                head = SORT_VALUE_HEAD;
            } else {
                head = SORT_VALUE_TAIL;
            }
        }
        return head;
    }

    /**
     * 初始化
     */
    public void init(Collection<IndexItem> indexItemList) {
        Context context = getContext();
        int size = 10;
        DisplayMetrics dm = new DisplayMetrics();
        if (context instanceof Activity) {
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            if (dm.densityDpi < DisplayMetrics.DENSITY_HIGH) {
                size = 8;
            }
        }

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1;
        for (IndexItem indexItem : indexItemList) {
            switch (indexItem.getType()) {
                case Text:
                    TextView indexView = new TextView(getContext());
                    indexView.setText((CharSequence) indexItem.getShowIndexValue());
                    indexView.setTag(indexItem);
                    indexView.setLayoutParams(lp);
                    indexView.setSingleLine(true);
                    indexView.setTextColor(0 == textColor ? DEF_TEXT_COLOR : textColor);
                    indexView.setTextSize(0);
                    if (0 == textSize) {
                        indexView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                    } else {
                        indexView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    }
                    indexView.setGravity(Gravity.CENTER);
                    addView(indexView);
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus && mDialog.getParent() != null) {
            setBackgroundDrawable(null);
            windowManager.removeView(mDialog);
        } else if (hasWindowFocus && mDialog.getParent() == null) {
            //            mWindowManager.addView(mDialog, lp);
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }

    public View getCurrentIndexView() {
        return currentIndexView;
    }

    public enum IndexItemType {
        Text,
        //        Image, Search
    }

    /**
     * 条目属性
     */
    public static class IndexItem implements Comparable<IndexItem> {

        /**
         * 条目类型
         */
        private IndexItemType type;
        /**
         * 索引显示值：type=Text时是String，type=Image或Search时是图片的resourceId，
         */
        private Object showIndexValue;
        /**
         * 条目显示值：type=Text时是String，type=Image或Search时是图片的resourceId，
         */
        private Object showGroupValue;
        /**
         * 排序用索引
         */
        private String indexValue;

        public IndexItem(IndexItemType type, Object showValue, String indexValue) {
            this.type = type;
            showIndexValue = showValue;
            showGroupValue = showValue;
            this.indexValue = indexValue;
        }

        public IndexItem(IndexItemType type, Object showIndexValue, Object showGroupValue,
                         String indexValue) {
            this.type = type;
            this.showIndexValue = showIndexValue;
            this.showGroupValue = showGroupValue;
            this.indexValue = indexValue;
        }

        public IndexItemType getType() {
            return type;
        }

        public void setType(IndexItemType type) {
            this.type = type;
        }

        public Object getShowIndexValue() {
            return showIndexValue;
        }

        public void setShowIndexValue(Object showIndexValue) {
            this.showIndexValue = showIndexValue;
        }

        public Object getShowGroupValue() {
            return showGroupValue;
        }

        public void setShowGroupValue(Object showGroupValue) {
            this.showGroupValue = showGroupValue;
        }

        public String getIndexValue() {
            return indexValue;
        }

        public void setIndexValue(String indexValue) {
            this.indexValue = indexValue;
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = prime * result + (indexValue == null ? 0 : indexValue.hashCode());
            result = prime * result + (showIndexValue == null ? 0 : showIndexValue.hashCode());
            result = prime * result + (showGroupValue == null ? 0 : showGroupValue.hashCode());
            result = prime * result + (type == null ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            IndexItem other = (IndexItem) obj;
            if (indexValue == null) {
                if (other.indexValue != null) {
                    return false;
                }
            } else if (!indexValue.equals(other.indexValue)) {
                return false;
            }
            if (showIndexValue == null) {
                if (other.showIndexValue != null) {
                    return false;
                }
            } else if (!showIndexValue.equals(other.showIndexValue)) {
                return false;
            }
            if (showGroupValue == null) {
                if (other.showGroupValue != null) {
                    return false;
                }
            } else if (!showGroupValue.equals(other.showGroupValue)) {
                return false;
            }
            return type == other.type;
        }

        @Override
        public String toString() {
            return "IndexItem [type=" + type + ", showIndexValue=" + showIndexValue + ", showGroupValue="
                    + showGroupValue + ", indexValue=" + indexValue + ']';
        }

        @Override
        public int compareTo(@NonNull IndexItem another) {
            return Util.stringCompare(indexValue, another.getIndexValue());
        }

    }
}
