package com.example.administrator.beidoulocation.trrain;

import android.content.Context;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.administrator.beidoulocation.R;
import com.example.administrator.beidoulocation.mvp.BasePresenterImpl;

/**
 * MVPPlugin
 * 邮箱 784787081@qq.com
 */

public class TrrainPresenter extends BasePresenterImpl<TrrainContract.View> implements TrrainContract.Presenter {


    private boolean isFirstLoc=true;

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result, BaiduMap mBaiduMap, Context context) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_gcoding)));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f",
                result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(context, strInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result, BaiduMap mBaiduMap, Context context) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_gcoding)));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));
        Toast.makeText(context, result.getAddress(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLocationOnMap(LatLng ptCenter, GeoCoder mSearch, BDLocation location, BaiduMap mBaiduMap) {
//        // 反Geo搜索
//        ptCenter = new LatLng(113.666521, 34.753978);
//        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
//                .location(ptCenter));

        MyLocationData locData = new MyLocationData.Builder()
//                .accuracy(location.getRadius())准确度
                // 此处设置开发者获取到的方向信息，顺时针0-360
//                .direction(100).latitude(location.getLatitude())
//                .longitude(location.getLongitude()).build();
                .direction(100).latitude(ptCenter.latitude)
                .longitude(ptCenter.longitude).build();
        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            isFirstLoc = false;
//            LatLng ll = new LatLng(ptCenter.latitude,
//                    ptCenter.longitude);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ptCenter).zoom(18.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }


}
