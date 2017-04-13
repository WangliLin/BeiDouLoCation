package com.example.administrator.beidoulocation;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private RelativeLayout rl_setting,rl_version;
    private CircleImageView civ_user_head;
    private Context context;
    private OfflineMapFragment offlineMapFragment;
    private TrrainFragment trrainFragment;
    private HomeFragment homeFragment;
    private TextView tv_downmap;
    boolean isMap = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initToobar();//添加数组
        initDrawerLayout();
        fra_layout = (LinearLayout) findViewById(R.id.fra_layout);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        trrainFragment = new TrrainFragment();
        homeFragment = new HomeFragment();
        radioGroup.check(R.id.rb_trrain);
        getSupportFragmentManager().beginTransaction().replace(R.id.fra_layout,
                trrainFragment).commit();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Fragment currentFragment = null;
                switch (checkedId){
                    case R.id.rb_home:
                        switchFragment(false);
                        isMap = false;
                        break;
                    case R.id.rb_trrain:
                        neededToMap(MapType.TERRAIN_MAP.mapCode);
                        isMap = true;
                        break;
                    case R.id.rb_map:
                        neededToMap(MapType.OFFLINE_MAP.mapCode);
                        isMap = true;
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

    public   DataChangeListener changeListener;
    public interface DataChangeListener{
        void onDataChange(BDLocation locations);
    }
    public void setonDataChangeListener(DataChangeListener changeListener){
        this.changeListener = changeListener;
    }

    public void onChangeData(BDLocation location){
        if (changeListener != null) {
            changeListener.onDataChange(location);
        }
    }

    public void  switchFragment(boolean isMap){
        if (isMap) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fra_layout,
                   trrainFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fra_layout,
                    homeFragment).commit();
        }
    }

    public void  neededToMap(int maptype){
        if (!isMap) {//从地图界面跳转
            switchFragment(true);
        } else {//不是地图界面跳转
            if (maptype == MapType.OFFLINE_MAP.mapCode) {//离线地图
                if (mapTypeListener != null) {
                    mapTypeListener.onMapTypeChangeListener(MapType.OFFLINE_MAP.mapCode);
                }
            } else {//地形图
                if (mapTypeListener != null) {
                    mapTypeListener.onMapTypeChangeListener(MapType.TERRAIN_MAP.mapCode);
                }
            }
        }
    }

    public MapTypeListener mapTypeListener;
    public interface MapTypeListener{
        void onMapTypeChangeListener(int type);
    }
    public void setOnMapTypeChangeListener(MapTypeListener mapTypeListener) {
        this.mapTypeListener = mapTypeListener;
    }
}
