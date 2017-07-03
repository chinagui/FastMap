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
    protected GlmTable objRefTable;
    protected String objRefCol;
    protected boolean editable;
    protected boolean maintable;//是否是对象的主表
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
	/**
	 * 依赖的对象主表，三级表会递归找到
	 * @return
	 */
	public String getObjName() {
//		if(maintable){
//			return name;
//		}else{
//			if(objRefTable==null){
//				return null;
//			}else{
//				return objRefTable.getObjName();
//			}
//		}
		if(maintable){
			return name;
		}
		if(objRefTable==null){
			return null;
		}else{
			if(objRefTable.isMaintable()){
				return objRefTable.getName();
			}else if(objRefTable.getObjRefTable()!=null){
				return objRefTable.getObjRefTable().getName();
			}else{
				return null;
			}
		}
	}

    /**
     * 三级表及以下此属性返回null
     * @return
     */
	public String getObjPidCol() {
		if(maintable||objRefTable.isMaintable()){
			return objRefCol;
		}
		return null;
	}

	public boolean isMaintable() {
		return maintable;
	}
	public void setMaintable(boolean maintable) {
		this.maintable = maintable;
	}
	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public GlmTable getObjRefTable() {
		return objRefTable;
	}
	public void setObjRefTable(GlmTable objRefTable) {
		this.objRefTable = objRefTable;
	}
	public String getObjRefCol() {
		return objRefCol;
	}
	public void setObjRefCol(String objRefCol) {
		this.objRefCol = objRefCol;
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

	public GlmColumn getPk() throws Exception{
		Set<GlmColumn> pks = getPks();
		if(pks==null||pks.size()!=1){
			throw new Exception("使用逻辑主键表没有单一主键。");
		}
		for(GlmColumn pk:pks){
			return pk;
		}
		return null;
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
