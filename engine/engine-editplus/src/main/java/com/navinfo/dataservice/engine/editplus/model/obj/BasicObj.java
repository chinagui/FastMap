package com.navinfo.dataservice.engine.editplus.model.obj;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.engine.editplus.diff.ObjectDiffConfig;
import com.navinfo.dataservice.engine.editplus.glm.GlmTableNotFoundException;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.navicommons.database.sql.RunnableSQL;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;


/** 
 * @ClassName: BasicObj:有主键PID即为一个对象
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: BasicObj.java
 */
public abstract class BasicObj {
	protected int lifeCycle=0;
	protected BasicRow mainrow;
	//protected Map<Class<? extends BasicObj>, List<BasicObj>> childobjs;//存储对象下面的子对象，不包含子表
//	protected Map<Class<? extends BasicRow>, List<BasicRow>> childrows;//存储对象下的子表,包括二级、三级子表...
	protected Map<String,List<BasicRow>> subrows=new HashMap<String,List<BasicRow>>();//key:table_name,value:rows
	protected BasicObjGrid grid;
	
	public BasicObjGrid getGrid() throws Exception {
		if(this.grid==null){
			//生成grid信息
			Geometry geo = (Geometry) mainrow.getAttrByColName("GEOMETRY");
			Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(geo);
			grid = new BasicObjGrid();
			grid.setGridListAfter(grids);
			if(mainrow.getOldValues()!=null&&!mainrow.getOldValues().isEmpty()&&mainrow.getOldValues().containsKey("GEOMETRY")){
				Geometry geoBefore = (Geometry) mainrow.getOldValues().get("GEOMETRY");
				Set<String> gridsBefore = CompGeometryUtil.geo2GridsWithoutBreak(geoBefore);
				grid.setGridListBefore(gridsBefore);
			}
			grid.setGridListBefore(grids);
		}
		return grid;
	}
	public void setGrid(BasicObjGrid grid) {
		this.grid = grid;
	}
	public BasicObj(BasicRow mainrow){
		this.mainrow=mainrow;
	}
	public int getLifeCycle() {
		return lifeCycle;
	}
	public void setLifeCycle(int lifeCycle) {
		this.lifeCycle = lifeCycle;
	}
	public BasicRow getMainrow() {
		return mainrow;
	}
	public Map<String, List<BasicRow>> getSubrows() {
		return subrows;
	}
	/**
	 * 不会维护状态，操作阶段不要使用
	 * @param tableName
	 * @param basicRowList
	 */
	public void setSubrows(String tableName,List<BasicRow> basicRowList) {
		subrows.put(tableName, basicRowList);
	}
	
	/**
	 * 写入一条子表记录前，使用对象的createXXX子表表方法创建记录
	 * @param subrow
	 */
	protected void insertSubrow(BasicRow subrow)throws WrongOperationException{
		//insert某子表的记录时，改子表在对象中一定加载过，List<BasicRow>不会为null，如果为null，报错
		String tname = subrow.tableName();
		if(mainrow.getOpType().equals(OperationType.DELETE)
				||mainrow.getOpType().equals(OperationType.INSERT_DELETE)){
			throw new WrongOperationException("删除的对象不允许写入记录");
		}else if(mainrow.getOpType().equals(OperationType.INSERT)){
			List<BasicRow> rows = subrows.get(tname);
			if(rows==null){
				rows = new ArrayList<BasicRow>();
				rows.add(subrow);
				subrows.put(tname, rows);
			}else{
				rows.add(subrow);
			}
		}else{
			List<BasicRow> rows = subrows.get(tname);
			if(rows==null){
				throw new WrongOperationException("修改的对象但未初始化该子表");
			}else{
				rows.add(subrow);
			}
		}
		List<BasicRow> rows = subrows.get(tname);
		rows.add(subrow);
	}
	
	/**
	 * 如果是新增状态，物理删除，其他状态打删除标识
	 * @param subrow
	 */
	public void deleteSubrow(BasicRow subrow){
		if(subrow.getOpType().equals(OperationType.INSERT)){
			String tname = subrow.tableName();
			subrows.get(tname).remove(subrow);
		}else{
			subrow.setOpType(OperationType.DELETE);
		}
	}
	/**
	 * 注意：对于一次操作中新增再删除的，在流程中控制不进入OperationResult
	 */
	public void deleteObj(){
		//如果是新增后删除，那么主表打上insert_delete状态，子表直接删除
		if(mainrow.getOpType().equals(OperationType.INSERT)){
			mainrow.setOpType(OperationType.INSERT_DELETE);
			subrows.clear();
			return;
		}
		this.mainrow.setOpType(OperationType.DELETE);
		for(List<BasicRow> rows:subrows.values()){
			if(rows!=null){
				for(BasicRow row:rows){
					deleteSubrow(row);
				}
			}
		}
	}
	/**
	 * 持久化后理论上应该所有删除的对象，不会再进入下一操作阶段
	 */
	public void afterPersist(){
		this.mainrow.afterPersist();
		for(List<BasicRow> rows:subrows.values()){
			if(rows!=null){
				for(BasicRow row:rows){
					row.afterPersist();
				}
			}
		}
	}
	
	public abstract String objType();
	
	public long objPid() {
		return mainrow.getObjPid();
	}
	public OperationType opType(){
		return mainrow.getOpType();
	}
	
	public List<BasicRow> selectRowsByName(Connection conn,String tableName) throws GlmTableNotFoundException, SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		List<BasicRow> rows = subrows.get(tableName);
		if(rows==null&&mainrow.getOpType().equals(OperationType.UPDATE)){
			//ObjSelector
			ObjSelector.selectChildren(conn,this,tableName);
		}
		return subrows.get(tableName);
	}

	public List<BasicRow> getRowsByName(String tableName){
		List<BasicRow> rows = subrows.get(tableName);
		return rows;
	}
	
	public BasicObj copy(){
		return null;
	}
	
	public String identity(){
		return objType()+objPid();
	}
	@Override
	public int hashCode(){
		return identity().hashCode();
	}
	/**
	 * 如果pid<=0,不比较
	 */
	@Override
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof BasicObj
				&&objPid()>0&&identity().equals(((BasicObj) anObject).identity())){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 生成这个对象写入库中的sql
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	public List<RunnableSQL> generateSql() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		List<RunnableSQL> sqlList = new ArrayList<RunnableSQL>();
		//mainrow
		if(mainrow.getOpType().equals(OperationType.INSERT_DELETE)){
			return sqlList;
		}
		RunnableSQL mainsql = mainrow.generateSql();
		if(mainsql!=null){
			sqlList.add(mainsql);
		}
		//subrow
		for(Entry<String, List<BasicRow>> entry:subrows.entrySet()){
			for(BasicRow subrow:entry.getValue()){
				RunnableSQL sql = subrow.generateSql();
				if(sql!=null){
					sqlList.add(sql);
				}	
			}
		}
		return sqlList;
	}
	
	/**
	 * 根据传入的diffConfig差分更新对象属性
	 * 主表不差分pid，所有表不差分rowid
	 * @param obj：参考的对象
	 * @return：是否有更新
	 * @throws Exception
	 */
	public void diff(BasicObj obj,ObjectDiffConfig diffConfig)throws Exception{
		//todo
//		boolean isDefer=false;
		//根据差分配置
		if(this.getClass().getName().equals(obj.getClass().getName())){
			this.mainrow.setAttrByCol("col1", obj.mainrow.getAttrByColName("col1"));
		}
//		return isDefer;
	}
	

}
