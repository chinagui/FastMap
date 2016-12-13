/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector;

/** 
 * @ClassName: SqlHelper 
 * @author Zhang Xiaolong
 * @date 2016年7月21日 上午10:56:17 
 * @Description: TODO
 */
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.CharUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;

import oracle.sql.STRUCT;

public class ReflectionAttrUtils {

	/**
	 * 将一条记录转成一个对象
	 * 
	 * @param cls
	 *            泛型类型
	 * @param rs
	 *            ResultSet对象
	 * @return 泛型类型对象
	 * @throws Exception
	 */
	public static void executeResultSet(IRow row, ResultSet rs) throws Exception {
		Class<?> clazz = row.getClass();
		ResultSetMetaData rsm = rs.getMetaData();
		int columnCount = rsm.getColumnCount();
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 1; i <= columnCount; i++) {
			map.put(fieldToProperty(rsm.getColumnName(i), row), rs.getObject(i));
		}
		for (String fieldName : map.keySet()) {
			Field field = null;
			if (fieldName.equals("pid") && row instanceof IObj) {
				field = clazz.getDeclaredField("pid");
				field.setAccessible(true);
				field.set(row, Integer.valueOf(map.get("pid").toString()));
				continue;
			}
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				// if ("uRecord".equals(fieldName) ||
				// "uFields".equals(fieldName)
				// || "uDate".equals(fieldName))
				// continue;
				// System.out.println(fieldName + "在" + clazz.getName()
				// + "中没有对应的属性");
				continue;
			}
			Object value = map.get(fieldName);
			if (null != value) {
				switch (field.getType().getName()) {
				case "java.lang.String":
					if (fieldName.equalsIgnoreCase("rowid")) {
						value = rs.getString("row_id");
					} else
						value = String.valueOf(value);
					break;
				case "int":
					value = Integer.valueOf(value.toString());
					break;
				case "long":
					value = Long.valueOf(value.toString());
					break;
				case "double":
					value = Double.valueOf(value.toString());
					break;
				case "java.math.BigInteger":
					value = new BigInteger(value.toString());
					break;
				case "java.math.BigDecimal":
					value = new BigDecimal(value.toString());
				case "java.sql.Date":
				case "java.util.Date":
					value = rs.getTimestamp(StringUtils.toColumnName(fieldName));
					break;
				case "com.vividsolutions.jts.geom.Geometry":
					value = GeoTranslator.struct2Jts((STRUCT) value, 100000, 0);
				default:
					break;
				}
			}
			field.setAccessible(true);
			field.set(row, value);
		}

	}

	/**
	 * 枚举类型转表名称
	 * 
	 * @param objType
	 * @return
	 * @throws Exception
	 */
	public static String getTableNameByObjType(ObjType objType) throws Exception {
		switch (objType) {
		case RDLINK:
			return "RD_LINK";
		case LCFACE:
			return "LC_FACE";
		case RDSE:
			return "RD_SE";
		case RDSPEEDBUMP:
			return "RD_SPEEDBUMP";
		case RDSAMENODE:
			return "RD_SAMENODE";
		case RDSAMELINK:
			return "RD_SAMELINK";
		case RDDIRECTROUTE:
			return "RD_DIRECTROUTE";
		case RDTOLLGATE:
			return "RD_TOLLGATE";
		case RDOBJECT:
			return "RD_OBJECT";
		case RDROAD:
			return "RD_ROAD";
		case RDVOICEGUIDE:
			return "RD_VOICEGUIDE";
		case RDVARIABLESPEED:
			return "RD_VARIABLESPEED";
		case RDLANE:
			return "RD_LANE";
		case IXSAMEPOI:
			return "IX_SAMEPOI";
		case RDHGWGLIMIT:
			return "RD_HGWGLIMIT";
		case RDMILEAGEPILE:
			return "RD_MILEAGEPILE";
		case RDTMCLOCATION:
			return "RD_TMCLOCATION";
		case RDNODE:
			return "RD_NODE";
		case RDRESTRICTION:
			return "RD_RESTRICTION";
		case RDCROSS:
			return "RD_CROSS";
		case RDLANECONNEXITY:
			return "RD_LANECONNEXITY";
		case RDSPEEDLIMIT:
			return "RD_SPEEDLIMIT";
		case RDLINKSPEEDLIMIT:
			return "RD_LINK_SPEEDLIMIT";
		case RDBRANCH:
			return "RD_BRANCH";
		case RDGSC:
			return "RD_GSC";
		case ADLINK:
			return "AD_LINK";
		case ADFACE:
			return "AD_FACE";
		case ADNODE:
			return "AD_NODE";
		case RWLINK:
			return "RW_LINK";
		case RWNODE:
			return "RW_NODE";
		case ADADMIN:
			return "AD_ADMIN";
		case IXPOI:
			return "IX_POI";
		case ZONENODE:
			return "ZONE_NODE";
		case ZONELINK:
			return "ZONE_LINK";
		case ZONEFACE:
			return "ZONE_FACE";
		case LUNODE:
			return "LU_NODE";
		case LULINK:
			return "LU_LINK";
		case LUFACE:
			return "LU_FACE";
		case RDTRAFFICSIGNAL:
			return "RD_TRAFFICSIGNAL";
		case RDELECTRONICEYE:
			return "RD_ELECTRONICEYE";
		case RDWARNINGINFO:
			return "RD_WARNINGINFO";
		case RDSLOPE:
			return "RD_SLOPE";
		case RDGATE:
			return "RD_GATE";
		case RDINTER:
			return "RD_INTER";
		case LCNODE:
			return "LC_NODE";
		case LCLINK:
			return "LC_LINK";
		case RDBRANCHDETAIL:
			return "RD_BRANCH_DETAIL";
		case RDSIGNBOARD:
			return "RD_SIGNBOARD";
		default:
			throw new Exception("不支持的对象类型:" + objType.toString());
		}
	}

	/**
	 * 表名称转为枚举对象类型
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static ObjType getObjTypeByTableName(String tableName) throws Exception {
		switch (tableName) {
		case "RD_NODE":
			return ObjType.RDNODE;
		case "AD_NODE":
			return ObjType.ADNODE;
		case "ZONE_NODE":
			return ObjType.ZONENODE;
		case "LU_NODE":
			return ObjType.LUNODE;
		case "RW_NODE":
			return ObjType.RWNODE;
		case "RD_LINK":
			return ObjType.RDLINK;
		case "AD_LINK":
			return ObjType.ADLINK;
		case "ZONE_LINK":
			return ObjType.ZONELINK;
		case "LU_LINK":
			return ObjType.LULINK;
		case "RW_LINK":
			return ObjType.RWLINK;
		default:
			throw new Exception("不支持的表名转对象名称:" + tableName);
		}
	}

	/**
	 * 字段转换成对象属性 例如：user_name to userName
	 * 
	 * @param field
	 * @return
	 */
	public static String fieldToProperty(String field, IRow row) {
		if (null == field) {
			return "";
		}
		if (row instanceof IObj) {
			if (((IObj) row).primaryKey().equalsIgnoreCase(field))
				return "pid";
		} else if (row instanceof IxPoiPhoto && field.equalsIgnoreCase("pid")) {
			return "fccPid";
		}
		char[] chars = field.toLowerCase().toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '_') {
				int j = i + 1;
				if (j < chars.length) {
					sb.append(org.apache.commons.lang.StringUtils.upperCase(CharUtils.toString(chars[j])));
					i++;
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();

	}

}
