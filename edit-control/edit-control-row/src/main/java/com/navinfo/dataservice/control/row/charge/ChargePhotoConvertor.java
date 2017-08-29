package com.navinfo.dataservice.control.row.charge;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.photo.HBaseController;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ChargePhotoConvertor
 * @author Han Shaoming
 * @date 2017年8月25日 下午3:01:05
 * @Description TODO
 */
public class ChargePhotoConvertor {
protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private JSONArray errorLog = new JSONArray();
	
	public JSONArray getErrorLog() {
		return errorLog;
	}
	
	//省市城市列表
	private Map<Long,BasicObj> objsChild;
	private String photoPath;
	private String dir;
	
	public ChargePhotoConvertor(Map<Long, BasicObj> objsChild, String photoPath, String dir) {
		super();
		this.objsChild = objsChild;
		this.photoPath = photoPath;
		this.dir = dir;
	}
	
	/**
	 * 初始化
	 * @author Han Shaoming
	 * @param poi
	 * @return
	 * @throws Exception 
	 */
	public JSONArray initPoi(IxPoiObj poiObj) throws Exception{
		try {
			//获取充电桩数据
			Map<Long, BasicObj> plotMap = this.getChargePlot(poiObj);
			//过滤数据
			boolean filterPoi = this.filterPoi(poiObj,plotMap);
			if(!filterPoi){return null;}
			//处理通用字段
			JSONArray chargePoi = toJson(poiObj,plotMap);
			
			return chargePoi;
			
		} catch (Exception e) {
			throw new Exception("充电站pid("+poiObj.objPid()+"),"+e.getMessage(),e);
		}
	}
	/**
	 * 增量
	 * @author Han Shaoming
	 * @param objM 
	 * @param poi
	 * @return
	 * @throws Exception 
	 */
	public JSONArray addPoi(IxPoiObj poiObj) throws Exception{
		try {
			//获取充电桩数据
			Map<Long, BasicObj> plotMap = this.getChargePlot(poiObj);
			//过滤数据
			boolean filterPoi = this.filterPoiAdd(poiObj);
			if(!filterPoi){return null;}
			//处理通用字段
			JSONArray chargePoi = toJson(poiObj,plotMap);
			
			return chargePoi;
			
		} catch (Exception e) {
			throw new Exception("充电站pid("+poiObj.objPid()+"),"+e.getMessage(),e);
		}
	}
	
