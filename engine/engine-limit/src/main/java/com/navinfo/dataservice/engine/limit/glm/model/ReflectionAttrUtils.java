package com.navinfo.dataservice.engine.limit.glm.model;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import oracle.sql.STRUCT;
import org.apache.commons.lang.CharUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

public class ReflectionAttrUtils {

    /**
     * 将一条记录转成一个对象
     *
     * @param row 泛型类型
     * @param rs  ResultSet对象
     * @return 泛型类型对象
     * @throws Exception
     */
    public static void executeResultSet(IRow row, ResultSet rs) throws Exception {
        Class<?> clazz = row.getClass();
        ResultSetMetaData rsm = rs.getMetaData();
        int columnCount = rsm.getColumnCount();
        Map<String, Object> map = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            map.put(fieldToProperty(rsm.getColumnName(i), row), rs.getObject(i));
        }
        for (String fieldName : map.keySet()) {
            Field field = null;
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
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
     * 字段转换成对象属性 例如：user_name to userName
     *
     * @param field
     * @return
     */
    public static String fieldToProperty(String field, IRow row) {
        if (null == field) {
            return "";
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
