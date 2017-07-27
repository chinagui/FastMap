package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @Title: EnglishConvert
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: 翻译-中文字符串转英文核心类
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.1
 */
public class EnglishConvert {

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(EnglishConvert.class);

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
     * 处理过多音字后的拼音内容
     */
    private List<String> pinyins;

    /**
     * 记录未查询到对应英文的汉字下标
     */
    private List<Integer> wordIndex = new ArrayList<>();

    /**
     * 数组下标偏移量（由于汉字后均后跟随空格）
     */
    private final static Integer OFFSET = 2;

    private final static String SPLIT_WORD = " ";


    public EnglishConvert() {
    }

    public EnglishConvert(String adminCode) {
        if (StringUtils.isNotEmpty(adminCode)) {
            this.adminCode = adminCode;
        }
    }

    /**
     * Setter method for property <tt>adminCode</tt>.
     *
     * @param adminCode value to be assigned to property adminCode
     */
    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    /**
     * 英文翻译接口
     *
     * @param sourceText 待转换为英文的字符串
     * @param pinyin 已处理完多音字的拼音文本
     * @return 翻译后字符串
     */
    public String convert(String sourceText, String pinyin) {
        this.pinyins = new ArrayList(Arrays.asList(org.apache.commons.lang.StringUtils.split(pinyin, SPLIT_WORD)));
        return convert(sourceText);
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

            result = SplitUtil.split(result, wordIndex);

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

        return result.toString();
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
                    String curAdminArea = map.get("adminArea");

                    if (StringUtils.isNotEmpty(curAdminArea) && adminCode.startsWith(curAdminArea)) {
                        result.append(pinyinOne).append(" ");
                    } else {
                        result.append(subText).append(" ");
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

        char[] chars = sourceText.toCharArray();

        // Character[] characters = connChineseCharacter(chars);

        Iterator<Integer> indexIterator = wordIndex.iterator();

        for (Character character : chars) {
            if (ConvertUtil.isChinese(character)) {
                if (CollectionUtils.isEmpty(pinyins)) {
                    if (TranslateDictData.getInstance().getDictDictionary().containsKey(String.valueOf(character))) {
                        List<String> pinyins = TranslateDictData.getInstance().getDictDictionary().get(String.valueOf(character));
                        if (convertPolyhonic) {
                            // TODO 暂不处理多音字
                        } else {
                            result.append(pinyins.iterator().next());
                        }
                    }
                } else {
                    if (indexIterator.hasNext()) {
                        result.append(pinyins.get(indexIterator.next()));
                    }
                }
            } else {
                result.append(character);
            }
        }

        return result.toString();
    }

    /**
     * 连接单个汉字（连续出现3个或以下的汉字，连接后再转换拼音）
     * <br>2017.5.17 企划贾晓晶提供输入
     * @param array 待转换词组
     * @return 连接后词组
     */
    private Character[] connChineseCharacter(char[] array) {
        List<Character> characters = new ArrayList<>();

        int index = 0;
        while (index < array.length) {
            if (ConvertUtil.isChinese(array[index])) {
                int count = countChineseCharacter(index, array);
                int maxIndex = index + count * OFFSET;
                if (ConvertUtil.isChinese(array[array.length - 1])) {
                    maxIndex--;
                }

                if (count <= TranslateConstant.MAX_CONNECTION_CHARACTER) {
                    for (; index < maxIndex; index += OFFSET) {
                        characters.add(array[index]);
                    }
                    characters.add(' ');
                } else {
                    for (; index < maxIndex; index++) {
                        characters.add(array[index]);
                    }
                }
            } else {
                characters.add(array[index++]);
            }
        }

        return characters.toArray(new Character[]{});
    }

    /**
     * 计算连续出现单个汉字的数量
     * @param index 起始下标
     * @param array 目标数组
     * @return 连续汉字数量
     */
    private int countChineseCharacter(int index, char[] array) {
        int count = 0;
        for (; index < array.length; index++) {
            if (ConvertUtil.isChinese(array[index])) {
                count++;
            } else if (' ' == array[index]) {
                continue;
            } else {
                break;
            }
        }
        return count;
    }
}
