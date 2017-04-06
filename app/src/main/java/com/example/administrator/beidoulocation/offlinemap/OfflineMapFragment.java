package com.example.administrator.beidoulocation.offlinemap;


import android.view.View;

import com.baidu.mapapi.map.MapView;
import com.example.administrator.beidoulocation.R;
import com.example.administrator.beidoulocation.mvp.MVPBaseFragment;

/**
 * MVPPlugin
 *  邮箱 784787081@qq.com
 */

public class OfflineMapFragment extends MVPBaseFragment<OfflineMapContract.View, OfflineMapPresenter>
        implements OfflineMapContract.View {

    private MapView mMapView;

    @Override
    public int getLayoutResId() {
        return R.layout.offlinemap_fragment;
    }

    @Override
    public void initView(View view) {
        mMapView = (MapView) view.findViewById(R.id.bmapView);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

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
        mMapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

}