	/**
	 * 通用字段处理
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private JSONArray toJson(IxPoiObj poiObj,Map<Long, BasicObj> plotMap) throws Exception{
		JSONArray chargePoi = new JSONArray();
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		long pid = ixPoi.getPid();
		String kindCode = ixPoi.getKindCode();
		if(kindCode == null || !"230218".equals(kindCode)){return null;}
		//fid
		String fid = "";
		if(StringUtils.isNotEmpty(ixPoi.getPoiNum())){
			fid = ixPoi.getPoiNum();
		}
		//名称
		IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
		String name = "";
		if(ixPoiName != null){
			if(StringUtils.isNotEmpty(ixPoiName.getName())){
				name = ixPoiName.getName();
			}
		}
		//地址
		IxPoiAddress ixPoiAddress = poiObj.getCHAddress();
		String address = "";
		if(ixPoiAddress != null){
			if(StringUtils.isNotEmpty(ixPoiAddress.getFullname())){
				address = ixPoiAddress.getFullname();
			}
		}
		//行政区划
		String adminCode = String.valueOf(poiObj.getAdminId());
		//处理照片路径
		String photoDir = dir+pid+File.separator;
		File file = new File(photoDir);
		file.mkdirs();
		String url = photoPath+pid+File.separator;
		//处理充电站照片数据
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_PHOTO");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				JSONObject jso = new JSONObject();
				IxPoiPhoto ixPoiPhoto = (IxPoiPhoto) row;
				int tag = ixPoiPhoto.getTag();
				String rowkey = ixPoiPhoto.getPid();
				//生成物理照片
				String path = photoDir+rowkey+".jpg";
				handlePhoto(poiObj, rowkey, path);
				
				String url_base = url+rowkey+".jpg";
				
				jso.put("PID",pid);
				jso.put("FID",fid);
				jso.put("adminCode",adminCode);
				jso.put("source_PID",pid);
				jso.put("source_FID",fid);
				jso.put("picture_ID",rowkey);
				jso.put("POI_name",name);
				jso.put("POI_address",address);
				jso.put("tag",tag);
				jso.put("url_base",url_base);
				jso.put("url_pro","");
				jso.put("audit_state",1);
				jso.put("source_flag",1);
				jso.put("state",0);
				jso.put("operator","");
				//以下四个字段需要桩家处理
				JSONArray jsa = new JSONArray();
				jso.put("operatorHistory",jsa);
				jso.put("size",jsa);
				jso.put("init_time","");
				jso.put("audit_time","");
				
				chargePoi.add(jso);
			}
		}
		//处理充电桩照片数据
		if(plotMap.size() != 0){
			for (BasicObj obj : plotMap.values()) {
				IxPoiObj plotObj = (IxPoiObj) obj;
				IxPoi plotPoi = (IxPoi) plotObj.getMainrow();
				long plotPid = plotPoi.getPid();
				String plotFid = plotPoi.getPoiNum();
				//处理照片数据
				List<BasicRow> plotRows = plotObj.getRowsByName("IX_POI_PHOTO");
				if(plotRows != null && plotRows.size() > 0){
					for (BasicRow plotRow : plotRows) {
						JSONObject jso = new JSONObject();
						IxPoiPhoto ixPoiPhoto = (IxPoiPhoto) plotRow;
						int tag = ixPoiPhoto.getTag();
						String rowkey = ixPoiPhoto.getPid();
						//生成物理照片
						String path = photoDir+rowkey+".jpg";
						handlePhoto(poiObj, rowkey, path);
						
						String url_base = url+rowkey+".jpg";
						
						jso.put("PID",pid);
						jso.put("FID",fid);
						jso.put("adminCode",adminCode);
						jso.put("source_PID",plotPid);
						jso.put("source_FID",plotFid);
						jso.put("picture_ID",rowkey);
						jso.put("POI_name",name);
						jso.put("POI_address",address);
						jso.put("tag",tag);
						jso.put("url_base",url_base);
						jso.put("url_pro","");
						jso.put("audit_state",1);
						jso.put("source_flag",1);
						jso.put("state",0);
						jso.put("operator","");
						//以下四个字段需要桩家处理
						JSONArray jsa = new JSONArray();
						jso.put("operatorHistory",jsa);
						jso.put("size",jsa);
						jso.put("init_time","");
						jso.put("audit_time","");
						
						chargePoi.add(jso);
					}
				}
			}
		}
		return chargePoi;
	}
	
	/**
	 * 获取充电桩
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private Map<Long,BasicObj> getChargePlot(IxPoiObj poiObj) throws Exception{
		try {
			//获取子充电桩pid
			List<Long> childPids = new ArrayList<Long>();
			List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHILDREN");
			if(rows!=null && rows.size()>0){
				for(BasicRow row:rows){
					IxPoiChildren children = (IxPoiChildren) row;
					childPids.add(children.getChildPoiPid());
				}
			}
			Map<Long,BasicObj> objs = new HashMap<Long,BasicObj>();
			for (Long childPid : childPids) {
				//pid=0时不转出
				if(childPid == 0){continue;}
				if(objsChild.containsKey(childPid)){
					objs.put(childPid, objsChild.get(childPid));
				}
			}
			return objs;
		} catch (Exception e) {
			log.error("pid:"+poiObj.objPid()+",获取充电桩出错:"+e.getMessage(),e);
			throw new Exception("获取充电桩出错:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 根据条件过滤数据
	 * @author Han Shaoming
	 * @param poiObj
	 * @return 
	 */
	private boolean filterPoi(IxPoiObj poiObj,Map<Long, BasicObj> plotMap){
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		long pid = ixPoi.getPid();
		//当POI的pid为0时，此站或桩不转出（外业作业中的新增POI未经过行编）
		if(pid == 0){return false;}
		//如果站下没有充电桩或站下所有的充电桩均为删除状态，则站及桩均不转出（当IX_POI_CHARGINGSTATION表中的CHARGING_TYPE=2或4时，充电站需要转出）；
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows == null || rows.size() == 0){return false;}
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				int type = ixPoiChargingstation.getChargingType();
				if(type != 2 && type != 4){
					List<BasicRow> childs = poiObj.getRowsByName("IX_POI_CHILDREN");
					if(childs == null || childs.size() == 0 || plotMap.size() == 0){return false;}
				}
			}
		}
		return true;
	}
	
	/**
	 * 根据条件过滤数据(增量原则:
	 * 其父充电站未删除，则只将桩家中对应的充电桩插口记录删除，同时更新父充电站下的详情；
	 * -----若桩删除后父充电站下已没有非删除的桩，按照原则日库中的站也应为删除状态，
	 * 如果站此时仍为非删除状态，则建议桩家库中的站暂时保留，防止外业又在站下新增桩记录时无法正常更新
	 * )
	 * 增量原则,有站没有充电桩的也要转入,在桩家(新增时)过滤,
     * 保证原先有站有桩的数据更新为有站无桩时能够保证桩家库数据也更新
	 * @author Han Shaoming
	 * @param poiObj
	 * @return 
	 */
	private boolean filterPoiAdd(IxPoiObj poiObj){
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		long pid = ixPoi.getPid();
		//当POI的pid为0时，此站或桩不转出（外业作业中的新增POI未经过行编）
		if(pid == 0){return false;}
		//如果站下没有充电桩或站下所有的充电桩均为删除状态，则站及桩均不转出（当IX_POI_CHARGINGSTATION表中的CHARGING_TYPE=2或4时，充电站需要转出）；
//		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
//		if(rows == null || rows.size() == 0){return false;}
		return true;
	}
	
	
	
	
	/**
	 * 生成照片
	 * @author Han Shaoming
	 * @param poiObj
	 * @param pid
	 * @return
	 * @throws Exception 
	 */
	private void handlePhoto(IxPoiObj poiObj,String rowkey,String path) throws Exception{
		IxPoi ixPoi =(IxPoi) poiObj.getMainrow();
		long pid = ixPoi.getPid();
		ByteArrayInputStream bais = null;
		try {
			//获取照片的数据
			HBaseController control = new HBaseController();
			byte[] photo = control.getPhotoByRowkey(rowkey);
			//生成物理照片
			File file = new File(path);
            bais = new ByteArrayInputStream(photo); 
            BufferedImage bi1 = ImageIO.read(bais); 
            ImageIO.write(bi1, "jpg", file);
		} catch (Exception e) {
			log.error("pid:"+pid+",生成物理照片报错,"+e.getMessage(),e);
			errorLog.add("pid:"+pid+",生成物理照片报错");
		} finally {
			if(bais != null){
				bais.close();
			}
		}
	}
	
}
