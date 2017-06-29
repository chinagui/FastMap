package com.navinfo.dataservice.engine.fcc.tips;

import com.navinfo.nirobot.common.utils.GeometryConvertor;
import com.navinfo.nirobot.common.utils.GeometryUtils;
import com.navinfo.nirobot.common.utils.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TipsGeomUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory();


    /**
     * 获取按照道路通行方向取四分之一位置
     * @param geom
     * @param direct
     * @return double[] 位置坐标
     */
    public static double[] get1_4Point(Geometry geom, int direct) throws Exception {

//        double linkPoints[][] = getLinkPoints(geom, direct);
        double linkPoints[][] = TipsGeomUtils.convertLinkToMerArray(geom);

        double linkLength = GeometryUtils.getLinkLength(geom);// getLinkLength(linkPoints);

        double len1_4 = linkLength / 4;

        double[] point = lookFor1_4Point(linkPoints, len1_4);

        return point;

    }

    // 获取道路link中点
    public static double[] get1_2Point(Geometry geom) throws Exception {

        double linkPoints[][] = TipsGeomUtils.convertLinkToMerArray(geom);

        double linkLength = GeometryUtils.getLinkLength(geom);

        double len1_2 = linkLength / 2;

        double[] point = lookFor1_4Point(linkPoints, len1_2);

        return point;

    }

//    /**
//     * 根据通行方向，返回link的所有形状点数组
//     * @param geom
//     * @param direct
//     * @return double[][]
//     */
//    private static double[][] getLinkPoints(JGeometry geom, int direct) {
//
//        double[] ps = geom.getOrdinatesArray();
//
//        double[][] points = new double[ps.length / 2][];
//
//        int num = 0;
//
//        if (direct == 1 || direct == 2) {
//            for (int i = 0; i < ps.length; i += 2) {
//
//                double lng = ps[i];
//
//                double lat = ps[i + 1];
//
////                double lngMer = MercatorProjection.longitudeToMetersX(lng);
////
////                double latMer = MercatorProjection.latitudeToMetersY(lat);
//
//                points[num++] = new double[] { lng, lat };
//            }
//        } else {
//            for (int i = ps.length - 1; i >= 0; i -= 2) {
//
//                double lat = ps[i];
//
//                double lng = ps[i - 1];
//
////                double lngMer = MercatorProjection.longitudeToMetersX(lng);
////
////                double latMer = MercatorProjection.latitudeToMetersY(lat);
//
//                points[num++] = new double[] { lng, lat };
//            }
//        }
//
//        return points;
//
//    }

//    //不区分方向，用于取道路中点
//    private static double[][] getLinkPoints(JGeometry geom) {
//
//        double[] ps = geom.getOrdinatesArray();
//
//        double[][] points = new double[ps.length / 2][];
//
//        int num = 0;
//
//        for (int i = 0; i < ps.length; i += 2) {
//
//            double lng = ps[i];
//
//            double lat = ps[i + 1];
//
//            double lngMer = MercatorProjection.longitudeToMetersX(lng);
//
//            double latMer = MercatorProjection.latitudeToMetersY(lat);
//
//            points[num++] = new double[] { lngMer, latMer };
//        }
//
//        return points;
//
//    }

    /**
     * 获取link四分之一位置
     * @param points
     * @param len1_4
     * @return
     */
    private static double[] lookFor1_4Point(double[][] points, double len1_4) throws Exception {
        double point[] = new double[2];

        for (int i = 1; i < points.length; i++) {
            double prePoint[] = points[i - 1];

            double currentPoint[] = points[i];

//            double len = Math.sqrt(Math.pow(currentPoint[0] - prePoint[0], 2)
//                    + Math.pow(currentPoint[1] - prePoint[1], 2));
            Geometry geom = GeometryUtils.getLineFromPoint(currentPoint, prePoint);
            double len = GeometryUtils.getLinkLength(geom);

            if (len >= len1_4) {

                point[0] = prePoint[0] + len1_4 / len
                        * (currentPoint[0] - prePoint[0]);

                point[1] = prePoint[1] + len1_4 / len
                        * (currentPoint[1] - prePoint[1]);

                break;
            }

            len1_4 -= len;
        }

//        point[0] = MercatorProjection.metersXToLongitude(point[0]);
//
//        point[1] = MercatorProjection.metersYToLatitude(point[1]);

        return point;
    }

    /**
     * 获取路口处tips引导坐标和显示坐标，注意：添加新的路口处tips，调用的getLinkPointPos中第三个参数要随之修改
     * @param linkWkt  进入线几何
     * @param pointWkt 进入点几何
     * @param seqNum   tips顺序号
     * @return double[0] 引导坐标 double[1] 显示坐标
     * @throws Exception
     */
    public static double[][] getTipsPointPos(String linkWkt, String pointWkt,
                                             int seqNum) throws Exception {

        return getLinkPointPos(linkWkt, pointWkt, 12, seqNum, 3, 8, 6);
    }

    public static double[][] getTipsPointPos(String linkWkt, String pointWkt,
                                             int seqNum,double dist) throws Exception {

        return getLinkPointPos(linkWkt, pointWkt, 12, seqNum, 3, 8, dist);
    }

    public static double[][] getTipsPointPos(String linkWkt, String pointWkt,
                                             double base, double vertiUnit) throws Exception {

        return getLinkPointPos(linkWkt, pointWkt, 1, 0, base, 8, vertiUnit);
    }

    /**
     * 根据link实际Tips数量计算引导坐标和显示坐标
     * @param linkWkt 进入线
     * @param pointWkt 进入点
     * @param totalCnt link Tips总数
     * @return
     * @throws Exception
     */
    public static double[][] getTipsPointPos(String linkWkt, String pointWkt,int totalCnt,
                                             int seqNum) throws Exception {

        return getLinkPointPos(linkWkt, pointWkt, totalCnt, seqNum, 3, 8, 6);
    }

//    /**
//     * 获取点线结构类tips的引导坐标和显示坐标（坡度等）
//     * @param linkWkt  线几何
//     * @param pointWkt 点几何
//     * @return double[0] 引导坐标 double[1] 显示坐标
//     * @throws Exception
//     */
//    public static double[][] getSlopeTipsPointPos(String linkWkt, String pointWkt) throws Exception {
//
//        return getLinkPointPos(linkWkt, pointWkt, 1, 0, 5, 0, 0);
//    }

