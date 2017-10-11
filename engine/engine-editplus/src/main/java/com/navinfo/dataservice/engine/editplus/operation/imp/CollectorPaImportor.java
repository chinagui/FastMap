package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPointaddressSelector;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

/**
 * 
 * @ClassName: CollectorPaImportor
 * @author zl
 * @date 2017年10月09日
 * @Description: CollectorPaImportor.java
 */
public class CollectorPaImportor extends AbstractOperation {

	// 获取当前做业季
	String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
	String langCode = "CHI";//FIXME:为什么这里是CHI。CHT呢？默认返回简体中文; 港澳的后续在增加逻辑吧。
	Map<String,String> attrTableMap;
	Set<String> tabNames;
	
	protected int successNum = 0;
	protected List<ErrorLog> errLogs = new ArrayList<ErrorLog>();
//	protected CollectorUploadPoiSpRelation sps = new CollectorUploadPoiSpRelation();
//	protected CollectorUploadPoiPcRelation pcs = new CollectorUploadPoiPcRelation();
	protected Set<Long> freshVerPas = new HashSet<Long>();
	
	//****zl 2017.06.06 ***
	protected Map<Long,String> allPas = new HashMap<Long,String>();
	public Map<Long, String> getAllPas() {
		return allPas;
	}


	protected Set<Long> noChangedPas = new HashSet<Long>();
	//父子关系暂时不处理

	public CollectorPaImportor(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}
	
	public void setLangCode(String langCode){
		this.langCode=langCode;
	}

	public int getSuccessNum(){
		return this.successNum;
	}
	
	public List<ErrorLog> getErrLogs() {
		return errLogs;
	}

	
	public Set<Long> getFreshVerPas(){
		return freshVerPas;
	}
	
	
	public Set<Long> getNoChangedPas(){
		return noChangedPas;
	}
	
	/**
	 * 初始化所需子表、属性和子表名的映射关系
	 */
	public void init(){
		//添加所需的子表
		tabNames = new HashSet<String>();
		
		//属性和表名映射
		attrTableMap = new HashMap<String,String>();
		
	}

	@Override
	public String getName() {
		return "CollectorPaImportor";
	}
	
	public void operate(AbstractCommand cmd) throws Exception {
		CollectorUploadPas uploadPas = ((CollectorPaImportorCommand)cmd).getPas();
		
//		init();
		//处理修改的数据
		Map<String,JSONObject> updatePas = uploadPas.getUpdatePas();
		if(updatePas!=null&&updatePas.size()>0){
			//根据fid查询pa
			//key:fid
			Map<String,BasicObj> objs = IxPointaddressSelector.selectByFids(conn,null,updatePas.keySet(),true,true);
			
			for(Entry<String, JSONObject> entry:updatePas.entrySet()){
				String fid = entry.getKey();
				try{
					IxPointAddressObj paObj = null;
					if(objs!=null&&objs.keySet().contains(fid)){
						//处理未修改
						paObj = (IxPointAddressObj)objs.get(fid);
						if(paObj.opType().equals(OperationType.PRE_DELETED)){
							log.info("fid:"+fid+"在库中已删除");
							errLogs.add(new ErrorLog(fid,3,"pa在库中已删除"));
							continue;
						}else{
							log.info("fid:"+fid+"在库中存在，作为修改处理");
						}
					}else{
						//库中未找到数据，处理为新增
						log.info("fid:"+fid+"在库中未找到，作为新增处理");
						paObj = (IxPointAddressObj) ObjFactory.getInstance().create(ObjectName.IX_POINTADDRESS);
					}
					setPaAttr(paObj,entry.getValue());
					//************
					//计算鲜度验证
					if(paObj.isFreshFlag()){//鲜度验证
						freshVerPas.add(paObj.objPid());
					}
					//所有的pa
					allPas.put(paObj.objPid(), "");
						
					result.putObj(paObj);
					successNum++;
					
				}catch(Exception e){
					errLogs.add(new ErrorLog(fid,0,"未分类错误："+e.getMessage()));
					log.warn("fid（"+fid+"）入库发生错误："+e.getMessage());
					log.warn(e.getMessage(),e);
				}
			}
		}else{
			log.info("无修改的poi数据需要导入");
		}
		//处理删除的数据
		Map<String,JSONObject> deletePas = uploadPas.getDeletePas();
		if(deletePas!=null&&deletePas.size()>0){
			//根据fid查询pa
			//key:fid
			Map<String,BasicObj> objs = IxPointaddressSelector.selectByFids(conn,tabNames,deletePas.keySet(),true,true);
			if(objs!=null&&objs.size()>0){
				Set<String> keys = objs.keySet();
				Collection<BasicObj> values = objs.values();
				for(BasicObj obj:values){
					//删除
					obj.deleteObj();
					result.putObj(obj);
					//allPas.put(obj.objPid(), deletePas.get(((IxPoi)obj.getMainrow()).getPoiNum()).getString("rawFields"));
					successNum++;
				}
				for(String fid:deletePas.keySet()){
					if(!keys.contains(fid)){
						log.info("删除的pa在库中未找到。fid:"+fid);
						//
						errLogs.add(new ErrorLog(fid,0,"删除的poi在库中未找到"));
					}
				}
			}else{
				log.info("删除的pa在库中均没找到。pids:"+StringUtils.join(deletePas.keySet(),","));
				//err log
				for(String fid:deletePas.keySet()){
					errLogs.add(new ErrorLog(fid,0,"删除的pa在库中未找到"));
				}
			}
		}else{
			log.info("无删除的pa数据需要导入");
		}
	}
	
	

