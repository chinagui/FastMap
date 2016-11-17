package com.navinfo.dataservice.engine.check.rules;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.dao.log.LogReader;

/** 
 * @ClassName: CheckRuleFMYW20235
 * @author Gao Pengrong
 * @date 2016-11-15 上午11:30:24 
 * @Description: 检查条件：
  				  停车场poi无照片（IX_POI_PHOTO中无关联记录），但是有营业时间（IX_POI_PARKING.OPEN_TIME）并且有修改营业时间的履历的数据，
  				  则报出log：无照片，但是有营业时间和营业时间履历。
 */

public class CheckRuleFMYW20235 extends baseRule {
	
	public void preCheck(CheckCommand checkCommand){
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof IxPoi){
				IxPoi ixPoi = (IxPoi)obj;
				innerCheck(ixPoi);	
				}
			}
	}
	
	public void innerCheck(IxPoi ixPoi) throws Exception{
		int pid = ixPoi.getPid();
		LogReader logReader = new LogReader(getConn());
		if (logReader.getObjectState(pid,"IX_POI")==2){
			return;
		}
		List<IRow> rows = ixPoi.getPhotos();
		String log;
		if (rows.isEmpty()){
			List<IRow> parkRows = ixPoi.getParkings();
			for(IRow row : parkRows)
			{
				IxPoiParking poiPark = (IxPoiParking) row;
				String openTiime=poiPark.getOpenTiime();
				
				if ("".equals(openTiime)){
					
					if (logReader.isUpdateforObjFeild(pid, "IX_POI", "IX_POI_PARKINGS", "OPEN_TIIME")){
						log = "无照片，但是有营业时间和营业时间履历。";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					}
				}
			}
			
		}
		
	}
}