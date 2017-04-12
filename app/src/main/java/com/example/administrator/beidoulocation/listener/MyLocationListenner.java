package com.example.administrator.beidoulocation.listener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 *
 */

public class MyLocationListenner implements BDLocationListener {
    MapView mMapView;
    BaiduMap mBaiduMap;
    private boolean isFirstLoc=false;

    public MyLocationListenner(MapView mMapView, BaiduMap mBaiduMap) {
        this.mMapView = mMapView;
        this.mBaiduMap = mBaiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        // map view 销毁后不在处理新接收的位置
        if (location == null || mMapView == null) {
            return;
        }
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        if (locationData != null) {
            locationData.getLocationData(location);
        }
        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    public void onReceivePoi(BDLocation poiLocation) {

    }

    public interface  LocationData{
         void getLocationData(BDLocation location);
    }

    public LocationData locationData;
    public  void setOnLocationListener(LocationData locationData){
        this.locationData = locationData;
    }

}
