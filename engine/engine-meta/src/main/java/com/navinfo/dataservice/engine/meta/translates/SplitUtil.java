package com.navinfo.dataservice.engine.meta.translates;


import java.util.List;
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


    public static String split(String sourceText, List<Integer> wordIndex){
        sourceText = ConvertUtil.convertHalf2Full(sourceText);

        sourceText = ConvertUtil.removeRepeatSpace(sourceText);

        sourceText = splitWords(sourceText, wordIndex);

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

    private static String splitWords(String sourceText, List<Integer> wordIndex) {
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
                        if (TranslateConstant.SYMBOL_WORD.containsKey(subStr)) {
                            index = index + subStr.length();
                            wordValue = subStr + "/";
                            flag = false;
                            continue;
                        }

                        for (Map.Entry<String, String> entry : TranslateDictData.getInstance().getDictChi2Eng().entrySet()) {
                            if (subStr.equals(entry.getKey())) {
                                wordValue = entry.getValue() + "/";
                                index = index + subStr.length();
                                flag = false;
                            }
                        }
                    }
                    if(flag){
                        wordIndex.add(index++);
                        wordValue = wordValue + "/";
                    }
                }else {
                    if(++index == length){
                        wordValue = currentChar + "/";
                    }else {
                        char nextChar = charArray[index];
                        if(Character.isSpaceChar(currentChar) || Character.isSpaceChar(nextChar)) {
                            wordValue = String.valueOf(currentChar);
                        } else if(ConvertUtil.isLetter(currentChar) && ConvertUtil.isNotLetter(nextChar)){
                            wordValue = currentChar + "/";
                        } else if(ConvertUtil.isChinese(currentChar) && ConvertUtil.isNotChinese(nextChar)){
                            wordValue = currentChar + "/";
                        } else if(ConvertUtil.isNotChinese(currentChar) && ConvertUtil.isChinese(nextChar)){
                            wordValue = currentChar + "/";
                        } else if(Character.isDigit(currentChar) && !Character.isDigit(nextChar)) {
                            wordValue = currentChar + "/";
                        } else if(ConvertUtil.isChinesePunctuation(currentChar)) {
                            wordValue = currentChar + "/";
                        }
                    }
                }
                result.append(wordValue);
            }
        }

        return result.substring(0, result.length() - 1);
    }


}
