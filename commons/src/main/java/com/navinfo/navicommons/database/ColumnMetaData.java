package com.navinfo.navicommons.database;

/**
 * 
 * @author liuqing
 * 
 */
public class ColumnMetaData {



	private String tableName;
	private String columnName;
	private String columnTypeName;

	private int columnType;
	private int columnCount;
	private int precision;
	private int scale;

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public String getColumnTypeName() {
		return columnTypeName;
	}

	public void setColumnTypeName(String columnTypeName) {
		this.columnTypeName = columnTypeName;
		//System.out.println(columnTypeName);
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName.toUpperCase().trim();
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getColumnType() {
		return columnType;
	}

	public void setColumnType(int columnType) {
		this.columnType = columnType;
	}

	public boolean isStringColumn() {
		return (columnTypeName.indexOf("CHAR") > -1);
	}

	public boolean isLongColumn() {
		return (columnTypeName.indexOf("NUMBER") > -1 && scale <= 0);
	}

	public boolean isDoubleColumn() {
		return (columnTypeName.indexOf("NUMBER") > -1 && scale > 0);
	}
	
	public boolean isGeometryColumn() {
		return (columnTypeName.indexOf("SDO_GEOMETRY") > -1);
	}
	
	public boolean isXmlColumn() {
		return (columnTypeName.indexOf("XML") > -1);
	}
	
	public boolean isClobColumn() {
		return (columnTypeName.indexOf("CLOB") > -1);
	}
	
	public boolean isBlobColumn() {
		return (columnTypeName.indexOf("BLOB") > -1);
	}
	
	public boolean isDateColumn() {
		return (columnTypeName.indexOf("DATE") > -1);
	}
	public boolean isTimestampColumn() {
		return (columnTypeName.indexOf("TIME") > -1);
	}
    public boolean isRawColumn()
    {
        return (columnTypeName.indexOf("RAW") > -1);
    }
    /*
        sqlite的null类型 
     */
    public boolean isNullColumn()
    {
        return (columnTypeName.indexOf("null") > -1);
    }

}
