package com.lansosdk.videoeditor;

import android.content.Context;
import android.content.res.AssetManager;

public class LanSoEditor {
    private static boolean isLoaded = false;

    public static void initSDK(Context context) {
        loadLibraries();
        VideoEditor.logEnable(context);
    }

    private static synchronized void loadLibraries() {
        if (isLoaded)
            return;
        System.loadLibrary("LanSongffmpeg");

        isLoaded = true;
    }

    public static native void nativeInit(Context ctx, AssetManager ass, String filename);

    public static native void nativeUninit();


}
