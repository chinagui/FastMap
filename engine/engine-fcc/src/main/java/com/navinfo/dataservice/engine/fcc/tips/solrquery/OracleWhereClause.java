package com.navinfo.dataservice.engine.fcc.tips.solrquery;

import java.util.List;

public class OracleWhereClause {
	private String sql ;
	private List<Object> values;
	public OracleWhereClause(String sql, List<Object> values) {
		super();
		this.sql = sql;
		this.values = values;
	}
	public String getSql() {
		return sql;
	}
	public List<Object> getValues() {
		return values;
	}
	
	
}
