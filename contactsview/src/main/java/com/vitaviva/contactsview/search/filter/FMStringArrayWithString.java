package com.vitaviva.contactsview.search.filter;

import com.vitaviva.contactsview.util.Util;

public class FMStringArrayWithString implements FilterMatcher<String[]> {

    @Override
    public boolean match(String[] obj1, CharSequence obj2) {
        if (!Util.isEmpty(obj1)) {
            for (String item : obj1) {
                if (matchString(item, obj2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchString(String str1, CharSequence str2) {
        return str1.contains(str2);
    }
}
