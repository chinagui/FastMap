package com.navinfo.dataservice.engine.meta.translates;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: Param
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 7/26/2017
 * @Version: V1.0
 */
public class Param {

    /**
     * 行政区划代码（该字段预留，暂未使用）
     */
    public String adminCode = "";

    /**
     * 处理过多音字后的拼音内容
     */
    public List<String> pinyins;

    /**
     * 记录未查询到对应英文的汉字下标
     */
    public List<Integer> wordIndex;

    /**
     * POI、Hamlet对应KIND_CODE
     */
    public String kindCode;

    /**
     * POI、Hamlet对应CHAIN
     */
    public String chain;

    /**
     * 特殊翻译原则PRIORITY
     */
    public String priority;

    /**
     * 最后一位译文的
     */
    public List<String> translateWords;

}
