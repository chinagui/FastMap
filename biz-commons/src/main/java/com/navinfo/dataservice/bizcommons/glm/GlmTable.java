package com.navinfo.dataservice.bizcommons.glm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/** 
 * @ClassName: Table 
 * @author Xiao Xiaowen 
 * @date 2016-1-12 下午2:06:42 
 * @Description: TODO
 */
public class GlmTable {
    protected String name;
    protected int featureType;
    protected boolean editable;
	//主键字段无顺序
	protected Set<GlmColumn> pks;
	//所有字段按column_id排序；
	protected List<GlmColumn> columns;
	
	public GlmTable(String name){
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getFeatureType() {
		return featureType;
	}
	public void setFeatureType(int featureType) {
		this.featureType = featureType;
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
