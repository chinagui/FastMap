package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: TestLinkLength
 * @Package: com.navinfo.dataservice.engine.edit.zhangyuntao.rd
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 5/24/2017
 * @Version: V1.0
 */
public class TestLinkLength {

    private static Logger logger = Logger.getLogger(TestLinkLength.class);

    // [116.37570351362227,39.99977176483953],[116.3757249712944,39.999770737490444]

    private static List<Coordinate> init(String coors) {
        List<Coordinate> list = new ArrayList<>();
        coors = coors.substring(1, coors.length() - 1);
        for (String obj : coors.split("],\\[")) {
            Coordinate coor = new Coordinate();
            for (String str : String.valueOf(obj).split(",")) {
                if (0 == coor.x) {
                    coor.x = Double.parseDouble(str);
                } else {
                    coor.y = Double.parseDouble(str);
                }
            }
            list.add(coor);
        }
        return list;
    }

    private static void calcLinkLength (String coors) {
        GeometryFactory factory = new GeometryFactory();
        LineString lineString = factory.createLineString(init(coors).toArray(new Coordinate[]{}));
        logger.info(String.format("全精度计算： %.5f",GeometryUtils.getLinkLength(lineString)));

        Geometry geometry = GeoTranslator.transform(lineString, Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        logger.info(String.format("五位精度计算: %.5f", GeometryUtils.getLinkLength(geometry)));
    }

    public static void main(String[] args) {
        calcLinkLength("[116.37570351362227,39.99977176483953],[116.3757249712944,39.999770737490444]");
        calcLinkLength("[116.37550637125969,39.99978614772529],[116.37552514672278,39.99978717507415]");
        calcLinkLength("[116.37547686696051,39.99979025712064],[116.37550368905067,39.99979025712064]");
    }
}
