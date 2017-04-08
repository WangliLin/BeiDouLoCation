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

import com.baidu.mapapi.SDKInitializer;
import com.example.administrator.beidoulocation.home.HomeFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        context = this;
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initToobar();
        initDrawerLayout();
        fra_layout = (LinearLayout) findViewById(R.id.fra_layout);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        radioGroup.check(R.id.rb_home);
        getSupportFragmentManager().beginTransaction().replace(R.id.fra_layout,
                new HomeFragment()).commit();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            private OfflineMapFragment offlineMapFragment;
            private TrrainFragment trrainFragment;
            private HomeFragment homeFragment;

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Fragment currentFragment = null;
                switch (checkedId){
                    case R.id.rb_home:
                        if (homeFragment == null) {
                             homeFragment = new HomeFragment();
                        }
                        currentFragment = homeFragment;
                        break;
                    case R.id.rb_trrain:
//                        if (trrainFragment == null) {
                            trrainFragment = new TrrainFragment();
//                        }
                        currentFragment = trrainFragment;
                        break;
                    case R.id.rb_map:
                        if (offlineMapFragment == null) {
                            offlineMapFragment = new OfflineMapFragment();
                        }
                        currentFragment = offlineMapFragment;
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fra_layout,
                        currentFragment).commit();

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
        rl_setting = (RelativeLayout) findViewById(R.id.rl_setting);
        rl_version = (RelativeLayout) findViewById(R.id.rl_version);
        civ_user_head = (CircleImageView) findViewById(R.id.civ_user_head);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout
                , toolbar, R.string.open_draw, R.string.close_draw);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
    }
}
