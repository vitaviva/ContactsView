package com.vitaviva.contactsview.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vitaviva.contactsview.R;
import com.vitaviva.contactsview.search.filter.ITokenAdapter;
import com.vitaviva.contactsview.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索条
 * <pre>
 * 1. 根据输入框中的内容与数据的关键字是否匹配进行筛选, 支持自定义匹配算法
 * 2. 某些条件下为线程安全. 条件为若数据需要变化，请重新创建Adapter并set.
 * 3. 为保证线程安全，除ITokenAdapter#updateDataSource以外，不要对adapter的数据进行动态改变。
 * 4. 不确保数据显示顺序，若需要顺序显示请自行在ITokenAdapter#updateDataSource中进
 * 中排序
 * </pre>
 */
public class PopupSearchViewController {

    Activity context;
    private final int mWindowSoftInputMode;

    protected PopupWindow vSearchWindow;
    protected View vContainView;
    protected EditText vInput;
    protected View vCancel;
    protected View vClear;
    protected View vContentFrame;
    protected View vProgressFrame;
    protected View vProgress;
    protected View vProgressInfo;
    protected ListView vList;
    protected ViewGroup vListEmpty;

    private PopupSearchViewAdapterDecorator popupSearchViewAdapterDecorator;

    private AdapterView.OnItemClickListener itemClickListener;

    private View.OnClickListener searchClickListener;

    private PopupWindow.OnDismissListener dismissListener;

    private OnShowListener onShowListener;

    private boolean filterEnable = true;

    private boolean isInitView;

    private final Filter.FilterListener filterListener = new Filter.FilterListener() {
        @Override
        public void onFilterComplete(int count) {
            vProgressFrame.setVisibility(View.GONE);
            if (count == 0) {
                if (vContentFrame.getVisibility() == View.VISIBLE && TextUtils
                        .isEmpty(vInput.getText())) {
                    vContentFrame.setVisibility(View.GONE);
                }
            } else {
                vContentFrame.setVisibility(View.VISIBLE);
            }
        }
    };

    public PopupSearchViewController(Activity context) {
        this.context = context;
        mWindowSoftInputMode = context.getWindow().getAttributes().softInputMode;
    }

    public PopupSearchViewController initViewAndListenerIfNeed() {
        if (!isInitView) {
            isInitView = true;
            initView(context);
            initViewListener();

            vContentFrame.setVisibility(View.GONE);
            vProgressFrame.setVisibility(View.GONE);
        }
        return this;
    }

