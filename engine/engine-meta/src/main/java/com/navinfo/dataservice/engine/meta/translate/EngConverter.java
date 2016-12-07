package com.navinfo.dataservice.engine.meta.translate;

import com.navinfo.dataservice.commons.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by chaixin on 2016/11/29 0029.
 */
public class EngConverter {

    private static String strPoiName;

    private static String strPyPoiName;

    private boolean bFirstPy = false;

    private boolean bMultiMark = false;

    private boolean bSpace = false;

    private boolean bFirstCapital = true;

    private boolean bConfuseMark = false;

    private boolean bConvNum = false;

    public String convertEng(String sourceText, String pinyin, String splitWord, String splitEngWord) throws IOException {
        if (StringUtils.isEmpty(sourceText)) return "";
        return convertAll(sourceText, pinyin, splitWord, splitEngWord);
    }

    private String convertAll(String sourceText, String pinyin, String splitWord, String splitEngWord) {
        String[] tmpAry = ConvertUtil.delNoChinese(sourceText);
        strPoiName = tmpAry[0];
        strPyPoiName = tmpAry[1];

        String strOld = new String(splitEngWord);

        while (strOld.indexOf("//") != -1) {
            strOld = strOld.replaceAll("//", "/");
        }

        strOld = convertKernel(strOld);

        strOld = ConvertUtil.trimMultiSpace(strOld);

        strOld = ConvertUtil.firstCapital(strOld);

        strOld = ConvertUtil.trimMultiSpace(strOld);

        strOld = strOld.replace("@", " ");
        strOld = strOld.replace(" )", ")");
        strOld = strOld.replace(" ( ", " (");
        strOld = strOld.replace("( ", "(");
        strOld = strOld.replace(" . ", ".");
        strOld = strOld.replace(" & ", "&");
        strOld = strOld.replace("& ", "&");
        strOld = strOld.replace(" &", "&");
        strOld = strOld.replace(" - ", "-");
        strOld = strOld.replace("- ", "-");
        strOld = strOld.replace(" + ", "+");
        strOld = strOld.replace("+ ", "+");
        strOld = strOld.replace(" / ", "/");
        strOld = strOld.replace("/ ", "/");

        strOld = ConvertUtil.trimMultiSpace(strOld);

        return strOld;
    }

    private String convertKernel(String text) {
        text = text.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ");
        if (text.startsWith("-") && text.length() > 1)
            text = text.substring(1);

        text = convertSpecialWord(text);

        // 转数字
        if (bConvNum) {
            text = ConvertUtil.numToPy(text);
            text = ConvertUtil.romeNumToPy(text);
        }

        // 转英文
        text = convertMultiWordEx(text);

        // 删除特殊字符
        text = ConvertUtil.delSymbol(text);

        // 全角转为半角
        text = ConvertUtil.convFull2Half(text);

        // 不包含中文不做转拼音
        if (ConvertUtil.checkContainCh(text)) {
            text = convertHZ(text, 0);
        }

        text = ConvertUtil.trimMultiSpace(text);

        return text;
    }

