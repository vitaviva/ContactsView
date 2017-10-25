package com.vitaviva.contactsview.search.filter;

import android.text.TextUtils;

import com.vitaviva.contactsview.util.Util;

public class FMStringArrayWithStringIgnoreCase implements FilterMatcher<String[]> {

    @Override
    public boolean match(String[] obj1, CharSequence obj2) {
        if (!Util.isEmpty(obj1)) {
            for (String item : obj1) {
                if (matchStringIgnoreCase(item, obj2)) {
                    return true;
                }
                if (onMatch(Util.stringNotNULL(item).toLowerCase(),
                        Util.stringNotNULL(obj2.toString()).toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean onMatch(String item, CharSequence obj2) {
        return false;
    }

    private static boolean matchStringIgnoreCase(String str1, CharSequence str2) {
        return !TextUtils.isEmpty(str1) && str1.toLowerCase()
                .contains(str2.toString().toLowerCase());
    }
}
