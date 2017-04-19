package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.meta.translate.TranslateDictData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Title: EnglishConvert
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: 中文字符串转英文核心类
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.0
 */
public class EnglishConvert {

    public EnglishConvert() {
    }

    public EnglishConvert(String convertType, String adminCode) {
        if (HANDLE_POLYPHONIC_WORD.equals(convertType)) {
            this.convertPolyhonic = true;
        }
        this.adminCode = adminCode;
    }

    /**
     * 是否处理多音字标识
     * 1：不处理（默认值）
     * 2：处理
     */
    private static final String HANDLE_POLYPHONIC_WORD = "2";

    /**
     * 是否处理多音字标识,与HANDLE_POLYPHONIC_WORD对应
     * 1. false（默认值）
     * 2. true
     */
    private boolean convertPolyhonic = false;

    /**
     * 行政区划代码（该字段预留，暂未使用）
     */
    private String adminCode = "";

    /**
     * 汉字转英文对照表
     */
    private static final Map<String, String> CHI_TO_ENG_MAP = TranslateDictData.getInstance().getDictChi2Eng();

    /**
     * 字典表
     */
    private static final Map<String, List<String>> DICTIONARY_MAP = TranslateDictData.getInstance().getDictDictionary();

    /**
     * 汉字转拼音对照表
     */
    private static final Map<String, List<Map<String, String>>> DICT_WORD_MAP = TranslateDictData.getInstance().getDictWord();

    /**
     * 英文翻译接口
     *
     * @param sourceText 待转换为英文的字符串
     * @return 翻译后字符串
     */
    public String convert(String sourceText) {
        if (StringUtils.isEmpty(sourceText)) {
            return sourceText;
        }

        String result = this.replaceKeyWord(sourceText);

        result = SplitUtil.split(result);

        String[] tmpArr = ConvertUtil.delNoChinese(result);

        result = ConvertUtil.removeRepeatBackSlash(result);

        result = this.convertKernel(result);

        result = ConvertUtil.removeRepeatSpace(result);

        result = ConvertUtil.firstCapital(result);

        result = ConvertUtil.trimSymbolSpace(result);

        return result;
    }

    /**
     * 翻译核心方法
     *
     * @param sourceText 待翻译字符串
     * @return 翻译后文本
     */
    private String convertKernel(String sourceText) {
        sourceText = sourceText.replaceAll("\t", " ");
        sourceText = sourceText.replaceAll("\n", " ");
        sourceText = sourceText.replaceAll("\r", " ");

        sourceText = this.convertSpecialWord(sourceText);

        // 目前不对数字进行翻译
        // sourceText = ConvertUtil.digital2Pinyin(sourceText);

        // 转英文
        sourceText = this.convertEnglishCharacter(sourceText);

        sourceText = ConvertUtil.removeSymbolWord(sourceText);

        sourceText = ConvertUtil.convertFull2Half(sourceText);

        sourceText = ConvertUtil.removeRepeatSpace(sourceText);

        if (ConvertUtil.hasChineseWord(sourceText)) {
            sourceText = this.convertChineseCharacter(sourceText);
        }

        return sourceText;
    }

    /**
     * 处理自定义字符串
     * @param sourceText 待处理文本
     * @return 处理后字符串
     */
    private String convertSpecialWord(String sourceText) {
        String regex = "[Ａ-Ｚａ-ｚ０-９0-9]+";
        Pattern pattern = Pattern.compile(regex);

        String[] words = sourceText.split("/");

        StringBuffer result = new StringBuffer();

        for (int index = 0, length = words.length; index < length; index++) {
            String word = words[index];
            if (StringUtils.isNotEmpty(result.toString())) {
                result.append("/");
            }

            if (index < (length - 1) && pattern.matcher(word).matches()) {
                String wordValue = "";
                String oneWord = words[index + 1];
                if (index + 2 < length) {
                    String twoWord = oneWord + words[index + 2];
                    if (SYMBOL_WORD.containsKey(twoWord)) {
                        wordValue = SYMBOL_WORD.get(twoWord).replace("$$", word);
                        index = index + 2;
                    } else if (SYMBOL_WORD.containsKey(oneWord)) {
                        wordValue = SYMBOL_WORD.get(oneWord).replace("$$", word);
                        index = index + 1;
                    } else {
                        wordValue = word;
                    }
                } else {
                    if (SYMBOL_WORD.containsKey(oneWord)) {
                        wordValue = SYMBOL_WORD.get(oneWord).replace("$$", word);
                        index = index + 1;
                    } else {
                        wordValue = word;
                    }
                }
                result.append(wordValue);
            } else {
                result.append(word);
            }
        }

        return sourceText;
    }

