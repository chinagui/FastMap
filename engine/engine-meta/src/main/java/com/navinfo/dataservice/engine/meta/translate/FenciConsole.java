package com.navinfo.dataservice.engine.meta.translate;

import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by chaixin on 2016/11/29 0029.
 */
public class FenciConsole {

    public String[] mainSplit(String text) throws IOException {
        ConvertUtil convertUtil = new ConvertUtil();
        text = convertUtil.convHalf2Full(text);
        return doSegment(text);
    }

    private String[] doSegment(String text) throws IOException {
        Map<String, String> map = TranslateDictData.getInstance().getDictChi2Eng();
        String[] tmp = replaceKeyWord(text);
        String res = doSplit(map, tmp[0], false);
        String eng = doSplit(map, tmp[1], true);
        res = connSingleLetter(res);
        eng = connSingleLetter(eng);
        return new String[]{res, eng};
    }

    private String doSplit(Map<String, String> map, String text, boolean isEng) {
        while (text.indexOf("  ") != -1)
            text = text.replaceAll("  ", " ");

        StringBuffer res = new StringBuffer();
        for (String tmpStr : text.split("/")) {
            char[] charAry = tmpStr.toCharArray();
            int index = 0;
            while (index < tmpStr.length()) {
                char c = charAry[index];
                if (ConvertUtil.isChinese(c)) {
                    StringBuffer resTmp = new StringBuffer();
                    int subIndex = 0;
                    int length = tmpStr.length() - index;
                    while (subIndex < length) {
                        boolean isFind = false;
                        String subText = "";
                        for (int i = 0; i < length - subIndex; i++) {
                            subText = tmpStr.substring(index, tmpStr.length() - i);
                            if (map.containsKey(subText)) {
                                if (isEng) {
                                    resTmp.append(map.get(subText)).append("/");
                                } else {
                                    resTmp.append(subText).append("/");
                                }
                                index += subText.length();
                                isFind = true;
                                break;
                            }
                        }
                        if (!isFind) {
                            resTmp.append(subText).append("/");
                            index++;
                        } else {
                            break;
                        }
                        subIndex++;
                    }
                    res.append(resTmp);
                } else {
                    char next = c;
                    if (index + 1 < tmpStr.length())
                        next = charAry[index + 1];
                    index++;
                    if (c == ' ') {
                        res.append(" ");
                        continue;
                    }
                    if (next == ' ') {
                        res.append(c);
                        continue;
                    }
                    if (ConvertUtil.isLetter(c) && !ConvertUtil.isLetter(next)) {
                        res.append(c).append("/");
                        continue;
                    }
                    if (ConvertUtil.isChinese(c) && !ConvertUtil.isChinese(next)) {
                        res.append(c).append("/");
                        continue;
                    }
                    if (Character.isDigit(c) && !Character.isDigit(next)) {
                        res.append(c).append("/");
                        continue;
                    }
                    res.append(c);
                    if (index == tmpStr.length()) res.append("/");
                }
            }
        }
        text = res.toString();
        return text.endsWith("/") ? text.substring(0, text.length() - 1) : text;
    }

    private String connSingleLetter(String text) {
        StringBuffer result = new StringBuffer();
        String regex = "[Ａ-Ｚａ-ｚ０-９1-9]{1}";
        Pattern pattern = Pattern.compile(regex);
        String[] textAry = text.split("/");
        boolean isNumOrLet = false;
        for (int i = 0; i < textAry.length; i++) {
            String str = textAry[i];
            if (str.length() > 1) {
                if (isNumOrLet)
                    result.append("/").append(str).append("/");
                else
                    result.append(str).append("/");
                isNumOrLet = false;
            } else if (i < textAry.length - 1) {
                if (pattern.matcher(str).matches()) {
                    result.append(str);
                    isNumOrLet = true;
                } else if ("　".equals(str)) {
                    result.append("/");
                } else {
                    if (isNumOrLet)
                        result.append("/").append(str).append("/");
                    else
                        result.append(str).append("/");
                }
            } else {
                if (pattern.matcher(str).matches()) {
                    result.append(str).append("/");
                } else {
                    if (isNumOrLet)
                        result.append("/").append(str).append("/");
                    else
                        result.append(str).append("/");
                }
            }
        }
        return result.toString();
    }

    private Map<String, String> position = new HashMap<String, String>() {{
        put("东", "East");
        put("西", "West");
        put("南", "South");
        put("北", "North");
        put("中", "Middle");
    }};

    private Map<String, String> keyWord = new HashMap<String, String>() {{
        put("收费站", "Toll Gate");
        put("收费点", "Toll Gate");
        put("收费处", "Toll Gate");
        put("岗", "Post");
        put("口", "Intersection");
        put("桥", "Bridge");
        put("岛", "Island");
        put("宫", "Temple ");
        put("庙", "Temple ");
        put("寺", "Temple ");
        put("祠", "Temple ");
        put("墓", "Tomb");
        put("塔", "Tower");
        put("林", "Wood  ");
        put("苑", "Court");
        put("陵", "Mausoleum");
        put("峰", "Peak");
        put("井", "Well");
    }};

    private String[] replaceKeyWord(String text) {
        StringBuffer res = new StringBuffer();
        StringBuffer eng = new StringBuffer();
        JSONObject json = endOfKeyWord(text);
        if (null != json) {
            Integer index = json.getInt("index");
            String posKey = text.substring(index - 1, index);
            if (position.containsKey(posKey)) {
                res.append(text.substring(0, index - 1)).append("/");
                res.append(posKey).append("/");
                res.append(json.getString("key"));

                eng.append(text.substring(0, index - 1)).append("/");
                eng.append(position.get(posKey)).append("/");
                eng.append(json.getString("value"));
            } else {
                res.append(text.substring(0, index)).append("/");
                res.append(json.getString("key"));

                eng.append(text.substring(0, index)).append("/");
                eng.append(json.getString("value"));
            }
        } else {
            res = new StringBuffer(text);
            eng = new StringBuffer(text);
        }
        return new String[]{res.toString(), eng.toString()};
    }

    private JSONObject endOfKeyWord(String text) {
        JSONObject res = null;
        for (String key : keyWord.keySet()) {
            int index = 0;
//            if (text.endsWith(key) && ((index = text.indexOf(key)) >= 2)) {
            if ((index = text.indexOf(key)) >= 2) {
                res = new JSONObject();
                res.put("index", index);
                res.put("key", key);
                res.put("value", keyWord.get(key));
            }
        }
        return res;
    }

    public static void main(String[] args) {
        FenciConsole console = new FenciConsole();
        System.out.println(Arrays.toString(console.replaceKeyWord("渭a b南cc f东NO 5收费站")));
    }


}
