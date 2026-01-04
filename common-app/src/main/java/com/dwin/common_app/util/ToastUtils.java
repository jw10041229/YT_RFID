package com.dwin.common_app.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void showShort(Context context, String content){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }
}
