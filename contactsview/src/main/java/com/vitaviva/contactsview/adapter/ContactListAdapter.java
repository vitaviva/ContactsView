package com.vitaviva.contactsview.adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitavia.pinyin.PinyinHelper;
import com.vitaviva.contactsview.R;
import com.vitaviva.contactsview.controller.ListViewAndIndexBarController;
import com.vitaviva.contactsview.model.LocalContactModel;
import com.vitaviva.contactsview.search.filter.ITokenAdapter;
import com.vitaviva.contactsview.util.Util;
import com.vitaviva.contactsview.view.IDivider;
import com.vitaviva.contactsview.view.IndexBar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactListAdapter extends BaseAdapter
        implements ListViewAndIndexBarController.IBuildData<LocalContactModel>,
        ITokenAdapter<LocalContactModel, String[]> {

    private List<LocalContactModel> dataSource;

    private final Collection<IndexBar.IndexItem> showGroupData = new LinkedList<>();
    private final Map<IndexBar.IndexItem, List<ListViewAndIndexBarController.ChildItem<LocalContactModel>>> showChildData = new HashMap<>();

    public ContactListAdapter(List<LocalContactModel> dataSource) {
        this.dataSource = dataSource;
        buildData();
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (null == convertView) {
            holder = new Holder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_contact_item, null);
            holder.head = (ImageView) convertView.findViewById(R.id.head_image);
            holder.name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.phone = (TextView) convertView.findViewById(R.id.tv_phone);
            holder.vDiver = convertView.findViewById(R.id.diver);
            holder.check = (CheckBox) convertView.findViewById(android.R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        LocalContactModel item = (LocalContactModel) getItem(position);
        holder.name.setText(Util.stringNotNULL(item.getName()));
        holder.phone.setText(Util.stringNotNULL(item.getTelephone()));
        holder.head.setImageResource(R.drawable.ic_head_default);
        holder.check.setChecked(item.isSelected());
        loadHeadImage(parent.getContext(), holder.head, item.getRawContactId());

        convertView.setOnClickListener(v -> {
            dataSource.get(position).setSelected(!dataSource.get(position).isSelected());
            holder.check.setChecked(dataSource.get(position).isSelected());
        });
        convertView.setClickable(false);
        return convertView;
    }

    private void loadHeadImage(Context context, ImageView imageView, long id) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        imageView.setTag(id);
        executorService.execute(() -> {
            ContentResolver cr = context.getContentResolver();
            InputStream input = ContactsContract.Contacts
                    .openContactPhotoInputStream(cr, ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI, id));
            if (input != null) {
                Bitmap photo = BitmapFactory.decodeStream(input);
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.post(() -> {
                    if (imageView.getTag().equals(id)) {
                        imageView.setImageBitmap(photo);
                    }
                });
            }

        });

    }


    @Override
    public void buildData(List<IndexBar.IndexItem> showGroupData, Map<IndexBar.IndexItem, List<ListViewAndIndexBarController.ChildItem<LocalContactModel>>> showChildData) {
        showGroupData.clear();
        showChildData.clear();
        showGroupData.addAll(this.showGroupData);
        showChildData.putAll(this.showChildData);
    }

    @Override
    public String getIndexValue(LocalContactModel data) {
        return IndexBar.getIndexValue(Util.stringNotNULL(data.getPinyin()));
    }

    private void buildData() {
        showGroupData.clear();
        showChildData.clear();
        List<IndexBar.IndexItem> indexBarData = IndexBar.getContactAZTailIndexItemList();
        Collections.sort(dataSource);
        int index = 0;
        for (LocalContactModel model : dataSource) {
            String indexValue = getIndexValue(model);
            for (IndexBar.IndexItem indexItem : indexBarData) {
                if (TextUtils.equals(indexValue, indexItem.getIndexValue())) {
                    List<ListViewAndIndexBarController.ChildItem<LocalContactModel>> showChildList = showChildData.get(indexItem);
                    if (showChildList == null) {
                        showChildList = new LinkedList<>();
                        showChildData.put(indexItem, showChildList);
                    }
                    showChildList.add(new ListViewAndIndexBarController.ChildItem<>(index++, model));
                    break;
                }
            }
        }
        showGroupData.addAll(indexBarData);
        showGroupData.retainAll(showChildData.keySet());
    }

    @Override
    public String[] token(LocalContactModel item) {
        return new String[]{
                Util.stringNotNULL(item.getName()),
                Util.stringNotNULL(item.getPinyin()),
                Util.stringNotNULL(item.getTelephone())
        };
    }

    @Override
    public void updateDataSource(List<LocalContactModel> newData, CharSequence keyword) {
        if (newData != null)
            dataSource = newData;
    }

    private static class Holder implements IDivider {
        CheckBox check;
        ImageView head;
        TextView name;
        TextView phone;
        View vDiver;

        @Override
        public void showDivider() {
            if (vDiver != null) {
                vDiver.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void hideDivider() {
            if (vDiver != null) {
                vDiver.setVisibility(View.INVISIBLE);
            }
        }
    }
}
