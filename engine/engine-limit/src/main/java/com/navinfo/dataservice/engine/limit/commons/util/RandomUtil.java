package com.navinfo.dataservice.engine.limit.commons.util;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11-5-6
 * Time: 下午1:35
 * To change this template use File | Settings | File Templates.
 */

import java.util.Random;

/**
 * 随机数工具类
 */
public abstract class RandomUtil {
    public static final String RANDOM_NUMBER = "0123456789";
    public static final String RANDOM_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final Random RANDOM = new Random();

    /**
     * 从一个范围创建随机数，范围包含start,不包含end
     *
     * @param r
     * @param start
     * @param end
     * @return
     */
    public static int nextInt(Random r, int start, int end) {
        int l = end - start + 1;
        return r.nextInt(l) + start;
    }

    /**
     * 从一个范围创建随机数，范围包含start,不包含end
     *
     * @param start
     * @param end
     * @return
     */
    public static int nextInt(int start, int end) {
        return nextInt(RANDOM, start, end);
    }

    /**
     * 从一个范围创建随机数，范围包含start,不包含end
     *
     * @param r
     * @param start
     * @param end
     * @return
     */
    public static long nextLong(Random r, long start, long end) {
        long l = end - start + 1;
        long v = r.nextLong();
        if (v < 0)
            v = -v;
        return v % l + start;
    }

    /**
     * * 从一个范围创建随机数，范围包含start,不包含end
     *
     * @param start
     * @param end
     * @return
     */
    public static long nextLong(long start, long end) {
        return nextLong(RANDOM, start, end);
    }

    /**
     * 根据char的集合创建定长的字符串
     *
     * @param random
     * @param seekStr
     * @param len
     * @return
     */
    public static String nextString(Random random, CharSequence seekStr, int len) {
        StringBuffer buf = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            int idx = random.nextInt(seekStr.length());
            buf.append(seekStr.charAt(idx));
        }
        return buf.toString();
    }

    /**
     * 根据char的集合创建定长的字符串
     *
     * @param seekStr
     * @param len
     * @return
     */
    public static String nextString(CharSequence seekStr, int len) {
        return nextString(RANDOM, seekStr, len);
    }

    /**
     * 得到一串给定长度的随机数字
     *
     * @param len
     * @return
     */
    public static String nextNumberStr(int len) {
        return nextString(RANDOM_NUMBER, len);
    }

    /**
     * 根据一串给定长度的随机字符串<BR>
     * 字符串包括[a-zA-Z0-9_]
     *
     * @param len
     * @return
     */
    public static String nextString(int len) {
        return nextString(RANDOM_STRING, len);
    }

}