    /**
     * 自定义特殊转换规则
     * 使用指定字符替换$$
     */
    private static final Map<String, String> SYMBOL_WORD = new HashMap<String, String>() {{
        put("国道", "G$$");
        put("省道", "S$$");
        put("县道", "X$$");
    }};

    /**
     * 根据关键字表替换字符
     * @param sourceText 待处理文本
     * @return 处理后文本
     */
    private String replaceKeyWord(String sourceText) {
        StringBuffer result = new StringBuffer(sourceText);
        for (Map.Entry<String, String> wordEntry : END_KEY_WORD.entrySet()) {
            String wordKey = wordEntry.getKey();
            String wordValue = wordEntry.getValue();
            if (sourceText.endsWith(wordKey)) {
                String preffix = sourceText.substring(0, sourceText.lastIndexOf(wordKey));
                String position = "";
                for (Map.Entry<String, String> positionEntry : POSITION_WORD.entrySet()) {
                    String positionKey = positionEntry.getKey();
                    if (preffix.endsWith(positionKey)) {
                        preffix = preffix.substring(0, preffix.length() - 1);
                        position = positionEntry.getValue();
                    }
                }
                result.setLength(0);
                result.append(preffix);
                //result.append("/");
                result.append(position);
                result.append(wordValue);
                break;
            }
        }

        return result.toString();
    }

    /**
     * 方位词
     */
    private static final Map<String, String> POSITION_WORD = new HashMap<String, String>() {{
        put("东", "East ");
        put("西", "West ");
        put("南", "South ");
        put("北", "North ");
        put("中", "Middle ");
    }};

    /**
     * 特殊结尾词
     */
    private static final Map<String, String> END_KEY_WORD = new HashMap<String, String>() {{
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
     * 英文转换方法
     * @param sourceText 待转换文本
     * @return 转换后文本
     */
    private String convertEnglishCharacter(String sourceText) {
        StringBuffer result = new StringBuffer();

        for (String subText : sourceText.split("/")) {
            if (CHI_TO_ENG_MAP.containsKey(subText)) {
                result.append(CHI_TO_ENG_MAP.get(subText)).append(" ");

            } else if (DICT_WORD_MAP.containsKey(subText)) {
                List<Map<String, String>> wordList = DICT_WORD_MAP.get(subText);
                for (Map<String, String> map : wordList) {
                    String pinyinOne = map.get("py");
                    String pinyinTwo = map.get("py2");
                    String curAdminCode = map.get("adminCode");
                    if (StringUtils.isEmpty(this.adminCode) || this.adminCode.equals(curAdminCode)) {
                        result.append(pinyinOne).append(" ");
                    }
                }
            } else {
                result.append(subText).append(" ");
            }
        }

        return result.toString();
    }

    /**
     * 中文转换方法(用于处理没有转换为英文的汉字)
     * @param sourceText 待转换文本
     * @return 转换后文本
     */
    private String convertChineseCharacter(String sourceText) {
        StringBuffer result = new StringBuffer();

        for (Character character : sourceText.toCharArray()) {
            if (ConvertUtil.isChinese(character)) {
                if (DICTIONARY_MAP.containsKey(String.valueOf(character))) {
                    List<String> pinyins = DICTIONARY_MAP.get(String.valueOf(character));
                    if (this.convertPolyhonic) {
                        // TODO 暂不处理多音字
                    } else {
                        result.append(pinyins.iterator().next());
                    }
                }
            } else {
                result.append(character);
            }
        }

        return result.toString();
    }
}