//    public static double[][] getGdbPointPos(String linkWkt, String pointWkt,
//            int seqNum) throws Exception {
//        return getLinkPointPos(linkWkt, pointWkt, 5, seqNum, 4.5,4, 3);
//    }


    /**
     * @param linkWkt 进入线几何
     * @param pointWkt 进入点几何
     * @param totalCnt 总共要素类型个数
     * @param seqNum 要素类型序号
     * @param base 基础距离
     * @param unit 递增距离
     * @param vertiUnit 线垂直方向移动距离
     * @return double[0] 引导坐标 double[1] 显示坐标
     * @throws Exception
     */
    private static double[][] getLinkPointPos(String linkWkt, String pointWkt,
                                              int totalCnt, int seqNum, double base, double unit, double vertiUnit) throws Exception {
        double[][] position = new double[2][2];

        /*
         * 1、对线、点进行墨卡托数组转
         * 2、根据点所处线的位置，转换线数组顺序
         * 3、求出线的长度，判断按照线上挂的tips个数，是否超过线的长度
         * 4、如果未超出，则按照3米为单位，向线通行方向的逆向扩展
         * 5、如果超出，则按照线长度与tips个数比例，向线通行方向的逆向扩展
         * 6、找出seqNum * 3米（或者新比例值）位置，作为引导坐标位置
         * 7、按照引导坐标位置和线通行方向向右找6米位置作为显示坐标位置
         * 8、转换墨卡托坐标为经纬度坐标返回
         */

        // 默认是3米，如果按照tips个数 * 3 米超出了LINK的长度，则重新计算这个值


        // 1、对线、点进行墨卡托数组转
        double[][] linkMerArray = convertLinkToMerArray(linkWkt);

        double[] pointMerArray = convertPointToMerArray(pointWkt);

        // 2、根据点所处线的位置，转换线数组顺序
        isReverseLinkOrder(linkMerArray, pointMerArray);

        // 3、求出线的长度，判断按照线上挂的tips个数，是否超过线的长度
        double linkLength = GeometryUtils.getLinkLength(linkWkt);

        boolean isExceedLink = false;

        double total = base + unit * (totalCnt-1);

        if (total > linkLength) {
            isExceedLink = true;
        }

        // 5、如果超出，则按照线长度与tips个数比例，向线通行方向的逆向扩展
        if (isExceedLink) {
            unit = linkLength / (totalCnt + 1);
            BigDecimal bigDecimal = new BigDecimal(unit);
            bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
            unit = bigDecimal.doubleValue();
            base = unit;
        }

        // 6、找出seqNum * 3米（或者新比例值）位置，作为引导坐标位置
        double[] guidePosition = new double[2];

        // 返回值为引导坐标所处的LINK形状段上的第几段，从0开始
        BigDecimal distanceDecimal = new BigDecimal(base + unit * seqNum);
        distanceDecimal = distanceDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
        int guideSeqNum = getGuidePosition(linkMerArray, distanceDecimal.doubleValue(),
                guidePosition);

        // 按照引导坐标位置和线通行方向向右找6米位置作为显示坐标位置
        double[] displayPosition = getDisplayPosition(linkMerArray,
                guidePosition, guideSeqNum, vertiUnit);

        // 转换墨卡托坐标为经纬度坐标返回

//        guidePosition[0] = MercatorProjection
//                .metersXToLongitude(guidePosition[0]);
//
//        guidePosition[1] = MercatorProjection
//                .metersYToLatitude(guidePosition[1]);

        position[0] = guidePosition;

//        displayPosition[0] = MercatorProjection
//                .metersXToLongitude(displayPosition[0]);
//
//        displayPosition[1] = MercatorProjection
//                .metersYToLatitude(displayPosition[1]);

        position[1] = displayPosition;

        return position;
    }


    /**
     * @param linkWkt 进入线几何
     * @param isReverse 是否反转进入线几何
     * @param base 基础距离
     * @param vertiUnit 线垂直方向移动距离，正值为向右侧移动，负值为向左侧移动
     * @return double[0] 引导坐标 double[1] 显示坐标
     * @throws Exception
     */
    public static double[][] getLinkPointPosForDir(String linkWkt,
                                                   boolean isReverse, double base, double vertiUnit) throws Exception {
        double[][] position = new double[2][2];

        // 1、对线、点进行墨卡托数组转
        double[][] linkMerArray = convertLinkToMerArray(linkWkt);

        // 2、根据传入参数，转换线数组顺序
        if (isReverse) {
            reverseLinkOrder(linkMerArray);
        }

        // 3、求出线的长度，如果初始的base长度已经大于link的长度，那么取link长度的一半作为base的新值
        double linkLength = GeometryUtils.getLinkLength(linkWkt);

        if (base > linkLength) {
            base = linkLength / 2;
        }

        // 4、找出base(默认3米)位置，作为引导坐标位置
        double[] guidePosition = new double[2];

        // 返回值为引导坐标所处的LINK形状段上的第几段，从0开始
        int guideSeqNum = getGuidePosition(linkMerArray, base,
                guidePosition);

        // 5、按照引导坐标位置和线通行方向向右（或向左）找6米位置作为显示坐标位置
        double[] displayPosition = getDisplayPosition(linkMerArray,
                guidePosition, guideSeqNum, vertiUnit);

        // 6、转换墨卡托坐标为经纬度坐标返回
//        guidePosition[0] = MercatorProjection
//                .metersXToLongitude(guidePosition[0]);
//
//        guidePosition[1] = MercatorProjection
//                .metersYToLatitude(guidePosition[1]);

        position[0] = guidePosition;

//        displayPosition[0] = MercatorProjection
//                .metersXToLongitude(displayPosition[0]);
//
//        displayPosition[1] = MercatorProjection
//                .metersYToLatitude(displayPosition[1]);

        position[1] = displayPosition;

        return position;
    }

    // 转换线经纬度wkt为以米为单位的二维数组
    public static double[][] convertLinkToMerArray(String linkWkt)
            throws Exception {

        WKTReader reader = new WKTReader();

        Geometry geom = reader.read(linkWkt);

        return convertLinkToMerArray(geom);

    }

    // 转换线经纬度wkt为以米为单位的二维数组
    public static double[][] convertLinkToMerArray(Geometry geom)
            throws Exception {

        Coordinate[] cs = geom.getCoordinates();

        double[][] linkMerArray = new double[cs.length][2];

        int num = 0;

        for (Coordinate c : cs) {
            double[] p = new double[2];

            // 此处需要进行对经纬度转换为墨卡托值
//            p[0] = MercatorProjection.longitudeToMetersX(c.x);
//
//            p[1] = MercatorProjection.latitudeToMetersY(c.y);

            p[0] = c.x;
            p[1] = c.y;

            linkMerArray[num++] = p;
        }

        return linkMerArray;

    }

    // 转换点经纬度wkt为以米为单位的数组
    public static double[] convertPointToMerArray(String pointWkt)
            throws Exception {

        double[] pointMerArray = new double[2];

        WKTReader reader = new WKTReader();

        Geometry geom = reader.read(pointWkt);

        // 此处需要进行对经纬度转换为墨卡托值
        pointMerArray[0] = geom.getCentroid().getX();

        pointMerArray[1] = geom.getCentroid().getY();

//        pointMerArray[0] = MercatorProjection
//                .longitudeToMetersX(pointMerArray[0]);
//
//        pointMerArray[1] = MercatorProjection
//                .latitudeToMetersY(pointMerArray[1]);

        return pointMerArray;
    }

    /**
     * 根据点所处线的位置，转换线数组顺序;如果点不是link的起点，则反转link数组
     * @param linkMerArray
     * @param pointMerArray
     */
    private static void isReverseLinkOrder(double[][] linkMerArray,
                                           double[] pointMerArray) {

        if (linkMerArray[0][0] != pointMerArray[0]
                || linkMerArray[0][1] != pointMerArray[1]) {
            reverseLinkOrder(linkMerArray);
        }
    }

    /**
     * 反转线数组顺序
     * @param linkMerArray
     */
    private static void reverseLinkOrder(double[][] linkMerArray) {
        int lenLinkArray = linkMerArray.length;

        int len = lenLinkArray / 2;

        double[] temp = null;

        for (int i = 0; i < len; i++) {
            temp = linkMerArray[i];
            linkMerArray[i] = linkMerArray[lenLinkArray - 1 - i];
            linkMerArray[lenLinkArray - 1 - i] = temp;
        }
    }

    /**
     * 判断node是否是link的端点
     * @param linkMerArray
     * @param pointMerArray
     * @return 0：不是；1：起点；2：终点
     */
    public static int isLinkEnd(double[][] linkMerArray,
                                double[] pointMerArray) {

        int iEndType = 0;
        if (linkMerArray[0][0] == pointMerArray[0]
                && linkMerArray[0][1] == pointMerArray[1]) {

            iEndType = 1;
        } else  if (linkMerArray[linkMerArray.length-1][0] == pointMerArray[0]
                && linkMerArray[linkMerArray.length-1][1] == pointMerArray[1]) {
            iEndType = 2;
        }

        return  iEndType;
    }

