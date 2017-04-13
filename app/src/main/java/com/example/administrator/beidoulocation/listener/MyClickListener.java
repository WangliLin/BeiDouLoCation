package com.example.administrator.beidoulocation.listener;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.administrator.beidoulocation.R;
import com.example.administrator.beidoulocation.offlinemap.DownloadOfflineMap;

/**
 * Created by 瑜哥 on 2017/4/13.
 */

public class MyClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ofl_map:
                Context context = v.getContext();
                Intent intent =new Intent(context, DownloadOfflineMap.class);
                context.startActivity(intent);
                break;
            case R.id.rl_version:
                break;
            case R.id.rl_setting:
                break;
            case R.id.civ_user_head:
                break;
        }
    }
}
