package com.vitaviva.contactsview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.vitaviva.contactsview.adapter.ContactListAdapter;
import com.vitaviva.contactsview.controller.ListViewAndIndexBarController;
import com.vitaviva.contactsview.controller.LocalContactController;
import com.vitaviva.contactsview.model.LocalContactModel;
import com.vitaviva.contactsview.search.PopupSearchViewController;
import com.vitaviva.contactsview.util.Util;
import com.vitaviva.contactsview.view.IndexBar;
import com.vitaviva.contactsview.view.SearchBar;
import com.vitaviva.floatinglistview.FloatingGroupExpandableListView;

import java.util.ArrayList;
import java.util.List;

import static com.vitaviva.contactsview.ContactsViewActivity.EXTRA_ARGS;

public class ContactsViewFragment extends Fragment {

    private SearchBar searchBar;
    private FloatingGroupExpandableListView expandableListView;
    protected PopupSearchViewController popupSearchViewController;
    private ContactListAdapter adapter;
    private IndexBar indexBar;
    private View titleLeft;
    private TextView tvRight;

    private ListViewAndIndexBarController listViewAndIndexBarController;
    private List<LocalContactModel> dataSource = new ArrayList<>();
    private List<LocalContactModel> selected = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        new Thread(() -> {
            dataSource = LocalContactController.getLocalContactsFromPhone(getContext());
            getActivity().runOnUiThread(() -> {
                adapter = new ContactListAdapter(dataSource);
                listViewAndIndexBarController.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                tvRight.setText("确认(0/" + adapter.getCount() + ")");
            });
        }).start();
    }

    private AdapterView.OnItemClickListener rightBtnListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (selected.contains(dataSource.get(position))) {
                selected.remove(dataSource.get(position));
            } else {
                selected.add(dataSource.get(position));
            }
            tvRight.setText("确认(" + selected.size() + "/" + dataSource.size() + ")");
            tvRight.setEnabled(selected.size() > 0);
        }
    };

    private void initView() {
        View view = getView();
        if (view == null) return;

        //title
        titleLeft = view.findViewById(R.id.titlebar_left_img);
        titleLeft.setOnClickListener(v -> getActivity().finish());
        tvRight = (TextView) view.findViewById(R.id.titlebar_right_text);
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(EXTRA_ARGS, (ArrayList<? extends Parcelable>) selected);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });

        //view_searchbar
        searchBar = (SearchBar) view.findViewById(R.id.searchbar);
        searchBar.setHint(R.string.hint_search_contact);
        searchBar.setOnClickListener(v -> {
            if (popupSearchViewController == null) {
                popupSearchViewController = new PopupSearchViewController(getActivity());
                popupSearchViewController.setAdapter(new ContactListAdapter(dataSource));
                popupSearchViewController.setOnItemClickListener((parent, view12, position, id) -> {
                    rightBtnListener.onItemClick(null, null,
                            dataSource.indexOf(popupSearchViewController.getItem(position)), -1);
                    view12.setClickable(true);
                    view12.performClick();
                    view12.setClickable(false);//防止子view拦截listview的click事件
                });
                popupSearchViewController.setOnDismissListener(() -> adapter.notifyDataSetChanged());
            }
            popupSearchViewController.show(getView());

        });

        //listview
        expandableListView = (FloatingGroupExpandableListView) view.findViewById(android.R.id.list);
        indexBar = (IndexBar) view.findViewById(R.id.indexBar);
        listViewAndIndexBarController = new ListViewAndIndexBarController(
                view.getContext(), expandableListView, indexBar, IndexBar.getAZTailIndexItemList());
        listViewAndIndexBarController.setMinPaddingRight(Util.dip2px(getView().getContext(), 0));
        listViewAndIndexBarController.setOnItemClickListener(rightBtnListener);
    }

}
