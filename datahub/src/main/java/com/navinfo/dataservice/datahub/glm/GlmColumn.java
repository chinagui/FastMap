package com.navinfo.dataservice.datahub.glm;

/** 
 * @ClassName: GlmColumn 
 * @author Xiao Xiaowen 
 * @date 2016-1-21 上午10:29:30 
 * @Description: TODO
 */
public class GlmColumn {
	private String name;
	private boolean pk;
	private String dataType;
	//给表找mesh_id用
	private GlmTable refTable;//只记录能够找到MESH_ID的字段就行
	
	public static final String TYPE_TIMESTAMP="TIMESTAMP(6)";
	public static final String TYPE_NUMBER="NUMBER";
	public static final String TYPE_RAW="RAW";
	public static final String TYPE_DATE="DATE";
	public static final String TYPE_UNDEFINED="UNDEFINED";
	public static final String TYPE_BLOB="BLOB";
	public static final String TYPE_VARCHAR2="VARCHAR2";
	public static final String TYPE_CLOB="CLOB";
	public static final String TYPE_SDO_GEOMETRY = "SDO_GEOMETRY";

	
	public GlmColumn(String name){
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isPk() {
		return pk;
	}
	public void setPk(boolean pk) {
		this.pk = pk;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

    public GlmTable getRefTable() {
		return refTable;
	}
	public void setRefTable(GlmTable refTable) {
		this.refTable = refTable;
	}
	
	public boolean isGeometryColumn(){
		return TYPE_SDO_GEOMETRY.equals(dataType);
	}
	public boolean isClobColumn(){
		return TYPE_CLOB.equals(dataType);
	}
	public boolean isVARCHAR2Column() {
		return TYPE_VARCHAR2.equals(dataType);
	}

	public boolean isNUMBERColumn() {
		return TYPE_NUMBER.equals(dataType);
	}

	public boolean isBlobColumn() {
		return TYPE_BLOB.equals(dataType);
	}
	
	public boolean isDateColumn() {
		return TYPE_DATE.equals(dataType);
	}
	public boolean isTimestampColumn() {
		return TYPE_TIMESTAMP.equals(dataType);
	}
    public boolean isRawColumn()
    {
        return TYPE_RAW.equals(dataType);
    }

	
	@Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GlmColumn table = (GlmColumn) o;
        return !(name != null ? !name.equals(table.name) : table.name != null);

    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
}