//    /**
//     * 求线的长度
//     * @param linkMerArray
//     * @return
//     */
//    public static double getLinkLength(double[][] linkMerArray) {
//
//        double length = 0;
//
//        for (int i = 0; i <= linkMerArray.length - 2; i++) {
//
//            double[] curPoint = linkMerArray[i];
//
//            double[] nextPoint = linkMerArray[i + 1];
//
//            length += Math.sqrt(Math.pow(nextPoint[0] - curPoint[0], 2)
//                    + Math.pow(nextPoint[1] - curPoint[1], 2));
//        }
//
//        length = Math.round(length * 100) / 100.0;
//
//        return length;
//    }

    /**
     * 求出引导坐标位置：注意
     * @param linkMerArray 线几何
     * @param guidePointDistance 与端点偏移距离
     * @param guidePosition 引导坐标位置返回值
     * @return 返回值为引导坐标所处的LINK形状段上的第几段，从0开始
     */
    private static int getGuidePosition(double[][] linkMerArray,
                                        double guidePointDistance, double[] guidePosition) throws Exception {

        int guideSeqNum = 0;

        for (int i = 0; i <= linkMerArray.length - 2; i++) {

            double[] curPoint = linkMerArray[i];

            double[] nextPoint = linkMerArray[i + 1];

            guideSeqNum = i;

//            double ppDistance = Math.sqrt(Math.pow(nextPoint[0] - curPoint[0],
//                    2) + Math.pow(nextPoint[1] - curPoint[1], 2));
            Geometry lineGeo = GeometryUtils.getLineFromPoint(curPoint, nextPoint);
            double ppDistance = GeometryUtils.getLinkLength(lineGeo);
            BigDecimal bigDecimal = new BigDecimal(ppDistance);
            bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
            ppDistance = bigDecimal.doubleValue();

            if (ppDistance >= guidePointDistance) {

                guidePosition[0] = curPoint[0]
                        + (guidePointDistance / ppDistance)
                        * (nextPoint[0] - curPoint[0]);
                guidePosition[1] = curPoint[1]
                        + (guidePointDistance / ppDistance)
                        * (nextPoint[1] - curPoint[1]);
                break;
            } else {
                BigDecimal disBigDecimal = new BigDecimal(guidePointDistance);
                disBigDecimal = disBigDecimal.subtract(bigDecimal);
                disBigDecimal = disBigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
                guidePointDistance = disBigDecimal.doubleValue();
//                guidePointDistance -= bigDecimal;
            }
        }

        return guideSeqNum;
    }

    // 按照引导坐标位置和线通行方向向右找vertiUnit=6米位置作为显示坐标位置

    /**
     *
     * @param linkMerArray    线几何
     * @param guidePosition   引导坐标几何
     * @param guideSeqNum     引导坐标在线的第几个段
     * @param vertiUnit       向右（向左）偏移距离：以引导坐标到所在线段的起点为向量，
     *                        正数表示向右偏移，负数表示向左偏移
     *                        注意：默认引导坐标点不会是所在线段的起点。
     * @return 显示坐标
     */
    private static double[] getDisplayPosition(double[][] linkMerArray,
                 double[] guidePosition, int guideSeqNum, double vertiUnit) throws Exception {

        double[] displayPosition = new double[2];

        double[] startPoint = linkMerArray[guideSeqNum];


//        double ppDistance = Math.sqrt(Math.pow(startPoint[0] - guidePosition[0],
//                2) + Math.pow(startPoint[1] - guidePosition[1], 2));
        Geometry lineGeo = GeometryUtils.getLineFromPoint(startPoint, guidePosition);
        double ppDistance = GeometryUtils.getLinkLength(lineGeo);

        //相似三角形+向量
        displayPosition[0] = guidePosition[0]
                + (vertiUnit / ppDistance)
                * (startPoint[1] - guidePosition[1]);
        displayPosition[1] = guidePosition[1]
                - (vertiUnit / ppDistance)
                * (startPoint[0] - guidePosition[0]);


        return displayPosition;
    }

    /*********************************************************************/

    public static double[] getCrossPointByAgl(Geometry inLink, Geometry outLink,
                                              Geometry point) throws Exception {
        Coordinate[] csIn = inLink.getCoordinates();
        Coordinate[] csOut = outLink.getCoordinates();
        Coordinate[] csPoint = point.getCoordinates();
        double csPointX = csPoint[0].x;
        double csPointY = csPoint[0].y;
        Geometry inAglGeo = null;
        if(csIn[0].x == csPointX && csIn[0].y == csPointY) {
            double[] p1 = new double[]{csIn[1].x,csIn[1].y};
            double[] p2 = new double[]{csIn[0].x,csIn[0].y};
            inAglGeo = GeometryUtils.getLineFromPoint(p1,p2);
        } else {
            double[] p1 = new double[]{csIn[csIn.length-2].x,csIn[csIn.length-2].y};
            double[] p2 = new double[]{csIn[csIn.length-1].x,csIn[csIn.length-1].y};
            inAglGeo = GeometryUtils.getLineFromPoint(p1,p2);
        }
        Geometry outAglGeo = null;
        if(csOut[0].x == csPointX && csOut[0].y == csPointY) {
            double[] p1 = new double[]{csOut[1].x,csOut[1].y};
            double[] p2 = new double[]{csOut[0].x,csOut[0].y};
            outAglGeo = GeometryUtils.getLineFromPoint(p1,p2);
        } else {
            double[] p1 = new double[]{csOut[csOut.length-2].x,csOut[csOut.length-2].y};
            double[] p2 = new double[]{csOut[csOut.length-1].x,csOut[csOut.length-1].y};
            outAglGeo = GeometryUtils.getLineFromPoint(p1,p2);
        }
        WKTWriter writer = new WKTWriter();
        String inLinkWkt = writer.write(inAglGeo);
        double inAgl = TipsGeomUtils.calIncloudedAngle(inLinkWkt, 3);
        String outLinkWkt = writer.write(outAglGeo);
        double outAgl = TipsGeomUtils.calIncloudedAngle(outLinkWkt, 3);
        double intersectionAgl = Math.abs(outAgl-inAgl);
        String computeLink = outLinkWkt;
        if(outAgl > inAgl) {
            if(intersectionAgl > 180) {
                computeLink = inLinkWkt;
            }
        } else {
            if(intersectionAgl <= 180) {
                computeLink = inLinkWkt;
            }
        }
        if(intersectionAgl > 180) {
            intersectionAgl = 360 - intersectionAgl;
        }
        double[] displayPoint = new double[2];
        if(Math.round(intersectionAgl) == 180) {
            String pointWkt = GeometryConvertor.jts2wkt(point);
            double[] disPoint = TipsGeomUtils.calDirAndDistPos(computeLink, pointWkt,
                    3, 30);
            displayPoint[0] = disPoint[0];
            displayPoint[1] = disPoint[1];
        }else{
            double base = Math.cos(Math.toRadians(intersectionAgl/2)) * 30;
            double vertiUnit = Math.sin(Math.toRadians(intersectionAgl/2)) * 30;
            double linkLength = GeometryUtils.getLinkLength(computeLink);
            boolean isExtend = false;
            if (base > linkLength) {
                vertiUnit = vertiUnit*(linkLength/base);
                base = linkLength;

                BigDecimal baseDecimal = new BigDecimal(base);
                baseDecimal = baseDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
                base = baseDecimal.doubleValue();

                BigDecimal vertiUnitDecimal = new BigDecimal(vertiUnit);
                vertiUnitDecimal = vertiUnitDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
                vertiUnit = vertiUnitDecimal.doubleValue();

                isExtend = true;
            }

            double[][] points = TipsGeomUtils.getLinkPointPosForDir(computeLink,true,base,vertiUnit);
            displayPoint[0] = points[1][0];
            displayPoint[1] = points[1][1];
            if(isExtend) {//利用相似三角形求出显示坐标
                double[] mainPoint = new double[2];
                mainPoint[0] = csPointX;
                mainPoint[1] = csPointY;
                Geometry geom = GeometryUtils.getLineFromPoint(mainPoint, displayPoint);
                double len = GeometryUtils.getLinkLength(geom);
                double k = len / 30;
                displayPoint[0] = ((displayPoint[0] - mainPoint[0]) / k) + mainPoint[0];
                displayPoint[1] = ((displayPoint[1] - mainPoint[1]) / k) + mainPoint[1];
            }
        }
        return displayPoint;
    }
