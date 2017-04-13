package com.example.administrator.beidoulocation.offlinemap;


import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.example.administrator.beidoulocation.MainActivity;
import com.example.administrator.beidoulocation.R;
import com.example.administrator.beidoulocation.listener.MyLocationListenner;
import com.example.administrator.beidoulocation.mvp.MVPBaseFragment;

/**
 * MVPPlugin
 *  邮箱 784787081@qq.com
 */

public class OfflineMapFragment extends MVPBaseFragment<OfflineMapContract.View, OfflineMapPresenter>
        implements OfflineMapContract.View {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;

    @Override
    public int getLayoutResId() {
        return R.layout.offlinemap_fragment;
    }

    @Override
    public void initView(View view) {
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationConfigeration(myLocationConfiguration);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        //         开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(getActivity());
        MyLocationListenner bdLocationListener = new MyLocationListenner(mMapView, mBaiduMap);
        bdLocationListener.setOnLocationListener(new MyLocationListenner.LocationData() {
            @Override
            public void getLocationData(BDLocation location) {
                ((MainActivity)  context).onChangeData(location);
            }
        });
        mLocClient.registerLocationListener(bdLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
//        mMapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
//        mMapView.onPause();
    }

}
