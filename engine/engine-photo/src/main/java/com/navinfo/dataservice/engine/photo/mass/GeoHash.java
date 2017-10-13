package com.navinfo.dataservice.engine.photo.mass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by maxiaoyu4030 on 2017/5/25.
 */
public class GeoHash {
    private LocationBean location;
    /**
     * 1 2500km;2 630km;3 78km;4 30km
     * 5 2.4km; 6 610m; 7 76m; 8 19m
     */
    private int hashLength = 8; //经纬度转化为geohash长度
    private int latLength = 20; //纬度转化为二进制长度
    private int lngLength = 20; //经度转化为二进制长度

    private double minLat;//每格纬度的单位大小
    private double minLng;//每个经度的倒下
    private static final char[] CHARS = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public GeoHash(double lat, double lng) {
        location = new LocationBean(lat, lng);
        setMinLatLng();
    }
    public GeoHash(){
        setMinLatLng();
    }
    public int gethashLength() {
        return hashLength;
    }

    /**
     * @Description: 设置经纬度的最小单位
     */
    private void setMinLatLng() {
        minLat = LocationBean.MAXLAT - LocationBean.MINLAT;
        for (int i = 0; i < latLength; i++) {
            minLat /= 2.0;
        }
        minLng = LocationBean.MAXLNG - LocationBean.MINLNG;
        for (int i = 0; i < lngLength; i++) {
            minLng /= 2.0;
        }
    }

    /**
     * @Description: 求所在坐标点及周围点组成的九个
     */
    public List<String> getGeoHashBase32For9() {
        double leftLat = location.getLat() - minLat;
        double rightLat = location.getLat() + minLat;
        double upLng = location.getLng() - minLng;
        double downLng = location.getLng() + minLng;
        List<String> base32For9 = new ArrayList<String>();
        //左侧从上到下 3个
        String leftUp = getGeoHashBase32(leftLat, upLng);
        if (!(leftUp == null || "".equals(leftUp))) {
            base32For9.add(leftUp);
        }
        String leftMid = getGeoHashBase32(leftLat, location.getLng());
        if (!(leftMid == null || "".equals(leftMid))) {
            base32For9.add(leftMid);
        }
        String leftDown = getGeoHashBase32(leftLat, downLng);
        if (!(leftDown == null || "".equals(leftDown))) {
            base32For9.add(leftDown);
        }
        //中间从上到下 3个
        String midUp = getGeoHashBase32(location.getLat(), upLng);
        if (!(midUp == null || "".equals(midUp))) {
            base32For9.add(midUp);
        }
        String midMid = getGeoHashBase32(location.getLat(), location.getLng());
        if (!(midMid == null || "".equals(midMid))) {
            base32For9.add(midMid);
        }
        String midDown = getGeoHashBase32(location.getLat(), downLng);
        if (!(midDown == null || "".equals(midDown))) {
            base32For9.add(midDown);
        }
        //右侧从上到下 3个
        String rightUp = getGeoHashBase32(rightLat, upLng);
        if (!(rightUp == null || "".equals(rightUp))) {
            base32For9.add(rightUp);
        }
        String rightMid = getGeoHashBase32(rightLat, location.getLng());
        if (!(rightMid == null || "".equals(rightMid))) {
            base32For9.add(rightMid);
        }
        String rightDown = getGeoHashBase32(rightLat, downLng);
        if (!(rightDown == null || "".equals(rightDown))) {
            base32For9.add(rightDown);
        }
        return base32For9;
    }
    /**
     * @Description: 求所在坐标点及周围点组成的九个
     */
    public List<String> getGeoHashBase32For9(double lat,double lon) {
        double leftLat = lat - minLat;
        double rightLat = lat + minLat;
        double upLng = lon - minLng;
        double downLng = lon + minLng;
        List<String> base32For9 = new ArrayList<String>();
        //左侧从上到下 3个
        String leftUp = getGeoHashBase32(leftLat, upLng);
        if (!(leftUp == null || "".equals(leftUp))) {
            base32For9.add(leftUp);
        }
        String leftMid = getGeoHashBase32(leftLat,lon);
        if (!(leftMid == null || "".equals(leftMid))) {
            base32For9.add(leftMid);
        }
        String leftDown = getGeoHashBase32(leftLat, downLng);
        if (!(leftDown == null || "".equals(leftDown))) {
            base32For9.add(leftDown);
        }
        //中间从上到下 3个
        String midUp = getGeoHashBase32(lat, upLng);
        if (!(midUp == null || "".equals(midUp))) {
            base32For9.add(midUp);
        }
        String midMid = getGeoHashBase32(lat, lon);
        if (!(midMid == null || "".equals(midMid))) {
            base32For9.add(midMid);
        }
        String midDown = getGeoHashBase32(lat, downLng);
        if (!(midDown == null || "".equals(midDown))) {
            base32For9.add(midDown);
        }
        //右侧从上到下 3个
        String rightUp = getGeoHashBase32(rightLat, upLng);
        if (!(rightUp == null || "".equals(rightUp))) {
            base32For9.add(rightUp);
        }
        String rightMid = getGeoHashBase32(rightLat, lon);
        if (!(rightMid == null || "".equals(rightMid))) {
            base32For9.add(rightMid);
        }
        String rightDown = getGeoHashBase32(rightLat, downLng);
        if (!(rightDown == null || "".equals(rightDown))) {
            base32For9.add(rightDown);
        }
        return base32For9;
    }

