package com.example.administrator.beidoulocation.home;

import android.content.Context;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.example.administrator.beidoulocation.mvp.BasePresenter;
import com.example.administrator.beidoulocation.mvp.BaseView;

/**
 * MVPPlugin
 *  邮箱 784787081@qq.com
 */

public class HomeContract {
    interface View extends BaseView {

    }

    interface  Presenter extends BasePresenter<View> {
         public void updataLocation(TextView tv1, TextView tv2, BDLocation location, Context context);
        public String getSunraiseSunsetTime(double latitude, double longitude);

    }
}
