package com.vitaviva.contactsview.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.vitavia.pinyin.PinyinHelper;
import com.vitaviva.contactsview.util.Util;

public class LocalContactModel implements Parcelable, Cloneable, Comparable<LocalContactModel> {

    private long rawContactId;
    private String name;
    private String telephone;
    private int version;
    private String pinyin;

    private LocalContactModel() {
    }

    protected LocalContactModel(Parcel in) {
        rawContactId = in.readLong();
        name = in.readString();
        telephone = in.readString();
        version = in.readInt();
        pinyin = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<LocalContactModel> CREATOR = new Creator<LocalContactModel>() {
        @Override
        public LocalContactModel createFromParcel(Parcel in) {
            return new LocalContactModel(in);
        }

        @Override
        public LocalContactModel[] newArray(int size) {
            return new LocalContactModel[size];
        }
    };

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {

        return selected;
    }

    private boolean selected;

    public static LocalContactModel newInstance() {
        return new LocalContactModel();
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Nullable
    @Override
    protected LocalContactModel clone() {
        try {
            return (LocalContactModel) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int compareTo(@NonNull LocalContactModel another) {
        if (TextUtils.isEmpty(pinyin)) {
            pinyin = PinyinHelper.getSinglePinyin(Util.stringNotNULL(name));
        }
        if (TextUtils.isEmpty(another.getPinyin())) {
            another.setPinyin(PinyinHelper.getSinglePinyin(Util.stringNotNULL(another.name)));
        }
        return Util.stringCompare(
                Util.stringNotNULL(another.pinyin).toLowerCase(),
                Util.stringNotNULL(pinyin).toLowerCase());
    }

    @Override
    public String toString() {
        return "LocalContactModel{" +
                "rawContactId=" + rawContactId +
                ", name='" + name + '\'' +
                ", telephone='" + telephone + '\'' +
                ", version=" + version +
                ", pinyin='" + pinyin + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(rawContactId);
        dest.writeString(name);
        dest.writeString(telephone);
        dest.writeInt(version);
        dest.writeString(pinyin);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}