	public void setPaAttr(IxPointAddressObj paObj,JSONObject jo)throws Exception{
		//to-do
		//table Ix_PointAddress
		IxPointaddress ixPa = (IxPointaddress)paObj.getMainrow();
		
		//
		
		//geometry
		Geometry geometry = JtsGeometryFactory.read(jo.getString("geometry"));
		ixPa.setGeometry(geometry);
		//guide
		if(JSONUtils.isNull(jo.get("guide"))){
			ixPa.setXGuide(0);
			ixPa.setYGuide(0);
			ixPa.setGuideLinkPid(0);
		}else {
			JSONObject guide = jo.getJSONObject("guide");
			double newXGuide = guide.getDouble("longitude");
			ixPa.setXGuide(DoubleUtil.keepSpecDecimal(newXGuide));
			
			double newYGuide = guide.getDouble("latitude");
			ixPa.setYGuide(DoubleUtil.keepSpecDecimal(newYGuide));
			
			long newLinkPid = guide.getLong("linkPid");
			ixPa.setGuideLinkPid(newLinkPid);
		}
		// meshid非0时原值转出；为0时根据几何计算；
		int meshId = jo.getInt("meshid");
		if (meshId == 0) {
			String[] meshIds = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
			meshId = Integer.parseInt(meshIds[0]);
		}
		ixPa.setMeshId(meshId);
		//
		ixPa.setIdcode(jo.getString("fid"));
		
		ixPa.setDprName(jo.getString("dprName"));
		
		ixPa.setDpName(jo.getString("dpName"));
		
		ixPa.setMemoire(jo.getString("memoire"));
		
		//attachments:
		if(!JSONUtils.isNull(jo.get("attachments"))){
			JSONArray attachments = jo.getJSONArray("attachments");
			if(attachments != null && attachments.size() > 0){
				for(Object obj : attachments){
					JSONObject attachmentObj = (JSONObject) obj;
					//attachments数组中type=3(文字）的记录的content字段值原值转出；
					int type = attachmentObj.getInt("type");
					if(type == 3){
						ixPa.setMemo(attachmentObj.getString("content"));
					}
				}
			}
		}
		
		ixPa.setDataVersion(version);
	}
	
	/**
	 * 属性是唯一的对象，并且根据rowId差分
	 * hotel|gasStation|parkings|foodtypes|chargingStation
	 * @param poiObj
	 * @param jo
	 * @param keyName：
	 * @throws Exception
	 */
/*	private void setSubrow(IxPoiObj poiObj,JSONObject jo,String keyName)throws Exception{
		String tableName = attrTableMap.get(keyName);
		//获取原始
		if(JSONUtils.isNull(jo.get(keyName))){//上传中没有子表信息，删除所有原有的记录
			poiObj.deleteSubrows(tableName);
		}else{
			JSONObject subJo = jo.getJSONObject(keyName);
			String rowid = subJo.getString("rowId");
			rowid = rowid.toUpperCase();
			//
			boolean exists = false;
			
			List<BasicRow> rows = poiObj.getRowsByName(tableName);
			if(rows!=null){
				for(BasicRow r:rows){//遍历删除非上传的记录
					if(r.getRowId().equals(rowid)){
						exists = true;
						setSubrowAttr(r,subJo,keyName);
					}else{
						poiObj.deleteSubrow(r);
					}
				}
			}
			//在库中不存在，则新增
			if(!exists){
				BasicRow nr = poiObj.createSubRowByTableName(tableName);
				nr.setRowId(rowid);
				setSubrowAttr(nr,subJo,keyName);
			}
		}
		
	}*/

