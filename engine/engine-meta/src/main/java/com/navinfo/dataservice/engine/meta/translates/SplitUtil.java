package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.engine.meta.translate.TranslateDictData;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Title: SplitUtil
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.0
 */
public class SplitUtil {

    static Map<String, String> CHI_TO_ENG_MAP = TranslateDictData.getInstance().getDictChi2Eng();

    public static String split(String sourceText){
        sourceText = ConvertUtil.convertHalf2Full(sourceText);

        sourceText = ConvertUtil.removeRepeatSpace(sourceText);

        sourceText = splitWords(sourceText);

        sourceText = joinSingleLetter(sourceText);

        return sourceText;
    }

    private static String joinSingleLetter(String sourceText) {
        StringBuffer result = new StringBuffer();

        String regex = "[Ａ-Ｚａ-ｚ０-９1-9]{1}";

        Pattern pattern = Pattern.compile(regex);

        // 0代表首个单词
        // 1代表上一位为单个字母或数字
        // 2代表上一位不是字母或数字
        int preWordIsDigitalOrLetter = 0;

        String[] textArray = sourceText.split("/");
        for(String subText : textArray){
            if(0 != preWordIsDigitalOrLetter &&
                    (-1 == preWordIsDigitalOrLetter || !pattern.matcher(subText).matches())){
                result.append("/");
            }
            result.append(subText);

            if(pattern.matcher(subText).matches()){
                preWordIsDigitalOrLetter = 1;
            }else {
                preWordIsDigitalOrLetter = -1;
            }
        }
        return result.toString();
    }

    private static String splitWords(String sourceText) {
        StringBuffer result = new StringBuffer();

        for(String subText : sourceText.split("/")){
            int length = subText.length();

            int index = 0;
            char[] charArray = subText.toCharArray();
            while (index < length){
                char currentChar = charArray[index];
                String wordValue = String.valueOf(currentChar);

                boolean flag = true;
                if(ConvertUtil.isChinese(currentChar)){
                    for(int j = length; j > index; j--){
                        String subStr = subText.substring(index, j);
                        if(CHI_TO_ENG_MAP.containsKey(subStr)){
                            wordValue = CHI_TO_ENG_MAP.get(subStr) + "/";
                            index = index + subStr.length();
                            flag = false;
                        }
                    }
                    if(flag){
                        index++;
                        wordValue = wordValue + "/";
                    }
                }else {
                    if(index++ == length){
                        wordValue = String.valueOf(currentChar);
                    }else {
                        char nextChar = charArray[index];
                        if(' ' == currentChar || ' ' == nextChar) {
                            wordValue = String.valueOf(currentChar);
                        } else if(ConvertUtil.isLetter(currentChar) && ConvertUtil.isNotLetter(nextChar)){
                            wordValue = currentChar + "/";
                        } else if(ConvertUtil.isChinese(currentChar) && ConvertUtil.isNotChinese(nextChar)){
                            wordValue = currentChar + "/";
                        }else if(Character.isDigit(currentChar) && !Character.isDigit(nextChar)) {
                            wordValue = currentChar + "/";
                        }
                    }
                }
                result.append(wordValue);
            }
            //result.append("/");
        }

        return result.substring(0, result.length() - 1);
    }


}
