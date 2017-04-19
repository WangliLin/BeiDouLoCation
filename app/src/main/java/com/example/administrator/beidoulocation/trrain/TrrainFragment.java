package com.example.administrator.beidoulocation.trrain;


import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.administrator.beidoulocation.MainActivity;
import com.example.administrator.beidoulocation.R;
import com.example.administrator.beidoulocation.enum_obj.MapType;
import com.example.administrator.beidoulocation.listener.MyLocationListenner;
import com.example.administrator.beidoulocation.mvp.MVPBaseFragment;



/**
 * MVPPlugin
 *
 */

public class TrrainFragment extends MVPBaseFragment<TrrainContract.View, TrrainPresenter>
        implements TrrainContract.View, OnGetGeoCoderResultListener {
    GeoCoder mSearch = null;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;

    @Override
   public int getLayoutResId() {
        return R.layout.terrain_fragment;
    }

    @Override
    public void initView(View view) {
        mMapView = (MapView) view.findViewById(R.id.bmapView1);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);//BaiduMap.MAP_TYPE_NORMAL
        mBaiduMap.setMyLocationConfigeration(myLocationConfiguration);
    }

    @Override
    public void initListener() {
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    @Override
    public void initData() {
//         开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(getActivity());
        ((MainActivity)  context).setOnMapTypeChangeListener(new MainActivity.MapTypeListener() {
            @Override
            public void onMapTypeChangeListener(int type) {
                if (type == MapType.OFFLINE_MAP.mapCode) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                } else {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
            }
        });
        MyLocationListenner bdLocationListener = new MyLocationListenner(mMapView, mBaiduMap);
        bdLocationListener.setOnLocationListener(new MyLocationListenner.LocationData() {
            @Override
            public void getLocationData(final BDLocation location) {
                ((MainActivity)  context).onChangeData(location);
            }
        });
        mLocClient.registerLocationListener(bdLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1);//定位次数
        mLocClient.setLocOption(option);
//         设置中心点  // 纬经度//120.093018,30.311045//浙江大学
//        MapStatusUpdate centerMapStateusUpdate = MapStatusUpdateFactory.newLatLng(new LatLng(120.093018,30.311045));
//        mBaiduMap.setMapStatus(centerMapStateusUpdate);
//        设置缩放级别
        MapStatusUpdate zoomMapStateusUpdate = MapStatusUpdateFactory.zoomTo(15);
        mBaiduMap.setMapStatus(zoomMapStateusUpdate);
        mLocClient.start();
        //实现MainActivity地图显示的接口
        ((MainActivity)  context).useLatLngOnMap(new MainActivity.ShowLocationFunction() {
            @Override
            public void showLatLngOnMap(LatLng latLng) {
                showLocationOnMap(latLng);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
//        mMapView.onDestroy();
//        mLocClient.stop();
    }
    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
//        mMapView.onResume();
//        mLocClient.start();
    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
//        mMapView.onPause();
//        mLocClient.stop();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        mPresenter.onGetGeoCodeResult(result,mBaiduMap,context);
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        mPresenter.onGetReverseGeoCodeResult(result,mBaiduMap,context);
    }

    public void showLocationOnMap(LatLng latLng){
        //120.093018,30.311045//浙江大学
//       LatLng latLng=new LatLng(30.311045d,120.093018d);
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(latLng));
        BDLocation bdLocation = new BDLocation();
        bdLocation.setLatitude(latLng.latitude);
        bdLocation.setLongitude(latLng.longitude);
        ((MainActivity)  context).onChangeData(bdLocation);
    }
}