//    // 获取路口主点挂接LINK角平分线30米位置点
//    public static double[] getCrossPoint(String inLinkWkt, String outLinkWkt,
//                                         String pointWkt) throws Exception {
//        double[] point = new double[2];
//
//        WKTReader reader = new WKTReader();
//
//        Geometry inLink = reader.read(inLinkWkt);
//
//        Geometry outLink = reader.read(outLinkWkt);
//
//        Geometry inPoint = reader.read(pointWkt);
//
//        double[] psInLink = new double[4];
//
//        double[] psOutLink = new double[4];
//
//        fillInOutPoints(inLink, outLink, inPoint, psInLink, psOutLink);
//
//        double[] ps = new double[2];
//
//        ps[0] = MercatorProjection
//                .longitudeToMetersX(inPoint.getCoordinate().x);
//
//        ps[1] = MercatorProjection.latitudeToMetersY(inPoint.getCoordinate().y);
//
//        // 交换进入线点位置
//        double temp = psInLink[0];
//
//        psInLink[0] = psInLink[2];
//
//        psInLink[2] = temp;
//
//        temp = psInLink[1];
//
//        psInLink[1] = psInLink[3];
//
//        psInLink[3] = temp;
//
//        double[] inLinkPoint = getDistance100mPoint(psInLink);
//
//        double[] outLinkPoint = getDistance100mPoint(psOutLink);
//
//        double midX = (inLinkPoint[0] + outLinkPoint[0]) / 2;
//
//        double midY = (inLinkPoint[1] + outLinkPoint[1]) / 2;
//
//        if (midX != ps[0]) {
//            double k = (midY - ps[1]) / (midX - ps[0]);
//
//            double c = ps[1] - k * ps[0];
//
//            if (ps[0] < midX) {
//                point[0] = ps[0] + 30;
//
//            } else {
//                point[0] = ps[0] - 30;
//            }
//
//            point[1] = k * point[0] + c;
//        } else {
//            point[0] = ps[0];
//
//            if (ps[1] < midY) {
//                point[1] = ps[1] + 30;
//            } else {
//                point[1] = ps[1] - 30;
//            }
//        }
//
//        point[0] = MercatorProjection.metersXToLongitude(point[0]);
//
//        point[1] = MercatorProjection.metersYToLatitude(point[1]);
//
//        return point;
//    }
//
//    private static double[] getDistance100mPoint(double link[]) {
//        double[] p = new double[2];
//
//        if (link[0] != link[2]) {
//            double k = (link[3] - link[1]) / (link[2] - link[0]);
//
//            double c = link[1] - k * link[0];
//
//            if (link[0] < link[2]) {
//                p[0] = link[0] + 100;
//            } else {
//                p[0] = link[0] - 100;
//            }
//
//            p[1] = k * p[0] + c;
//        } else {
//            p[0] = link[0];
//
//            if (link[1] < link[3]) {
//                p[1] = link[1] + 100;
//            } else {
//                p[1] = link[1] - 100;
//            }
//        }
//
//        return p;
//    }

