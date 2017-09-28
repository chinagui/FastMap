package com.navinfo.dataservice.engine.meta.translates;


import com.navinfo.dataservice.commons.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Title: ConvertUtil
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: 翻译-工具类
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.0
 */
public class ConvertUtil {


    public static String convertNoWord(String sourceText){
        String regex = "[Nn]+[Oo]+[.]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sourceText);

        while (matcher.find()) {
            sourceText = sourceText.replace(sourceText.substring(matcher.start(), matcher.end()), "No.");
        }

        return sourceText;
    }

    public static String removeRepeatBackSlash(String sourceText){
        while (-1 != sourceText.indexOf("//")) {
            sourceText = sourceText.replaceAll("//", "/");
        }
        return sourceText;
    }

    public static String removeRepeatSpace(String sourceText){
        while (-1 != sourceText.indexOf("  ")) {
            sourceText = sourceText.replaceAll("  ", " ");
        }
        return sourceText;
    }

    public static String trimSymbolSpace(String sourceText){
        sourceText = sourceText.replaceAll(" \\@ ", "@");
        sourceText = sourceText.replaceAll("\\@ ", "@");
        sourceText = sourceText.replaceAll(" \\@", "@");

        sourceText = sourceText.replaceAll(" \\)", ")");
        sourceText = sourceText.replaceAll(" \\) ", ") ");
        sourceText = sourceText.replaceAll(" \\( ", " (");
        sourceText = sourceText.replaceAll("\\( ", "(");

        sourceText = sourceText.replaceAll(" \\, ", ", ");
        sourceText = sourceText.replaceAll(" \\: ", ": ");
        sourceText = sourceText.replaceAll(" \\; ", "; ");

        sourceText = sourceText.replaceAll(" \\. ", ".");
        sourceText = sourceText.replaceAll("\\. ", ".");
        sourceText = sourceText.replaceAll(" \\.", ".");

        sourceText = sourceText.replaceAll(" \\' ", "'");

        //sourceText = sourceText.replaceAll(" \\& ", "&");
        //sourceText = sourceText.replaceAll("\\& ", "&");
        //sourceText = sourceText.replaceAll(" \\&", "&");

        sourceText = sourceText.replaceAll(" \\- ", "-");
        sourceText = sourceText.replaceAll("\\- ", "-");
        sourceText = sourceText.replaceAll(" \\-", "-");

        sourceText = sourceText.replaceAll(" \\_ ", "_");
        sourceText = sourceText.replaceAll("\\_ ", "_");
        sourceText = sourceText.replaceAll(" \\_", "_");

        sourceText = sourceText.replaceAll(" \\+ ", "+");
        sourceText = sourceText.replaceAll("\\+ ", "+");

        sourceText = sourceText.replaceAll(" \\/ ", "/");
        sourceText = sourceText.replaceAll("\\/ ", "/");
        sourceText = sourceText.replaceAll(" \\/", "/");

        sourceText = sourceText.replaceAll(" \\$ ", "\\$");

        return sourceText.trim();
    }

    public static String firstCapital(String sourceText){
        boolean flag = false;
        StringBuilder result = new StringBuilder();
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
        for (Map.Entry<String, String> entry : TranslateDictData.getInstance().getDictSymbolMap().entrySet()) {
            if (sourceText.indexOf(entry.getKey()) >= 0) {
                sourceText = sourceText.replaceAll(entry.getKey(), entry.getValue() + " ");
            }
        }
        return sourceText;
    }

    public static String convertHalf2Full(String sourceText) {
        return convFullHashWidth(sourceText, TranslateConstant.HALF_TO_FULL);
    }

    public static String convertFull2Half(String sourceText) {
        return convFullHashWidth(sourceText, TranslateConstant.FULL_TO_HALF);
    }

    private static String convFullHashWidth(String sourceText, Integer convertType) {
        StringBuilder result = new StringBuilder();
        String wordValue = "";
        if (TranslateConstant.HALF_TO_FULL .equals(convertType) ) {
            for (Character c : sourceText.toCharArray()) {
                wordValue = c.toString();
                for (Map.Entry<String, String> entry : TranslateDictData.getInstance().getDictFhWidth().entrySet()) {
                    if (String.valueOf(c).equals(entry.getKey())) {
                        wordValue = StringUtils.isEmpty(entry.getValue()) ? wordValue : entry.getValue();
                    }
                }
                result.append(wordValue);
            }
        } else if(TranslateConstant.FULL_TO_HALF.equals( convertType)) {
            for (Character c : sourceText.toCharArray()) {
                wordValue = c.toString();
                for (Map.Entry<String, String> e : TranslateDictData.getInstance().getDictFhWidth().entrySet()) {
                    if(e.getValue().equals(wordValue)){
                        wordValue = StringUtils.isEmpty(e.getKey()) ? wordValue : e.getKey();
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
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        if (sc == Character.UnicodeScript.HAN) {
            return true;
        }
        return false;
    }

    // 根据UnicodeBlock方法判断中文标点符号
    public static boolean isChinesePunctuation(char c) {
        String partten = "[\\pP‘’“”]";
        return Pattern.compile(partten).matcher(String.valueOf(c)).matches();
    }

    public static boolean isLetter(char c) {
        if (isCapitalLetter(c) || isLittleLetter(c)) {
            return true;
        }
        return false;
    }

    public static boolean isNotLetter(char c) {
        return !isLetter(c);
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

    public static String joinSpace(String words) {
        return org.apache.commons.lang.StringUtils.join(words.split("(?!^)"), " ");
    }

    public static boolean contains(String str, String search) {
        if (!org.apache.commons.lang.StringUtils.contains(search, " ")) {
            search = joinSpace(search);
        }
        return org.apache.commons.lang.StringUtils.contains(str, search);
    }

    public static boolean notContains(String str, String search) {
        return !contains(str, search);
    }

    public static boolean isChineseNum(Character character) {
        return TranslateConstant.CHINESE_NUMBER.keySet().contains(String.valueOf(character));
    }
}
