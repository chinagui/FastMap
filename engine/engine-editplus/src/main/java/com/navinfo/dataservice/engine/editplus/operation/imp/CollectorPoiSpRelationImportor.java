package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxSamePoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxSamePoiSelector;

/**
 * 处理逻辑
 * 1. 加载数据：由于关系数据不存在记录的修改，只有记录的删除或者新增，所以不需判断修改的记录是否删除，那么在加载库中数据时只需加载未删除的（r_record<>2）
 * 2. 处理上传原始poi：上传不存在关系的fid，记为删除同一关系，存在的记为修改fid
 * 3. 入库原则：删除关系的库中存在，则删除同一关系对象，修改fid，先判断是否存在，存在判断同一关系的是否一致，一致不修改，不一致，先删除同一关系对象，再新增同一关系对象
 * @ClassName: CollectorPoiSpRelationImportor
 * @author xiaoxiaowen4127
 * @date 2017年5月8日
 * @Description: CollectorPoiSpRelationImportor.java
 */
public class CollectorPoiSpRelationImportor extends AbstractOperation {
	
	Set<String> tabNames;
	
	protected List<ErrorLog> errLogs = new ArrayList<ErrorLog>();

	protected Set<Long> changedPids = new HashSet<Long>();//计算变更了同一关系的poi

	public CollectorPoiSpRelationImportor(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}
	
	public List<ErrorLog> getErrLogs() {
		return errLogs;
	}
	
	public Set<Long> getChangedPids(){
		return changedPids;
	}
	
	public void init(){
		//添加所需的子表
		tabNames = new HashSet<String>();
		tabNames.add(IxSamePoiObj.IX_SAMEPOI_PART);
	}

	@Override
	public String getName() {
		return "CollectorPoiPcRelationImportor";
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		CollectorUploadPoiSpRelation rels = ((CollectorPoiSpRelationImportorCommand)cmd).getRels();

		//统一预先处理
		Map<String,String> updateRels = rels.getUpdateSp();
		Set<String> deleteRels = rels.getDeleteSp();
		//1. 计算所有fids对应pid关系
		Set<String> allFids = new HashSet<String>();
		if(updateRels!=null){
			allFids.addAll(updateRels.keySet());
			allFids.addAll(updateRels.values());
		}
		if(deleteRels!=null){
			allFids.addAll(deleteRels);
		}
		if(allFids.size()==0){
			log.info("无同一关系数据需要导入。");
			return;
		}
		Map<String,Long> fidPidMap = IxPoiSelector.getPidByFidsIncludeDelData(conn,allFids);
		//加载库中数据
		ExistUtils util = new ExistUtils(fidPidMap);
		util.loadData();
		//开始导入
		init();
		//处理修改的数据
		if(updateRels!=null&&updateRels.size()>0){
			for(Entry<String, String> entry:updateRels.entrySet()){
				String fid = entry.getKey();
				try{
					String sameFid = entry.getValue();
					//判断两个fid是否在库中存在
					if(!util.fidExists(fid)){
						log.info("fid:"+fid+"在库中不存在");
						errLogs.add(new ErrorLog(fid,0,"poi在库中不存在"));
						continue;
					}
					if(!util.fidExists(sameFid)){
						log.info("同一poi(fid:"+sameFid+")在库中不存在");
						errLogs.add(new ErrorLog(fid,0,"同一poi(fid:"+sameFid+")在库中不存在"));
						continue;
					}
					//判断fid是否有同一关系
					IxSamePoiObj spObj = util.existsSpObj(fid);
					if(spObj!=null&&(!spObj.containsPoi(fidPidMap.get(sameFid)))){
						//先删除
						if(!spObj.isDeleted()){
							spObj.deleteObj();
						}
						result.putObj(spObj);
						Set<Long> poiPids = spObj.getPoiPids();
						if(poiPids!=null){
							changedPids.addAll(poiPids);
						}
						spObj = null;//重置
					}
					if(spObj==null){//未找到或者被删除了的，作为新增
						spObj = (IxSamePoiObj) ObjFactory.getInstance().create(ObjectName.IX_SAMEPOI);
						IxSamepoiPart spPart = spObj.createIxSamepoiPart();
						spPart.setPoiPid(fidPidMap.get(fid));
						IxSamepoiPart spPart2 = spObj.createIxSamepoiPart();
						spPart2.setPoiPid(fidPidMap.get(sameFid));
						util.addNewDate(spObj,fid,sameFid);
						result.putObj(spObj);
						Set<Long> poiPids = spObj.getPoiPids();
						if(poiPids!=null){
							changedPids.addAll(poiPids);
						}
					}
				}catch(Exception e){
					errLogs.add(new ErrorLog(fid,0,"未分类错误："+e.getMessage()));
					log.warn("fid（"+fid+"）同一关系入库发生错误："+e.getMessage());
					log.warn(e.getMessage(),e);
				}
			}
		}else{
			log.info("无修改的同一关系数据需要导入");
		}
		//处理删除的数据
		if(deleteRels!=null&&deleteRels.size()>0){
			for(String fid:deleteRels){
				//判断fid是否有同一关系
				IxSamePoiObj spObj = util.existsSpObj(fid);
				if(spObj!=null){
					if(!spObj.isDeleted()){
						spObj.deleteObj();
						result.putObj(spObj);
						Set<Long> poiPids = spObj.getPoiPids();
						if(poiPids!=null){
							changedPids.addAll(poiPids);
						}
					}
				}
			}
		}else{
			log.info("无删除的poi同一关系数据需要导入");
		}
	}
	
	class ExistUtils{
		Map<String,Long> fidPidMap;
		Map<String,Long> fidGroupids;
		Map<Long,BasicObj> samePoiObjs;
		ExistUtils(Map<String,Long> fidPidMap){
			this.fidPidMap = fidPidMap;
		}
		
		public void loadData()throws Exception{
			//计算所有group_id：key-fid，value是同一关系的groupID
			fidGroupids = IxSamePoiSelector.getPidByFids(conn, fidPidMap.keySet(), true);//返回值非空
			//根据groupd_id获取samepoi对象
			samePoiObjs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_SAMEPOI, tabNames,false,fidGroupids.values(), true, true);
		}
		
		public void addNewDate(IxSamePoiObj spObj,String fid,String sameFid){
			if(fidGroupids==null){
				fidGroupids = new HashMap<String,Long>();
			}
			fidGroupids.put(fid, spObj.objPid());
			fidGroupids.put(sameFid, spObj.objPid());
			if(samePoiObjs==null){
				samePoiObjs = new HashMap<Long,BasicObj>();
			}
			samePoiObjs.put(spObj.objPid(), spObj);
		}
		
		public boolean fidExists(String fid){
			if(fidPidMap!=null&&fidPidMap.containsKey(fid)){
				return true;
			}
			return false;
		}
		public IxSamePoiObj existsSpObj(String fid){
			if(fidGroupids!=null&&fidGroupids.containsKey(fid)){
				long gid = fidGroupids.get(fid);
				if(samePoiObjs!=null&&samePoiObjs.containsKey(gid)){
					return (IxSamePoiObj)samePoiObjs.get(gid);
					
				}
			}
			return null;
		}
	}
	
	
	
	public static void main(String[] args) {
		Map<String,Long> map = new HashMap<String,Long>();
		System.out.println(map.values());
		long v = map.get("key1");
		System.out.println(v);
		System.out.println(map.get("map"));
	}
	
}
