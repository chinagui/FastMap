package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.meta.translates.model.Chi2EngKeyword;
import com.navinfo.dataservice.engine.meta.translates.model.EngKeyword;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
     * 参数
     */
    private Param param = new Param();

    public EnglishConvert() {
    }

    public EnglishConvert(String adminCode) {
        if (StringUtils.isNotEmpty(adminCode)) {
            this.param.adminCode = adminCode;
        }
    }

    /**
     * Setter method for property <tt>adminCode</tt>.
     *
     * @param adminCode value to be assigned to property adminCode
     */
    public void setAdminCode(String adminCode) {
        this.param.adminCode = adminCode;
    }

    /**
     * Setter method for property <tt>kindCode</tt>.
     *
     * @param kindCode value to be assigned to property adminCode
     */
    public void setKindCode(String kindCode) {
        this.param.kindCode = StringUtils.isEmpty(kindCode) ? "" : kindCode;
    }

    /**
     * Setter method for property <tt>chain</tt>.
     *
     * @param chain value to be assigned to property adminCode
     */
    public void setChain(String chain) {
        this.param.chain = StringUtils.isEmpty(chain) ? "" : chain;
    }

    /**
     * Setter method for property <tt>priority</tt>.
     *
     * @param priority value to be assigned to property adminCode
     */
    public void setPriority(String priority) {
        this.param.priority = StringUtils.isEmpty(priority) ? "" : priority;
    }

    private void init() {
        this.param.translateWords = new ArrayList<>();
    }

    /**
     * 英文翻译接口
     *
     * @param sourceText 待转换为英文的字符串
     * @param pinyin 已处理完多音字的拼音文本
     * @return 翻译后字符串
     */
    public String convert(String sourceText, String pinyin) {
        this.param.pinyins = new ArrayList(Arrays.asList(org.apache.commons.lang.StringUtils.split(pinyin, TranslateConstant.SPLIT_WORD)));

        return convert(sourceText);
    }

    public String convert(String sourceText, String pinyin, String priority) {
        this.param.pinyins = new ArrayList(Arrays.asList(org.apache.commons.lang.StringUtils.split(pinyin, TranslateConstant.SPLIT_WORD)));
        this.setPriority(priority);

        return convert(sourceText);
    }

    public String convert(String sourceText, String pinyin, String priority , String adminCode) {
        this.param.pinyins = new ArrayList(Arrays.asList(org.apache.commons.lang.StringUtils.split(pinyin, TranslateConstant.SPLIT_WORD)));
        this.setPriority(priority);
        this.setAdminCode(adminCode);

        return convert(sourceText);
    }

    /**
     * 英文翻译接口
     *
     * @param sourceText 待转换为英文的字符串
     * @return 翻译后字符串
     */
    public String convert(String sourceText) {
        this.init();

        if (org.apache.commons.lang.StringUtils.isBlank(sourceText)) {
            return sourceText;
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(param.pinyins)) {
            if (param.pinyins.size() != sourceText.toCharArray().length) {
                param.pinyins = new ArrayList<>();
            }
        }

        String result = sourceText;
        try {
            result = this.convertWordSymbol(sourceText);

            result = SplitUtil.split(result, param);

            result = ConvertUtil.removeRepeatBackSlash(result);

            result = this.convertKernel(result);

            result = this.replaceKeyWord(result);

            result = this.reductionWordSymbol(result);

            result = ConvertUtil.removeRepeatSpace(result);

            result = ConvertUtil.firstCapital(result);

            result = ConvertUtil.trimSymbolSpace(result);

            result = ConvertUtil.convertNoWord(result);
        } catch (Exception e) {
            logger.error(String.format("英文翻译过程出错: [sourceText: %s]", sourceText), e);
            return result;
        }
        return result;
    }

    private String reductionWordSymbol(String sourceText) {
        return sourceText.replaceAll("& nbsp ;", " ");
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

        // 转特殊翻译原则
        sourceText = this.convertEngKeyword(sourceText);

        //sourceText = ConvertUtil.removeSymbolWord(sourceText);

        sourceText = ConvertUtil.convertFull2Half(sourceText);

        sourceText = ConvertUtil.removeRepeatSpace(sourceText);

        sourceText = this.convertChineseNum(sourceText);

        if (ConvertUtil.hasChineseWord(sourceText)) {
            sourceText = this.convertChineseCharacter(sourceText);
        }

        sourceText = this.connNum(sourceText);

        return sourceText;
    }

    /**
     * 处理特殊字符
     * @param sourceText 待处理文本
     * @return 处理后字符串
     */
    private String convertWordSymbol(String sourceText) {
        StringBuilder result = new StringBuilder();
        for (Character character : sourceText.toCharArray()) {
            if (character == ' ') {
                continue;
            }
            String tmpStr = String.valueOf(character);

            for (Map.Entry<String, String> entry : TranslateDictData.getInstance().getDictSymbolMap().entrySet()) {
                if (org.apache.commons.lang.StringUtils.equals(entry.getKey(), tmpStr)) {
                    tmpStr = " ".equals(entry.getValue()) ? "&nbsp;" : entry.getValue();
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

        StringBuilder result = new StringBuilder();

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
        StringBuilder result = new StringBuilder(sourceText);
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
        StringBuilder result = new StringBuilder();

        label1 :
        for (String subText : sourceText.split("/")) {
            for (Map.Entry<String, Chi2EngKeyword> entry : TranslateDictData.getInstance().getDictChi2Eng().entrySet()) {
                if (org.apache.commons.lang.StringUtils.equals(entry.getKey(), subText)) {
                    param.translateWords.add(entry.getValue().getEngkeywords());
                    result.append(entry.getValue().getEngkeywords()).append(" ");
                    continue label1;
                }
            }

            for (Map.Entry<String, List<Map<String, String>>> entry : TranslateDictData.getInstance().getDictWord().entrySet()) {
                if (org.apache.commons.lang.StringUtils.equals(entry.getKey(), subText)) {
                    for (Map<String, String> map : entry.getValue()) {
                        String pinyinOne = map.get("py");
                        String pinyinTwo = map.get("py2");
                        String curAdminArea = map.get("adminArea");

                        if (StringUtils.isNotEmpty(curAdminArea) && param.adminCode.startsWith(curAdminArea)) {
                            param.translateWords.add(pinyinOne);
                            result.append(pinyinOne).append(" ");
                            continue label1;
                        } else {
                            result.append(subText).append(" ");
                            continue label1;
                        }
                    }
                    continue label1;
                }
            }

            result.append(subText).append(" ");
        }

        return result.toString();
    }

    /**
     * 英文关键字翻译
     * @param result
     * @return
     */
    private String convertEngKeyword(String result) {

        AtomicInteger atomicInteger = new AtomicInteger(1);

        int maxLength;
        do {
            maxLength = 0;
            StringBuilder sb = new StringBuilder();

            String finalBefore = "";

            for (Map.Entry<String, List<EngKeyword>> entry : TranslateDictData.getInstance().getDictEngKeyword().entrySet()) {
                String key = ConvertUtil.joinSpace(entry.getKey());
                List<EngKeyword> keywords = entry.getValue();

                if (ConvertUtil.notContains(result, key)) {
                    continue;
                }

                int specwordIndex = result.indexOf(key);
                int selectwordIndex = specwordIndex;

                for (EngKeyword keyword : keywords) {

                    String regex = keyword.getCombinedWords();

                    int type = keyword.getType();

                    int transLength = 0;

                    String beforeResult = "";
                    String afterResult = "";
                    String engwords = "";

                    if (type == TranslateConstant.OR_SELECTED_COMBINED_SPEC) {
                        String subResult = result.substring(0, specwordIndex);
                        int startIndex = getStartIndex(subResult);

                        int selectwordLength = 0;
                        label1:
                        for (String selectword : keyword.getSelectedWords().split("/")) {
                            if (ConvertUtil.contains(subResult, selectword)) {
                                selectwordIndex = subResult.lastIndexOf(selectword);
                                selectwordLength = selectword.length();

                                for (Character character : subResult.substring(selectwordIndex + selectwordLength).toCharArray()) {
                                    if (!Pattern.compile(regex).matcher(String.valueOf(character)).matches()) {
                                        continue label1;
                                    }
                                }
                            }
                        }

                        if (selectwordLength == 0) {
                            for (Character character : org.apache.commons.lang.StringUtils.reverse(subResult).toCharArray()) {
                                if (startIndex == selectwordIndex) {
                                    break;
                                }

                                if (Pattern.compile(regex).matcher(String.valueOf(character)).matches() || character == ' ') {
                                    selectwordIndex--;
                                } else {
                                    break;
                                }
                            }
                        }
                        beforeResult = result.substring(0, selectwordIndex);
                        afterResult = result.substring(specwordIndex + key.length());

                        String transResult = result.substring(selectwordIndex + selectwordLength, specwordIndex).trim();
                        engwords = keyword.getEngWords().replaceAll("XX", transResult);

                        transLength = key.length() + transResult.length() + selectwordLength;

                    } else if (type == TranslateConstant.SELECTED_COMBINED_SPEC) {
                        String subResult = result.substring(specwordIndex + key.length());

                        label1:
                        for (String selectword : keyword.getSelectedWords().split("/")) {
                            selectword = ConvertUtil.joinSpace(selectword);

                            if (ConvertUtil.notContains(subResult, selectword)) {
                                continue label1;
                            }
                            selectwordIndex = subResult.indexOf(selectword);

                            for (Character character : subResult.substring(0, selectwordIndex).toCharArray()) {
                                if (!Pattern.compile(regex).matcher(String.valueOf(character)).matches()) {
                                    continue label1;
                                }
                            }

                            beforeResult = result.substring(0, specwordIndex);
                            afterResult = subResult.substring(selectwordIndex + selectword.length());

                            String transResult = subResult.substring(0, selectwordIndex).trim();
                            engwords = keyword.getEngWords().replaceAll("XX", transResult);

                            transLength = key.length() + transResult.length() + selectword.length();
                        }
                    } else if (type == TranslateConstant.SPEC_COMBINED) {
                        String subResult = result.substring(specwordIndex + key.length());

                        StringBuilder transResult = new StringBuilder();

                        for (Character character : subResult.toCharArray()) {
                            if (!Pattern.compile(regex).matcher(String.valueOf(character)).matches()) {
                                break;
                            }
                            transResult.append(character);
                        }

                        beforeResult = result.substring(0, specwordIndex);
                        afterResult = subResult.substring(transResult.length());

                        engwords = keyword.getEngWords().replaceAll("XX", transResult.toString().trim());

                        transLength = key.length() + transResult.toString().trim().length();
                    } else if (type == TranslateConstant.SPEC) {
                        String subResult = result.substring(0, specwordIndex);

                        int startIndex = getStartIndex(subResult);

                        if (subResult.length() == 0 || (subResult.length() != 0 &&
                                subResult.substring(startIndex, specwordIndex).trim().length() >= 3)) {

                            beforeResult = result.substring(0, specwordIndex);
                            afterResult = result.substring(specwordIndex + key.length());

                            engwords = keyword.getEngWords();
                            transLength = engwords.length();
                        }
                    } else if (type == TranslateConstant.PRIORITY) {
                        String priority = keyword.getPriority();
                        for (String tmp : priority.split("/")) {
                            if (!org.apache.commons.lang.StringUtils.equals(tmp, param.priority)) {
                                continue;
                            }

                            beforeResult = result.substring(0, specwordIndex);
                            afterResult = result.substring(specwordIndex + key.length());

                            engwords = keyword.getEngWords();
                            transLength = engwords.length();
                        }
                    }

                    if (transLength > maxLength && transLength > key.length()) {
                        maxLength = transLength;

                        finalBefore = beforeResult;

                        sb = new StringBuilder();
                        sb.append(beforeResult).append(" ");
                        //engwords = connNum(convertHz2Num(engwords));
                        engwords = convertHz2Num(engwords);
                        sb.append(engwords).append(" ");
                        sb.append(afterResult).append(" ");
                    }
                }
            }

            if (StringUtils.isNotEmpty(sb.toString())) {
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(param.wordIndex)) {
                    int index = 0;
                    for (Character character : finalBefore.toCharArray()) {
                        if (ConvertUtil.isChinese(character)) {
                            index++;
                        }
                    }
                    param.wordIndex.remove(index);
                }

                result = sb.toString();
            }
        } while (maxLength > 0 && atomicInteger.addAndGet(1) <= 50);

        //logger.info(String.format("特殊翻译原则执行%d次", atomicInteger.get()));
        return result;
    }

    private int getStartIndex(String subResult) {
        int startIndex = 0;
        for (String trans : param.translateWords) {
            int tmpIndex = org.apache.commons.lang.StringUtils.indexOf(subResult, trans);
            if (tmpIndex != -1) {
                startIndex = tmpIndex + trans.length();
            }
        }
        return startIndex;
    }

    /**
     * 中文转换方法(用于处理没有转换为英文的汉字)
     * @param sourceText 待转换文本
     * @return 转换后文本
     */
    private String convertChineseCharacter(String sourceText) {
        StringBuilder result = new StringBuilder();

        char[] chars = sourceText.toCharArray();

        Character[] characters = connChineseCharacter(chars);

        Iterator<Integer> indexIterator = param.wordIndex.iterator();

        for (Character character : characters) {
            if (ConvertUtil.isChinese(character)) {
                if (CollectionUtils.isEmpty(param.pinyins)) {
                    if (TranslateDictData.getInstance().getDictDictionary().containsKey(String.valueOf(character))) {
                        List<String> pinyins = TranslateDictData.getInstance().getDictDictionary().get(String.valueOf(character));
                        result.append(pinyins.iterator().next());
                    }
                } else {
                    if (indexIterator.hasNext()) {
                        result.append(param.pinyins.get(indexIterator.next()).toLowerCase());
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

        AtomicInteger index = new AtomicInteger(0);
        while (index.get() < array.length) {
            if (ConvertUtil.isChinese(array[index.get()])) {
                int count = countChineseCharacter(index.get(), array);
                int maxIndex = array.length;

                if (count <= TranslateConstant.MAX_CONNECTION_CHARACTER) {
                    while (index.get() < maxIndex && count > 0) {
                        if (array[index.get()] != ' ') {
                            characters.add(array[index.getAndAdd(1)]);
                            count--;
                        } else {
                            index.addAndGet(1);
                        }
                    }
                    characters.add(' ');
                } else {
                    while (index.get() < maxIndex) {
                        characters.add(array[index.getAndAdd(1)]);
                    }
                }
            } else {
                characters.add(array[index.getAndAdd(1)]);
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

    /**
     * 分词中纯数字段转换为阿拉伯数字
     * @param sourceText
     * @return
     */
    private String convertChineseNum(String sourceText) {
        StringBuilder sb = new StringBuilder();

        String[] array = sourceText.split(" ");
        label1:
        for (int index = 0; index < array.length; index++) {
            String after = "";
            String current = "";
            String before = "";

            if (index > 0) {
                after = array[index - 1];
            }
            current = array[index];
            if (index < array.length - 1) {
                before = array[index + 1];
            }
            for (String str : Arrays.asList(after, before)) {
                if (ConvertUtil.hasChineseWord(str)) {
                    sb.append(current).append(" ");
                    continue label1;
                }
            }
            for (Character character : current.toCharArray()) {
                if (!character.isDigit(character) && !ConvertUtil.isChineseNum(character)) {
                    sb.append(current).append(" ");
                    continue label1;
                }
            }
            sb.append(convertHz2Num(current)).append(" ");
        }
        return sb.toString();
    }

    /**
     * 中文数字转换为阿拉伯数字
     * @param sourceText
     * @return
     */
    private String convertHz2Num(String sourceText) {
        for (Map.Entry<String, String> entry : TranslateConstant.CHINESE_NUMBER.entrySet()) {
            sourceText = sourceText.replaceAll(entry.getKey(), entry.getValue());
        }
        return sourceText;
    }

    private String connNum(String sourceText) {
        StringBuilder sb = new StringBuilder();

        char[] characters = sourceText.toCharArray();
        for (int index = 0; index < characters.length; index++) {
            Character current = characters[index];
            sb.append(current);

            Character afterOne = null;
            Character afterTwo = null;

            if (index < characters.length - 1) {
                afterOne = characters[index + 1];
            }

            if (index < characters.length - 2) {
                afterTwo = characters[index + 2];
            }


            if (Character.isDigit(current)
                    && (null != afterOne && ' ' == afterOne)
                    && (null != afterTwo && Character.isDigit(afterTwo))) {
                index++;
            }
        }

        return sb.toString();
    }
}
