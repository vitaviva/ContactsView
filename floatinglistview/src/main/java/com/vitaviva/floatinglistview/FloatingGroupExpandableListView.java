package com.vitaviva.floatinglistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.vitaviva.floatinglistview.R;


public class FloatingGroupExpandableListView extends ExpandableListView {

    private static final int[] EMPTY_STATE_SET = {};

    // State indicating the group is expanded
    private static final int[] GROUP_EXPANDED_STATE_SET = {android.R.attr.state_expanded};

    // State indicating the group is empty (has no children)
    private static final int[] GROUP_EMPTY_STATE_SET = {android.R.attr.state_empty};

    // State indicating the group is expanded and empty (has no children)
    private static final int[] GROUP_EXPANDED_EMPTY_STATE_SET = {
            android.R.attr.state_expanded, android.R.attr.state_empty
    };

    // States for the group where the 0th bit is expanded and 1st bit is empty.
    private static final int[][] GROUP_STATE_SETS = {
            EMPTY_STATE_SET, // 00
            GROUP_EXPANDED_STATE_SET, // 01
            GROUP_EMPTY_STATE_SET, // 10
            GROUP_EXPANDED_EMPTY_STATE_SET // 11
    };

    private WrapperExpandableListAdapter mAdapter;
    private OnScrollListener mOnScrollListener;

    // By default, the floating group is enabled
    private boolean mFloatingGroupEnabled = true;

    private View mFloatingGroupView;
    private int mFloatingGroupPosition;

    private OnScrollFloatingGroupListener mOnScrollFloatingGroupListener;

    // An AttachInfo instance is added to the FloatingGroupView in order to have proper touch event handling
    private Object mViewAttachInfo;
    private boolean mHandledByOnInterceptTouchEvent;
    private boolean mHandledByOnTouchEvent;
    private Runnable mOnClickAction;

    private GestureDetector mGestureDetector;

    private boolean mSelectorEnabled;
    private boolean mShouldPositionSelector;
    private boolean mDrawSelectorOnTop;
    private Drawable mSelector;
    private int mSelectorPosition;
    private final Rect mSelectorRect = new Rect();
    private Runnable mPositionSelectorOnTapAction;

    private final Rect mIndicatorRect = new Rect();

    public FloatingGroupExpandableListView(Context context) {
        super(context);
        init();
    }

    public FloatingGroupExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingGroupExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //        setGroupIndicator(getResources().getDrawable(R.drawable.transparent));
        super.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (mOnScrollListener != null) {
                    mOnScrollListener
                            .onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }

