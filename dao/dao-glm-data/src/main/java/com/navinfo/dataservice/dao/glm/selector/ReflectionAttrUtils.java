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
import com.navinfo.dataservice.dao.glm.iface.IRow;
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
		ResultSetMetaData rsm = rs.getMetaData();
		int columnCount = rsm.getColumnCount();
		Field[] fields = row.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			if (fieldName.equals("pid")) {
				String pkName = row.parentPKName();
				Object value = rs.getInt(pkName);
				field.setAccessible(true);
				field.set(row, value);
				continue;
			}
			for (int j = 1; j <= columnCount; j++) {
				String columnName = rsm.getColumnName(j);
				if (fieldName.equalsIgnoreCase(StringUtils.toColumnName(columnName))) {
					int columnType = rsm.getColumnType(j);
					Object value = rs.getObject(j);
					if (Types.VARBINARY == columnType && fieldName.equals("rowId")) {
						String rowId = rs.getString(columnName);
						field.setAccessible(true);
						field.set(row, rowId);
						break;
					}
					if (Types.NUMERIC == columnType) {
						if (value.toString().contains(".")) {
							value = ((BigDecimal) value).doubleValue();
						} else {
							value = Integer.parseInt(value.toString());
						}
					}
					if (Types.STRUCT == columnType) {
						value = GeoTranslator.struct2Jts((STRUCT) value, 100000, 0);
					}
					field.setAccessible(true);
					field.set(row, value);
					break;
				}
			}
		}
	}





}
