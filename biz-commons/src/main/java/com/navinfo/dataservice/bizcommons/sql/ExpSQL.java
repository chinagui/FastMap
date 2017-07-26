package com.navinfo.dataservice.bizcommons.sql;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.navinfo.navicommons.database.DataBaseUtils;

/**
 * @author liuqing
 */
public class ExpSQL {


    private String sqlId;


    private String condition;
    private String sqlType;
    private String tableName;
    private String sqlExtendType;

    public String getSqlExtendType() {
		return sqlExtendType;
	}

	public void setSqlExtendType(String sqlExtendType) {
		this.sqlExtendType = sqlExtendType;
	}

	private String sql;
    private List<Integer> argTypes = new ArrayList<Integer>();
    private List<Object> args = new ArrayList<Object>();


    public ExpSQL(String sqlId, String sql) {
        this.sqlId = sqlId;
        setSql(sql);
    }

    public ExpSQL(String sql) {
        setSql(sql);
    }


    public ExpSQL() {
    }

    public String getSqlId() {
        return sqlId;
    }


    public String getSql() {
        return sql;
    }


    public String getRetureTableName() throws Exception {
        return DataBaseUtils.getReturnTable(sql);
    }

    public Integer[] getArgTypes() {
        return argTypes.toArray(new Integer[argTypes.size()]);
    }


    public Object[] getArgs() {
        return args.toArray(new Object[args.size()]);
    }

    public String getArgsString() {
        String str = "无参数";
        if (args != null) {
            str = ArrayUtils.toString(args);
        }
        return str;
    }

    public void setSql(String sql) {
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        this.sql = sql;
    }


    public boolean isDML() {
    	//if(sql.contains("@")) return false;//如果含有dblink，那么是从源上拷贝数据，需要在目标上执行
        return sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("DELETE")
                || sql.toUpperCase().startsWith("UPDATE");
    }

    public boolean isDDL() {
        return sql.toUpperCase().startsWith("CREATE") || sql.toUpperCase().startsWith("DROP")
                || sql.toUpperCase().startsWith("TRUNCATE");
    }
    public boolean isProgramBlock() {
        String upperCaseSql = sql.toUpperCase();
		return upperCaseSql.startsWith("BEGIN") && upperCaseSql.endsWith("END");
    }

    public void addArgTypes(int[] argTypes) {
        for (int i = 0; i < argTypes.length; i++) {
            int argType = argTypes[i];
            this.argTypes.add(argType);
        }

    }

    public void addArgs(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            this.args.add(arg);
        }
    }

    public static void main(String[] args) {
        ExpSQL expSQL = new ExpSQL();
        String sql = "SELECT P.* FROM RD_LANE_TOPOLOGY P,TEMP_RD_LANE_TOPOLOGY T WHERE P.TOPOLOGY_ID=T.PID";
        try {
            System.out.println(DataBaseUtils.getReturnTable(sql));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExpSQL expSQL2 = new ExpSQL("BEGIN  DBMS_STATS.gather_table_stats([schema_name],'TEMP_IX_POI'); END;");
        try {
        	System.out.println(expSQL2.isProgramBlock());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
    
    

}
