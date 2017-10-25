package com.vitaviva.contactsview.controller;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vitaviva.contactsview.R;
import com.vitaviva.contactsview.view.IDivider;
import com.vitaviva.contactsview.view.IndexBar;
import com.vitaviva.floatinglistview.FloatingGroupExpandableListView;
import com.vitaviva.floatinglistview.WrapperExpandableListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ListViewAndIndexBarController<T> {

    private final Context context;
    private FloatingGroupExpandableListView vList;
    private IndexBar vIndexBar;
    protected List<IndexBar.IndexItem> indexItemList;

    private boolean didHideGroupView;
    private AdapterView.OnItemClickListener itemClickListener;
    private AdapterView.OnItemLongClickListener itemLongClickListener;
    private IBuildData<T> adapter;

    private final List<IndexBar.IndexItem> showGroupData = new LinkedList<>();
    private final Map<IndexBar.IndexItem, List<ChildItem<T>>> showChildData = new HashMap<>();
    private int minPaddingRight; // 单位 pixels
    private WrapperExpandableListAdapter wrapperAdapter;

    public ListViewAndIndexBarController(Context context, ViewGroup vHostView, List<IndexBar.IndexItem> indexItemList) {
        this.context = context;
        this.indexItemList = indexItemList;
        initViews(vHostView);
        initIndexBar();
    }

    public ListViewAndIndexBarController(Context context, FloatingGroupExpandableListView vList, IndexBar vIndexBar, List<IndexBar.IndexItem> indexItemList) {
        this.context = context;
        this.vList = vList;
        this.vIndexBar = vIndexBar;
        this.indexItemList = indexItemList;
        initViews(null);
        initIndexBar();
    }

    private void initViews(ViewGroup vHostView) {
        if (vHostView != null) {
            vList = (FloatingGroupExpandableListView) vHostView.findViewById(android.R.id.list);
            vIndexBar = (IndexBar) vHostView.findViewById(R.id.indexBar);
        }
        vList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return true;
            }
        });

        vList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ChildItem<T> child = (ChildItem<T>) vList.getExpandableListAdapter().getChild(groupPosition, childPosition);
                if (null != itemClickListener) {
                    itemClickListener.onItemClick(parent, v, child.getSourceIndex(), id);
                    v.setClickable(true);
                    v.performClick();
                    v.setClickable(false);//防止子view拦截listview的click事件
                    return true;
                }
                return false;
            }
        });

        vList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                           long id) {
                if (itemLongClickListener != null) {
                    itemLongClickListener.onItemLongClick(parent, view, position, id);
                    return true;
                }
                return false;
            }
        });
    }

    private void initIndexBar() {
        if (vIndexBar != null) {
            if (indexItemList == null) {
                indexItemList = new ArrayList<>();
                String[] indexList = {
                        "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
                        "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                };
                for (String index : indexList) {
                    indexItemList.add(new IndexBar.IndexItem(IndexBar.IndexItemType.Text, index, index));
                }
            }
            vIndexBar.init(indexItemList);
            vIndexBar.registerCallback(new ExpandIndexBar(vList));
        }
    }

    public void setAdapter(IBuildData<T> adapter) {
        if (null != adapter && !(adapter instanceof BaseAdapter)) {
            throw new IllegalStateException("param should be BaseAdapter");
        }
        this.adapter = adapter;
        onAdapterChange();
    }

    public void setDidHideGroupView(boolean didHideGroupView) {
        this.didHideGroupView = didHideGroupView;
    }

    /**
     * 设置GroupView和ChildView的最小paddingRight
     *
     * @param minPaddingRight 单位 pixels
     */
    public void setMinPaddingRight(int minPaddingRight) {
        this.minPaddingRight = minPaddingRight;
    }

    public BaseAdapter getBaseAdapter() {
        return (BaseAdapter) adapter;
    }

    public void notifyDataSetChanged() {
        BaseAdapter listApr = (BaseAdapter) vList.getAdapter();
        if (null != listApr) {
            listApr.notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setOnItemLongClickListener(
            AdapterView.OnItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    private void onAdapterChange() {
        showGroupData.clear();
        showChildData.clear();
        rebuildIndexData();

        GroupExpandableAdapter groupExpandableAdapter = new GroupExpandableAdapter();
        wrapperAdapter = new WrapperExpandableListAdapter(
                groupExpandableAdapter);
        if (didHideGroupView) {
            wrapperAdapter.setDidHideGroupView(true);
        }
        vList.setAdapter(wrapperAdapter);
        for (int i = 0; i < wrapperAdapter.getGroupCount(); i++) {
            vList.expandGroup(i);
        }
    }

    public void rebuildIndexData() {
        if (null != adapter) {
            adapter.buildData(showGroupData, showChildData);
        }
        if (null != wrapperAdapter) {
            int count = wrapperAdapter.getGroupCount();
            for (int i = 0; i < count; i++) {
                vList.expandGroup(i);
            }
        }
    }

    private class GroupExpandableAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return showGroupData.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return showChildData.get(showGroupData.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return showGroupData.get(groupPosition);
        }

        @Override
        public ChildItem<T> getChild(int groupPosition, int childPosition) {
            return showChildData.get(showGroupData.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return (long) groupPosition * getGroupCount() + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = LayoutInflater.from(context).inflate(R.layout.view_contact_item_group, parent, false);
                if (minPaddingRight > 0 && convertView.getPaddingRight() < minPaddingRight) {
                    convertView.setPadding(convertView.getPaddingLeft(), convertView.getPaddingTop(), minPaddingRight, convertView.getPaddingBottom());
                }
            }
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
            IndexBar.IndexItem indexItem = (IndexBar.IndexItem) getGroup(groupPosition);
            switch (indexItem.getType()) {
                case Text:
                    textView.setText((CharSequence) indexItem.getShowGroupValue());
                    textView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                    break;
//                case Image:
//                    imageView.setImageResource((Integer) indexItem.getShowGroupValue());
//                    textView.setVisibility(View.INVISIBLE);
//                    imageView.setVisibility(View.VISIBLE);
//                    break;
                default:
                    break;
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View view = getBaseAdapter().getView(
                    getChild(groupPosition, childPosition).getSourceIndex(),
                    convertView, parent);
            if (view.getTag() instanceof IDivider) {
                IDivider iDivider = (IDivider) view.getTag();
                if (isLastChild && groupPosition != (getGroupCount() - 1)) {
                    iDivider.hideDivider();
                } else {
                    iDivider.showDivider();
                }
            }
            if (minPaddingRight > 0 && view.getPaddingRight() < minPaddingRight) {
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                        minPaddingRight, view.getPaddingBottom());
            }
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            getBaseAdapter().registerDataSetObserver(observer);
            super.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            getBaseAdapter().unregisterDataSetObserver(observer);
            super.unregisterDataSetObserver(observer);
        }
    }

    private class ExpandIndexBar implements IndexBar.OnIndexBar {

        private final ExpandableListView listView;

        private ExpandIndexBar(ExpandableListView listView) {
            this.listView = listView;
        }

        @Override
        public void onIndexBarTouch(View indexView, MotionEvent event) {

        }

        @Override
        public void onSearchSelect() {

        }

        @Override
        public void onIndexChange(View original, View present) {
            if (null == listView) {
                return;
            }
            if (present == null) {
                return;
            }
            Object oTag = present.getTag();
            if (!(oTag instanceof IndexBar.IndexItem)) {
                return;
            }
            IndexBar.IndexItem indexItem = (IndexBar.IndexItem) oTag;
            int groupPosition = Collections.binarySearch(showGroupData, indexItem);
            if (groupPosition >= 0) { // 找到索引，定位到所在索引
                listView.setSelectedGroup(groupPosition);
            } else if (groupPosition == -1) {// 小于开头(最小)索引，定位到开头(最小)索引
                if (listView.getAdapter() != null && listView.getAdapter()
                        .getCount() > 0) {
                    listView.setSelectedGroup(0);
                }
            } else {// 未找到索引，定位到小于且最大的索引
                listView.setSelectedGroup(Math.abs(groupPosition) - 2);
            }
        }
    }

    public void setSpecialItem(View view, boolean clean) {
        if (clean) {
            clearHeader();
            vList.addHeaderView(view);
            return;
        }
        vList.addHeaderView(view);
    }

    public void clearHeader() {
        for (int i = 0; i < vList.getHeaderViewsCount(); ++i) {
            ListAdapter adapter = vList.getAdapter();
            if (null != adapter) {
                View headView = adapter.getView(i, null, null);
                vList.removeHeaderView(headView);
            }
        }
    }

    public ListView getListView() {
        return vList;
    }

    public IndexBar getIndexBar() {
        return vIndexBar;
    }

    public void shadeHideIndexBar() {
        if (vIndexBar != null) {
            vIndexBar.shadeInvisible();
        }
    }

    public void shadeShowIndexBar() {
        if (vIndexBar != null) {
            vIndexBar.shadeVisible();
        }
    }

    public int getChildPosition(T data) {
        BaseAdapter baseAdapter = getBaseAdapter();
        if (null != baseAdapter) {
            String indexName = adapter.getIndexValue(data);
            int groupPosition = positionOfGroup(indexName);
            if (groupPosition < 0) {
                return -1;
            }
            int childPosition = -1;
            IndexBar.IndexItem indexItem = showGroupData.get(groupPosition);
            List<ChildItem<T>> childItems = showChildData.get(indexItem);
            for (int i = 0; i < childItems.size(); ++i) {
                if (childItems.get(i).equals(data)) {
                    childPosition = i;
                    break;
                }
            }
            if (childPosition < 0) {
                return -1;
            }

            long packPosition = ExpandableListView
                    .getPackedPositionForChild(groupPosition, childPosition);
            return vList.getFlatListPosition(packPosition);
        }
        return -1;
    }

    private int positionOfGroup(CharSequence indexName) {
        for (int i = 0; i < showGroupData.size(); i++) {
            IndexBar.IndexItem item = showGroupData.get(i);
            if (TextUtils.equals(item.getIndexValue(), indexName)) {
                return i;
            }
        }
        return -1;
    }

    public interface IBuildData<K> {
        void buildData(List<IndexBar.IndexItem> showGroupData, Map<IndexBar.IndexItem, List<ChildItem<K>>> showChildData);

        String getIndexValue(K data);
    }

    public static class ChildItem<S> {
        private int sourceIndex;
        private S child;

        public ChildItem() {
        }

        public ChildItem(int sourceIndex, S child) {
            this.sourceIndex = sourceIndex;
            this.child = child;
        }

        public int getSourceIndex() {
            return sourceIndex;
        }

        public void setSourceIndex(int sourceIndex) {
            this.sourceIndex = sourceIndex;
        }

        public S getChild() {
            return child;
        }

        public void setChild(S child) {
            this.child = child;
        }
    }
}
