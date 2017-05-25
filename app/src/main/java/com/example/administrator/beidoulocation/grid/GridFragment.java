package com.example.administrator.beidoulocation.grid;

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
        for (int i = 1; i < 50; i++) {
            mDatas.add("" + i);
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
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }

}
