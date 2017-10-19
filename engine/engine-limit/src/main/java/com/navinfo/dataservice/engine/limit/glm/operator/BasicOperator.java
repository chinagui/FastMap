package com.navinfo.dataservice.engine.limit.glm.operator;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/***
 * 基础操作类
 *
 * @author zhaokk
 *
 */

public class BasicOperator extends AbstractOperator {

    private static Logger logger = Logger.getLogger(BasicOperator.class);
    //	private static final String M_PID = "pid";
    private static final String M_PRIMARYKEY = "primaryKey";
    private static final String M_PRIMARYKEY_VALUE = "primaryKeyValue";
    private static final String M_PARENTPK = "parentPKName";
    private static final String M_PARENTPKVALUE = "parentPKValue";
    private static final String M_STRING = "class java.lang.String";
    private static final String M_GEOMETRY = "class com.vividsolutions.jts.geom.Geometry";
    private static final String M_DATE = "class java.util.Date";
    private static final String M_U_DATE = "u_date";
    private IRow row;

    public BasicOperator(Connection conn, IRow row) throws Exception {
        super(conn);
        this.row = row;
    }

    @Override
    public void insertRow2Sql(Statement stmt) throws Exception {
        this.generateInsertSql(stmt, row);
    }

    /***
     * 自动组装新增语句
     *
     * @param stmt
     * @param row
     * @throws Exception
     */
    public void generateInsertSql(Statement stmt, IRow row) throws Exception {
        Class<?> c = row.getClass();
        Field[] fields = c.getDeclaredFields();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        key.append("insert into ");
        key.append(row.tableName());
        key.append("(");
        value.append(" values ( ");

        boolean complexGeo = false;

        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (isBasicType(f)) {
                if (f.getModifiers() == 4) {
                    continue;
                }
                f.setAccessible(true);
                Object oj = f.get(row);
                if (oj instanceof Integer) {
                    oj = Integer.parseInt(String.valueOf(oj));
                } else if (oj instanceof Double) {
                    oj = Double.parseDouble(String.valueOf(oj));
                } else if (oj instanceof String) {
                    oj = "'" + String.valueOf(oj).replaceAll("'", "''") + "'";
                } else if (oj instanceof Geometry) {
                    String wkt = GeoTranslator.jts2Wkt((Geometry) oj, 0.00001,
                            5);
                    if (wkt.length() > 4000) {
                        key.insert(0,
                                "DECLARE v_geo SDO_GEOMETRY := sdo_geometry('"
                                        + wkt + "', 8307); BEGIN ");
                        oj = "v_geo";
                        complexGeo = true;
                    } else {
                        oj = ("sdo_geometry('" + wkt + "',8307)");
                    }
                }

                String columnName = StringUtils.toColumnName(f.getName());

                if (columnName.equals(M_U_DATE)) {
                    oj = "'" + StringUtils.getCurrentDay() + "'";
                }

                if (i > 0) {
                    key.append(",");
                    value.append(",");
                }
                key.append(columnName);
                value.append(oj);
            }
        }
        key.append(") ");
        value.append(") ");

