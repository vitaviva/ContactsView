package com.vitaviva.contactsview.search.filter;

import java.util.List;

/**
 * Token 检索及更新的能力。
 */
public interface ITokenAdapter<T, S> {
    /**
     * 检索的Token.
     */
    S token(T item);

    /**
     * 筛选后的数据,要更新adapter数据。 若需要, 可以选择在此时进行sort。
     */
    void updateDataSource(List<T> newData, CharSequence keyword);

}
