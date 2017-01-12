package com.navinfo.dataservice.engine.meta.translate;

import com.navinfo.dataservice.commons.util.StringUtils;

import java.util.Map;

/**
 * Created by chaixin on 2016/11/29 0029.
 */
public class ConvertUtil {

    private static Pyconverter pyconverter = new Pyconverter();

    public static String[] delNoChinese(String text) {
        text = convFull2Half(text).replaceAll(" ", "");
        StringBuffer strName = new StringBuffer();
        String strPyName = "";
        for (Character c : text.toCharArray()) {
            if (isChinese(c)) {
                strName.append(c);
            }
        }
        if (checkContainLetter(text)) {
            strPyName = pyconverter.convertPy(strName.toString());
        } else {
            if (StringUtils.isNotEmpty(strPyName.toString())) {
                strPyName = convFull2Half(strPyName);
                for (Character tmpPy : strPyName.toString().toCharArray()) {
                    if (isLetter(tmpPy) || tmpPy == ' ') {
                        strPyName += tmpPy;
                    }
                }
            } else {
                strPyName = pyconverter.convertPy(strName.toString());
            }
        }
        strPyName = trimMultiSpace(strPyName);
        return new String[]{strName.toString(), strPyName};
    }

    public static String convHalf2Full(String str) {
        return convFullHashWidth(str, 1);
    }

    public static String convFull2Half(String str) {
        return convFullHashWidth(str, 2);
    }

    private static String convFullHashWidth(String str, Integer convType) {
        StringBuffer text = new StringBuffer();
        Map<String, String> map = TranslateDictData.getInstance().getDictFhWidth();
        if (convType == 1) {
            for (Character c : str.toCharArray()) {
                if (map.containsKey(String.valueOf(c))) {
                    text.append(map.get(String.valueOf(c)));
                } else {
                    text.append(c);
                }
            }
        } else {
            for (Character c : str.toCharArray()) {
                boolean isFind = false;
                for (Map.Entry<String, String> e : map.entrySet()) {
                    if (String.valueOf(c).equals(e.getValue())) {
                        text.append(e.getKey());
                        isFind = true;
                    }
                }
                if (!isFind)
                    text.append(c);
            }
        }
        return text.toString();
    }

    private static boolean checkContainLetter(String text) {
        for (Character c : text.toCharArray()) {
            if (isLetter(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkContainCh(String text) {
        for (Character c : text.toCharArray()) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLetter(char c) {
        if (isCapitalLetter(c) || isLittleLetter(c))
            return true;
        return false;
    }

    public static boolean isCapitalLetter(char c) {
        if ((c >= 'A' && c <= 'Z') || (c > 'Ａ' && c < 'Ｚ'))
            return true;
        return false;
    }

    public static boolean isLittleLetter(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'ａ' && c <= 'ｚ'))
            return true;
        return false;
    }

    public static String trimMultiSpace(String text) {
        text = text.trim();
        while (text.indexOf("  ") != -1) {
            text = text.replaceAll("  ", " ");
        }
        return text;
    }

    public static String numToPy(String str) {
        //转半角数字
        str = str.replaceAll("0", " ling ");
        str = str.replaceAll("1", " yi ");
        str = str.replaceAll("2", " er ");
        str = str.replaceAll("3", " san ");
        str = str.replaceAll("4", " si ");
        str = str.replaceAll("5", " wu ");
        str = str.replaceAll("6", " liu ");
        str = str.replaceAll("7", " qi ");
        str = str.replaceAll("8", " ba ");
        str = str.replaceAll("9", " jiu ");
        //转全角数字
        str = str.replaceAll("０", " ling ");
        str = str.replaceAll("１", " yi ");
        str = str.replaceAll("２", " er ");
        str = str.replaceAll("３", " san ");
        str = str.replaceAll("４", " si ");
        str = str.replaceAll("５", " wu ");
        str = str.replaceAll("６", " liu ");
        str = str.replaceAll("７", " qi ");
        str = str.replaceAll("８", " ba ");
        str = str.replaceAll("９", " jiu ");
        return str;
    }

    //转换罗马数字为拼音
    public static String romeNumToPy(String str) {
        str = str.replaceAll("Ⅰ", " I ");
        str = str.replaceAll("Ⅱ", " II ");
        str = str.replaceAll("Ⅲ", " III ");
        str = str.replaceAll("Ⅳ", " IV ");
        str = str.replaceAll("Ⅴ", " V ");
        str = str.replaceAll("Ⅵ", " VI ");
        str = str.replaceAll("Ⅶ", " VII ");
        str = str.replaceAll("Ⅷ", " VIII ");
        str = str.replaceAll("Ⅸ", " IX ");
        str = str.replaceAll("Ⅹ", " X ");
        str = str.replaceAll("Ⅺ", " XI ");
        str = str.replaceAll("Ⅻ", " XII ");

        str = str.replaceAll("ⅰ", " i ");
        str = str.replaceAll("ⅱ", " ii ");
        str = str.replaceAll("ⅲ", " iii ");
        str = str.replaceAll("ⅳ", " iv ");
        str = str.replaceAll("ⅴ", " v ");
        str = str.replaceAll("ⅵ", " vi ");
        str = str.replaceAll("ⅶ", " vii ");
        str = str.replaceAll("ⅷ", " viii ");
        str = str.replaceAll("ⅸ", " ix ");
        str = str.replaceAll("ⅹ", " x ");
        return str;
    }

    public static String firstCapital(String text) {
        boolean bInWord = false;
        String textNew = "";
        for (Character c : text.toCharArray()) {
            // 全角
            if (isCapitalLetter(c) || Character.isDigit(c) || c == '@' || c == '\'') {
                textNew += c;
                bInWord = true;
                continue;
            }
            if (!bInWord) {
                if (isLittleLetter(c)) {
                    textNew += String.valueOf(c).toUpperCase();
                    bInWord = true;
                    continue;
                }
            }
            if (bInWord && !isLetter(c) && !Character.isDigit(c)) {
                textNew += c;
                bInWord = false;
                continue;
            }
            textNew += c;
        }
        return textNew;
    }

    public static String converFirstLittle(String str) {
        char c = str.charAt(0);
        if (isCapitalLetter(c)) {
            str = str.replaceFirst(String.valueOf(c), String.valueOf(c).toLowerCase());
        }
        return str;
    }

    public static String delSymbol(String text) {
        if (text.indexOf("°Ｃ") > 0) {
            text = text.replace("°Ｃ", " Degree ");
        }
        Map<String, String> symbol = TranslateDictData.getInstance().getDictSymbolMap();
        for (Map.Entry<String, String> entry : symbol.entrySet()) {
            if (text.indexOf(entry.getKey()) >= 0) {
                text = text.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }

    public static boolean isChinese(Character c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isLetter(("ｄ").toCharArray()[0]));
    }
}
