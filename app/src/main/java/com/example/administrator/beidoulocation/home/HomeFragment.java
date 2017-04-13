package com.example.administrator.beidoulocation.home;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.example.administrator.beidoulocation.MainActivity;
import com.example.administrator.beidoulocation.R;
import com.example.administrator.beidoulocation.mvp.MVPBaseFragment;
import com.example.administrator.beidoulocation.utils.DeviceListActivity;
import com.example.administrator.beidoulocation.view.CompassView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

/**
 * MVPPlugin
 *  修改注释
 *  
 */

public  class HomeFragment extends MVPBaseFragment<HomeContract.View, HomePresenter>
        implements HomeContract.View, View.OnClickListener,MainActivity.DataChangeListener {
//测试IAE   A
    private static final int MATRIX_SIZE = 9;
    private final float MAX_ROATE_DEGREE = 1.0f;
    private SensorManager mSensorManager;
    // private Sensor mOrientationSensor;

    private Sensor mAccelerometerSensor;
    private Sensor mMagneticFieldSensor;

    private LocationManager mLocationManager;
    private String mLocationProvider;
    private float mDirection;
    private float mTargetDirection;
    private AccelerateInterpolator mInterpolator;
    protected final Handler mHandler = new Handler();
    private boolean mStopDrawing;

    private boolean mChinese;

    private View mCompassView;
    private CompassView mPointer;
    private TextView mLocationTextView;
    private LinearLayout mDirectionLayout;
    private LinearLayout mAngleLayout;
    private View mViewGuide;
    private AnimationDrawable mGuideAnimation;
    private Vibrator mVibrator;
//    private Context context;

    private static final int MAX_ACCURATE_COUNT = 20;
    private static final int MAX_INACCURATE_COUNT = 20;

    private volatile int mAccurateCount;
    private volatile int mInaccurateCount;

    private volatile boolean mCalibration;

    private void resetAccurateCount() {
        mAccurateCount = 0;
    }

    private void increaseAccurateCount() {
        mAccurateCount++;
    }

    private void resetInaccurateCount() {
        mInaccurateCount = 0;
    }

    private void increaseInaccurateCount() {
        mInaccurateCount++;
    }


    private void switchMode(boolean calibration) {
        mCalibration = calibration;
        if (calibration) {
            mViewGuide.setVisibility(View.VISIBLE);
            mGuideAnimation.start();

            resetAccurateCount();
        } else {
            mGuideAnimation.stop();
            mViewGuide.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "定位", Toast.LENGTH_SHORT).show();
            mVibrator.vibrate(200);
            resetInaccurateCount();
        }
    }

    protected Runnable mCompassViewUpdater = new Runnable() {
        @Override
        public void run() {
            if (mPointer != null && !mStopDrawing) {

                calculateTargetDirection();

                if (mDirection != mTargetDirection) {

                    // calculate the short routine
                    float to = mTargetDirection;
                    if (to - mDirection > 180) {
                        to -= 360;
                    } else if (to - mDirection < -180) {
                        to += 360;
                    }

                    // limit the max speed to MAX_ROTATE_DEGREE
                    float distance = to - mDirection;
                    if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                        distance = distance > 0 ? MAX_ROATE_DEGREE : (-1.0f * MAX_ROATE_DEGREE);
                    }

                    // need to slow down if the distance is short
                    mDirection = normalizeDegree(mDirection
                            + ((to - mDirection) * mInterpolator.getInterpolation(Math
                            .abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));
                    mPointer.updateDirection(mDirection);
                }

                updateDirection();

                mHandler.postDelayed(mCompassViewUpdater, 20);
            }
        }
    };

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        context = getActivity();
//        View view = View.inflate(getActivity(), R.layout.fragment_home,
//                null);
//        initView(view);
//        initLayout(view);
//        initServices();
//        return view;
//    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_home;
    }


    @Override
    public void initView(View view) {
        initLayout(view);
        mDirection = 0.0f;
        mTargetDirection = 0.0f;
        mInterpolator = new AccelerateInterpolator();
        mStopDrawing = true;

        mCompassView = view.findViewById(R.id.view_compass);
        mPointer = (CompassView) view.findViewById(R.id.compass_pointer);
//        mLocationTextView = (TextView) view.findViewById(R.id.textview_location);
        mDirectionLayout = (LinearLayout) view.findViewById(R.id.layout_direction);
        mAngleLayout = (LinearLayout) view.findViewById(R.id.layout_angle);

        mPointer.setImageResource(R.drawable.compass);

//        mViewGuide = view.findViewById(R.id.view_guide);

        //ImageView animationImage = (ImageView) view.findViewById(R.id.guide_animation);

        //mGuideAnimation = (AnimationDrawable) animationImage.getDrawable();

        mChinese = TextUtils.equals(Locale.getDefault().getLanguage(), "zh");
        initLayout(view);
        ((MainActivity)context).setonDataChangeListener(this);
    }

    private void initServices() {
        // sensor manager
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // mOrientationSensor =
        // mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // location manager
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        mLocationProvider = mLocationManager.getBestProvider(criteria, true);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocationProvider != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.getLastKnownLocation(mLocationProvider);
            updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider));
            mLocationManager.requestLocationUpdates(mLocationProvider, 2000, 10, mLocationListener);
        } else {
            //mLocationTextView.setText(R.string.cannot_get_location);
        }

        // if (mOrientationSensor != null) {
        // mSensorManager.registerListener(mOrientationSensorEventListener,
        // mOrientationSensor,
        // SensorManager.SENSOR_DELAY_GAME);
        // }

        if (mAccelerometerSensor != null) {
            mSensorManager.registerListener(mAccelerometerSensorEventListener, mAccelerometerSensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }

        if (mMagneticFieldSensor != null) {
            mSensorManager.registerListener(mMagnetFieldSensorEventListener, mMagneticFieldSensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }

        mStopDrawing = false;
        mHandler.postDelayed(mCompassViewUpdater, 20);

    }

    @Override
    public void onPause() {
        super.onPause();
        mStopDrawing = true;
        // if (mOrientationSensor != null) {
        // mSensorManager.unregisterListener(mOrientationSensorEventListener);
        // }

        if (mAccelerometerSensor != null) {
            mSensorManager.unregisterListener(mAccelerometerSensorEventListener);
        }

        if (mMagneticFieldSensor != null) {
            mSensorManager.unregisterListener(mMagnetFieldSensorEventListener);
        }

        if (mLocationProvider != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.removeUpdates(mLocationListener);
        }

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        mStopDrawing = true;
//        // if (mOrientationSensor != null) {
//        // mSensorManager.unregisterListener(mOrientationSensorEventListener);
//        // }
//
//        if (mAccelerometerSensor != null) {
//            mSensorManager.unregisterListener(mAccelerometerSensorEventListener);
//        }
//
//        if (mMagneticFieldSensor != null) {
//            mSensorManager.unregisterListener(mMagnetFieldSensorEventListener);
//        }
//
//        if (mLocationProvider != null) {
//            mLocationManager.removeUpdates(mLocationListener);
//        }
//    }

    private void updateDirection() {
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(40, 40);

        mDirectionLayout.removeAllViews();
        mAngleLayout.removeAllViews();

        ImageView east = null;
        ImageView west = null;
        ImageView south = null;
        ImageView north = null;
        float direction = normalizeDegree(mTargetDirection * -1.0f);
        if (direction > 22.5f && direction < 157.5f) {
            // east
            east = new ImageView(context);
            east.setImageResource(R.drawable.e);
            east.setLayoutParams(lp);
        } else if (direction > 202.5f && direction < 337.5f) {
            // west
            west = new ImageView(context);
            west.setImageResource(R.drawable.w);
            west.setLayoutParams(lp);
        }

        if (direction > 112.5f && direction < 247.5f) {
            // south
            south = new ImageView(context);
            south.setImageResource(R.drawable.s);
            south.setLayoutParams(lp);
        } else if (direction < 67.5 || direction > 292.5f) {
            // north
            north = new ImageView(context);
            north.setImageResource(R.drawable.n);
            north.setLayoutParams(lp);
        }

        if (mChinese) {
            // east/west should be before north/south
            if (east != null) {
                mDirectionLayout.addView(east);
            }
            if (west != null) {
                mDirectionLayout.addView(west);
            }
            if (south != null) {
                mDirectionLayout.addView(south);
            }
            if (north != null) {
                mDirectionLayout.addView(north);
            }
        } else {
            // north/south should be before east/west
            if (south != null) {
                mDirectionLayout.addView(south);
            }
            if (north != null) {
                mDirectionLayout.addView(north);
            }
            if (east != null) {
                mDirectionLayout.addView(east);
            }
            if (west != null) {
                mDirectionLayout.addView(west);
            }
        }

        int direction2 = (int) direction;
        boolean show = false;
        if (direction2 >= 100) {
            mAngleLayout.addView(getNumberImage(direction2 / 100));
            direction2 %= 100;
            show = true;
        }
        if (direction2 >= 10 || show) {
            mAngleLayout.addView(getNumberImage(direction2 / 10));
            direction2 %= 10;
        }
        mAngleLayout.addView(getNumberImage(direction2));

        ImageView degreeImageView = new ImageView(context);
        degreeImageView.setImageResource(R.drawable.degree);
        degreeImageView.setLayoutParams(lp);
        mAngleLayout.addView(degreeImageView);
    }

    private ImageView getNumberImage(int number) {
        ImageView image = new ImageView(context);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(40, 40);
        switch (number) {
            case 0:
                image.setImageResource(R.drawable.number_0);
                break;
            case 1:
                image.setImageResource(R.drawable.number_1);
                break;
            case 2:
                image.setImageResource(R.drawable.number_2);
                break;
            case 3:
                image.setImageResource(R.drawable.number_3);
                break;
            case 4:
                image.setImageResource(R.drawable.number_4);
                break;
            case 5:
                image.setImageResource(R.drawable.number_5);
                break;
            case 6:
                image.setImageResource(R.drawable.number_6);
                break;
            case 7:
                image.setImageResource(R.drawable.number_7);
                break;
            case 8:
                image.setImageResource(R.drawable.number_8);
                break;
            case 9:
                image.setImageResource(R.drawable.number_9);
                break;
        }
        image.setLayoutParams(lp);
        return image;
    }

    private void updateLocation(Location location) {
        if (location == null) {
            //mLocationTextView.setText(R.string.getting_location);
        } else {
            StringBuilder sb = new StringBuilder();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            if (latitude >= 0.0f) {
                sb.append(getString(R.string.location_north, getLocationString(latitude)));
            } else {
                sb.append(getString(R.string.location_south, getLocationString(-1.0 * latitude)));
            }

            sb.append("    ");

            if (longitude >= 0.0f) {
                sb.append(getString(R.string.location_east, getLocationString(longitude)));
            } else {
                sb.append(getString(R.string.location_west, getLocationString(-1.0 * longitude)));
            }

//            mLocationTextView.setText(sb.toString());
        }
    }

    private String getLocationString(double input) {
        int du = (int) input;
        int fen = (((int) ((input - du) * 3600))) / 60;
        int miao = (((int) ((input - du) * 3600))) % 60;
        return String.valueOf(du) + "°" + String.valueOf(fen) + "'" + String.valueOf(miao) + "\"";
    }

    // private SensorEventListener mOrientationSensorEventListener = new
    // SensorEventListener() {
    //
    // @Override
    // public void onSensorChanged(SensorEvent event) {
    // float direction = event.values[0] * -1.0f;
    // mTargetDirection = normalizeDegree(direction);
    // }
    //
    // @Override
    // public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // }
    // };

    private void calculateTargetDirection() {
        synchronized (this) {
            double data = Math.sqrt(Math.pow(mMagneticFieldValues[0], 2) + Math.pow(mMagneticFieldValues[1], 2)
                    + Math.pow(mMagneticFieldValues[2], 2));

            Log.d("Compass", "data = " + data);

            if (mCalibration) {
                if (mMagneticFieldAccuracy != SensorManager.SENSOR_STATUS_UNRELIABLE && (data >= 25 && data <= 65)) {
                    increaseAccurateCount();
                } else {
                    resetAccurateCount();
                }

                Log.d("Compass", "accurate count = " + mAccurateCount);

                if (mAccurateCount >= MAX_ACCURATE_COUNT) {
//                    switchMode(false);
                }

            } else {
                if (mMagneticFieldAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || (data < 25 || data > 65)) {
                    increaseInaccurateCount();
                } else {
                    resetInaccurateCount();
                }

                Log.d("Compass", "inaccurate count = " + mInaccurateCount);

                if (mInaccurateCount >= MAX_INACCURATE_COUNT) {
//                    switchMode(true);
                }
            }

            if (mMagneticFieldValues != null && mAccelerometerValues != null) {
                float[] R = new float[MATRIX_SIZE];
                if (SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticFieldValues)) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    float direction = (float) Math.toDegrees(orientation[0]) * -1.0f;
                    mTargetDirection = normalizeDegree(direction);
                    Log.d("Compass", "mTargetDirection = " + mTargetDirection);
                } else {
                    Log.d("Compass", "Error: SensorManager.getRotationMatrix");
                }
            }
        }
    }

    private int mMagneticFieldAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;
    private float[] mMagneticFieldValues = new float[3];
    private float[] mAccelerometerValues = new float[3];

    private SensorEventListener mAccelerometerSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub

            // if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // return;
            // }

            System.arraycopy(event.values, 0, mAccelerometerValues, 0, 3);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    private SensorEventListener mMagnetFieldSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub

            // if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // return;
            // }

            System.arraycopy(event.values, 0, mMagneticFieldValues, 0, 3);
            mMagneticFieldAccuracy = event.accuracy;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    public LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status != LocationProvider.OUT_OF_SERVICE) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider));
            } else {
                mLocationTextView.setText(R.string.cannot_get_location);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

    };

    /**
     * ------------------------------------  拿到数据做展示的部分  经纬度，连接盒子的状态----------------------
     */
