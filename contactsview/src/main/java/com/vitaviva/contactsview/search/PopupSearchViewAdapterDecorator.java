package com.vitaviva.contactsview.search;

import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.widget.Filter;

import com.vitaviva.contactsview.search.filter.FMStringArrayWithStringFuzzy;
import com.vitaviva.contactsview.search.filter.FilterMatcher;
import com.vitaviva.contactsview.search.filter.ITokenAdapter;
import com.vitaviva.contactsview.util.Util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * BaseAdapter的包装类，提供了Filter的功能。
 * 请确保数据不被改变，否则导致线程非安全。 若数据一定要改变的话，那请重新创建一个Decorator。
 */
public abstract class PopupSearchViewAdapterDecorator<T> {

    private final ITokenAdapter adapter;

    private final Collection<T> dataSource = new LinkedList<>(); // main thread

    private List<T> unFilteredData; // work thread

    private List<T> filteredData; // work thread

    private Filter filter;

    private FilterMatcher filterMatcher = new FMStringArrayWithStringFuzzy();

    public PopupSearchViewAdapterDecorator(ITokenAdapter adapter) {
        this.adapter = adapter;

        BaseAdapter baseAdapter = (BaseAdapter) adapter;
        for (int i = 0; i < baseAdapter.getCount(); ++i) {
            T item = (T) baseAdapter.getItem(i);
            dataSource.add(item);
        }
    }

    public ITokenAdapter getAdapter() {
        return adapter;
    }

    public BaseAdapter getBaseAdapter() {
        return (BaseAdapter) adapter;
    }

    public Filter getFilter() {
        return null == filter ? filter = new TokenFilter() : filter;
    }

    protected abstract void updateFilteredDataSource(ITokenAdapter adapter, List<T> dataSource,
                                                     CharSequence keyword);

    public class TokenFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (null == unFilteredData) {
                unFilteredData = new LinkedList<>(dataSource); // only first time.
            }
            if (null == filteredData) {
                filteredData = new LinkedList<>();
            }

            FilterResults filterResults = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.values = new LinkedList();
                filterResults.count = 0;
                unFilteredData.addAll(filteredData);
                filteredData.clear();
            } else {
                Collection<T> result = new LinkedList<>();
                if (filteredData.size() != 0) {
                    for (int i = 0; i < filteredData.size(); ++i) {
                        T item = filteredData.get(i);
                        Object tokens = adapter.token(item);
                        if (null == tokens || null != filterMatcher && filterMatcher
                                .match(tokens, constraint)) {
                            result.add(item);
                        } else {
                            unFilteredData.add(item);
                        }
                    }
                } else {
                    for (int i = unFilteredData.size() - 1; i >= 0; --i) {
                        T item = unFilteredData.get(i);
                        Object tokens = adapter.token(item);
                        if (null == tokens || null != filterMatcher && filterMatcher
                                .match(tokens, constraint)) {
                            result.add(item);
                            unFilteredData.remove(i);
                        }
                    }
                }

                filteredData.clear();
                filteredData.addAll(result);
                filterResults.values = result;
                filterResults.count = result.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (dataSource.size() != 0) {
                dataSource.clear();
            }
            List<T> values = (List<T>) results.values;
            updateFilteredDataSource(adapter, values, constraint);
            if (!Util.isEmpty(values)) {
                ((BaseAdapter) adapter).notifyDataSetInvalidated();
            } else {
                ((BaseAdapter) adapter).notifyDataSetChanged();
            }
        }
    }

}
