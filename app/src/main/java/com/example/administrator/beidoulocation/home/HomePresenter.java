package com.example.administrator.beidoulocation.home;

import android.content.Context;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.example.administrator.beidoulocation.MainActivity;
import com.example.administrator.beidoulocation.mvp.BasePresenterImpl;
import com.example.administrator.beidoulocation.utils.Location;
import com.example.administrator.beidoulocation.utils.SunriseSunsetCalculator;

import java.util.Calendar;

/**
 * MVPPlugin
 *  邮箱 784787081@qq.com
 */

public class HomePresenter extends BasePresenterImpl<HomeContract.View>
        implements HomeContract.Presenter{

    @Override
    public void updataLocation(final TextView tvlati, final TextView tvlong, final BDLocation location, Context context) {
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvlati.setText(location.getLatitude()+"123");
                tvlong.setText(location.getLongitude()+"");
            }
        });

    }

    @Override
    public String getSunraiseSunsetTime(double latitude, double longitude) {


//        Calendar cal = Calendar.getInstance();
//        Date reqdDate = cal.getTime();
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");

//        try {
//            reqdDate = sdf.parse(dateButton.getText().toString());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        selectedtimezoneidentifier = timeZoneSpinner.getSelectedItem()
//                .toString();

        // process data

        Location location = new Location(latitude, longitude);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
                location, "Asia/Shanghai");

        Calendar cal = Calendar.getInstance();
//        Date reqdDate = cal.getTime();
//        cal.setTime(reqdDate);
        return  calculator.getOfficialSunriseForDate(cal)+","+calculator.getOfficialSunsetForDate(cal);
    }

}