    /**
     * @Description: 9位GeoHash时，求所在坐标点及周围点组成的九个
     * int x= 0 代表得到一个小格
     * int x= 1 代表得到9(3*3)个小格
     * int x= 2 代表得到25(5*5)个小格
     * int x= 3 代表得到49(7*7)个小格
     */
    public List<String> getGeoHashBase32For9_X(double lat,double lon,int x) {
        double leftLat = 0.0;
        double upLng = 0.0;
        List<String> base32ForX = new ArrayList<String>();

        for(int i=0;i<(2*x+1);i++)
        {
            leftLat = lat + ((i-x)*minLat);

            for(int j=0;j<(2*x+1);j++)
            {
                upLng = lon + ((j-x)*minLng);

                String leftUp = getGeoHashBase32(leftLat, upLng);
                if (!(leftUp == null || "".equals(leftUp))) {
                    base32ForX.add(leftUp);
                }
            }

        }

        return base32ForX;
    }

    /**
     * @param length
     * @Description: 设置经纬度转化为geohash长度
     */
    public boolean sethashLength(int length) {
        if (length < 1) {
            return false;
        }
        hashLength = length;
        latLength = (length * 5) / 2;
        if (length % 2 == 0) {
            lngLength = latLength;
        } else {
            lngLength = latLength + 1;
        }
        setMinLatLng();
        return true;
    }

    /**
     * @Description: 获取经纬度的base32字符串
     */
    public String getGeoHashBase32() {
        return getGeoHashBase32(location.getLat(), location.getLng());
    }

    /**
     * @param lat
     * @param lng
     * @Description: 获取经纬度的base32字符串
     */
    public String getGeoHashBase32(double lat, double lng) {
        boolean[] bools = getGeoBinary(lat, lng);
        if (bools == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bools.length; i = i + 5) {
            boolean[] base32 = new boolean[5];
            for (int j = 0; j < 5; j++) {
                base32[j] = bools[i + j];
            }
            char cha = getBase32Char(base32);
            if (' ' == cha) {
                return null;
            }
            sb.append(cha);
        }
        return sb.toString();
    }

    /**
     * @param base32
     * @Description: 将五位二进制转化为base32
     */
    private char getBase32Char(boolean[] base32) {
        if (base32 == null || base32.length != 5) {
            return ' ';
        }
        int num = 0;
        for (boolean bool : base32) {
            num <<= 1;
            if (bool) {
                num += 1;
            }
        }
        return CHARS[num % CHARS.length];
    }

    /**
     * @param lat
     * @param lng
     * @Description: 获取坐标的geo二进制字符串
     */
    private boolean[] getGeoBinary(double lat, double lng) {
        boolean[] latArray = getHashArray(lat, LocationBean.MINLAT, LocationBean.MAXLAT, latLength);
        boolean[] lngArray = getHashArray(lng, LocationBean.MINLNG, LocationBean.MAXLNG, lngLength);
        return merge(latArray, lngArray);
    }

    /**
     * @Description: 合并经纬度二进制
     */
    private boolean[] merge(boolean[] latArray, boolean[] lngArray) {
        if (latArray == null || lngArray == null) {
            return null;
        }
        boolean[] result = new boolean[lngArray.length + latArray.length];
        Arrays.fill(result, false);
        for (int i = 0; i < lngArray.length; i++) {
            result[2 * i] = lngArray[i];
        }
        for (int i = 0; i < latArray.length; i++) {
            result[2 * i + 1] = latArray[i];
        }
        return result;
    }

    /**
     * @Description: 将数字转化为geohash二进制字符串
     */
    private boolean[] getHashArray(double value, double min, double max, int length) {
        if (value < min || value > max) {
            return null;
        }
        if (length < 1) {
            return null;
        }
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            double mid = (min + max) / 2.0;
            if (value > mid) {
                result[i] = true;
                min = mid;
            } else {
                result[i] = false;
                max = mid;
            }
        }
        return result;
    }

    public static void main(String[] args){
        //106.656001,31.136457
        //116.958487,-28.194083
        //-107.045304,-60.821006
        //39.9571400282118,116.578614908854
//        double lat=39.9571400282118;
//        double lon=116.578614908854;
//        GeoHash g=new GeoHash(lat,lon);
//        String hash1=g.getGeoHashBase32();
//        List<String> list=g.getGeoHashBase32For9();
//        g.sethashLength(9);
//        String hash11=g.getGeoHashBase32();
//        List<String> list2=g.getGeoHashBase32For9();
//
//        GeoHash gg=new GeoHash(-60.821006,-107.045304);
//        String hash2=gg.getGeoHashBase32();
//        gg.sethashLength(9);
//        String hash21=gg.getGeoHashBase32();
//        List<String>attrs=g.getGeoHashBase32For9();
//        int kk=0;

        GeoHash g_x=new GeoHash();
        g_x.sethashLength(9);
        List<String> attrs_9=g_x.getGeoHashBase32For9(-60.821006,-107.045304);

        List<String> attrs_x=g_x.getGeoHashBase32For9_X(-60.821006,-107.045304,1);

        GeoHash aa_x=new GeoHash();
        aa_x.sethashLength(8);
        List<String> aa_9=aa_x.getGeoHashBase32For9(-60.821006,-107.045304);

        List<String> aaa_x=aa_x.getGeoHashBase32For9_X(-60.821006,-107.045304,1);

        int kk=0;

    }
}
