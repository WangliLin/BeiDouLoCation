package com.example.administrator.beidoulocation.grid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.administrator.beidoulocation.R;

/**
 * 7*7网格实现
 * Created by Wangli on 2017/5/24.
 */

public class GridFragment extends android.support.v4.app.Fragment {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = LinearLayout.inflate(getActivity(), R.layout.fragment_grid, null);
        }

        return view;
    }

}
