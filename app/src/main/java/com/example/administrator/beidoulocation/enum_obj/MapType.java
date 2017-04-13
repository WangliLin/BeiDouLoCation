package com.example.administrator.beidoulocation.enum_obj;

/**
 * Created by seeker on 2017/4/13.
 */

public  enum MapType {
    TERRAIN_MAP(1, "地形图"),
    OFFLINE_MAP(2, "平面图");

    public int mapCode;
    public String mapType;

    MapType(int mapCode, String mapType) {
        this.mapCode = mapCode;
        this.mapType = mapType;
    }

}
