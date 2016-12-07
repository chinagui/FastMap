package com.navinfo.dataservice.engine.meta.translate;

import com.navinfo.dataservice.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by chaixin on 2016/11/29 0029.
 */
public class SpecialRules {

    public List<String> processNOJuHao(String text) {
        List<String> splitStr = new ArrayList<>();
        if (StringUtils.isEmpty(text))
            return splitStr;

        String regex = ".*[Ｎｎ]+[ｏＯ]+．+.*";
        Pattern pattern = Pattern.compile(regex);
        String[] nums = new String[]{"Ｎｏ．", "ＮＯ．", "ｎｏ．", "ｎＯ．"};
        while (pattern.matcher(text).matches()) {
            for (String num : nums) {
                int index = -1;
                if ((index = text.indexOf(num)) != -1) {
                    if (index > 0)
                        splitStr.add(text.substring(0, index));
                    splitStr.add(num);
                    text = text.substring(index + num.length());
                } else {
                    continue;
                }
            }
        }
        splitStr.add(text);
        return splitStr;
    }

    public boolean endsWithNOPoint(String text) {
        if (text.endsWith("ｎｏ．") || text.endsWith("ｎＯ．") ||
                text.endsWith("Ｎｏ．") || text.endsWith("ＮＯ．"))
            return true;
        return false;
    }

    public String processPYForNO(String text, String oldText) {
        if (StringUtils.isEmpty(text))
            return text;

        Pattern pattern = Pattern.compile(".*[Ｎｎ]+[ｏＯ]+．+.*");
        if (!pattern.matcher(oldText).matches())
            return text;

        pattern = Pattern.compile(".*[nN]+[oO]+( .)+.*");
        String[] strList = new String[]{"No . ", "NO . ", "no . ", "nO . ", "No .", "NO .", "no .", "nO ."};
        while (pattern.matcher(text).matches()) {
            String strNo = "";
            int index = -1;
            int ct = 0;
            for (String str : strList) {
                ct += 1;
                index = text.indexOf(str);
                if (index >= 0) {
                    strNo = str;
                    break;
                }
            }
            if (ct >= 8) {
                break;
            }
            if (index >= 0) {
                text = text.replace(strNo, strNo.replace(" ", ""));
            }
        }
        return text.trim();
    }

    public static void main(String[] args) {
        SpecialRules rules = new SpecialRules();
        System.out.println(Arrays.toString((rules.processNOJuHao("zzＮＯ．123").toArray())));
    }
}
