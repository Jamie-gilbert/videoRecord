package com.fooww.videorecord.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.fooww.weixinrecorded.R;

/**
 * @author ggg
 * @version 1.0
 * @date 2018/10/26 9:54
 * @description
 */
public class ToastUtils {
    public static void showWarns(Context context, String str) {
        Toast warnToast = new Toast(context);
        warnToast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, DeviceUtils.dipToPX(context, 130)); // Toast显示的位置
        warnToast.setDuration(Toast.LENGTH_SHORT); // Toast显示的时间
        TextView textView = new TextView(context);
        textView.setPadding(DeviceUtils.dipToPX(context, 10), DeviceUtils.dipToPX(context, 10),
                DeviceUtils.dipToPX(context, 10), DeviceUtils.dipToPX(context, 10));
        textView.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_toast_record));
        textView.setTextColor(ContextCompat.getColor(context,R.color.white));
        textView.setText(String.valueOf(str));
        warnToast.setView(textView);
        warnToast.show();
    }
}
