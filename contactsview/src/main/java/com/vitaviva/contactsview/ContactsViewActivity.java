package com.vitaviva.contactsview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.vitavia.pinyin.PinyinHelper;

public class ContactsViewActivity extends FragmentActivity {

    public final static int REQUEST_CODE = 1;
    public final static String EXTRA_ARGS = "contactList";

    private static boolean load = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!load) {
            PinyinHelper.initialize(this);
            load = true;
        }
        setContentView(R.layout.activity_contact_view);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content, new ContactsViewFragment()).commit();
    }
}
