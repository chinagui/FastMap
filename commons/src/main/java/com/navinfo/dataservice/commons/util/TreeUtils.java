package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeUtils {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Map<String, Object>> mapList2Tree(
			List<Map<String, Object>> dataList, String idField,
			String parnetIdField, String childrenField) {
		if (dataList == null || dataList.size() == 0) {
			return new ArrayList();
		}

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> obj : dataList) {
			mapList.add(obj);
		}
		List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> obj : mapList) {
			boolean mark = false;
			for (Map<String, Object> parentObj : mapList) {
				if (obj.get(parnetIdField) != null
						&& obj.get(parnetIdField)
								.equals(parentObj.get(idField))) {
					mark = true;
					if (parentObj.get(childrenField) == null)
						parentObj.put(childrenField,
								new ArrayList<Map<String, Object>>());
					((ArrayList<Map<String, Object>>) parentObj
							.get(childrenField)).add(obj);
					break;
				}
			}
			if (!mark) {
				returnList.add(obj);
			}
		}
		return returnList;
	}
}
