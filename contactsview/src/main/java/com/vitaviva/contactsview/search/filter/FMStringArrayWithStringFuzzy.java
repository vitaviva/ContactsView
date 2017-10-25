
package com.vitaviva.contactsview.search.filter;

import android.text.TextUtils;
import android.util.Pair;

import com.vitaviva.contactsview.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FMStringArrayWithStringFuzzy extends FMStringArrayWithStringIgnoreCase {

    @Override
    protected boolean onMatch(String item, CharSequence obj2) {
        return !matchOnFuzzy(item, obj2).isEmpty();
    }

    /**
     * 用于搜索弹窗的过滤器
     */
    public static List<Pair<Integer, CharSequence>> matchOnFuzzy(String item, CharSequence obj2) {
        if (TextUtils.isEmpty(item) || TextUtils.isEmpty(obj2)) {
            return Collections.emptyList();
        }
        String[] blocks = blockToken(item);
        int tokenCursor = 0;
        List<Pair<Integer, CharSequence>> indexes = new LinkedList<>();
        String key = obj2.toString().replaceAll("\\s", "");
        for (int i = 0; i < blocks.length; ++i) {
            String block = blocks[i];
            for (int j = key.length(); j > 0; --j) {
                if (tokenCursor >= j) {
                    continue;
                }
                CharSequence partToken = key.subSequence(tokenCursor, j);
                if (block.startsWith(partToken.toString())) {
                    tokenCursor = j;
                    indexes.add(new Pair<>(i, partToken));
                    if (tokenCursor == key.length()) {
                        return indexes;
                    }
                    break;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 多音字匹配用
     */
    public static List<Pair<Integer, CharSequence>> matchOnFuzzy(String[] item, CharSequence obj2) {
        if (item == null || item.length <= 0 || TextUtils.isEmpty(obj2)) {
            return Collections.emptyList();
        }
        String[][] blocks = new String[item.length][];
        for (int i = 0; i < item.length; i++) {
            blocks[i] = blockToken(item[i]);
        }
        List<Pair<Integer, CharSequence>> indexes = new LinkedList<>();
        Collection<Pair<Integer, CharSequence>> temp = new LinkedList<>();
        String key = obj2.toString().replaceAll("\\s", "");
        for (String[] b : blocks) {
            temp.clear();
            boolean flag = false;
            int tokenCursor = 0;
            for (int i = 0; i < b.length; ++i) {
                String block = b[i];
                for (int j = key.length(); j > 0; --j) {
                    if (tokenCursor >= j) {
                        continue;
                    }
                    CharSequence partToken = key.subSequence(tokenCursor, j);
                    if (block.startsWith(partToken.toString())) {
                        tokenCursor = j;
                        temp.add(new Pair<>(i, partToken));
                        if (tokenCursor == key.length()) {
                            flag = true;
                        }
                        break;
                    }
                }
                if (flag) {
                    break;
                }
            }
            if (temp.size() > indexes.size()
                    || temp.size() == indexes.size() && !has0Index(indexes)) {
                indexes.clear();
                indexes.addAll(temp);
            }
        }
        return indexes;
    }

    /**
     * 确保从第一个字开始高亮 如：单车,输入c时，保证“单”高亮（“单”也念“chan2”），而不是“车”
     */
    private static boolean has0Index(Collection<Pair<Integer, CharSequence>> indexes) {
        if (!Util.isEmpty(indexes)) {
            return false;
        }
        for (Pair<Integer, CharSequence> index : indexes) {
            if (index.first == 0) {
                return true;
            }
        }
        return false;
    }

    public static String[] blockToken(String item) {
        return item.split("\\s+");
    }
}
