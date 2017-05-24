package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Title: EnglishConvert
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: 翻译-中文字符串转英文核心类
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.0
 */
public class EnglishConvert {

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(EnglishConvert.class);

    public EnglishConvert() {
    }

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

    public EnglishConvert(String convertType, String adminCode) {
        if (TranslateConstant.HANDLE_POLYPHONIC_WORD.equals(convertType)) {
            this.convertPolyhonic = true;
        }
        this.adminCode = adminCode;
    }

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

        String result = sourceText;
        try {
            result = this.replaceKeyWord(sourceText);

            result = SplitUtil.split(result);

            result = ConvertUtil.convertNoWord(result);

            result = ConvertUtil.removeRepeatBackSlash(result);

            result = this.convertKernel(result);

            result = ConvertUtil.removeRepeatSpace(result);

            result = ConvertUtil.firstCapital(result);

            result = ConvertUtil.trimSymbolSpace(result);
        } catch (Exception e) {
            logger.error(String.format("英文翻译过程出错: [sourceText: %s]", sourceText), e);
            return result;
        }
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

        sourceText = this.convertWordSymbol(sourceText);
        //sourceText = ConvertUtil.removeSymbolWord(sourceText);

        sourceText = ConvertUtil.convertFull2Half(sourceText);

        sourceText = ConvertUtil.removeRepeatSpace(sourceText);

        if (ConvertUtil.hasChineseWord(sourceText)) {
            sourceText = this.convertChineseCharacter(sourceText);
        }

        return sourceText;
    }

    /**
     * 处理特殊字符
     * @param sourceText 待处理文本
     * @return 处理后字符串
     */
    private String convertWordSymbol(String sourceText) {
        StringBuffer result = new StringBuffer();
        for (Character character : sourceText.toCharArray()) {
            String tmpStr = String.valueOf(character);
            for (Map.Entry<String, String> entry : TranslateDictData.getInstance().getDictSymbolMap().entrySet()) {
                if (entry.getKey().equals(tmpStr)) {
                    tmpStr = StringUtils.isEmpty(entry.getValue()) ? "" : entry.getValue();
                    break;
                }
            }
            result.append(tmpStr);
        }
        return result.toString();
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
                    if (TranslateConstant.SYMBOL_WORD.containsKey(twoWord)) {
                        wordValue = TranslateConstant.SYMBOL_WORD.get(twoWord).replace("$$", word);
                        index = index + 2;
                    } else if (TranslateConstant.SYMBOL_WORD.containsKey(oneWord)) {
                        wordValue = TranslateConstant.SYMBOL_WORD.get(oneWord).replace("$$", word);
                        index = index + 1;
                    } else {
                        wordValue = word;
                    }
                } else {
                    if (TranslateConstant.SYMBOL_WORD.containsKey(oneWord)) {
                        wordValue = TranslateConstant.SYMBOL_WORD.get(oneWord).replace("$$", word);
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
     * 根据关键字表替换字符
     * @param sourceText 待处理文本
     * @return 处理后文本
     */
    private String replaceKeyWord(String sourceText) {
        StringBuffer result = new StringBuffer(sourceText);
        for (Map.Entry<String, String> wordEntry : TranslateConstant.END_KEY_WORD.entrySet()) {
            String wordKey = wordEntry.getKey();
            String wordValue = wordEntry.getValue();
            if (sourceText.endsWith(wordKey)) {
                String preffix = sourceText.substring(0, sourceText.lastIndexOf(wordKey));
                String position = "";
                for (Map.Entry<String, String> positionEntry : TranslateConstant.POSITION_WORD.entrySet()) {
                    String positionKey = positionEntry.getKey();
                    if (preffix.endsWith(positionKey)) {
                        preffix = preffix.substring(0, preffix.length() - 1);
                        position = positionEntry.getValue();
                    }
                }
                result.setLength(0);
                result.append(preffix);
                result.append(position);
                result.append(wordValue);
                break;
            }
        }

        return result.toString();
    }

    /**
     * 英文转换方法
     * @param sourceText 待转换文本
     * @return 转换后文本
     */
    private String convertEnglishCharacter(String sourceText) {
        StringBuffer result = new StringBuffer();

        for (String subText : sourceText.split("/")) {
            if (TranslateDictData.getInstance().getDictChi2Eng().containsKey(subText)) {
                result.append(TranslateDictData.getInstance().getDictChi2Eng().get(subText)).append(" ");

            } else if (TranslateDictData.getInstance().getDictWord().containsKey(subText)) {
                List<Map<String, String>> wordList = TranslateDictData.getInstance().getDictWord().get(subText);
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
                if (TranslateDictData.getInstance().getDictDictionary().containsKey(String.valueOf(character))) {
                    List<String> pinyins = TranslateDictData.getInstance().getDictDictionary().get(String.valueOf(character));
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
