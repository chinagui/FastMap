package com.navinfo.dataservice.engine.meta.translate;

import com.navinfo.dataservice.commons.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by chaixin on 2016/11/29 0029.
 */
public class Pyconverter {

    private SpecialRules rules;

    private String CCPREFIXCC = "ccprefixcc";

    private boolean getBFirstPy = false;

    private boolean getBMultiMark = false;

    private boolean getBSpace = false;

    private boolean getBFirstCapital = true;

    public Pyconverter() {
        rules = new SpecialRules();
    }

    public String convertPy(String text) {
        if (StringUtils.isEmpty(text))
            return "";
        return convert(text);
    }

    private String convert(String sourceText) {
        String text = new String(sourceText);
        String pinyin = "";
        text = ConvertUtil.convHalf2Full(text);
        List<String> list = rules.processNOJuHao(text);
        if (list.size() > 1) {
            for (String item : list) {
                if (rules.endsWithNOPoint(item)) {
                    pinyin += convert(item);
                    continue;
                }
                pinyin += convert(item) + " ";
            }
            pinyin = pinyin.replaceAll("  ", "  ");
            return pinyin;
        }

        text = ConvertUtil.convFull2Half(text);

        text = addPrefix(text);

        text = convertKernel(text);

        text = ConvertUtil.convFull2Half(text).trim();

        if (getBFirstCapital)
            text = ConvertUtil.firstCapital(text);

        if (getBSpace)
            text = text.replaceAll(" ", "");

        text = ConvertUtil.trimMultiSpace(text);

        pinyin = rules.processPYForNO(text, sourceText);

        return pinyin;
    }


    private String convertKernel(String text) {
        text = text.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ");
        text = ConvertUtil.numToPy(text);
        text = ConvertUtil.romeNumToPy(text);
        text = convertMultiWordEx(text);
        text = convertHZ(text);
        return text;
    }

    private String addPrefix(String text) {
        StringBuffer textNew = new StringBuffer();
        boolean increase = true;
        for (Character c : text.toCharArray()) {
            if (ConvertUtil.isLetter(c)) {
                if (increase) {
                    textNew.append(CCPREFIXCC).append(c);
                    increase = false;
                }
            } else {
                textNew.append(c);
                increase = true;
            }
        }
        return textNew.toString();
    }

    private String convertHZ(String text) {
        String textNew = "";
        Map<String, List<String>> map = TranslateDictData.getInstance().getDictDictionary();
        for (Character c : text.toCharArray()) {
            try {
                if (c > 0 && c < 128) {
                    if ((c >= 48 && c <= 57) || (c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
                        textNew += c;
                    } else {
                        textNew += " " + c + " ";
                    }
                    continue;
                }
            } catch (Exception e) {
            }

            if (map.containsKey(String.valueOf(c))) {
                List<String> wordList = map.get(String.valueOf(c));
                if (wordList.size() == 1) {
                    textNew += " " + wordList.get(0) + " ";
                } else {
                    if (getBFirstPy) {
                        textNew += " " + "(";
                        for (String pyTmp : wordList) {
                            textNew += pyTmp + " ";
                        }
                        textNew = textNew.trim();
                        textNew += ")" + " ";
                    } else if (getBMultiMark) {
                        textNew += " " + "(" + wordList.get(0) + ")" + " ";
                    } else {
                        textNew += " " + wordList.get(0) + " ";
                    }
                }
            } else {
                textNew += " " + c + " ";
            }
        }
        return textNew;
    }

    private String convertMultiWordEx(String text) {
        String textNew = "";
        String strPy = "";
        String strPy2 = "";
        int i = 0;
        Map<String, String> indexMap = TranslateDictData.getInstance().getDictWordIndex();
        Map<String, List<Map<String, String>>> map = TranslateDictData.getInstance().getDictWord();
        while (i < text.length()) {
            // 非中文字符直接算拼音了
            try {
                char c = text.charAt(i);
                if (c > 0 && c < 128) {
                    textNew += c;
                    i++;
                    continue;
                }
            } catch (Exception e) {
            }

            String strMatch = "";
            String strMatch2 = "";
            int length = text.length() - i;
            for (int x = 1; x < length + 1; x++) {
                String strTmp = text.substring(i, i + x);
                if (!indexMap.containsKey(strTmp)) {
                    break;
                } else {
                    strMatch = indexMap.get(strTmp);
                }

                if (map.containsKey(strMatch)) {
                    List<Map<String, String>> wordList = map.get(strMatch);
                    int iFound = 0;
                    for (Map<String, String> wordMap : wordList) {
                        if ((iFound < 1) && StringUtils.isEmpty(wordMap.get("admin"))) {
                            iFound = 1;
                            strPy = wordMap.get("py");
                            strPy2 = wordMap.get("py2");
                            continue;
                        } else if (StringUtils.isNotEmpty(wordMap.get("admin")) && "".
                                startsWith(wordMap.get("admin"))) {
                            iFound = 2;
                            strPy = wordMap.get("py");
                            strPy2 = wordMap.get("py2");
                            break;
                        }
                    }
                    if (iFound > 0) {
                        strMatch2 = strMatch;
                    }
                }
            }
            if (StringUtils.isEmpty(strMatch) || StringUtils.isEmpty(strMatch2)) {
                textNew += text.substring(i, i + 1);
                i += 1;
                continue;
            }

            // 增加对多音字分隔符的设置支持
            if (StringUtils.isNotEmpty(strPy2)) {
                strPy2 = strPy2.replace("(", "{");
                strPy2 = strPy2.replace(")", "}");
            }
//            if self.m_context.getBMultiMark() and not StringHelper.isEmptyString(strPy2):
//            strNew += " " + strPy2 + " "
//            else:
            textNew += " " + strPy + " ";

            i += strMatch2.length();
            continue;
        }
        return textNew;
    }

    public static void main(String[] args) {
        Pyconverter pyconverter = new Pyconverter();
        System.out.println(pyconverter.addPrefix("abc汉def"));
    }
}
