package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

import net.sf.json.JSONArray;

/**
 * 
 * @ClassName: CollectorPoiImportor
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: CollectorPoiImportor.java
 */
public class CollectorPoiPcRelationImportor extends AbstractOperation {

	
	protected List<ErrorLog> errLogs = new ArrayList<ErrorLog>();
	
	protected Set<Long> changedPids = new HashSet<Long>();

	public CollectorPoiPcRelationImportor(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}
	
	public List<ErrorLog> getErrLogs() {
		return errLogs;
	}

	public Set<Long> getChangedPids(){
		return changedPids;
	}

	@Override
	public String getName() {
		return "CollectorPoiPcRelationImportor";
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		CollectorUploadPoiPcRelation rels = ((CollectorPoiPcRelationImportorCommand)cmd).getRels();
		Map<Long,Set<String>> fRels = rels.getUpdateChildren();
		if(fRels.size()==0){
			log.info("无POI父子关系数据导入。");
			return;
		}
		//获取子的pid
		Set<String> childrenFids = rels.getChildrenFids();
		Map<String,Long> fidPidMap = IxPoiSelector.getPidByFids(conn,childrenFids);
		//替换子fid为pid
		Map<Long,Set<Long>> pRels = new HashMap<Long,Set<Long>>();
		for(Entry<Long,Set<String>> en:fRels.entrySet()){
			Set<Long> pSet = new HashSet<Long>();
			for(String f:en.getValue()){
				if(fidPidMap.get(f)!=null){
					pSet.add(fidPidMap.get(f));
				}else{
					log.info("poi(pid:"+en.getKey()+")的子(fid:"+f+")库中不存在");
					errLogs.add(new ErrorLog("pid"+en.getKey(),"poi(pid:"+en.getKey()+")的子(fid:"+f+")库中不存在"));
					break;
				}
			}
			if(pSet.size()>0){
				pRels.put(en.getKey(), pSet);
			}
		}
		//获取父的obj
		Map<Long, BasicObj> pois = result.getObjsMapByType(ObjectName.IX_POI);
		//开始差分
		/**
		 * 差分逻辑
		 * 1. 父POI库中不存在，报错误
		 * 2. 
		 */
		for(Entry<Long,Set<Long>> en : pRels.entrySet()){

			
			long parentPid = en.getKey();
			Set<Long> cPids = en.getValue();

			IxPoiObj poiObj = (IxPoiObj)pois.get(parentPid);
			if(poiObj==null){
				log.info("poi(pid:"+en.getKey()+"在库中不存在");
				errLogs.add(new ErrorLog("pid"+en.getKey(),"poi在库中不存在"));
				continue;
			}
			//先判断是否存在父子关系
			List<IxPoiParent> ipps = poiObj.getIxPoiParents();
			IxPoiParent ipp = null;
			if(ipps==null||ipps.size()==0){//不存在，则直接新增父表
				ipp = poiObj.createIxPoiParent();
				ipp.setParentPoiPid(parentPid);
				changedPids.add(poiObj.objPid());
			}else{
				ipp = ipps.get(0);
			}
			//差分子表
			List<IxPoiChildren> ipcs = poiObj.getIxPoiChildrens();
			if(ipcs!=null){
				for(IxPoiChildren ipc:ipcs){
					long ipcPid = ipc.getChildPoiPid();
					if(cPids.contains(ipcPid)){
						cPids.remove(ipcPid);
					}else{
						poiObj.deleteSubrow(ipc);
						changedPids.add(poiObj.objPid());//当前poi pid
						changedPids.add(ipcPid);//子 poi  pid
					}
				}
			}
			if(cPids.size()>0){
				for(Long cpid:cPids){
					IxPoiChildren c = poiObj.createIxPoiChildren(ipp.getGroupId());
					c.setChildPoiPid(cpid);
					c.setRelationType(2);
					changedPids.add(poiObj.objPid());//当前poi pid
					changedPids.add(cpid);//子 poi  pid
				}
			}
		}
		
	}
	public static void main(String[] args) {
		IxPoiObj poiObj = (IxPoiObj)null;
		System.out.println(poiObj);
	}
}
