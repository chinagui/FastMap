package com.navinfo.dataservice.expcore.source.parameter.converter;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dms.commons.geo.GeoUtils;

/**
 * 按区域导出图幅，先将区域转换成图幅
 * User: liuqing
 * Date: 2010-9-29
 * Time: 13:13:03
 */
public class Area2MeshParamConverter implements ParamConverter {

    public List<String> convert(List<String> params) {
        List<String> result = new ArrayList<String>();
        if (params == null || params.isEmpty()) {
            return result;
        }
        //这里坐标生成图幅没有检查，需要增加检查
        for(String area:params){
            String loc[] = area.split(",");
            String[] meshArray = GeoUtils.area2Meshes(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));
            for (int i = 0; i < meshArray.length; i++) {
                String meshId = meshArray[i];
                result.add(meshId);
            }
        }
        return result;
    }
}
