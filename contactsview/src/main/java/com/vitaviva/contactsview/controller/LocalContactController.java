package com.vitaviva.contactsview.controller;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.util.LongSparseArray;

import com.vitaviva.contactsview.model.LocalContactModel;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class LocalContactController {

    public static List<LocalContactModel> getLocalContactsFromPhone(Context context) {
        List<LocalContactModel> result = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver()
                    .query(ContactsContract.RawContacts.CONTENT_URI,
                            new String[]{
                                    ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                                    ContactsContract.RawContacts._ID,
                                    ContactsContract.RawContacts.VERSION
                            },
                            ContactsContract.RawContacts.DELETED + " = 0",
                            null,
                            null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    long rawContactId = cursor.getLong(1);
                    int version = cursor.getInt(2);
                    result.add(newLocalContactModel(rawContactId, name, version, null));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        setTelephones(context, result);
        return result;
    }

    private static void setTelephones(Context context, List<LocalContactModel> contacts) {
        StringBuilder stringBuilder = new StringBuilder();
        for (LocalContactModel item : contacts) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(',');
            }
            stringBuilder.append(item.getRawContactId());
        }

        Cursor c = null;
        LongSparseArray<List<String>> telephoneMap = new LongSparseArray<>();
        try {
            c = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " in ( " + stringBuilder + " )",
                    null,
                    null
            );
            if (c != null) {
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    String telephone = c.getString(1);
                    List<String> telephones = telephoneMap.get(id);
                    if (telephones == null) {
                        telephones = new LinkedList<>();
                        telephoneMap.put(id, telephones);
                    }
                    telephones.add(telephone);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        ListIterator<LocalContactModel> iterator = contacts.listIterator();
        while (iterator.hasNext()) {
            LocalContactModel item = iterator.next();
            List<String> telephones = telephoneMap.get(item.getRawContactId());
            if (telephones == null) {
                continue;
            }
            for (String str : telephones) {
                if (item.getTelephone() == null) {
                    item.setTelephone(str);
                } else {
                    iterator.add(newLocalContactModel(item.getRawContactId(), item.getName(), item.getVersion(), str));
                }
            }
        }
    }

    private static LocalContactModel newLocalContactModel(long rawId, String name, int version, String telephone) {
        LocalContactModel model = LocalContactModel.newInstance();
        model.setRawContactId(rawId);
        model.setName(name);
        model.setVersion(version);
        model.setTelephone(telephone);
        return model;
    }

}
