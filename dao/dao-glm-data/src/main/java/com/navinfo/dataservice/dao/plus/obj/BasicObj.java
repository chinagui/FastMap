package com.navinfo.dataservice.dao.plus.obj;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.Set;

import com.navinfo.dataservice.dao.plus.diff.ObjectDiffConfig;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmRef;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.navicommons.database.sql.RunnableSQL;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;


/** 
 * @ClassName: BasicObj:有主键PID即为一个对象
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: BasicObj.java
 */
public abstract class BasicObj {
	protected Logger log = Logger.getLogger(this.getClass());
	protected int lifeCycle=0;
	protected BasicRow mainrow;
	//protected Map<Class<? extends BasicObj>, List<BasicObj>> childobjs;//存储对象下面的子对象，不包含子表
//	protected Map<Class<? extends BasicRow>, List<BasicRow>> childrows;//存储对象下的子表,包括二级、三级子表...
	protected Map<String,List<BasicRow>> subrows=new HashMap<String,List<BasicRow>>();//key:table_name,value:rows
	protected BasicObjGrid grid;
	
	public BasicObj(BasicRow mainrow){
		this.mainrow=mainrow;
	}
	//对象是否被删除
	public boolean isDeleted(){
		if(this.mainrow.getOpType().equals(OperationType.PRE_DELETED)
				||this.mainrow.getOpType().equals(OperationType.DELETE)
				||this.mainrow.getOpType().equals(OperationType.INSERT_DELETE)){
			return true;
		}else{
			return false;
		}
	}
	
