package com.fooww.videorecord;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import com.fooww.videorecord.util.DeviceUtils;
import com.fooww.weixinrecorded.R;

public abstract class BaseActivity extends AppCompatActivity {

    public AlertDialog progressDialog;
    public Activity mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);

            View view = View.inflate(this, R.layout.dialog_loading, null);
            builder.setView(view);
            ProgressBar pb_loading = view.findViewById(R.id.pb_loading);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pb_loading.setIndeterminateTintList(ContextCompat.getColorStateList(this, R.color.dialog_pro_color));
            }
            progressDialog = builder.create();
        }
        progressDialog.show();
        progressDialog.getWindow().setGravity(Gravity.CENTER);
        WindowManager.LayoutParams layoutParams = progressDialog.getWindow().getAttributes();
        layoutParams.height = DeviceUtils.dipToPX(this, 100f);
        layoutParams.width = DeviceUtils.dipToPX(this, 100f);
        progressDialog.getWindow().setAttributes(layoutParams);
        progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_loading);

    }

    public void closeProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
