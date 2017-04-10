package com.example.administrator.beidoulocation.trrain;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.administrator.beidoulocation.mvp.BasePresenter;
import com.example.administrator.beidoulocation.mvp.BaseView;

/**
 * MVPPlugin
 *  邮箱 784787081@qq.com
 */

public class TrrainContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter<View> {

        public void onGetGeoCodeResult(GeoCodeResult result, BaiduMap mBaiduMap, Context context);

        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result, BaiduMap mBaiduMap, Context context);

        public void showLocationOnMap(LatLng ptCenter, GeoCoder mSearch, BDLocation location, BaiduMap mBaiduMap);

    }
}
