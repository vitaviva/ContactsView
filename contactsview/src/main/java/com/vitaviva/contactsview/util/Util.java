package com.vitaviva.contactsview.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Collection;
import java.util.Map;

public class Util {

    public static int stringCompare(String l, String r) {
        if (l == null) {
            return r == null ? 0 : 1;
        } else {
            return r == null ? 1 : l.compareTo(r);
        }
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String stringNotNULL(String string) {
        return TextUtils.isEmpty(string) ? "" : string;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return null == collection || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    public static boolean isEmpty(Object[] array) {
        return null == array || array.length == 0;
    }

    static public final class SoftKeyboardUtil {
        public static final String TAG = "SoftKeyboardUtil";

        private SoftKeyboardUtil() {
        }

        public static ViewTreeObserver.OnGlobalLayoutListener addOnSoftKeyBoardVisibleListener(
                Activity activity, final OnSoftKeyBoardVisibleListener listener) {
            final View decorView = activity.getWindow().getDecorView();
            ViewTreeObserver.OnGlobalLayoutListener newListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect rect = new Rect();
                    decorView.getRootView().getWindowVisibleDisplayFrame(rect);
                    int displayHight = rect.bottom;
                    int hight = decorView.getHeight();
                    boolean visible = (double) displayHight / hight < 0.8;
                    listener.onSoftKeyBoardVisible(visible);
                }
            };
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(newListener);
            return newListener;
        }

        //    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public static void removeOnSoftKeyBoardVisibleListener(
                Activity activity, ViewTreeObserver.OnGlobalLayoutListener listener) {
            View decorView = activity.getWindow().getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                decorView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            } else {
                decorView.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
            }
        }

        public static void hideSoftKeyBoard(View v) {
            if (v == null) {
                return;
            }
            InputMethodManager inputMethodManager = getInputMethodManager(v);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }

        public static void toggleSoftKeyBoard(View v) {
            InputMethodManager inputMethodManager = getInputMethodManager(v);
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }

        public static void showSoftKeyBoard(View v) {
            InputMethodManager inputMethodManager = getInputMethodManager(v);
            inputMethodManager.showSoftInput(v, 0);
        }

        public static InputMethodManager getInputMethodManager(View v) {
            return (InputMethodManager) v.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        public static void insertTextInCurrentPosition(EditText tv, String str) {
            if (tv == null) {
                return;
            }
            String str1 = str;
            if (str1 == null) {
                str1 = "";
            }
            int start = tv.getSelectionStart();
            int end = tv.getSelectionEnd();
            String currentStr = tv.getEditableText().toString();
            String pre = currentStr.substring(0, start);
            String post = currentStr.substring(end);
            StringBuilder sb = new StringBuilder();
            sb.append(pre).append(str1);
            int index = sb.length();
            sb.append(post);
            tv.setText(sb.toString());
            tv.setSelection(index);
        }

        public static boolean isSoftKeyboardShown(Activity activity, int topId) {
            View rootView = activity.getWindow().getDecorView();
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            int displayHeight = rect.bottom;
            int height = rootView.getHeight();
            return (double) displayHeight / height < 0.8;
        }

        public interface OnSoftKeyBoardVisibleListener {
            void onSoftKeyBoardVisible(boolean visible);
        }
    }
}
