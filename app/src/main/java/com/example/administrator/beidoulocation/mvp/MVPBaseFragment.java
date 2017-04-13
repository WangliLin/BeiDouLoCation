package com.example.administrator.beidoulocation.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.MyLocationConfiguration;

import java.lang.reflect.ParameterizedType;

/**
 * MVPPlugin
 *  邮箱 784787081@qq.com
 */

public abstract class MVPBaseFragment<V extends BaseView,T extends BasePresenterImpl<V>> extends Fragment implements BaseView{
    public T mPresenter;
    public Context context;
    public  MyLocationConfiguration myLocationConfiguration;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter= getInstance(this,1);
        mPresenter.attachView((V) this);
        context = getActivity();
        myLocationConfiguration = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.COMPASS, true, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter!=null)
            mPresenter.detachView();
    }

    @Override
    public Context getContext() {
        return context;
    }

    public  <T> T getInstance(Object o, int i) {
            try {
                return ((Class<T>) ((ParameterizedType) (o.getClass()
                        .getGenericSuperclass())).getActualTypeArguments()[i])
                        .newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), getLayoutResId(), null);
        //1. 查找控件
        initView(view);
        //2. 设置监听
        initListener();
        //3. 设置数据
        initData();
        return view;
    }



    /**
     * 获取当前fragment对应的布局id
     * @return
     */
    public abstract int getLayoutResId() ;


    /**
     * 初始化view:
     * 查找控件
     * @param view
     */
    public abstract void initView(View view) ;



    /**
     * 初始化监听
     */
    public abstract void initListener() ;



    /**
     * 初始化数据
     * 给控件设置内容
     */
    public abstract void initData() ;

}
