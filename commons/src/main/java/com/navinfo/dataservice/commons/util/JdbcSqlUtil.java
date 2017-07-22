package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public class JdbcSqlUtil {
	  
	  
    /** 
     * 根据传入的List和参数，拼接in条件，防止in超过999条 
     * 
     * @param list 要分割的list
     * @param parameter 查询的条件
     * @return list.size()=n  'list1','list2',...,'list900') or parameter in ('list901','list902',...,'list1800') or parameter in ('list1801','list1802',...,'listn' 
     * list.size()=0  '' 
     */  
    public static String getInParameter(List list, String parameter) {  
        if (!list.isEmpty()) {  
            List<String> setList = new ArrayList<String>(0);  
            Set set = new HashSet();  
            StringBuffer stringBuffer = new StringBuffer();  
            for (int i = 1; i <= list.size(); i++) {  
                set.add(list.get(i - 1));  
                if (i % 900 == 0) {//900为阈值  
                    setList.add(StringUtils.join(set.iterator(), ","));  
                    set.clear();  
                }  
            }
            if (!set.isEmpty()) {  
                setList.add(StringUtils.join(set.iterator(), ","));  
            }  
            stringBuffer.append(setList.get(0));  
            for (int j = 1; j < setList.size(); j++) {  
                stringBuffer.append(") or " + parameter + " in (");  
                stringBuffer.append(setList.get(j));  
            }  
            return stringBuffer.toString();  
        } else {  
            return "''";  
        }  
    }  
}
