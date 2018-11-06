package com.fooww.videorecord.util;

import android.content.Context;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * 系统版本信息类
 * 
 * @author tangjun
 * 
 */
public class DeviceUtils {


    public static int dipToPX(final Context ctx, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, ctx.getResources().getDisplayMetrics());
    }

    /** 获取屏幕宽度 */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getWidth();
    }

    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getHeight();
    }
}