	/**
	 * 属性是对象的数组，并且数组内的对象根据rowId差分
	 * contacts|chargingPole
	 * @param poiObj
	 * @param jo：raw poi json
	 * @param keyName：contacts|chargingPole
	 * @throws Exception
	 */
	/*private void setSubrows(IxPoiObj poiObj,JSONObject jo,String keyName)throws Exception{
		String tableName = attrTableMap.get(keyName);
		//获取原始
		if(JSONUtils.isNull(jo.get(keyName))){//上传中没有子表信息，删除所有原有的记录
			poiObj.deleteSubrows(tableName);
		}else{
			JSONArray subJos = jo.getJSONArray(keyName);
			if(subJos.size()==0){//上传中没有子表信息，删除所有原有的记录
				poiObj.deleteSubrows(tableName);
			}else{
				//转map
				Map<String,JSONObject> subJoMap = new HashMap<String,JSONObject>();
				for(Object so:subJos){
					JSONObject j = (JSONObject)so;
					subJoMap.put(j.getString("rowId").toUpperCase(), j);
				}
				List<BasicRow> rows = poiObj.getRowsByName(tableName);
				if(rows!=null&&rows.size()>0){
					//开始差分
					//获取已存在的rowId
					Map<String,BasicRow> exists = null;
					Set<BasicRow> notExists = null; 
					for(BasicRow r:rows){
						if(subJoMap.keySet().contains(r.getRowId())){
							if(exists==null){
								exists = new HashMap<String,BasicRow>();
							}
							exists.put(r.getRowId(),r);
						}else{
							if(notExists==null){
								notExists = new HashSet<BasicRow>();
							}
							notExists.add(r);
						}
					}
					//删除rows中存在，exists中不存在的
					if(notExists!=null){
						for(BasicRow c:notExists){
							poiObj.deleteSubrow(c);
						}
					}
					//修改exists中存在
					if(exists!=null){
						for(Entry<String,BasicRow> e:exists.entrySet()){
							setSubrowAttr(e.getValue(),subJoMap.get(e.getKey()),keyName);
							subJoMap.remove(e.getKey());
						}
					}
					//
					//在库中不存在，则新增
					if(subJoMap!=null&&subJoMap.size()>0){
						for(Entry<String,JSONObject> e : subJoMap.entrySet()){//经过修改中删除，剩下的都是要新增入库的
							BasicRow br = poiObj.createSubRowByTableName(tableName);
							br.setRowId(e.getKey());
							setSubrowAttr(br,e.getValue(),keyName);
						}
					}
				}else{
					for(Entry<String,JSONObject> e : subJoMap.entrySet()){//全部新增
						BasicRow br = poiObj.createSubRowByTableName(tableName);
						br.setRowId(e.getKey());
						setSubrowAttr(br,e.getValue(),keyName);
					}
				}
			}
		}
	}*/
	
	/*private void setSubrowAttr(BasicRow row,JSONObject jo,String keyName)throws Exception{
		if("hotel".equals(keyName)){
			setHotelAttr((IxPoiHotel)row,jo);
		}else if("gasStation".equals(keyName)){
			setGasStationAttr((IxPoiGasstation)row,jo);
		}else if("parkings".equals(keyName)){
			setParkingAttr((IxPoiParking)row,jo);
		}else if("foodtypes".equals(keyName)){
			setRestaurantAttr((IxPoiRestaurant)row,jo);
		}else if("chargingStation".equals(keyName)){
			setChargingstationAttr((IxPoiChargingstation)row,jo);
		}else if("contacts".equals(keyName)){
			setContactAttr((IxPoiContact)row,jo);
		}else if("chargingPole".equals(keyName)){
			setChargingPlotAttr((IxPoiChargingplot)row,jo);
		}
	}*/

}