	//对象是否被修改:主表、所有子表新增、修改、删除返回true
	public boolean isChanged(){
		if(this.mainrow.isChanged()){
			return true;
		}
		for(Map.Entry<String, List<BasicRow>> entry:this.subrows.entrySet()){
			List<BasicRow> subrowList = entry.getValue();
			if(subrowList==null){continue;}
			for(BasicRow basicRow:subrowList){
				if(basicRow.isChanged()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断某个子表是否有更改
	 * @param tableName
	 * @return
	 */
	public boolean isSubrowChanged(String tableName){
		List<BasicRow> subrowList = subrows.get(tableName);
		if(subrowList!=null){
			for(BasicRow basicRow:subrowList){
				if(basicRow.isChanged()){
					return true;
				}
			}
		}
		return false;
	}
	
	public BasicObjGrid getGrid() throws Exception {
		if(this.grid==null&&objType().equals(ObjType.FEATURE)){
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
	public List<BasicRow> getRowsByName(String tableName){
		List<BasicRow> rows = subrows.get(tableName);
		return rows;
	}
	public BasicRow getSubrow(String tableName,String rowId){
		List<BasicRow> rows=subrows.get(tableName);
		if(rows!=null){
			for(BasicRow row:rows){
				if(row.getRowId().equals(rowId)){
					return row;
				}
			}
		}
		return null;
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
	}
	
	/**
	 * 如果是新增状态，物理删除，刚新增进来的子表记录，如果要删除，不能从子表list中循环获取然后传入
	 * 其他状态打删除标识
	 * 注意：对于新增的子表，又通过此方法进行删除，存在异常可能：当外面使用for循环删除表的list时，会出现java.util.ConcurrentModificationException
	 * @param subrow
	 */
	public void deleteSubrow(BasicRow subrow){
		if(subrow.getOpType().equals(OperationType.INSERT)){
			String tname = subrow.tableName();
			subrows.get(tname).remove(subrow);//当外面使用for循环删除表的list时，会出现java.util.ConcurrentModificationException
		}else{
			subrow.setOpType(OperationType.DELETE);
		}
	}

	/**
	 * 根据表名删除，会把该子表全部记录删除
	 * 如果是新增状态，物理删除，其他状态打删除标识
	 * @param subrow
	 */
	public void deleteSubrows(String tableName){
		List<BasicRow> rows = subrows.get(tableName);
		if(rows!=null){
			for(Iterator<BasicRow> it= rows.iterator();it.hasNext();){
				BasicRow r = it.next();
				if(r.getOpType().equals(OperationType.INSERT)){
					it.remove();
				}else{
					r.setOpType(OperationType.DELETE);
				}
			}
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
		//如果是已删除，则忽略
		if(mainrow.getOpType().equals(OperationType.PRE_DELETED)
				||mainrow.getOpType().equals(OperationType.INSERT_DELETE)){
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
	 * 持久化后理论上应该所有INSERT_DELETE的对象会删除，不会再进入下一操作阶段
	 * 删除的子表在afterPersist后会移除
	 */
	public void afterPersist(){
		this.mainrow.afterPersist();
		for(List<BasicRow> rows:subrows.values()){
			if(rows!=null){
				for(Iterator<BasicRow> it=rows.iterator();it.hasNext();){
					BasicRow row = it.next();
					//理论上INSERT_DELETE,PRE_DELETE两种状态不会出现在子表上
					if(row.getOpType().equals(OperationType.DELETE)
							||row.getOpType().equals(OperationType.INSERT_DELETE)
							||row.getOpType().equals(OperationType.PRE_DELETED)){
						it.remove();
					}else{
						row.afterPersist();
					}
				}
			}
		}
	}
	/**
	 * IX_POI OR AD_LINK FROM ObjectName
	 * @return
	 */
	public abstract String objName();
	/**
	 * FEATURE OR RELATION FROM ObjType
	 * @return
	 */
	public abstract String objType();
	
	public long objPid() {
		return mainrow.getObjPid();
	}
	public OperationType opType(){
		return mainrow.getOpType();
	}
	
	public BasicObj copy(){
		return null;
	}
	
	public String identity(){
		return objName()+objPid();
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
	public List<RunnableSQL> generateSql(boolean physiDelete) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		List<RunnableSQL> sqlList = new ArrayList<RunnableSQL>();
		//mainrow
		if(mainrow.getOpType().equals(OperationType.INSERT_DELETE)||mainrow.getOpType().equals(OperationType.PRE_DELETED)){
			return sqlList;
		}
		log.info(" physiDelete:  "+physiDelete+" mainrow: "+mainrow.getObjPid()+"  mainrow.getOpType():"+mainrow.getOpType());
		RunnableSQL mainsql = mainrow.generateSql(physiDelete);
		if(mainsql!=null){
			sqlList.add(mainsql);
		}
		//subrow
		//先放一级子表sql;再放二级子表sql
		//一级子表
		for(Entry<String, List<BasicRow>> entry:subrows.entrySet()){
			log.info("entry key : "+entry.getKey()+ " entry value: "+entry.getValue());
			//****zl 2017.03.04 修改********
			if(entry.getKey().equals("IX_POI")){
				continue;
			}
			if(entry.getKey().equals("IX_SAMEPOI")){
				continue;
			}
			//*****************************
			for(BasicRow subrow:entry.getValue()){
				GlmTable glmTable = GlmFactory.getInstance().getTableByName(subrow.tableName());
				log.info("glmTable: "+ glmTable);
				GlmRef glmRef = glmTable.getObjRef();
				log.info(glmRef);
				log.info(glmRef.isRefMain());
				
				if(!glmRef.isRefMain()){
					continue;
				}
				RunnableSQL sql = subrow.generateSql(physiDelete);
				if(sql!=null){
					sqlList.add(sql);
				}	
			}
		}
		//二级子表
		for(Entry<String, List<BasicRow>> entry:subrows.entrySet()){
			log.info("二级子表 entry key : "+entry.getKey()+ " entry value: "+entry.getValue());
			//****zl 2017.03.04 修改********
			if(entry.getKey().equals("IX_POI")){
				continue;
			}
			if(entry.getKey().equals("IX_SAMEPOI")){
				continue;
			}
			//*****************************
			for(BasicRow subrow:entry.getValue()){
				GlmTable glmTable = GlmFactory.getInstance().getTableByName(subrow.tableName());
				GlmRef glmRef = glmTable.getObjRef();
				if(glmRef.isRefMain()){
					continue;
				}
				RunnableSQL sql = subrow.generateSql(physiDelete);
				if(sql!=null){
					sqlList.add(sql);
				}	
			}
		}
//		for(Entry<String, List<BasicRow>> entry:subrows.entrySet()){
//			for(BasicRow subrow:entry.getValue()){
//				RunnableSQL sql = subrow.generateSql(physiDelete);
//				if(sql!=null){
//					sqlList.add(sql);
//				}	
//			}
//		}
		return sqlList;
	}
	
	/**
	 * 根据传入的diffConfig差分更新对象属性
	 * 所有表不差分rowid
	 * @param obj：参考的对象
	 * @return：是否有更新
	 * @throws Exception
	 */
	public void diff(BasicObj obj,ObjectDiffConfig diffConfig)throws Exception{
		//todo
		//根据差分配置
		if(this.getClass().getName().equals(obj.getClass().getName())){
			//主表差分
			this.mainrow.diff(obj.mainrow, null);
			//
			Map<String,List<BasicRow>> tarSubrows = obj.getSubrows();
			//构造所有子表名
			Set<String> subtables = new HashSet<String>();
			subtables.addAll(subrows.keySet());
			subtables.addAll(tarSubrows.keySet());
			//差分子表
			for(String tab:subtables){
				List<BasicRow> myRows = subrows.get(tab);
				List<BasicRow> tarRows = tarSubrows.get(tab);
				if(tarRows==null||tarRows.size()==0){//目标库子表无，直接删除
					this.deleteSubrows(tab);
				}else{
					if(myRows==null||myRows.size()==0){//本库子表无，直接新增的
						for(BasicRow r:tarRows){
							r.setOpType(OperationType.INSERT);
							this.insertSubrow(r);
						}
					}else{//子表都有，再差分
						//新增和修改的
						for(BasicRow tr:tarRows){
							if(!myRows.contains(tr)){
								tr.setOpType(OperationType.INSERT);
								this.insertSubrow(tr);
							}
						}
						//删除的
						for(BasicRow mr:myRows){
							if(!tarRows.contains(mr)){
								this.deleteSubrow(mr);
							}
						}
					}
				}
			}
			
			
		}else{
			throw new Exception("不同对象类型，无能差分。");
		}
	}

	public boolean isGeoChanged() {
		if(objType().equals(ObjType.FEATURE)){
			//对象新增删除的算几何修改
			if(mainrow.getOpType().equals(OperationType.INSERT)
					||mainrow.getOpType().equals(OperationType.DELETE)){
				return true;
			}
			//对象修改的，看看有没有geometry字段修改
			if(mainrow.getOpType().equals(OperationType.UPDATE)
					&&mainrow.getOldValues()!=null
					&&mainrow.getOldValues().containsKey("GEOMETRY")){
				return true;
			}
		}
		if(mainrow.getOpType().equals(OperationType.INSERT)){}
		return false;
	}
	public abstract BasicRow createSubRowByTableName(String tableName)throws Exception;
	
	//根据json中的key创建二级对象
	public abstract BasicRow createSubRowByName(String subRowName) throws Exception;
	//根据json中的key创建三级对象
	public abstract BasicRow createSubSubRowByName(String subRowName,long subId) throws Exception;
	//根据json中的key获取对象
	public abstract List<BasicRow> getSubRowByName(String subRowName) throws Exception;
	
	public boolean hisOldValueContains(String tableName){
		List<BasicRow> rows = getRowsByName(tableName);
		for(BasicRow basicRow:rows){
			if(basicRow.isHisChanged()){
				return true;
			}
		}
		return false;
	}
	
	public boolean hisOldValueContains(String tableName,String columnName){
		List<BasicRow> rows = getRowsByName(tableName);
		for(BasicRow basicRow:rows){
			if(basicRow.isHisChanged()){
				if(basicRow.hisOldValueContains(columnName)){
					return true;
				}
			}
		}
		return false;
	}

}
