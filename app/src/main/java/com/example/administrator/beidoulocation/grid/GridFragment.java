package com.example.administrator.beidoulocation.grid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.beidoulocation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 7*7网格实现
 * Created by Wangli on 2017/5/24.
 */

public class GridFragment extends android.support.v4.app.Fragment {

    private View view;
    private RecyclerView recyclerView;
    private List<String> mDatas;
    private HomeAdapter mAdapter;

    int[] colorStr = new int[]{ R.color.quality_3, R.color.quality_4, R.color.quality_5, R.color.quality_6 };

    int[] tempers = new int[] {
            26, 27, 28, 28, 29, 29, 29,
            26, 27, 28, 28, 29, 29, 29,
            26, 27, 27, 28, 29, 29, 29,
            27, 27, 28, 28, 29, 29, 29,
            27, 27, 28, 28, 29, 29, 29,
            28, 28, 28, 28, 29, 29, 29,
            28, 29, 29, 29, 29, 29, 29
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        if (view == null) {
//            view = LinearLayout.inflate(getActivity(), R.layout.fragment_grid, null);
            view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grid, container, false);
        }

        initData();
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 7));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        return view;
    }

    protected void initData() {
        mDatas = new ArrayList<String>();
        for (int i = 0; i < 49; i++) {
            mDatas.add("" + tempers[i]);
        }
        mAdapter = new HomeAdapter();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyView);
    }

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public MyViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.id_num);
            }
        }


        @Override
        public HomeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //这里必须用LayoutInflater,而不能用LinearLayout.inflate
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.item_recy, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(HomeAdapter.MyViewHolder holder, int position) {
            holder.tv.setText(mDatas.get(position));
            holder.tv.setTextColor(Color.WHITE);
            holder.tv.setBackgroundColor(getResources().getColor(colorStr[ tempers[position] % 26]));
//            holder.tv.setBackgroundColor(getResources().getColor(R.color.quality_1));
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }



    /*

    private int showColor(String temper) {

        double temp = Double.parseDouble(temper);

        return colorStr[(int) temp/7];

    }*/

}