//    private static void fillInOutPoints(Geometry inLink, Geometry outLink,
//                                        Geometry ps, double[] psInLink, double[] psOutLink) {
//
//        Coordinate[] csIn = inLink.getCoordinates();
//
//        Coordinate[] csOut = outLink.getCoordinates();
//
//        Point point = (Point) ps;
//
//        if (csIn[0].x == point.getX() && csIn[0].y == point.getY()) {
//
//            psInLink[0] = csIn[1].x;
//
//            psInLink[1] = csIn[1].y;
//
//            psInLink[2] = csIn[0].x;
//
//            psInLink[3] = csIn[0].y;
//
//            if (csOut[0].x == point.getX() && csOut[0].y == point.getY()) {
//
//                psOutLink[0] = csOut[0].x;
//
//                psOutLink[1] = csOut[0].y;
//
//                psOutLink[2] = csOut[1].x;
//
//                psOutLink[3] = csOut[1].y;
//            } else {
//                int len = csOut.length;
//
//                psOutLink[0] = csOut[len - 1].x;
//
//                psOutLink[1] = csOut[len - 1].y;
//
//                psOutLink[2] = csOut[len - 2].x;
//
//                psOutLink[3] = csOut[len - 2].y;
//            }
//
//        } else {
//
//            int len = csIn.length;
//
//            psInLink[0] = csIn[len - 2].x;
//
//            psInLink[1] = csIn[len - 2].y;
//
//            psInLink[2] = csIn[len - 1].x;
//
//            psInLink[3] = csIn[len - 1].y;
//
//            if (csOut[0].x == point.getX() && csOut[0].y == point.getY()) {
//
//                psOutLink[0] = csOut[0].x;
//
//                psOutLink[1] = csOut[0].y;
//
//                psOutLink[2] = csOut[1].x;
//
//                psOutLink[3] = csOut[1].y;
//            } else {
//                len = csOut.length;
//
//                psOutLink[0] = csOut[len - 1].x;
//
//                psOutLink[1] = csOut[len - 1].y;
//
//                psOutLink[2] = csOut[len - 2].x;
//
//                psOutLink[3] = csOut[len - 2].y;
//            }
//        }
//
//        psInLink[0] = MercatorProjection.longitudeToMetersX(psInLink[0]);
//
//        psInLink[1] = MercatorProjection.latitudeToMetersY(psInLink[1]);
//
//        psInLink[2] = MercatorProjection.longitudeToMetersX(psInLink[2]);
//
//        psInLink[3] = MercatorProjection.latitudeToMetersY(psInLink[3]);
//
//        psOutLink[0] = MercatorProjection.longitudeToMetersX(psOutLink[0]);
//
//        psOutLink[1] = MercatorProjection.latitudeToMetersY(psOutLink[1]);
//
//        psOutLink[2] = MercatorProjection.longitudeToMetersX(psOutLink[2]);
//
//        psOutLink[3] = MercatorProjection.latitudeToMetersY(psOutLink[3]);
//    }

    /*************************************************************************/

    /**
     *  求跨图幅的LINK，被图幅边界打断后的WKT列表
     * @param wkt
     * @return
     * @throws ParseException
     */
    public static List<String> getSplitLinkByMeshs(String wkt)
            throws ParseException {

        WKTReader reader = new WKTReader();

        WKTWriter writer = new WKTWriter();

        Geometry geomLink = reader.read(wkt);

        Geometry geomBound = geomLink.getBoundary();

        double minLon = 180, minLat = 90, maxLon = 0, maxLat = 0;

        Coordinate[] csBound = geomBound.getCoordinates();

        for (Coordinate c : csBound) {
            if (minLon > c.x) {
                minLon = c.x;
            }

            if (minLat > c.y) {
                minLat = c.y;
            }

            if (maxLon < c.x) {
                maxLon = c.x;
            }

            if (maxLat < c.y) {
                maxLat = c.y;
            }
        }

        // String[] meshs = MeshUtils.area2Meshes(minLon, minLat, maxLon,
        // maxLat);

        Set<String> meshs = new HashSet<String>();

        meshs.add(MeshUtils.lonlat2Mesh(minLon, minLat));

        meshs.add(MeshUtils.lonlat2Mesh(maxLon, minLat));

        meshs.add(MeshUtils.lonlat2Mesh(maxLon, maxLat));

        meshs.add(MeshUtils.lonlat2Mesh(minLon, maxLat));

        if (meshs.size() > 1) {

            List<String> list = new ArrayList<String>();

            for (String mesh : meshs) {

                String meshWkt = MeshUtils.mesh2WKT(mesh);

                Geometry geomMesh = reader.read(meshWkt);

                Geometry geomInter = geomMesh.intersection(geomLink);

                list.add(writer.write(geomInter));
            }

            return list;
        } else {
            return null;
        }

    }

    /**********************************************************************************/

    /**
     *  计算LINK与正北方向的夹角,tips初始化使用
     * @param wkt link 
     * @param direct  顺逆  ： 0,1,2按顺计算；3 按逆计算
     * @return
     * @throws Exception
     */
    public static double calIncloudedAngle(String wkt, int direct)
            throws Exception {
    	 WKTReader reader = new WKTReader();
    	Geometry link = reader.read(wkt);
        double[] points = getTraffic2Points(link, direct);
        return calAngle(points);
    }
    
    /**
     *  计算LINK与正北方向的夹角,robot自动录入使用
     * @param wkt link 
     * @param direct  顺逆  ： 0,1,2按顺计算；3 按逆计算
     * @return
     * @throws Exception
     */
    public static double calAngleAndNorth(String wkt, int direct)
            throws Exception {
    	 WKTReader reader = new WKTReader();
    	Geometry link = reader.read(wkt);
        double[] points = getTwoRecentPoints(link,direct);
        return calAngle(points);
    }
    
    
    public static double calAngle(double[] points)
    {
    	double includedAngle = 0;
        double startX = 0, startY = 0, stopX = 0, stopY = 0;
        startX = points[0];
        startY = points[1];
        stopX = points[2];
        stopY = points[3];
        if (startX != stopX && startY != stopY) {
            int quadrant = getQuadrant(startX, startY, stopX, stopY);
            switch (quadrant) {
                case 1:
                    includedAngle = 90
                            - Math.atan((stopY - startY) / (stopX - startX)) * 180
                            / Math.PI;
                    break;
                case 2:
                    includedAngle = Math.atan((stopY - startY) / (startX - stopX))
                            * 180 / Math.PI + 270;
                    break;
                case 3:
                    includedAngle = Math.atan((startX - stopX) / (startY - stopY))
                            * 180 / Math.PI + 180;
                    break;
                case 4:
                    includedAngle = Math.atan((startY - stopY) / (stopX - startX))
                            * 180 / Math.PI + 90;
                    break;
                default:
                    break;
            }
        } else {
            if (startX == stopX) {
                if (startY < stopY) {
                    includedAngle = 0;
                } else {
                    includedAngle = 180;
                }
            } else {
                if (startX < stopX) {
                    includedAngle = 90;
                } else {
                    includedAngle = 270;
                }
            }
        }
        return includedAngle;
    }
    
    /**
     *  计算LINK进入点附近的坐标
     * @param link link坐标 
     * @param direct  顺逆  ： 0,1,2按顺计算；3 按逆计算
     * @return
     * @throws Exception
     */
    private static double[] getTraffic2Points(Geometry link, int direct) {

        double[] points = new double[4];

        Coordinate[] cs = link.getCoordinates();

        if (direct == 0 || direct == 1 || direct == 2) {
            int len = cs.length;

            points[0] = cs[len - 2].x;

            points[1] = cs[len - 2].y;

            points[2] = cs[len - 1].x;

            points[3] = cs[len - 1].y;

        } else {

            points[0] = cs[1].x;

            points[1] = cs[1].y;

            points[2] = cs[0].x;

            points[3] = cs[0].y;
        }

        return points;
    }

    /**
     *  获取通行方向上，第1、2个形状点，自动录入使用
     * @param link link坐标 
     * @param direct  顺逆  ： 0,1,2按顺计算；3 按逆计算
     * @return
     * @throws Exception
     */
    public static double[] getTwoRecentPoints(Geometry link, int direct) {
        double[] points = new double[4];
        Coordinate[] cs = link.getCoordinates();
        if (direct == 0 || direct == 1 || direct == 2) {
            points[0] = cs[0].x;
            points[1] = cs[0].y;
            points[2] = cs[1].x;
            points[3] = cs[1].y;
        } else {
        	int len = cs.length;
            points[0] = cs[len - 1].x;
            points[1] = cs[len - 1].y;
            points[2] = cs[len - 2].x;
            points[3] = cs[len - 2].y;
        }
        return points;
    }
    
    /**
     *  获取通行方向上，倒数第2、1个点，计算3D分歧左右退出线位置用
     * @param link link坐标 
     * @param direct  顺逆  ： 0,1,2按顺计算；3 按逆计算
     * @return
     * @throws Exception
     */
    public static double[] getLastTwoPoints(Geometry link, int direct) {
        double[] points = new double[4];
        Coordinate[] cs = link.getCoordinates();
        int len = cs.length;
        if (direct == 0 || direct == 1 || direct == 2) {
            points[0] = cs[len - 2].x;
            points[1] = cs[len - 2].y;
            points[2] = cs[len - 1].x;
            points[3] = cs[len - 1].y;
        } else {
        	  points[0] = cs[1].x;
              points[1] = cs[1].y;
              points[2] = cs[0].x;
              points[3] = cs[0].y;
        }
        return points;
    }
    
    private static int getQuadrant(double startX, double startY, double stopX,
                                   double stopY) {

        if (startX < stopX) {
            if (startY < stopY) {
                // 第一象限
                return 1;
            } else {
                return 4;
            }
        } else {
            if (startY < stopY) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    // 求限速tips显示坐标位置

    /**
     * 根据引导坐标和方向，确定显示坐标的位置
     * @param linkWkt  关联link
     * @param pointWkt 引导坐标
     * @param moveDirect   方向
     * @param dist     偏移距离
     * @return 显示坐标
     * @throws Exception
     */
    public static double[] calDirAndDistPos(String linkWkt, String pointWkt,
                                            int moveDirect,double dist) throws Exception {
        double[] position = new double[2];

        double[][] linkMerArray = convertLinkToMerArray(linkWkt);

        double[] pointMerArray = convertPointToMerArray(pointWkt);

        double startx, starty, stopx, stopy,originalX,originalY;

        double range[] = findLinkRange(linkMerArray, pointMerArray, moveDirect);

        startx = range[0];

        starty = range[1];

        stopx = range[2];

        stopy = range[3];

        originalX = range[4];

        originalY = range[5];

        dist = GeometryUtils.convert2Degree(dist);
        BigDecimal distDecimal = new BigDecimal(dist);
        distDecimal = distDecimal.setScale(5, BigDecimal.ROUND_HALF_UP);
        dist = distDecimal.doubleValue();

        if (originalX != stopx) {
            double k = (originalY - stopy) / (originalX - stopx);

            if (originalY != stopy) {
                double c1 = starty - (-1 / k) * startx;

                if (k > 0) {
                    if (originalX < stopx) {

                        position[0] = startx + dist/ (Math.sqrt(1 + ((1 / k) * (1 + 1 / k))));

                        position[1] = position[0] * (-1 / k) + c1;
                    } else {
                        position[0] = startx - dist/ (Math.sqrt(1 + ((1 / k) * (1 + 1 / k))));

                        position[1] = position[0] * (-1 / k) + c1;
                    }
                } else {
                    if (originalX < stopx) {

                        position[0] = startx - dist
                                / (Math.sqrt(1 + ((1 / k) * (1 + 1 / k))));

                        position[1] = position[0] * (-1 / k) + c1;
                    } else {
                        position[0] = startx + dist
                                / (Math.sqrt(1 + ((1 / k) * (1 + 1 / k))));

                        position[1] = position[0] * (-1 / k) + c1;
                    }
                }
            } else {
                position[0] = startx;

                if (originalX < stopx) {

                    position[1] = starty - dist;
                } else {
                    position[1] = starty + dist;
                }
            }
        } else {
            position[1] = starty;

            if (originalY < stopy) {
                position[0] = startx + dist;
            } else {
                position[0] = startx - dist;
            }
        }

//        position[0] = MercatorProjection.metersXToLongitude(position[0]);
//
//        position[1] = MercatorProjection.metersYToLatitude(position[1]);

        return position;
    }

    private static double[] findLinkRange(double[][] linkMerArray,
                                          double[] pointMerArray, int direct) {
        double[] interPoint = new double[2];

        double range[] = new double[6];

        double startx = 0, starty = 0, stopx = 0, stopy = 0;

        if (direct == 2) {

            for (int i = 0; i < linkMerArray.length - 1; i++) {

                if (((linkMerArray[i][0] <= pointMerArray[0] && linkMerArray[i + 1][0] >= pointMerArray[0]) || (linkMerArray[i][0] >= pointMerArray[0] && linkMerArray[i + 1][0] <= pointMerArray[0]))
                        && ((linkMerArray[i][1] <= pointMerArray[1] && linkMerArray[i + 1][1] >= pointMerArray[1]) || (linkMerArray[i][1] >= pointMerArray[1] && linkMerArray[i + 1][1] <= pointMerArray[1]))) {

                    startx = linkMerArray[i][0];

                    starty = linkMerArray[i][1];

                    stopx = linkMerArray[i + 1][0];

                    stopy = linkMerArray[i + 1][1];

                    break;
                }

            }

        } else {

            for (int i = linkMerArray.length - 1; i > 0; i--) {

                if (((linkMerArray[i][0] <= pointMerArray[0] && linkMerArray[i - 1][0] >= pointMerArray[0]) || (linkMerArray[i][0] >= pointMerArray[0] && linkMerArray[i - 1][0] <= pointMerArray[0]))
                        && ((linkMerArray[i][1] <= pointMerArray[1] && linkMerArray[i - 1][1] >= pointMerArray[1]) || (linkMerArray[i][1] >= pointMerArray[1] && linkMerArray[i - 1][1] <= pointMerArray[1]))) {

                    startx = linkMerArray[i][0];

                    starty = linkMerArray[i][1];

                    stopx = linkMerArray[i - 1][0];

                    stopy = linkMerArray[i - 1][1];

                    break;
                }

            }
        }

        if ((starty - pointMerArray[1]) * (pointMerArray[0] - stopx) != (pointMerArray[1] - stopy)
                * (startx - pointMerArray[0])) {//引导坐标不在link段上

            if (startx != stopx) {

                if (starty != stopy) {

                    double k1 = (starty - stopy) / (startx - stopx);

                    double c1 = starty - k1 * startx;

                    double k2 = -1 / k1;

                    double c2 = pointMerArray[1] - k2 * pointMerArray[0];

                    interPoint[0] = (c2 - c1) / (k1 - k2);

                    interPoint[1] = k1 * interPoint[0] + c1;

                } else {//映射到线上，线水平
                    interPoint[0] = pointMerArray[0];

                    interPoint[1] = starty;
                }

            } else {
                interPoint[0] = startx;

                interPoint[1] = pointMerArray[1];
            }

        } else {//引导坐标在link段上，直接取引导坐标
            interPoint[0] = pointMerArray[0];

            interPoint[1] = pointMerArray[1];
        }

        range[0] = interPoint[0];

        range[1] = interPoint[1];

        range[2] = stopx;

        range[3] = stopy;

        range[4] = startx;

        range[5] = starty;

        return range;
    }

    /**
     * 查找坐标所在link段
     * @param linkWkt
     * @param pointWkt
     * @return
     * @throws Exception
     */
    public static String findPointLinkRange(String linkWkt, String pointWkt, int direct) throws Exception {
        double[][] linkMerArray = convertLinkToMerArray(linkWkt);
        double[] pointMerArray = convertPointToMerArray(pointWkt);
        double startx = 0;
        double starty = 0;
        double stopx = 0;
        double stopy = 0;
        if(direct == 3) {
            for (int i = linkMerArray.length - 1; i > 0; i--) {
                if (((linkMerArray[i][0] <= pointMerArray[0] && linkMerArray[i - 1][0] >= pointMerArray[0]) || (linkMerArray[i][0] >= pointMerArray[0] && linkMerArray[i - 1][0] <= pointMerArray[0]))
                        && ((linkMerArray[i][1] <= pointMerArray[1] && linkMerArray[i - 1][1] >= pointMerArray[1]) || (linkMerArray[i][1] >= pointMerArray[1] && linkMerArray[i - 1][1] <= pointMerArray[1]))) {
                    startx = linkMerArray[i - 1][0];
                    starty = linkMerArray[i - 1][1];
                    stopx = linkMerArray[i][0];
                    stopy = linkMerArray[i][1];
                    break;
                }
            }
        }else{
            for (int i = 0; i < linkMerArray.length - 1; i++) {
                if (((linkMerArray[i][0] <= pointMerArray[0] && linkMerArray[i + 1][0] >= pointMerArray[0]) || (linkMerArray[i][0] >= pointMerArray[0] && linkMerArray[i + 1][0] <= pointMerArray[0]))
                        && ((linkMerArray[i][1] <= pointMerArray[1] && linkMerArray[i + 1][1] >= pointMerArray[1]) || (linkMerArray[i][1] >= pointMerArray[1] && linkMerArray[i + 1][1] <= pointMerArray[1]))) {
                    startx = linkMerArray[i][0];
                    starty = linkMerArray[i][1];
                    stopx = linkMerArray[i + 1][0];
                    stopy = linkMerArray[i + 1][1];
                    break;
                }
            }
        }

        double[] startPoint = new double[2];
        startPoint[0] = startx;
        startPoint[1] = starty;
        double[] stopPoint = new double[2];
        stopPoint[0] = stopx;
        stopPoint[1] = stopy;
        Geometry linkGeo = GeometryUtils.getLineFromPoint(startPoint, stopPoint);
        return GeometryConvertor.jts2wkt(linkGeo);
    }

//    // 显示坐标：
//    // 取link中点坐标，限速的通行方向的右侧2米
//    public static double[] getMid2MPosition(String linkWkt, int direct)
//            throws Exception {
//        double[] position = new double[2];
//
//        double[][] linkMerArray = convertLinkToMerArray(linkWkt);
//
//        if (direct == 3) {
//            int len = linkMerArray.length;
//
//            for (int i = 0; i < len / 2; i++) {
//                double tmpPoint[] = linkMerArray[i];
//                linkMerArray[i] = linkMerArray[len - i - 1];
//                linkMerArray[len - i - 1] = tmpPoint;
//            }
//        }
//
//        double linkLength = GeometryUtils.getLinkLength(linkWkt);
//
//        double[] midPoint = new double[2];
//
//        double[] range = getMidPointRange(linkMerArray, linkLength, midPoint);
//
//        if (range[0] != range[2]) {
//
//            if (range[1] != range[3]) {
//
//                double k = (range[3] - range[1]) / (range[2] - range[0]);
//
//                if (k > 0) {
//                    if (range[2] > range[0]) {
//                        // 递增
//                        position[0] = midPoint[0] + (2 / (1 + k * k));
//
//                        position[1] = midPoint[1] - (2 * k / (1 + k * k));
//                    } else {
//                        position[0] = midPoint[0] - (2 / (1 + k * k));
//
//                        position[1] = midPoint[1] + (2 * k / (1 + k * k));
//                    }
//                } else {
//                    if (range[3] > range[1]) {
//                        // 递增
//                        position[0] = midPoint[0] - (2 / (1 + k * k));
//
//                        position[1] = midPoint[1] - (2 * k / (1 + k * k));
//                    } else {
//                        position[0] = midPoint[0] - (2 / (1 + k * k));
//
//                        position[1] = midPoint[1] + (2 * k / (1 + k * k));
//                    }
//                }
//
//            } else {
//                // 与X轴平行
//                if (range[2] > range[0]) {
//                    position[0] = midPoint[0];
//
//                    position[1] = midPoint[1] - 2;
//                } else {
//                    position[0] = midPoint[0];
//
//                    position[1] = midPoint[1] + 2;
//                }
//            }
//
//        } else {
//            // 与Y轴平行
//            if (range[3] > range[1]) {
//                position[0] = midPoint[0] + 2;
//
//                position[1] = midPoint[1];
//            } else {
//                position[0] = midPoint[0] - 2;
//
//                position[1] = midPoint[1];
//            }
//        }
//
////        position[0] = MercatorProjection.metersXToLongitude(position[0]);
////
////        position[1] = MercatorProjection.metersYToLatitude(position[1]);
//
//        return position;
//    }

//    private static double[] getMidPointRange(double[][] linkMerArray,
//                                             double linkLength, double[] midPoint) throws Exception {
//        double[] range = new double[4];
//
//        double halfLength = linkLength / 2;
//
//        for (int i = 0; i < linkMerArray.length - 1; i++) {
////            double len = Math.sqrt(Math.pow(linkMerArray[i][0]
////                    - linkMerArray[i + 1][0], 2)
////                    + Math.pow(linkMerArray[i][1] - linkMerArray[i + 1][1], 2));
//            Geometry lineGeo = GeometryUtils.getLineFromPoint(linkMerArray[i], linkMerArray[i+1]);
//            double len = GeometryUtils.getLinkLength(lineGeo);
//
//            if (len < halfLength) {
//                halfLength -= len;
//            } else {
//                range[0] = linkMerArray[i][0];
//
//                range[1] = linkMerArray[i][1];
//
//                range[2] = linkMerArray[i + 1][0];
//
//                range[3] = linkMerArray[i + 1][1];
//
//                if (len > halfLength) {
//
//                    double scale = halfLength / len;
//
//                    midPoint[0] = linkMerArray[i][0] + scale
//                            * (linkMerArray[i + 1][0] - linkMerArray[i][0]);
//
//                    midPoint[1] = linkMerArray[i][1] + scale
//                            * (linkMerArray[i + 1][1] - linkMerArray[i][1]);
//
//                    break;
//                } else {
//
//                    midPoint[0] = linkMerArray[i + 1][0];
//
//                    midPoint[1] = linkMerArray[i + 1][1];
//
//                    break;
//                }
//            }
//        }
//
//        return range;
//    }

    /**
     * 计算通行方向
     *
     * @param linkWkt
     *            进入线
     * @param pointWkt
     *            进入点
     * @return
     * @throws ParseException
     */
    public static int getDirect(String linkWkt, String pointWkt)
            throws ParseException {

        int direct = 2;

        Geometry link = new WKTReader().read(linkWkt);

        Geometry point = new WKTReader().read(pointWkt);

        Coordinate[] csLink = link.getCoordinates();

        Coordinate cPoint = point.getCoordinate();

        if (csLink[0].x == cPoint.x && csLink[0].y == cPoint.y) {
            direct = 3;
        }

        return direct;
    }

//    /**
//     * 计算在线上的距离端点距离为dist的点的坐标
//     * @param coord 墨卡托坐标
//     * @param next 墨卡托坐标
//     * @param dist 要获取的点距离coord点的距离（米）
//     * @return 经纬度坐标
//     */
//    private static Coordinate getPointOnLinkByDistance(Coordinate coord, Coordinate next, double dist){
//        Coordinate result = new Coordinate();
//
//        double distance = Math.sqrt(Math.pow(next.x - coord.x,
//                2) + Math.pow(next.y - coord.y, 2));
//
//
//        if (coord.x != next.x) {
//            double k = (coord.y - next.y)
//                    / (coord.x - next.x);
//
//            double c = coord.y - k * coord.x;
//
//            result.x = coord.x
//                    + (dist / distance)
//                    * (next.x - coord.x);
//
//            result.y = k * result.x + c;
//        } else {
//            // 与x轴垂直
//
//            result.x = coord.x;
//
//            if (coord.y < next.y) {
//                result.y = coord.y + dist;
//            } else {
//                result.y  = coord.y - dist;
//            }
//        }
//
//        // 转换墨卡托坐标为经纬度坐标返回
//
//        result.x = MercatorProjection
//                .metersXToLongitude(result.x);
//
//        result.y = MercatorProjection
//                .metersYToLatitude(result.y);
//
//        return result;
//    }

//    public static LineString getGscLine4Web(Geometry linkGeo, int startEnd, int seqNum, int z) throws Exception{
//
//        double offset = 10;
//
//        switch(z){
//            case 16:
//            case 17:
//                offset = 8; break;
//            case 18:
//                offset = 5; break;
//            case 19:
//                offset = 4; break;
//            case 20:
//                offset = 3; break;
//        }
//
//        double[][] linkMerArray = convertLinkToMerArray(linkGeo);
//
//        Coordinate[] coords = linkGeo.getCoordinates();
//
//        List<Coordinate> coordList = new ArrayList<Coordinate>();
//
//        Coordinate coord = new Coordinate(linkMerArray[seqNum][0],linkMerArray[seqNum][1]);
//
//        if (startEnd == 0) {
//
//            if (seqNum - 1 >= 0)
//            {
//                Coordinate next = new Coordinate(linkMerArray[seqNum-1][0],linkMerArray[seqNum-1][1]);
//
//                Coordinate result = getPointOnLinkByDistance(coord, next, offset);
//
//                coordList.add(result);
//            }
//
//            coordList.add(coords[seqNum]);
//
//            if ((seqNum + 1) < coords.length) {
//
//                Coordinate next = new Coordinate(linkMerArray[seqNum+1][0],linkMerArray[seqNum+1][1]);
//
//                Coordinate result = getPointOnLinkByDistance(coord, next, offset);
//
//                coordList.add(result);
//            }
//        }else if (startEnd == 1) {
//
//            coordList.add(coords[seqNum]);
//
//            if ((seqNum + 1) < coords.length) {
//
//                Coordinate next = new Coordinate(linkMerArray[seqNum+1][0],linkMerArray[seqNum+1][1]);
//
//                Coordinate result = getPointOnLinkByDistance(coord, next, offset);
//
//                coordList.add(result);
//            }
//        } else {
//
//            coordList.add(coords[seqNum]);
//
//            if (seqNum - 1 >= 0){
//
//                Coordinate next = new Coordinate(linkMerArray[seqNum-1][0],linkMerArray[seqNum-1][1]);
//
//                Coordinate result = getPointOnLinkByDistance(coord, next, offset);
//
//                coordList.add(result);
//            }
//        }
//
//        Coordinate[] newCoords = new Coordinate[coordList.size()];
//
//        for (int i = 0; i < coordList.size(); i++) {
//            newCoords[i] = coordList.get(i);
//        }
//
//        LineString line = geometryFactory.createLineString(newCoords);
//
//        return line;
//    }

//    /**
//     * 计算立交的线几何
//     * @param linkGeo
//     * @param startEnd
//     * @param seqNum
//     * @return
//     */
//    public static LineString getGscLine(Geometry linkGeo, int startEnd, int seqNum){
//        Coordinate[] coords = linkGeo.getCoordinates();
//
//        List<Coordinate> coordList = new ArrayList<Coordinate>();
//
//        if (startEnd == 0) {
//
//            double dist = 0;
//
//            int current = seqNum;
//
//            while (dist < 20) {
//
//                if (current - 1 < 0) {
//                    break;
//                }
//
//                Coordinate coord = coords[current];
//
//                Coordinate next = coords[current - 1];
//
//                coordList.add(next);
//
//                dist += GeometryUtils.getDistance(coord, next);
//
//                current--;
//            }
//
//            Collections.reverse(coordList);
//
//            dist = 0;
//
//            current = seqNum;
//
//            coordList.add(coords[seqNum]);
//
//            while (dist < 20) {
//
//                if ((current + 1) >= coords.length) {
//                    break;
//                }
//
//                Coordinate coord = coords[current];
//
//                Coordinate next = coords[current + 1];
//
//                coordList.add(next);
//
//                dist += GeometryUtils.getDistance(coord, next);
//
//                current++;
//            }
//        } else if (startEnd == 1) {
//            double dist = 0;
//
//            int current = 0;
//
//            coordList.add(coords[0]);
//
//            while (dist < 20) {
//
//                if ((current + 1) >= coords.length) {
//                    break;
//                }
//
//                Coordinate coord = coords[current];
//
//                Coordinate next = coords[current + 1];
//
//                coordList.add(next);
//
//                dist += GeometryUtils.getDistance(coord, next);
//
//                current++;
//            }
//        } else {
//            double dist = 0;
//
//            int current = coords.length - 1;
//
//            coordList.add(coords[current]);
//
//            while (dist < 20) {
//
//                if (current - 1 < 0) {
//                    break;
//                }
//
//                Coordinate coord = coords[current];
//
//                Coordinate next = coords[current - 1];
//
//                coordList.add(next);
//
//                dist += GeometryUtils.getDistance(coord, next);
//
//                current--;
//            }
//
//            Collections.reverse(coordList);
//
//        }
//
//        Coordinate[] newCoords = new Coordinate[coordList.size()];
//
//        for (int i = 0; i < coordList.size(); i++) {
//            newCoords[i] = coordList.get(i);
//        }
//
//        LineString line = geometryFactory.createLineString(newCoords);
//
//        return line;
//    }

    public static void main(String[] args) throws Exception {
        String wkt = "LINESTRING(116.48686 40.01237, 116.48690 40.01244)";

        System.out.println(calIncloudedAngle(wkt, 2));
    }
}