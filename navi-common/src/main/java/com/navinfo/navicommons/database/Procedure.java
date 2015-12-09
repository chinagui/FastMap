package com.navinfo.navicommons.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.SqlParameter;

import com.navinfo.navicommons.database.sql.GenericStoredProcedure;

/**
 * User: liuqing
 * Date: 2010-10-27
 * Time: 15:42:49
 */
public class Procedure {

    private List<SqlParameter> sqlParameterList = new ArrayList<SqlParameter>();
    private String procedureName;
    private Map<String, String> initParamValues = new HashMap<String, String>();

    public Procedure(String procedureName) {
        this.procedureName = procedureName;
    }

    public Map<String, String> getInitParamValues() {
        return initParamValues;
    }

    public void declareParameter(GenericStoredProcedure gsp) {
        for (int i = 0; i < sqlParameterList.size(); i++) {
            SqlParameter sqlParameter = sqlParameterList.get(i);
            gsp.declareParameter(sqlParameter);
        }

    }

    public void addSqlParameter(String name, int type, String value) {
        sqlParameterList.add(new SqlParameter(name, type));
        initParamValues.put(name, value);
    }

    public String getProcedureName() {
        return procedureName;
    }
}