        if (complexGeo) {
            value.append(";");
            value.append("END;");
        }
        String sql = key.append(value).toString();
        logger.info("sql == " + sql);
        stmt.addBatch(sql);
        if (row.children() != null) {
            List<List<IRow>> lists = row.children();
            for (List<IRow> list : lists) {
                for (IRow iRow : list) {
                    this.generateInsertSql(stmt, iRow);
                }
            }
        }
    }

    @Override
    public void updateRow2Sql(Statement stmt) throws Exception {

        StringBuilder strSql = new StringBuilder("update ");

        strSql.append(row.tableName());

        strSql.append(" set ");

        Set<Entry<String, Object>> set = row.changedFields().entrySet();

        Iterator<Entry<String, Object>> it = set.iterator();

        boolean complexGeo = false;

        while (it.hasNext()) {
            Entry<String, Object> en = it.next();

            String column = en.getKey();

            Object columnValue = en.getValue();

            Field field = row.getClass().getDeclaredField(column);

            field.setAccessible(true);

            Object value = field.get(row);

            column = StringUtils.toColumnName(column);

            if ((value instanceof String || value == null)
                    && !StringUtils.isStringSame(String.valueOf(value), String.valueOf(columnValue))) {

                strSql.append(column);

                if (columnValue == null) {

                    strSql.append("=null,");
                } else {
                    strSql.append("='");
                    strSql.append(String.valueOf(columnValue));
                    strSql.append("',");
                }
                this.setChanged(true);

            } else if (value instanceof Double) {

                if (Double.parseDouble(String.valueOf(value)) != Double
                        .parseDouble(String.valueOf(columnValue))) {

                    strSql.append(column);
                    strSql.append("=");
                    strSql.append(Double.parseDouble(String.valueOf(columnValue)));
                    strSql.append(",");

                    this.setChanged(true);
                }

            } else if (value instanceof Long) {

                if (Long.parseLong(String.valueOf(value)) != Long
                        .parseLong(String.valueOf(columnValue))) {
                    strSql.append(column);
                    strSql.append("=");
                    strSql.append(Long.parseLong(String.valueOf(columnValue)));
                    strSql.append(",");

                    this.setChanged(true);
                }

            } else if (value instanceof Integer) {

                if (Integer.parseInt(String.valueOf(value)) != Integer
                        .parseInt(String.valueOf(columnValue))) {

                    strSql.append(column);
                    strSql.append("=");
                    strSql.append(Integer.parseInt(String.valueOf(columnValue)));
                    strSql.append(",");

                    this.setChanged(true);
                }

            } else if (value instanceof Geometry) {
                // 先降级转WKT

                String oldWkt = GeoTranslator.jts2Wkt((Geometry) value,
                        0.00001, 5);

                Geometry newGeo = GeoTranslator.geojson2Jts((JSONObject) columnValue, 0.00001, 5);

                String newWkt = GeoTranslator.jts2Wkt(newGeo);

                if (!StringUtils.isStringSame(oldWkt, newWkt)) {
                    if (newWkt.length() > 4000) {

                        String strWkt = "DECLARE v_geo SDO_GEOMETRY := sdo_geometry('" + newWkt.trim() + "',8307); BEGIN ";

                        strSql.insert(0, strWkt);

                        strSql.append("geometry=v_geo,");
                        complexGeo = true;
                    } else {
                        strSql.append("geometry=sdo_geometry('");

                        strSql.append(String.valueOf(newWkt));
                        strSql.append("',8307),");
                    }

                    this.setChanged(true);
                }
            }
        }

        setWhereForTable(row, strSql);

        String sql = strSql.toString();
        sql = sql.replace(", where", " where");
        System.out.println(sql);
        if (complexGeo) {
            sql += "; END;";
        }
        stmt.addBatch(sql);
    }



    @Override
    public void deleteRow2Sql(Statement stmt) throws Exception {
        this.generateDeleteSql(stmt, row);

    }

    /***
     * 自动组装删除语句
     *
     * @param stmt
     * @param row
     * @throws Exception
     */
    private void generateDeleteSql(Statement stmt, IRow row) throws Exception {
        StringBuilder strSql = new StringBuilder("delete ");

        strSql.append(row.tableName());

        setWhereForTable(row, strSql);

        String sql = strSql.toString();

        stmt.addBatch(sql);

        if (row.children() != null) {
            List<List<IRow>> lists = row.children();
            for (List<IRow> list : lists) {
                for (IRow iRow : list) {
                    this.generateDeleteSql(stmt, iRow);
                }
            }
        }
    }

    private String setWhereForTable(IRow row, StringBuilder strWhereSql) throws Exception {

        strWhereSql.append(" where ");

        String primaryKey = String.valueOf(row.getClass().getMethod(M_PRIMARYKEY).invoke(row));

        String primaryKeyValue = String.valueOf(row.getClass().getMethod(M_PRIMARYKEY_VALUE).invoke(row));

        strWhereSql.append(primaryKey);

        if (row instanceof ScPlateresRdLink) {

            strWhereSql.append("=");
            strWhereSql.append(primaryKeyValue);
            strWhereSql.append(" ");

        } else if (row instanceof ScPlateresManoeuvre) {

            String primaryPk = String.valueOf(row.getClass().getMethod(M_PARENTPK).invoke(row));
            String primaryPkValue = String.valueOf(row.getClass().getMethod(M_PARENTPKVALUE).invoke(row));

            strWhereSql.append("=");
            strWhereSql.append(primaryKeyValue);
            strWhereSql.append(" and ");
            strWhereSql.append(primaryPk);
            strWhereSql.append("='");
            strWhereSql.append(primaryPkValue);
            strWhereSql.append("' ");

        } else {
            strWhereSql.append("='");
            strWhereSql.append(primaryKeyValue);
            strWhereSql.append("' ");
        }

        return strWhereSql.toString();
    }

    /**
     * 判断模型基本类型
     *
     * @param f
     * @return
     */
    public static boolean isBasicType(Field f) {
        if (f.getGenericType().toString().equals(M_STRING)) {
            return true;
        }
        if (f.getType() == Integer.TYPE) {
            return true;
        }
        if (f.getType() == Double.TYPE) {
            return true;
        }
        if (f.getGenericType().toString().equals(M_GEOMETRY)) {
            return true;
        }
        if (f.getGenericType().toString().equals(M_DATE)) {
            return true;
        }
        if (f.getType() == Float.TYPE) {
            return true;
        }
        if (f.getType() == Short.TYPE) {
            return true;
        }
        if (f.getType() == Long.TYPE) {
            return true;
        }

        return false;
    }


}