    private String convertSpecialWord(String text) {
        String regex = "[Ａ-Ｚａ-ｚ０-９0-9]+";
        String textTemps = "";
        String[] words = text.split("/");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (StringUtils.isNotEmpty(textTemps))
                textTemps += "/";

            if (Pattern.compile(regex).matcher(word).matches()) {
                if (i + 1 >= words.length) break;
                String oneWord = words[i + 1];
                if (i + 2 < words.length) {
                    String twoWord = oneWord + words[i + 2];
                    if (keyWord.containsKey(twoWord)) {
                        textTemps += keyWord.get(twoWord).replace("$$", word);
                        i += 2;
                    } else if (keyWord.containsKey(oneWord)) {
                        textTemps += keyWord.get(oneWord).replace("$$", word);
                        i += 1;
                    } else {
                        textTemps += word;
                    }
                } else {
                    if (keyWord.containsKey(oneWord)) {
                        textTemps += keyWord.get(oneWord).replace("$$", word);
                        i += 1;
                    } else {
                        textTemps += word;
                    }
                }
            } else {
                textTemps += word;
            }
        }
        return textTemps;
    }

    private String convertMultiWordEx(String text) {
        String textNew = "";
        String strPy = "";
        String strPy2 = "";
        String[] wordSplist = text.split("/");
        Map<String, String> indexMap = TranslateDictData.getInstance().getDictWordIndex();
        Map<String, List<Map<String, String>>> map = TranslateDictData.getInstance().getDictWord();
        for (String strTmp : wordSplist) {
            if (map.containsKey(strTmp)) {
                List<Map<String, String>> wordList = map.get(strTmp);
                int iFound = 0;
                for (Map<String, String> wordMap : wordList) {
                    if (iFound < 1 && StringUtils.isEmpty(wordMap.get("admin"))) {
                        iFound = 1;
                        strPy = wordMap.get("py");
                        strPy2 = wordMap.get("py2");
                        continue;
                    } else if (StringUtils.isNotEmpty(wordMap.get("admin")) && "".startsWith(wordMap.get("admin"))) {
                        iFound = 2;
                        strPy = wordMap.get("py");
                        strPy2 = wordMap.get("py2");
                        break;
                    }
                }
                // 增加对多音字分隔符的设置支持
                if (StringUtils.isNotEmpty(strPy2)) {
                    strPy2 = strPy2.replace("(", "{");
                    strPy2 = strPy2.replace(")", "}");
                }
                if (bMultiMark && StringUtils.isNotEmpty(strPy2)) {
                    strPy2 = strPy2.replace(" ", "@");
                    textNew += "@" + strPy2;
                } else {
                    if (StringUtils.isNotEmpty(strPy)) {
                        int index = strPy.indexOf("’");
                        if (index >= 0) {
                            List<String> strList = new ArrayList<>();
                            boolean flag = false;
                            for (Character str : strPy.toCharArray()) {
                                if (flag) {
                                    strList.add(ConvertUtil.firstCapital(str + ""));
                                } else {
                                    strList.add(str + "");
                                }
                                if (str == '’') {
                                    flag = true;
                                } else {
                                    flag = false;
                                }
                            }
                            strPy = Arrays.toString(strList.toArray()).replaceAll("\\[", "").replaceAll("]", "").replaceAll(",", "");
                            strPy = strPy.replaceAll("’", " ");
                        }
                        strPy = strPy.replace(" ", "@");
                        textNew += "@" + strPy;
                    } else {
                        textNew += " " + convertHZ(strTmp, 1) + " ";
                    }
                }
            } else {
                textNew += " " + strTmp + " ";
                continue;
            }
        }
        return textNew;
    }

    private String convertHZ(String text, int type) {
        text = ConvertUtil.trimMultiSpace(text);
        String textNew = "";
        int nFlag = 0; // 标示是否四个单字
        Map<String, List<String>> map = TranslateDictData.getInstance().getDictDictionary();
        for (Character c : text.toCharArray()) {
            if (c == ' ') {
                if (textNew.endsWith("11111")) {
                    continue;
                }
            }

            if (!ConvertUtil.isChinese(c)) {
                if (textNew.endsWith("22222") || textNew.endsWith("33333") || textNew.endsWith("11111")) {
                    textNew += " " + c;
                } else {
                    textNew += c;
                }
                if (nFlag > 0) {
                    if (nFlag >= 4) {// cz 处理4以上的单字拼音
                        textNew = textNew.replace("11111", "22222"); //4 个
                    } else {
                        textNew = textNew.replace("11111", "33333"); //3 个
                    }
                }
                nFlag = 0;
                continue;
            }
            String mark;
            List<String> wordList = new ArrayList<>();
            if (map.containsKey(String.valueOf(c))) {
                wordList = map.get(String.valueOf(c));
                if (StringUtils.isNotEmpty(textNew)) {
                    mark = "";
                } else {
                    mark = "11111";
                }
                if (wordList.size() == 1) {
                    textNew += mark + wordList.get(0) + "11111";
                } else {
                    int charIndex = strPoiName.indexOf(String.valueOf(c));
                    String[] pyList = strPyPoiName.split(" ");
                    textNew += mark + ConvertUtil.converFirstLittle(pyList[charIndex]) + "11111";
                }
                nFlag += 1;
            } else {
                textNew += c;
            }
        }
        if (nFlag > 0) {
            if (nFlag >= 4) { // cz 处理4以上的单字拼音
                textNew = textNew.replace("11111", "22222"); // 4 个
            } else {
                textNew = textNew.replace("11111", "33333"); // 3 个
            }
        }
        textNew = textNew.replace("22222", " ");
        // 加混淆音分隔
        if (bConfuseMark) {
//            textNew = self.convUtil.AddConfuseMark(strNew)
        }
        textNew = textNew.replace("33333", " ");
        if (type == 1) {
            textNew = textNew.replace(" ", "");
        }
        return textNew;
    }

    private Map<String, String> keyWord = new HashMap<String, String>() {{
        put("国道", "G$$");
        put("省道", "S$$");
        put("县道", "X$$");
    }};

}
