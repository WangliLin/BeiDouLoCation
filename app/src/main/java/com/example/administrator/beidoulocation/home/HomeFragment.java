package com.example.administrator.beidoulocation.home;


import android.Manifest;
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

import com.example.administrator.beidoulocation.R;
import com.example.administrator.beidoulocation.mvp.MVPBaseFragment;
import com.example.administrator.beidoulocation.utils.DeviceListActivity;
import com.example.administrator.beidoulocation.view.CompassView;

import java.util.Locale;

/**
 * MVPPlugin
 *  修改注释
 *  
 */

public  class HomeFragment extends MVPBaseFragment<HomeContract.View, HomePresenter>
        implements HomeContract.View, View.OnClickListener {
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
        //mLocationTextView = (TextView) view.findViewById(R.id.textview_location);
        mDirectionLayout = (LinearLayout) view.findViewById(R.id.layout_direction);
        mAngleLayout = (LinearLayout) view.findViewById(R.id.layout_angle);

        mPointer.setImageResource(R.drawable.compass);

//        mViewGuide = view.findViewById(R.id.view_guide);

        //ImageView animationImage = (ImageView) view.findViewById(R.id.guide_animation);

        //mGuideAnimation = (AnimationDrawable) animationImage.getDrawable();

        mChinese = TextUtils.equals(Locale.getDefault().getLanguage(), "zh");

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
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

            mLocationTextView.setText(sb.toString());
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

            // if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // return;
            // }

            System.arraycopy(event.values, 0, mAccelerometerValues, 0, 3);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener mMagnetFieldSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // return;
            // }

            System.arraycopy(event.values, 0, mMagneticFieldValues, 0, 3);
            mMagneticFieldAccuracy = event.accuracy;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                startActivity(intentDevice);
                break;
        }
    }



/**
 * ----------------------------  实时获取经纬度----------------------------------------
 */


}
