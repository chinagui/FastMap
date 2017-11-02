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
     * 最大可连接汉字数
     */
    static Integer MAX_CONNECTION_CHARACTER = 3;

    /**
     * 是否处理多音字标识
     * 1：不处理（默认值）
     * 2：处理
     */
    static final String HANDLE_POLYPHONIC_WORD = "2";

    /**
     * 默认分割字符
     */
    static final String SPLIT_WORD = " ";

    static final Integer OR_SELECTED_COMBINED_SPEC = 1;

    static final Integer SELECTED_COMBINED_SPEC = 2;

    static final Integer SPEC_COMBINED = 3;

    static final Integer SPEC = 4;

    static final Integer PRIORITY = 5;

    static final Map<String, String> CHINESE_NUMBER = new HashMap<>();

    static {
        CHINESE_NUMBER.put("零", "0");
        CHINESE_NUMBER.put("一", "1");
        CHINESE_NUMBER.put("二", "2");
        CHINESE_NUMBER.put("三", "3");
        CHINESE_NUMBER.put("四", "4");
        CHINESE_NUMBER.put("五", "5");
        CHINESE_NUMBER.put("六", "6");
        CHINESE_NUMBER.put("七", "7");
        CHINESE_NUMBER.put("八", "8");
        CHINESE_NUMBER.put("九", "9");
    };

    /**
     * 方位词
     */
    static final Map<String, String> POSITION_WORD = new HashMap<>();

    static {
        POSITION_WORD.put("东", "East ");
        POSITION_WORD.put("西", "West ");
        POSITION_WORD.put("南", "South ");
        POSITION_WORD.put("北", "North ");
        POSITION_WORD.put("中", "Middle ");
    };

    /**
     * 特殊结尾词
     */
    static final Map<String, String> END_KEY_WORD = new HashMap<>();

    static  {
        END_KEY_WORD.put("收费站", "Toll Gate ");
        END_KEY_WORD.put("收费点", "Toll Gate ");
        END_KEY_WORD.put("收费处", "Toll Gate ");
        END_KEY_WORD.put("岗", "Post ");
        END_KEY_WORD.put("口", "Intersection ");
        END_KEY_WORD.put("桥", "Brg ");
        END_KEY_WORD.put("岛", "Island ");
        END_KEY_WORD.put("宫", "Temple ");
        END_KEY_WORD.put("庙", "Temple ");
        END_KEY_WORD.put("寺", "Temple ");
        END_KEY_WORD.put("祠", "Temple ");
        END_KEY_WORD.put("墓", "Tomb ");
        END_KEY_WORD.put("塔", "Tower ");
        END_KEY_WORD.put("林", "Wood ");
        END_KEY_WORD.put("苑", "Court ");
        END_KEY_WORD.put("陵", "Mausoleum ");
        END_KEY_WORD.put("峰", "Peak ");
        END_KEY_WORD.put("井", "Well ");

        END_KEY_WORD.put("线", "Line ");
    };


    /**
     * 自定义特殊转换规则
     * 使用指定字符替换$$
     */
    static final Map<String, String> SYMBOL_WORD = new HashMap<>();

    static {{
        SYMBOL_WORD.put("国道", "G$$");
        SYMBOL_WORD.put("省道", "S$$");
        SYMBOL_WORD.put("县道", "X$$");
    }};

    /**
     * 罗马字符对应阿拉伯数字
     */
    static final Map<String, String> ROMA_NUMBER = new HashMap<>();

    static {{
        ROMA_NUMBER.put("Ⅰ", "1");
        ROMA_NUMBER.put("Ⅱ", "2");
        ROMA_NUMBER.put("Ⅲ", "3");
        ROMA_NUMBER.put("Ⅳ", "4");
        ROMA_NUMBER.put("Ⅴ", "5");
        ROMA_NUMBER.put("Ⅵ", "6");
        ROMA_NUMBER.put("Ⅶ", "7");
        ROMA_NUMBER.put("Ⅷ", "8");
        ROMA_NUMBER.put("Ⅸ", "9");
        ROMA_NUMBER.put("Ⅹ", "10");
        ROMA_NUMBER.put("Ⅺ", "11");
        ROMA_NUMBER.put("Ⅻ", "12");
    }};

}
