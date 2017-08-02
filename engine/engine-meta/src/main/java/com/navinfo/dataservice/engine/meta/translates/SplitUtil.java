package com.navinfo.dataservice.engine.meta.translates;


import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.meta.translates.model.Chi2EngKeyword;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Title: SplitUtil
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: 翻译-分词工具类
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.0
 */
public class SplitUtil {


    public static String split(String sourceText, Param param){
        sourceText = ConvertUtil.convertHalf2Full(sourceText);

        sourceText = ConvertUtil.removeRepeatSpace(sourceText);

        sourceText = splitWords(sourceText, param);

        sourceText = joinSingleLetter(sourceText);

        return sourceText;
    }

    private static String joinSingleLetter(String sourceText) {
        StringBuilder result = new StringBuilder();

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

    private static String splitWords(String sourceText, Param param) {
        StringBuilder result = new StringBuilder();

        param.wordIndex = new ArrayList<>();

        StringBuilder kindChain = new StringBuilder();
        if (StringUtils.isNotEmpty(param.kindCode)) {
            kindChain.append(param.kindCode);
        }
        if (StringUtils.isNotEmpty(param.chain)) {
            kindChain.append(",").append(param.chain);
        }

        for(String subText : sourceText.split("/")){
            int length = subText.length();

            int index = 0;

            boolean connNum = true;

            char[] charArray = subText.toCharArray();
            while (index < length) {
                char currentChar = charArray[index];
                String wordValue = String.valueOf(currentChar);


                boolean flag = true;
                if(ConvertUtil.isChinese(currentChar) && !TranslateConstant.CHINESE_NUMBER.keySet().contains(String.valueOf(currentChar))){
                    for(int j = length; j > index; j--){
                        String subStr = subText.substring(index, j);
                        if (TranslateConstant.SYMBOL_WORD.containsKey(subStr)) {
                            index = index + subStr.length();
                            wordValue = subStr + "/";
                            flag = false;
                            connNum = true;
                            continue;
                        }

                        for (Map.Entry<String, Chi2EngKeyword> entry : TranslateDictData.getInstance().getDictChi2Eng().entrySet()) {
                            if (StringUtils.isNotEmpty(kindChain.toString()) &&
                                    !org.apache.commons.lang.StringUtils.contains(entry.getValue().getKind(), kindChain.toString())) {
                                continue;
                            }

                            if (subStr.equals(entry.getKey())) {
                                wordValue = entry.getKey() + "/";
                                index = index + subStr.length();
                                flag = false;
                                connNum = true;
                            }
                        }
                    }
                    if(flag){
                        param.wordIndex.add(index++);
                        wordValue = wordValue + "/";
                        connNum = false;
                    }
                }else {
                    if(++index == length){
                        wordValue = currentChar + "/";
                    }else {
                        char afterChar = charArray[index];
                        if(Character.isSpaceChar(currentChar) || Character.isSpaceChar(afterChar)) {
                            wordValue = String.valueOf(currentChar);
                        } else if (ConvertUtil.isLetter(currentChar) && ConvertUtil.isNotLetter(afterChar)){
                            wordValue = currentChar + "/";
                        } else if (ConvertUtil.isNotLetter(currentChar) && ConvertUtil.isLetter(afterChar)) {
                            wordValue = currentChar + "/";
                        } else if (ConvertUtil.isNotChinese(currentChar) && ConvertUtil.isChinese(afterChar)){
                            wordValue = currentChar + "/";
                        } else if (Character.isDigit(currentChar) && !Character.isDigit(afterChar)) {
                            wordValue = currentChar + "/";
                        } else if (!Character.isDigit(currentChar) && Character.isDigit(afterChar)) {
                            wordValue = currentChar + "/";
                        } else if (ConvertUtil.isChinesePunctuation(currentChar)) {
                            wordValue = currentChar + "/";
                        } else if (ConvertUtil.isChineseNum(currentChar)) {
                            if (!ConvertUtil.isChineseNum(afterChar) || !connNum) {
                                wordValue = currentChar + "/";
                            }
                        }
                    }
                }
                result.append(wordValue);
            }
        }

        return result.substring(0, result.length() - 1);
    }

}
