package com.example.administrator.beidoulocation;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.example.administrator.beidoulocation.enum_obj.MapType;
import com.example.administrator.beidoulocation.home.HomeFragment;
import com.example.administrator.beidoulocation.listener.MyClickListener;
import com.example.administrator.beidoulocation.offlinemap.OfflineMapFragment;
import com.example.administrator.beidoulocation.trrain.TrrainFragment;
import com.example.administrator.beidoulocation.view.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout fra_layout;
    private RadioGroup radioGroup;
    private Toolbar toolbar;
    private RelativeLayout rl_setting, rl_version;
    private CircleImageView civ_user_head;
    private Context context;
    private OfflineMapFragment offlineMapFragment;
    private TrrainFragment trrainFragment;
    private HomeFragment homeFragment;
    private TextView tv_downmap;
    private final int HOME_FRAGMENT = 0, TERRAIN_FRAGMENT = 1, OFFLINE_FRAGMENT = 2;
    private int last_fragment = HOME_FRAGMENT;
    private FragmentManager supportFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initToobar();
        initDrawerLayout();
        fra_layout = (LinearLayout) findViewById(R.id.fra_layout);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        trrainFragment = new TrrainFragment();
        homeFragment = new HomeFragment();
        homeFragment.setMyLocationOnMap(new HomeFragment.ShowMyLocation() {
            @Override
            public void requestShowLocation(LatLng latLng) {
                if (locationFunction != null) {
                    locationFunction.showLatLngOnMap(latLng);
                }
            }
        });
        radioGroup.check(R.id.rb_home);

        supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager.beginTransaction()
                .add(R.id.fra_layout, trrainFragment)
                .add(R.id.fra_layout, homeFragment)
                .show(homeFragment)
                .hide(trrainFragment)
                .commit();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_home:
                        supportFragmentManager.beginTransaction().show(homeFragment).hide(trrainFragment).commit();
                        last_fragment = HOME_FRAGMENT;
                        break;
                    case R.id.rb_trrain:
                        switchFragment(TERRAIN_FRAGMENT);
                        last_fragment = TERRAIN_FRAGMENT;
                        break;
                    case R.id.rb_map:
                        switchFragment(OFFLINE_FRAGMENT);
                        last_fragment = OFFLINE_FRAGMENT;
                        break;
                }
            }
        });
    }

    private void initToobar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("北斗定位");
        }
    }

    private void initDrawerLayout() {
        MyClickListener listener = new MyClickListener();
        rl_setting = (RelativeLayout) findViewById(R.id.rl_setting);
        rl_version = (RelativeLayout) findViewById(R.id.rl_version);
        civ_user_head = (CircleImageView) findViewById(R.id.civ_user_head);
        tv_downmap = (TextView) findViewById(R.id.tv_ofl_map);
        rl_setting.setOnClickListener(listener);
        rl_version.setOnClickListener(listener);
        civ_user_head.setOnClickListener(listener);
        tv_downmap.setOnClickListener(listener);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout
                , toolbar, R.string.open_draw, R.string.close_draw);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
    }

    public DataChangeListener changeListener;

    public interface DataChangeListener {
        void onDataChange(BDLocation locations);
    }

    public void setonDataChangeListener(DataChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void onChangeData(BDLocation location) {
        if (changeListener != null) {
            changeListener.onDataChange(location);
        }
    }

    public void switchFragment(int targetFargment) {

        switch (targetFargment) {
            case TERRAIN_FRAGMENT:
                switchMap(targetFargment);
                break;
            case OFFLINE_FRAGMENT:
                switchMap(targetFargment);
                break;
        }
    }

    private void switchMap(int targetFargment) {
        if (last_fragment == HOME_FRAGMENT) {
            supportFragmentManager.beginTransaction().show(trrainFragment).hide(homeFragment).commit();
            switchLayer(targetFargment);
        } else {
            switchLayer(targetFargment);
        }
    }

    private void switchLayer(int targetFargment) {
        if (targetFargment == TERRAIN_FRAGMENT) {
            if (mapTypeListener != null) {
                mapTypeListener.onMapTypeChangeListener(MapType.TERRAIN_MAP.mapCode);
            }
        } else {
            if (mapTypeListener != null) {
                mapTypeListener.onMapTypeChangeListener(MapType.OFFLINE_MAP.mapCode);
            }
        }
    }


    public MapTypeListener mapTypeListener;//改变地图图层的接口

    public interface MapTypeListener {
        void onMapTypeChangeListener(int type);
    }

    public void setOnMapTypeChangeListener(MapTypeListener mapTypeListener) {
        this.mapTypeListener = mapTypeListener;
    }

    ShowLocationFunction locationFunction;//展示经纬度在地图上的 -- 接口

    public void useLatLngOnMap(ShowLocationFunction locationFunction) {
        this.locationFunction = locationFunction;
    }

    public interface ShowLocationFunction {
        void showLatLngOnMap(LatLng latLng);
    }
}
