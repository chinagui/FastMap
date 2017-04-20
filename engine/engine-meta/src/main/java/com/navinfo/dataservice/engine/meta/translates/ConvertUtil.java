package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.engine.meta.translate.TranslateDictData;

import java.util.Map;

/**
 * @Title: ConvertUtil
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.0
 */
public class ConvertUtil {

    static Integer HALF_TO_FULL = 1;

    static Integer FULL_TO_HALF = 2;

    static Map<String, String> HALF_FULL_MAP = TranslateDictData.getInstance().getDictFhWidth();

    static Map<String, String> SYMBOL_MAP = TranslateDictData.getInstance().getDictSymbolMap();

    public static String[] delNoChinese(String sourceText){

        return new String[]{};
    }

    public static String removeRepeatBackSlash(String sourceText){
        while (-1 != sourceText.indexOf("//")) {
            sourceText = sourceText.replaceAll("//", "/");
        }
        return sourceText.trim();
    }

    public static String removeRepeatSpace(String sourceText){
        while (-1 != sourceText.indexOf("  ")) {
            sourceText = sourceText.replaceAll("  ", " ");
        }
        return sourceText.trim();
    }

    public static String trimSymbolSpace(String sourceText){
        sourceText = sourceText.replaceAll("@", " ");
        sourceText = sourceText.replaceAll(" \\)", ")");
        sourceText = sourceText.replaceAll(" \\( ", " (");
        sourceText = sourceText.replaceAll("\\( ", "(");
        sourceText = sourceText.replaceAll(" \\. ", ".");
        sourceText = sourceText.replaceAll(" & ", "&");
        sourceText = sourceText.replaceAll("& ", "&");
        sourceText = sourceText.replaceAll(" &", "&");
        sourceText = sourceText.replaceAll(" - ", "-");
        sourceText = sourceText.replaceAll("- ", "-");
        sourceText = sourceText.replaceAll(" + ", "+");
        sourceText = sourceText.replaceAll("\\+ ", "+");
        sourceText = sourceText.replaceAll(" / ", "/");
        sourceText = sourceText.replaceAll("/ ", "/");
        return sourceText;
    }

    public static String firstCapital(String sourceText){
        boolean flag = false;
        StringBuffer result = new StringBuffer();
        for (Character c : sourceText.toCharArray()) {
            char wordValue = c;
            // 全角
            if (isCapitalLetter(c) || Character.isDigit(c) || c == '@' || c == '\'') {
                flag = true;
            }else if (!flag && isLittleLetter(c)) {
                wordValue = Character.toUpperCase(c);
                flag = true;
            }else if (flag && isNotLetter(c) && !Character.isDigit(c)) {
                flag = false;
            }
            result.append(wordValue);
        }
        return result.toString();
    }

    public static String digital2Pinyin(String sourceText){
        sourceText = halfDigital2Pinyin(sourceText);
        sourceText = fullDigital2Pinyin(sourceText);
        return sourceText;
    }

    private static String halfDigital2Pinyin(String sourceText){
        //转半角数字
        sourceText = sourceText.replaceAll("0", " ling ");
        sourceText = sourceText.replaceAll("1", " yi ");
        sourceText = sourceText.replaceAll("2", " er ");
        sourceText = sourceText.replaceAll("3", " san ");
        sourceText = sourceText.replaceAll("4", " si ");
        sourceText = sourceText.replaceAll("5", " wu ");
        sourceText = sourceText.replaceAll("6", " liu ");
        sourceText = sourceText.replaceAll("7", " qi ");
        sourceText = sourceText.replaceAll("8", " ba ");
        sourceText = sourceText.replaceAll("9", " jiu ");

        return sourceText;
    }

    private static String fullDigital2Pinyin(String sourceText){
        //转全角数字
        sourceText = sourceText.replaceAll("０", " ling ");
        sourceText = sourceText.replaceAll("１", " yi ");
        sourceText = sourceText.replaceAll("２", " er ");
        sourceText = sourceText.replaceAll("３", " san ");
        sourceText = sourceText.replaceAll("４", " si ");
        sourceText = sourceText.replaceAll("５", " wu ");
        sourceText = sourceText.replaceAll("６", " liu ");
        sourceText = sourceText.replaceAll("７", " qi ");
        sourceText = sourceText.replaceAll("８", " ba ");
        sourceText = sourceText.replaceAll("９", " jiu ");

        return sourceText;
    }

    public static String removeSymbolWord(String sourceText) {
        for (Map.Entry<String, String> entry : SYMBOL_MAP.entrySet()) {
            if (sourceText.indexOf(entry.getKey()) >= 0) {
                sourceText = sourceText.replaceAll(entry.getKey(), entry.getValue() + " ");
            }
        }
        return sourceText;
    }

    public static String convertHalf2Full(String sourceText) {
        return convFullHashWidth(sourceText, HALF_TO_FULL);
    }

    public static String convertFull2Half(String sourceText) {
        return convFullHashWidth(sourceText, FULL_TO_HALF);
    }

    private static String convFullHashWidth(String sourceText, Integer convertType) {
        StringBuffer result = new StringBuffer();
        String wordValue = "";
        if (HALF_TO_FULL == convertType) {
            for (Character c : sourceText.toCharArray()) {
                wordValue = c.toString();
                if (HALF_FULL_MAP.containsKey(wordValue)) {
                    wordValue = HALF_FULL_MAP.get(wordValue);
                }
                result.append(wordValue);
            }
        } else if(FULL_TO_HALF == convertType) {
            for (Character c : sourceText.toCharArray()) {
                wordValue = c.toString();
                for (Map.Entry<String, String> e : HALF_FULL_MAP.entrySet()) {
                    if(e.getValue().equals(wordValue)){
                        wordValue = e.getKey();
                    }
                }
                result.append(wordValue);
            }
        }
        return result.toString();
    }

    public static boolean hasChineseWord(String sourceText) {
        for (Character c : sourceText.toCharArray()) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotChinese(char c){
        return !isChinese(c);
    }

    public static boolean isChinese(Character c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public static boolean isLetter(char c) {
        if (isCapitalLetter(c) || isLittleLetter(c))
            return true;
        return false;
    }

    public static boolean isNotLetter(char c) {
        if (isCapitalLetter(c) || isLittleLetter(c))
            return false;
        return true;
    }

    public static boolean isCapitalLetter(char c) {
        if ((c >= 'A' && c <= 'Z') || (c >= 'Ａ' && c <= 'Ｚ'))
            return true;
        return false;
    }

    public static boolean isLittleLetter(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'ａ' && c <= 'ｚ'))
            return true;
        return false;
    }
}
