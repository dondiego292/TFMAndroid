package net.geomovil.gestor.util;

import android.text.TextUtils;

public class TextHelper {
    public static boolean isEmptyData(String data){
        return TextUtils.isEmpty(data) || data.equals("null")
                || data.equals("0.0");
    }
}
