package com.navinfo.dataservice.commons.util;

import java.security.MessageDigest;

public class MD5Utils {
//CREATE OR REPLACE FUNCTION JAVA_MD5 (P_VALUE VARCHAR2) RETURN VARCHAR2
//AS LANGUAGE JAVA
//NAME 'MD5Utils.md5(java.lang.String) RETURN java.lang.String';
///;
    private static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";

        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;

            // if (n<b.length-1) hs=hs+":";
        }
        return hs.toUpperCase();
    }

    public static String md5(String value) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(value.getBytes("UTF-8"));

            byte[] guid = md5.digest();

            return (byte2hex(guid));

        } catch (Exception e) {
            return null;
        }

    }


    public static boolean stringEqual(String befMd5, String afMd5) {
        if (md5(befMd5).equals(afMd5) || befMd5.equals(afMd5)) {
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        for(int i=0;i<1000000;i++){
            md5("11111111111111111111111111111111111112dddddddddddddddddddddddddddddddddddddddd"+i);
        }
        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1));
    }
}