//       UUID  0000ffe1-0000-1000-8000-00805f9b34f
    private TextView tv_box_id,tv_box_state,tv_longitude,tv_latitude;
    private TextView tv_signal,tv_electricity,tv_sunrise,tv_sunset;
    private LinearLayout ll_connect;

    private void initLayout(View view) {
        tv_box_id = (TextView) view.findViewById(R.id.tv_box_id);
        tv_box_state = (TextView) view.findViewById(R.id.tv_box_state);
        tv_longitude = (TextView) view.findViewById(R.id.tv_longitude);
        tv_longitude.setText("586");
        tv_latitude = (TextView) view.findViewById(R.id.tv_latitude);
        tv_signal = (TextView) view.findViewById(R.id.tv_signal);
        tv_electricity = (TextView) view.findViewById(R.id.tv_electricity);
        tv_sunrise = (TextView) view.findViewById(R.id.tv_sunrise);
        tv_sunset = (TextView) view.findViewById(R.id.tv_sunset);
        ll_connect = (LinearLayout) view.findViewById(R.id.ll_connect);

        //显示经纬度
        initServices();
    }

    /**
     * 初始化
     */
    @Override
    public void initListener() {
        ll_connect.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_connect:
                Intent intentDevice = new Intent(context, DeviceListActivity.class);
                startActivityForResult(intentDevice, REQUEST_CONNECT_DEVICE);
                break;
        }
    }


    @Override
    public void onDataChange(BDLocation locations) {
//        Toast.makeText(context,locations.getLongitude()+"", Toast.LENGTH_SHORT).show();
        tv_longitude.setText(locations.getLongitude()+"");
        tv_latitude.setText(locations.getLatitude()+"");
        if (tv_sunrise.getText().toString().equals("未知")) {
            String[] split = mPresenter.getSunraiseSunsetTime(locations.getLatitude(), locations.getLongitude()).split(",");
            tv_sunrise.setText(split[0]);
            tv_sunset.setText(split[1]);//添加一个注释
            tv_signal.setText(locations.getAltitude()+"");
        }
    }


    /**
     * ----------------------------  获取连接 ----------------------------------------
     * 0000ffe1-0000-1000-8000-00805f9b34f
     */
