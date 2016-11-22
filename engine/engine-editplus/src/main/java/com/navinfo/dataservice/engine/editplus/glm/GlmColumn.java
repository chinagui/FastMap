package com.navinfo.dataservice.engine.editplus.glm;

/** 
 * @ClassName: GlmColumn
 * @author xiaoxiaowen4127
 * @date 2016年11月18日
 * @Description: GlmColumn.java
 */
public class GlmColumn {
	protected String name;
	protected String type;
	protected int dataPrecision=0;
	protected int dataScale=0;
	public final static String TYPE_NUMBER="NUMBER";
	public final static String TYPE_VARCHAR="VARCHAR2";
	public final static String TYPE_GEOMETRY="SDO_GEOMETRY";
	public final static String TYPE_RAW="RAW";
	public final static String TYPE_TIMESTAMP="TIMESTAMP";
	public GlmColumn(String name,String type){
		this.name=name;
		this.type=type;
	}
	public GlmColumn(String name,String type,int dataPrecision,int dataScale){
		this.name=name;
		this.type=type;
		this.dataPrecision=dataPrecision;
		this.dataScale=dataScale;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getDataPrecision() {
		return dataPrecision;
	}
	public void setDataPrecision(int dataPrecision) {
		this.dataPrecision = dataPrecision;
	}
	public int getDataScale() {
		return dataScale;
	}
	public void setDataScale(int dataScale) {
		this.dataScale = dataScale;
	}
}
