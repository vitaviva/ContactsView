package com.vitaviva.contactsview.search.filter;

public interface FilterMatcher<T> {
    boolean match(T obj1, CharSequence obj2);
}
