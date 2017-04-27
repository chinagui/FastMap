package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

import com.ctc.wstx.util.DataUtil;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

/**
 * 
 * @ClassName: CollectorPoiImportor
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: CollectorPoiImportor.java
 */
public class CollectorPoiImportor extends AbstractOperation {

	
	protected List<ErrorLog> errLogs = new ArrayList<ErrorLog>();
	protected List<PoiRelation> parentPid = new ArrayList<PoiRelation>();

	public CollectorPoiImportor(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}

	public List<ErrorLog> getErrLogs() {
		return errLogs;
	}

	public List<PoiRelation> getParentPid() {
		return parentPid;
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		// 获取当前做业季
		String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
		CollectorUploadPois uploadPois = ((CollectorPoiImportorCommand)cmd).getPois();
		//处理修改的数据
		Map<String,JSONObject> updatePois = uploadPois.getUpdatePois();
		if(updatePois!=null&&updatePois.size()>0){
			//根据fid查询poi
			//key:fid
			Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,getTabNames(),updatePois.keySet(),true,true);
			
			for(Entry<String, JSONObject> entry:updatePois.entrySet()){
				IxPoiObj poiObj = null;
				if(objs!=null&&objs.keySet().contains(entry.getKey())){
					//处理未修改
					log.info("fid:"+entry.getKey()+"在库中存在，作为修改处理");
					poiObj = (IxPoiObj)objs.get(entry.getKey());
				}else{
					//库中未找到数据，处理为新增
					log.info("fid:"+entry.getKey()+"在库中未找到，作为新增处理");
					poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectName.IX_POI);
				}
				setPoiAttr(poiObj,entry.getValue());
				result.putObj(poiObj);
				//关系处理
				//...
			}
			
			if(objs!=null&&objs.size()>0){
				
			}else{
				log.info("");
			}
		}else{
			log.info("无修改的poi数据需要导入");
		}
		//处理删除的数据
		Map<String,JSONObject> deletePois = uploadPois.getDeletePois();
		if(deletePois!=null&&deletePois.size()>0){
			//根据fid查询poi
			//key:fid
			Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,getTabNames(),deletePois.keySet(),true,true);
			if(objs!=null&&objs.size()>0){
				Set<String> keys = objs.keySet();
				Collection<BasicObj> values = objs.values();
				for(BasicObj obj:values){
					//删除
					obj.deleteObj();
					result.putObj(obj);
					//关系数据处理
					//...
				}
				for(String fid:deletePois.keySet()){
					if(!keys.contains(fid)){
						log.info("删除的poi在库中未找到。fid:"+fid);
						//
						errLogs.add(new ErrorLog(fid,"删除的poi在库中未找到"));
					}
				}
			}else{
				log.info("删除的poi在库中均没找到。pids:"+StringUtils.join(deletePois.keySet(),","));
				//err log
				for(String fid:deletePois.keySet()){
					errLogs.add(new ErrorLog(fid,"删除的poi在库中未找到"));
				}
			}
		}else{
			log.info("无删除的poi数据需要导入");
		}
	}
	
	public void setPoiAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		//to-do
	}
	
	/**
	 * 获取查询所需子表
	 * @author Han Shaoming
	 * @return
	 */
	public Set<String> getTabNames(){
		//添加所需的子表
		Set<String> tabNames = new HashSet<>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_CONTACT");
		tabNames.add("IX_POI_ADDRESS");
		tabNames.add("IX_POI_RESTAURANT");
		tabNames.add("IX_POI_CHILDREN");
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_DETAIL");
		tabNames.add("IX_POI_PHOTO");
		tabNames.add("IX_POI_GASSTATION");
		tabNames.add("IX_POI_PARKING");
		tabNames.add("IX_POI_HOTEL");
		tabNames.add("IX_POI_CHARGINGSTATION");
		tabNames.add("IX_POI_CHARGINGPLOT");
		return tabNames;
	}
	
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "CollectorUpload";
	}
	
	public static void main(String[] args) {
		
	}
	
}
