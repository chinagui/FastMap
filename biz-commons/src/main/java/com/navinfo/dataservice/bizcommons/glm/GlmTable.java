package com.navinfo.dataservice.bizcommons.glm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;


/** 
 * @ClassName: Table 
 * @author Xiao Xiaowen 
 * @date 2016-1-12 下午2:06:42 
 * @Description: TODO
 */
public class GlmTable {
    protected String name;
    protected String featureType;
    protected String objName;
    protected String objPidCol;//三级表为空
    protected boolean editable;
	//主键字段无顺序
	protected Set<GlmColumn> pks;
	//所有字段按column_id排序；
	protected List<GlmColumn> columns;

	public final static String FEATURE_TYPE_ALL="all";
	public final static String FEATURE_TYPE_POI="poi";
	public final static String FEATURE_TYPE_ROAD="road";
	
	public GlmTable(String name){
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFeatureType() {
		return featureType;
	}
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	public String getObjName() {
		return objName;
	}
	public void setObjName(String objName) {
		this.objName = objName;
	}
	public String getObjPidCol() {
		return objPidCol;
	}
	public void setObjPidCol(String objPidCol) {
		this.objPidCol = objPidCol;
	}
	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public Set<GlmColumn> getPks() {
		if(pks==null&&columns!=null){
			synchronized(this){
				if(pks==null){
					pks = new HashSet<GlmColumn>();
					for(GlmColumn col:columns){
						if(col.isPk()){
							pks.add(col);
						}
					}
				}
			}
		}
		return pks;
	}
	public List<GlmColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<GlmColumn> columns) {
		this.columns = columns;
	}
	public GlmColumn getColumnByName(String colName){
		if(columns!=null){
			for(GlmColumn col:columns){
				if(col.getName().equals(colName)){
					return col;
				}
			}
		}
		return null;
	}
	public boolean isPksHasBigColumn(){
		Set<GlmColumn> set = getPks();
		if(set!=null){
			for(GlmColumn col:set){
				if(col.isBlobColumn()
						||col.isClobColumn()
						||col.isGeometryColumn()){
					return true;
				}
			}
		}
		return false;
	}
	
	
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GlmTable table = (GlmTable) o;
        return !(name != null ? !name.equals(table.name) : table.name != null);

    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
}