                if (mFloatingGroupEnabled && mAdapter != null && mAdapter
                        .getGroupCount() > 0 && visibleItemCount > 0) {
                    createFloatingGroupView(firstVisibleItem);
                }
            }
        });

        mOnClickAction = new Runnable() {

            @Override
            public void run() {
                //                if (mAdapter.isGroupExpanded(mFloatingGroupPosition)) {
                //                    collapseGroup(mFloatingGroupPosition);
                //                } else {
                //                    expandGroup(mFloatingGroupPosition);
                //                }
                setSelectedGroup(mFloatingGroupPosition);
            }
        };

        mPositionSelectorOnTapAction = new Runnable() {

            @Override
            public void run() {
                positionSelectorOnFloatingGroup();
                setPressed(true);
                if (mFloatingGroupView != null) {
                    mFloatingGroupView.setPressed(true);
                }
            }
        };

        mGestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public void onLongPress(MotionEvent e) {
                        if (!mFloatingGroupView.isLongClickable()) {
                            ContextMenuInfo contextMenuInfo = new ExpandableListContextMenuInfo(
                                    mFloatingGroupView,
                                    getPackedPositionForGroup(mFloatingGroupPosition),
                                    mAdapter.getGroupId(mFloatingGroupPosition));
                            Reflector.setFieldSafe(AbsListView.class, "mContextMenuInfo",
                                    FloatingGroupExpandableListView.this, contextMenuInfo);
                            showContextMenu();
                        }
                    }
                });
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Reflection is used here to obtain info about the selector
        //
        Integer result = (Integer) Reflector
                .getFieldSafe(AbsListView.class, "mMotionPosition", this);
        if (result != null) {
            mSelectorPosition = result;
        }
        mSelectorRect.set((Rect) Reflector.getFieldSafe(AbsListView.class, "mSelectorRect", this));

        //        if (!mDrawSelectorOnTop) {
        //			drawDefaultSelector(canvas);
        //        }

        super.dispatchDraw(canvas);

        if (mFloatingGroupEnabled && mFloatingGroupView != null && mFloatingGroupView
                .getVisibility() == View.VISIBLE) {
            if (!mDrawSelectorOnTop) {
                drawFloatingGroupSelector(canvas);
            }

            canvas.save();
            canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                    getHeight() - getPaddingBottom());
            drawChild(canvas, mFloatingGroupView, getDrawingTime());
            drawFloatingGroupIndicator(canvas);
            canvas.restore();
        }

        if (mDrawSelectorOnTop) {
            drawDefaultSelector(canvas);
            drawFloatingGroupSelector(canvas);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL) {
            mHandledByOnInterceptTouchEvent = false;
            mHandledByOnTouchEvent = false;
            mShouldPositionSelector = false;
        }

        // If touch events are being handled by onInterceptTouchEvent() or onTouchEvent() we shouldn't dispatch them to the floating group
        if (!mHandledByOnInterceptTouchEvent && !mHandledByOnTouchEvent && mFloatingGroupView != null) {
            int[] screenCoords = new int[2];
            getLocationInWindow(screenCoords);
            RectF floatingGroupRect = new RectF(
                    screenCoords[0] + mFloatingGroupView.getLeft(),
                    screenCoords[1] + mFloatingGroupView.getTop(),
                    screenCoords[0] + mFloatingGroupView.getRight(),
                    screenCoords[1] + mFloatingGroupView.getBottom());

            if (floatingGroupRect.contains(ev.getRawX(), ev.getRawY())) {
                if (mSelectorEnabled) {
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            mShouldPositionSelector = true;
                            removeCallbacks(mPositionSelectorOnTapAction);
                            postDelayed(mPositionSelectorOnTapAction,
                                    ViewConfiguration.getTapTimeout());
                            break;
                        case MotionEvent.ACTION_UP:
                            positionSelectorOnFloatingGroup();
                            setPressed(true);
                            mFloatingGroupView.setPressed(true);
                            break;
                        default:
                            break;
                    }
                }

                if (mFloatingGroupView.dispatchTouchEvent(ev)) {
                    mGestureDetector.onTouchEvent(ev);
                    onInterceptTouchEvent(ev);
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mHandledByOnInterceptTouchEvent = super.onInterceptTouchEvent(ev);
        return mHandledByOnInterceptTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mHandledByOnTouchEvent = super.onTouchEvent(ev);
        return mHandledByOnTouchEvent;
    }

    @Override
    public void setSelector(Drawable sel) {
        //        super.setSelector(new ColorDrawable(Color.TRANSPARENT));
        super.setSelector(sel);
        if (mSelector != null) {
            mSelector.setCallback(null);
            unscheduleDrawable(mSelector);
        }
        mSelector = sel;
        mSelector.setCallback(this);
    }

    @Override
    public void setDrawSelectorOnTop(boolean onTop) {
        super.setDrawSelectorOnTop(onTop);
        mDrawSelectorOnTop = onTop;
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        if (!(adapter instanceof WrapperExpandableListAdapter)) {
            throw new IllegalArgumentException(
                    "The adapter must be an instance of WrapperExpandableListAdapter");
        }
        setAdapter((WrapperExpandableListAdapter) adapter);
    }

    public void setAdapter(WrapperExpandableListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = adapter;
        mAdapter.setContext(getContext());
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    public void setFloatingGroupEnabled(boolean floatingGroupEnabled) {
        mFloatingGroupEnabled = floatingGroupEnabled;
    }

    public void setOnScrollFloatingGroupListener(OnScrollFloatingGroupListener listener) {
        mOnScrollFloatingGroupListener = listener;
    }

    private void createFloatingGroupView(int position) {
        mFloatingGroupView = null;
        mFloatingGroupPosition = getPackedPositionGroup(getExpandableListPosition(position));

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            Object tag = child.getTag(R.id.fgelv_tag_changed_visibility);
            if (tag instanceof Boolean) {
                boolean changedVisibility = (Boolean) tag;
                if (changedVisibility) {
                    child.setVisibility(View.VISIBLE);
                    child.setTag(R.id.fgelv_tag_changed_visibility, null);
                }
            }
        }

        if (!mFloatingGroupEnabled) {
            return;
        }

        int floatingGroupFlatPosition = getFlatListPosition(
                getPackedPositionForGroup(mFloatingGroupPosition));
        int floatingGroupListPosition = floatingGroupFlatPosition - position;

        if (floatingGroupListPosition >= 0 && floatingGroupListPosition < getChildCount()) {
            View currentGroupView = getChildAt(floatingGroupListPosition);

            if (currentGroupView.getTop() > getPaddingTop()) {
                return;
            } else if (currentGroupView.getTop() <= getPaddingTop() && currentGroupView
                    .getVisibility() == View.VISIBLE) {
                currentGroupView.setVisibility(View.INVISIBLE);
                currentGroupView.setTag(R.id.fgelv_tag_changed_visibility, true);
            }
        }

        if (mFloatingGroupPosition >= 0) {
            mFloatingGroupView = mAdapter.getGroupView(mFloatingGroupPosition,
                    mAdapter.isGroupExpanded(mFloatingGroupPosition), mFloatingGroupView, this);

            if (!mFloatingGroupView.isClickable()) {
                mSelectorEnabled = true;
                mFloatingGroupView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        postDelayed(mOnClickAction, ViewConfiguration.getPressedStateDuration());
                    }
                });
            } else {
                mSelectorEnabled = false;
            }

            loadAttachInfo();
            setAttachInfo(mFloatingGroupView);
        }

        if (mFloatingGroupView == null || mFloatingGroupView.getVisibility() != View.VISIBLE) {
            return;
        }

        int widthMeasureSpec = MeasureSpec
                .makeMeasureSpec(getWidth() - getPaddingLeft() - getPaddingRight(),
                        MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        if (mFloatingGroupView.getLayoutParams() == null) {
            mFloatingGroupView.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        mFloatingGroupView.measure(widthMeasureSpec, heightMeasureSpec);

        int floatingGroupScrollY = 0;

        int nextGroupFlatPosition = getFlatListPosition(
                getPackedPositionForGroup(mFloatingGroupPosition + 1));
        int nextGroupListPosition = nextGroupFlatPosition - position;

        if (nextGroupListPosition >= 0 && nextGroupListPosition < getChildCount()) {
            View nextGroupView = getChildAt(nextGroupListPosition);

            if (nextGroupView != null && nextGroupView
                    .getTop() < getPaddingTop() + mFloatingGroupView
                    .getMeasuredHeight() + getDividerHeight()) {
                floatingGroupScrollY = nextGroupView
                        .getTop() - (getPaddingTop() + mFloatingGroupView
                        .getMeasuredHeight() + getDividerHeight());
            }
        }

        int left = getPaddingLeft();
        int top = getPaddingTop() + floatingGroupScrollY;
        int right = left + mFloatingGroupView.getMeasuredWidth();
        int bottom = top + mFloatingGroupView.getMeasuredHeight();
        mFloatingGroupView.layout(left, top, right, bottom);

        if (mOnScrollFloatingGroupListener != null) {
            mOnScrollFloatingGroupListener
                    .onScrollFloatingGroupListener(mFloatingGroupView, floatingGroupScrollY);
        }
    }

    private void loadAttachInfo() {
        if (mViewAttachInfo == null) {
            mViewAttachInfo = Reflector.getFieldSafe(View.class, "mAttachInfo", this);
        }
    }

    private void setAttachInfo(View v) {
        if (v == null) {
            return;
        }
        if (mViewAttachInfo != null) {
            Reflector.setFieldSafe(View.class, "mAttachInfo", v, mViewAttachInfo);
        }
        if (v instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setAttachInfo(viewGroup.getChildAt(i));
            }
        }
    }

    private void positionSelectorOnFloatingGroup() {
        if (mShouldPositionSelector && mFloatingGroupView != null) {
            int floatingGroupFlatPosition = getFlatListPosition(
                    getPackedPositionForGroup(mFloatingGroupPosition));
            Reflector.invokeMethodExceptionSafe(this, "positionSelector",
                    new Reflector.TypedObject(floatingGroupFlatPosition, int.class),
                    new Reflector.TypedObject(mFloatingGroupView, View.class));
            Reflector.invokeMethodExceptionSafe(this, "positionSelector",
                    new Reflector.TypedObject(floatingGroupFlatPosition, int.class),
                    new Reflector.TypedObject(mFloatingGroupView, View.class));
            invalidate();
        }
        mShouldPositionSelector = false;
        removeCallbacks(mPositionSelectorOnTapAction);
    }

    private void drawDefaultSelector(Canvas canvas) {
        long packedPositionForGroup = getPackedPositionForGroup(mFloatingGroupPosition);
        if (packedPositionForGroup < 0) {
            return;
        }
        int floatingGroupFlatPosition = getFlatListPosition(packedPositionForGroup);
        int selectorListPosition = mSelectorPosition - getFirstVisiblePosition();

        if (selectorListPosition >= 0 && selectorListPosition < getChildCount() && mSelectorRect != null && !mSelectorRect
                .isEmpty()) {
            if (mSelectorPosition != floatingGroupFlatPosition || mFloatingGroupView == null) {
                drawSelector(canvas);
            }
        }
    }

    private void drawFloatingGroupSelector(Canvas canvas) {
        int floatingGroupFlatPosition = getFlatListPosition(
                getPackedPositionForGroup(mFloatingGroupPosition));

        if (mFloatingGroupEnabled && mFloatingGroupView != null && mFloatingGroupView
                .getVisibility() == View.VISIBLE && mSelectorPosition == floatingGroupFlatPosition && mSelectorRect != null && !mSelectorRect
                .isEmpty()) {
            mSelectorRect.set(mFloatingGroupView.getLeft(), mFloatingGroupView.getTop(),
                    mFloatingGroupView.getRight(), mFloatingGroupView.getBottom());
            drawSelector(canvas);
        }
    }

    private void drawSelector(Canvas canvas) {
        canvas.save();
        canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        if (isPressed()) {
            mSelector.setState(getDrawableState());
        } else {
            mSelector.setState(EMPTY_STATE_SET);
        }
        mSelector.setBounds(mSelectorRect);
        mSelector.draw(canvas);
        canvas.restore();
    }

    private void drawFloatingGroupIndicator(Canvas canvas) {
        Drawable groupIndicator = (Drawable) Reflector
                .getFieldSafe(ExpandableListView.class, "mGroupIndicator", this);
        if (groupIndicator != null) {
            int stateSetIndex =
                    (mAdapter.isGroupExpanded(mFloatingGroupPosition) ? 1 : 0) | // Expanded?
                            (mAdapter.getChildrenCount(mFloatingGroupPosition) > 0 ? 2
                                    : 0); // Empty?
            groupIndicator.setState(GROUP_STATE_SETS[stateSetIndex]);

            Integer indicatorLeft = (Integer) Reflector
                    .getFieldSafe(ExpandableListView.class, "mIndicatorLeft",
                            this);
            Integer indicatorRight = (Integer) Reflector
                    .getFieldSafe(ExpandableListView.class, "mIndicatorRight",
                            this);
            mIndicatorRect.set((indicatorLeft == null ? 0 : indicatorLeft) + getPaddingLeft(),
                    mFloatingGroupView.getTop(),
                    (indicatorRight == null ? 0 : indicatorRight) + getPaddingLeft(),
                    mFloatingGroupView.getBottom());

            groupIndicator.setBounds(mIndicatorRect);
            groupIndicator.draw(canvas);
        }
    }

    public interface OnScrollFloatingGroupListener {
        void onScrollFloatingGroupListener(View floatingGroupView, int scrollY);
    }
}
