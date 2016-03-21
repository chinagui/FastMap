package com.navinfo.navicommons.geo;

import java.sql.SQLException;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

/**
 * Oracle某些方法效率很低，所以用此类屏蔽效率低下的方法
 * User: liuqing
 * Date: 2010-8-6
 * Time: 9:07:13
 */
public class SpatialAdapters {
    protected static final WKT m_wktAdapter = new WKT();
    private static Logger logger = Logger.getLogger(SpatialAdapters.class);

    /**
     * @param struct
     * @return
     * @throws SQLException
     */
    public static String struct2Wkt(STRUCT struct) throws SQLException, GeometryExceptionWithContext {
        if (struct == null)
            return null;
        try {
            byte abyte0[] = m_wktAdapter.fromSTRUCT(struct);
            return new String(abyte0);
        } catch (Exception e) {
            //logger.error(e.getMessage(), e);
            return null;
        }
    }
    /**
     * @param struct
     * @return
     * @throws SQLException
     */
    public static String struct2Wkt(Object struct) throws SQLException{
        if (struct == null)
            return null;
        try {
            byte abyte0[] = m_wktAdapter.fromSTRUCT((STRUCT)struct);
            return new String(abyte0);
        } catch (Exception e) {
            //logger.error(e.getMessage(), e);
            return null;
        }
    }


    /**
     * 支持持2D
     * //POINT (115.93894 39.53994)
     *
     * @param wkt
     * @return
     * @throws Exception
     */
    public static String wkt2JGeometry(String wkt) throws Exception {
        JGeometry geom = m_wktAdapter.toJGeometry(wkt.getBytes());
        validateWKT(wkt);
        int type = geom.getType();
        int dim = geom.getDimensions();
        int gtype = dim * 1000 + type;
        double[] ordinates = geom.getOrdinatesArray();
        StringBuilder builder = new StringBuilder("SDO_GEOMETRY(");
        builder.append(gtype);
        builder.append(",");
        builder.append(8307);
        builder.append(",");

        if (type == 1 && ordinates == null) {
            double points[] = geom.getPoint();
            builder.append("SDO_POINT_TYPE(");
            for (int i = 0; i < points.length; i++) {
                double ordinate = points[i];
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(ordinate);

            }
            if (dim == 2)
                builder.append(",NULL");
            builder.append("),");
        } else {
            builder.append("NULL,");
        }
        if (type == 1) {
            builder.append("NULL,");
            builder.append("NULL");
        } else {
            builder.append("SDO_ELEM_INFO_ARRAY(");
            builder.append(geom.getElemInfo()[0]);
            builder.append(",");
            builder.append(geom.getElemInfo()[1]);
            builder.append(",");
            builder.append(geom.getElemInfo()[2]);
            builder.append("),");
            builder.append("SDO_ORDINATE_ARRAY(");

            for (int i = 0; i < ordinates.length; i++) {
                double ordinate = ordinates[i];
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(ordinate);

            }
            builder.append(")");
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 根据左下坐标点和右上坐标点生成geometry的字符表示形式
     *
     * @param lbx
     * @param lby
     * @param ltx
     * @param lty
     * @return
     */
    public static String createRectangleGeometry(double lbx, double lby, double ltx, double lty) {
        StringBuilder builder = new StringBuilder("SDO_GEOMETRY(2003,8307,NULL, ");
        builder.append(" MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 3),");
        builder.append(" MDSYS.SDO_ORDINATE_ARRAY(");
        builder.append(lbx);
        builder.append(",");
        builder.append(lby);
        builder.append(",");
        builder.append(ltx);
        builder.append(",");
        builder.append(lty);
        builder.append(" )");
        builder.append(" )");
        return builder.toString();

    }

    /**
     * 验证WKT
     *
     * @param wkt
     */
    private static void validateWKT(String wkt) {
        //TODO:正则验证wkt格式，否则oracle提供的API会进入死循环
    }

    public static void main(String[] args) {
        try {
            System.out.println(SpatialAdapters.wkt2JGeometry("POINT (115.93894 39.53994)"));
            System.out.println(SpatialAdapters.createRectangleGeometry(1, 2, 3, 4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
