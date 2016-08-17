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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

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
	public static void executeResultSet(IRow row, ResultSet rs)
			throws Exception {
		ResultSetMetaData rsm = rs.getMetaData();
		int columnCount = rsm.getColumnCount();
		Field[] fields = row.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			if (fieldName.equals("pid") && row instanceof IObj) {
				String pkName = ((IObj) row).primaryKey();
				Object value = rs.getInt(pkName);
				field.setAccessible(true);
				field.set(row, value);
				continue;
			}
			for (int j = 1; j <= columnCount; j++) {
				String columnName = rsm.getColumnName(j);
				if (columnName.equalsIgnoreCase(StringUtils
						.toColumnName(fieldName))) {
					int columnType = rsm.getColumnType(j);
					Object value = rs.getObject(j);
					if (value != null) {
						if (Types.VARBINARY == columnType
								|| Types.VARCHAR == columnType) {
							value = rs.getString(columnName);
						}
						if (Types.NUMERIC == columnType) {
							if (value.toString().contains(".")) {
								value = ((BigDecimal) value).doubleValue();
							} else {
								value = ((BigDecimal) value).intValue();
							}
						}
						if (Types.STRUCT == columnType) {
							value = GeoTranslator.struct2Jts((STRUCT) value,
									100000, 0);
						}
						if (Types.TIMESTAMP == columnType) {
							value = rs.getTimestamp(columnName);
						}
						field.setAccessible(true);
						field.set(row, value);
					}
					break;
				}
			}
		}
	}

	/**
	 * 枚举类型转表名称
	 * 
	 * @param objType
	 * @return
	 * @throws Exception
	 */
	public static String getTableNameByObjType(ObjType objType)
			throws Exception {
		switch (objType) {
		case RDNODE:
			return "RD_NODE";
		case ADNODE:
			return "AD_NODE";
		case ZONENODE:
			return "ZONE_NODE";
		case LUNODE:
			return "LU_NODE";
		case RWNODE:
			return "RW_NODE";
		case RDLINK:
			return "RD_LINK";
		case ADLINK:
			return "AD_LINK";
		case ZONELINK:
			return "ZONE_LINK";
		case LULINK:
			return "LU_LINK";
		case RWLINK:
			return "RW_LINK";
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
	public static ObjType getObjTypeByTableName(String tableName)
			throws Exception {
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
		default:
			throw new Exception("不支持的表名转对象名称:" + tableName);
		}
	}

}
