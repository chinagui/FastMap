package com.navinfo.dataservice.engine.meta.translates;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: TranslateConstant
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: 翻译-常量类
 * @Author: Crayeres
 * @Date: 04/28/17
 * @Version: V1.0
 */
public class TranslateConstant {
    private TranslateConstant() {
    }

    /**
     * 半角字符转全角字符
     */
    static Integer HALF_TO_FULL = 1;

    /**
     * 全角字符转半角字符
     */
    static Integer FULL_TO_HALF = 2;

    /**
     * 是否处理多音字标识
     * 1：不处理（默认值）
     * 2：处理
     */
    static final String HANDLE_POLYPHONIC_WORD = "2";

    /**
     * 方位词
     */
    static final Map<String, String> POSITION_WORD = new HashMap<String, String>() {{
        put("东", "East ");
        put("西", "West ");
        put("南", "South ");
        put("北", "North ");
        put("中", "Middle ");
    }};

    /**
     * 特殊结尾词
     */
    static final Map<String, String> END_KEY_WORD = new HashMap<String, String>() {{
        put("收费站", "Toll Gate ");
        put("收费点", "Toll Gate ");
        put("收费处", "Toll Gate ");
        put("岗", "Post ");
        put("口", "Intersection ");
        put("桥", "Bridge ");
        put("岛", "Island ");
        put("宫", "Temple ");
        put("庙", "Temple ");
        put("寺", "Temple ");
        put("祠", "Temple ");
        put("墓", "Tomb ");
        put("塔", "Tower ");
        put("林", "Wood ");
        put("苑", "Court ");
        put("陵", "Mausoleum ");
        put("峰", "Peak ");
        put("井", "Well ");
    }};


    /**
     * 自定义特殊转换规则
     * 使用指定字符替换$$
     */
    static final Map<String, String> SYMBOL_WORD = new HashMap<String, String>() {{
        put("国道", "G$$");
        put("省道", "S$$");
        put("县道", "X$$");
    }};

}
