package com.vitaviva.contactsview.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;


public class ListenPasteEditText extends android.support.v7.widget.AppCompatEditText {
    public interface PasteListener {
        /**
         * @return true：用户自行处理粘贴事件；false：继续执行TextView的粘贴事件
         */
        boolean onPaste(ListenPasteEditText v, CharSequence text);
    }

    private PasteListener pasteListener;


    public ListenPasteEditText(Context context) {
        super(context);
    }

    public ListenPasteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListenPasteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPasteListener(PasteListener pasteListener) {
        this.pasteListener = pasteListener;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean result = false;
        if (id == android.R.id.paste && pasteListener != null) {
            CharSequence charSequence = getCopyText();
            result = pasteListener.onPaste(this, charSequence);
        }
        return result || super.onTextContextMenuItem(id);
    }

    private CharSequence getCopyText() {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        if (null != primaryClip && primaryClip.getItemCount() > 0) {
            ClipData.Item itemAt = primaryClip.getItemAt(0);
            return itemAt.getText();
        }

        return null;
    }
}