// SPP服务UUID号
    private final static String MY_UUID = "0000ffe1-0000-1000-8000-00805f9b34f";
    // 获取本地蓝牙适配器，即蓝牙设备
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice _device = null; // 蓝牙设备
    BluetoothSocket _socket = null; // 蓝牙通信socket

    private InputStream is; // 输入流，用来接收蓝牙数据
    private final static int REQUEST_CONNECT_DEVICE = 1;
    boolean bRun = true;
    boolean bThread = false;

    byte[] readBuffer = new byte[1024];
    int readPoint = 0, readDeal = 0;
    private String smsg = ""; // 显示用数据缓存
    private String fmsg = ""; // 保存用数据缓存
    private int fmsglen = 0;//
    boolean refresh = false;

    // 接收活动结果，响应startActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: // 连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) { // 连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Toast.makeText(context, address, Toast.LENGTH_LONG).show();
                    // 得到蓝牙设备
                    _device = _bluetooth.getRemoteDevice(address);

                    // 用服务号得到socket
                    try {
                        _socket = _device.createRfcommSocketToServiceRecord(UUID
                                .fromString(MY_UUID));

                    } catch (IOException e) {
                        Toast.makeText(getContext(), "连接失败！拿不到socket", Toast.LENGTH_SHORT)
                                .show();
                    }

                    try {
                        _socket.connect();
                        Toast.makeText(getContext(),
                                "连接" + _device.getName() + "成功！",
                                Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        try {
                            Toast.makeText(getContext(), "连接失败！_socket连接不上",
                                    Toast.LENGTH_SHORT).show();
                            _socket.close();
                            _socket = null;
                        } catch (IOException ee) {
                            Toast.makeText(getContext(), "连接失败！",
                                    Toast.LENGTH_SHORT).show();
                        }

                        return;
                    }

                    // 打开接收线程
                    try {
                        is = _socket.getInputStream(); // 得到蓝牙数据输入流
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "接收数据失败！", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if (bThread == false) {
                        ReadThread.start();
                        bThread = true;
                    } else {
                        bRun = true;
                    }
                }
                break;
            default:
                break;
        }
    }


    // 接收数据线程
    Thread ReadThread = new Thread() {
        public void run() {
            int num = 0;
            byte[] buffer = new byte[2048];
            byte[] buffer_new = new byte[2048];
            int i = 0;
            int n = 0;
            bRun = true;
            boolean recedata = false;// 有数据
            String s0 = "", sone = "";

            // 接收线程
            while (true) {
                try {
                    if (is.available() > 0) {// 有数据
                        num = is.read(buffer); // 读入数据
                        s0 = new String(buffer, 0, num);
                        sone += s0;

                        fmsg += s0; // 保存收到数据
                        fmsglen += num;
                        if (fmsg.length() > 2048000) {
                            fmsg = fmsg.substring(fmsg.length() - 2048000);
                        }

                        smsg += s0; // 显示用
                        String ics;
                        if (smsg.length() > 1256) {
                            ics = smsg.substring(smsg.length() - 1256);
                            smsg = ics;
                        }
                        recedata = true;
                    } else {
                        if (recedata) {
                            // 发送显示消息，进行显示刷新
                            if (refresh == false) {
                                Message msg = handler.obtainMessage();
                                msg.obj = sone;
                                handler.sendMessage(msg);
                                refresh = true;
                                sone = "";
                            } else {//

                            }
                            recedata = false;

                        }

                        try {
                            sleep(10);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }

                    }

                } catch (IOException e) {

                }
            }
        }
    };


    // 消息处理队列
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String imsg = msg.obj.toString();// 当前消息信息
            if (!TextUtils.isEmpty(imsg)) {

            }

            System.out.println("--------数据数据数据>>>>>>" + imsg);
            String igga[] = imsg.split("\r\n");// 每条GGA
            for (int i = 0; i < igga.length; i++) {
                String ijwdu[] = igga[i].split(",");// 每个逗号

                if (ijwdu[0] != null) {
                    if (ijwdu[0].indexOf("GGA") != -1) {// 查到GGA
                        if (ijwdu.length > 13) {
                            if (ijwdu[6] != null) {
                                if (ijwdu[6].equals("4")) {// 差分定位

                                    //tv_jingdu.setText("高精度");

                                } else {

                                    //tv_jingdu.setText("普通精度");

                                }
                            }
                            if (ijwdu[13] != null) {
                                if (ijwdu[13].length() > 0) {
                                    // staDiff.append(" ;延时:" + ijwdu[13]);
                                }
                            }
                            if ((ijwdu[2].length() > 4)
                                    && (ijwdu[4].length() > 4)) {
                                double ij1 = 0, iw1 = 0;
                                CeJuType ceJuValue = new CeJuType();

                                ij1 = Double.valueOf(ijwdu[4]);// 经度
                                iw1 = Double.valueOf(ijwdu[2]);// 维度

                                {// 一点
                                    // 经度
                                    ceJuValue.jd1 = ((int) (ij1 / 100));
                                    ij1 = (ij1 - ceJuValue.jd1 * 100) / 60;
                                    ceJuValue.jd1 += ij1;
                                    ceJuValue.jd1 = ((double) ((long) (ceJuValue.jd1 * 100000000)) / 100000000);

                                    // 维度
                                    ceJuValue.wd1 = ((int) (iw1 / 100));
                                    iw1 = (iw1 - ceJuValue.wd1 * 100) / 60;
                                    ceJuValue.wd1 += iw1;
                                    ceJuValue.wd1 = (double) ((long) (ceJuValue.wd1 * 100000000)) / 100000000;


//									tv_jingweidu.setText(ceJuValue.jd1 + ","
//											+ ceJuValue.wd1);
                                }

                                if (ijwdu[7].length() > 0) {// 有效卫星数
                                    // jingweidu.append(";星数" + ijwdu[7]);
                                    //tv_weixingshu.setTag("星数" + ijwdu[7]);

                                }
                            }
                        }
                    } else if (ijwdu[0].indexOf("GSV") != -1) {// 卫星输出
                        if (ijwdu.length > 19) {
                            String iweixing = "";
                            if (ijwdu[2].length() > 0) {// GSV 序 号
                                if (ijwdu[2].equals("1")) {// 第一条

                                    if (ijwdu[3].length() > 0) {// 可视卫星数
                                        iweixing += "可视卫星数：" + ijwdu[3] + "\n";
                                    }
                                }
                            }

                            if (ijwdu[4].length() > 0) {// 4卫星号//5仰角//6方位角//7信噪比
                                iweixing += "卫星号:" + ijwdu[4] + ";仰角"
                                        + ijwdu[5] + ";方位" + ijwdu[6] + ";信噪比"
                                        + ijwdu[7] + "\n";
                            }
                            if (ijwdu[8].length() > 0) {// 8卫星号//9仰角//10方位角//11信噪比
                                iweixing += "卫星号:" + ijwdu[8] + ";仰角"
                                        + ijwdu[9] + ";方位" + ijwdu[10] + ";信噪比"
                                        + ijwdu[11] + "\n";
                            }

                            if (ijwdu[12].length() > 0) {// 12卫星号//13仰角//14方位角//15信噪比
                                iweixing += "卫星号:" + ijwdu[12] + ";仰角"
                                        + ijwdu[13] + ";方位" + ijwdu[14]
                                        + ";信噪比" + ijwdu[15] + "\n";
                            }

                            if (ijwdu[16].length() > 0) {// 16卫星号//17仰角//18方位角//19信噪比
                                iweixing += "卫星号:" + ijwdu[16] + ";仰角"
                                        + ijwdu[17] + ";方位" + ijwdu[18]
                                        + ";信噪比" + ijwdu[19] + "\n";
                            }


                        }
                    } else if (ijwdu[0].indexOf("GPRS") != -1) {// 查到GPRS
                        if (ijwdu.length >= 4) {
                            if (ijwdu[1].length() > 0) {// GPRS联网状态
                                int igprsInt = 0;
                                try {
                                    igprsInt = Integer.valueOf(ijwdu[1]);
                                } catch (Exception e) {

                                    e.printStackTrace();
                                    // staGprs.setText("GPRS 转换错误");
                                }

                                if (igprsInt < 5) {
                                    // staGprs.setText("未启动:" + igprsInt);
                                } else if (igprsInt < 15) {//
                                    // staGprs.setText("连网中:" + igprsInt);
                                } else if (igprsInt < 16) {//
                                    // staGprs.setText("获IP中:" + igprsInt);
                                } else if (igprsInt < 18) {//
                                    // staGprs.setText("连服务器中:" + igprsInt);
                                } else if (igprsInt == 20) {// 成功
                                    // staGprs.setText("成功:" + igprsInt);
                                    // staGprs.setTextColor(Color.rgb(0, 255,
                                    // 0));
                                }
                            }

                            if (ijwdu[2].length() > 0) {// GPRS 信号
                                // staGprs.append(" ;信号:" + ijwdu[2]);

                            }
                        }
                    }
                }
            }
            refresh = false;// 刷新界面结束
        }
    };

    class CeJuType {
        double jd1 = 0;// 经度
        double wd1 = 0;
    }

}