    private void initView(Context context) {
        vContainView = View.inflate(context, R.layout.view_popup_search, null);
        vSearchWindow = new PopupWindow(vContainView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        vSearchWindow.setFocusable(true);
        vSearchWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        vSearchWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        vSearchWindow.setBackgroundDrawable(new BitmapDrawable());

        vInput = (EditText) vContainView.findViewById(R.id.popup_search_input);
        vClear = vContainView.findViewById(R.id.popup_search_clear);
        vCancel = vContainView.findViewById(R.id.search_cancle);
        vContentFrame = vContainView.findViewById(R.id.popup_search_content_frame);
        vProgressFrame = vContainView.findViewById(R.id.popup_search_content_progress_frame);
        vProgress = vContainView.findViewById(R.id.popup_search_content_progress);
        vList = (ListView) vContainView.findViewById(R.id.popup_search_content_list);
        vProgressInfo = vContainView.findViewById(R.id.popup_search_content_progress_info);

        vListEmpty = (ViewGroup) vContainView.findViewById(R.id.result_empty_frame);
        vListEmpty.setVisibility(View.GONE);
        vList.setEmptyView(vListEmpty);

        onInitView();
    }


    protected void onInitView() {

    }

    public Object getItem(int position) {
        if (vList != null) {
            return vList.getAdapter().getItem(position);
        }
        return null;
    }

    private void initViewListener() {
        vSearchWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                vInput.clearFocus();
                context.getWindow().setSoftInputMode(mWindowSoftInputMode);
                if (null != popupSearchViewAdapterDecorator) {
                    popupSearchViewAdapterDecorator.getBaseAdapter().notifyDataSetChanged();
                }
                if (null != dismissListener) {
                    dismissListener.onDismiss();
                }
            }
        });

        vCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputMethod();
                vSearchWindow.dismiss();
            }
        });

        vClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        vInput.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String copy = "复制";
                final List<String> list = new ArrayList<>();
                if (!TextUtils.isEmpty(vInput.getText().toString())) {
                    list.add(copy);
                }
                final String paste = "粘贴";
                list.add(paste);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(list.toArray(new String[list.size()]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (copy.equals(list.get(which))) {
                                    vInput.onTextContextMenuItem(android.R.id.selectAll);
                                    vInput.onTextContextMenuItem(android.R.id.copy);
                                } else if (paste.equals(list.get(which))) {
                                    vInput.onTextContextMenuItem(android.R.id.paste);
                                }
                            }
                        }).show();
                return false;
            }
        });

        vInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideInputMethod();
                    //返回false继续触发onKey
                }
                return false;
            }
        });

        vInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent
                        .getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (null != searchClickListener) {
                        searchClickListener.onClick(view);
                    }
                    return true;
                }
                return false;
            }
        });

        vInput.addTextChangedListener(new TextWatcher() {
            CharSequence lastText;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastText = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (vContentFrame.getVisibility() != View.VISIBLE) {
                    vContentFrame.setVisibility(View.VISIBLE);
                }
                vProgressFrame.setVisibility(View.VISIBLE);
                if (null != popupSearchViewAdapterDecorator) {
                    popupSearchViewAdapterDecorator.getFilter()
                            .filter(s.toString(), filterListener);
                } else {
                    vProgressFrame.setVisibility(View.GONE);
                }

                vClear.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }
        });

        vList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != itemClickListener) {
                    itemClickListener.onItemClick(parent, view, position, id);
                }
            }
        });
    }

    public void clear() {
        if (!isInitView) {
            return;
        }
        vInput.setText("");
        vProgressFrame.setVisibility(View.GONE);
        vContentFrame.setVisibility(View.GONE);
    }

    /**
     * 销毁View
     */
    public void destroy() {
        hideInputMethod();
        if (!isInitView) {
            return;
        }
        vSearchWindow.dismiss();
    }

    /**
     * 显示popupwindow
     */
    public void show(View parentView) {
        initViewAndListenerIfNeed();
        //        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (!context.isFinishing() && !vSearchWindow.isShowing()) {
            vSearchWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
            vInput.requestFocus();
        }
        if (null != onShowListener) {
            onShowListener.onShow();
        }
    }

    /**
     * 设置数据源
     *
     * @param adapter 应为BaseAdapter的子类。
     */
    public <T, S> void setAdapter(ITokenAdapter<T, S> adapter) {
        if (null != adapter && !(adapter instanceof BaseAdapter)) {
            throw new IllegalStateException("param should be BaseAdapter");
        }
        initViewAndListenerIfNeed();
        vList.setAdapter((ListAdapter) adapter);

        if (null != adapter && filterEnable) {
            popupSearchViewAdapterDecorator = new PopupSearchViewAdapterDecorator<T>(adapter) {
                @Override
                protected void updateFilteredDataSource(ITokenAdapter adapter, List<T> dataSource,
                                                        CharSequence keyword) {
                    if (null != adapter) {
                        adapter.updateDataSource(dataSource, keyword);
                    }
                }
            };
            // 这里为了更新Adapter的话。要filter一下，否则有可能显示的是之前的adapter发出的filter数据。
            // 因为adapter中都有自己的filter.所以不用担心filter中workThread的数据会为脏数据。:)
            popupSearchViewAdapterDecorator.getFilter().filter(vInput.getText(), filterListener);
        } else {
            popupSearchViewAdapterDecorator = null;
        }
    }

    public void setListDivider(int srcId) {
        initViewAndListenerIfNeed();
        vList.setDivider(context.getResources().getDrawable(srcId));
    }

    public void setBackgroundColor(int color) {
        initViewAndListenerIfNeed();
        vContentFrame.setBackgroundColor(color);
        vList.setBackgroundColor(color);
    }

    public void setEmptyVisible(boolean visible) {
        View defaultEmptyView = getDefaultEmptyView();
        if (null != defaultEmptyView) {
            defaultEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setText(CharSequence sequence) {
        initViewAndListenerIfNeed();
        vInput.setText(null == sequence ? "" : sequence);
        vInput.setSelection(null == sequence ? 0 : sequence.length());
    }

    public String getText() {
        initViewAndListenerIfNeed();
        return vInput.getText().toString();
    }

    public void hideInputMethod() {
        initViewAndListenerIfNeed();
        Util.SoftKeyboardUtil.hideSoftKeyBoard(vInput);
    }

    /**
     * 设置点击事件。
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public void setOnSearchClickListener(View.OnClickListener clickListener) {
        searchClickListener = clickListener;
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    public void setHintText(int resid) {
        initViewAndListenerIfNeed();
        vInput.setHint(resid);
    }

    public void setEnableFilter(boolean enable) {
        filterEnable = enable;
    }

    /**
     * 获取过滤器包装者,可以改变Filter策略
     */
    public PopupSearchViewAdapterDecorator getDecorator() {
        return popupSearchViewAdapterDecorator;
    }

    public void addEmptyView(View view, SearchResultCallback callback) {
        initViewAndListenerIfNeed();
        vListEmpty.addView(view);
    }

    public View getDefaultEmptyView() {
        initViewAndListenerIfNeed();
        return vListEmpty.findViewById(R.id.emptyText);
    }

    public interface OnShowListener {
        void onShow();
    }

    public interface SearchResultCallback {
        void onSearchComplite(View view, CharSequence str, int count);
    }
}
