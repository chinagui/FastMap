package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
* @ClassName: CheckRuleFMYW20219 
* @author: zhangpengpeng 
* @date: 2016年11月12日
* @Desc: 通用深度信息传真个数检查
*		检查条件：非删除（根据履历判断删除）
		检查原则：传真个数（IX_POI_CONTACT.CONTACT_TYPE=11）不能超过3个；
		Log：传真个数超过三个；
*/
public class CheckRuleFMYW20219 extends baseRule{
	public void preCheck(CheckCommand checkCommand){
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{
		for (IRow obj: checkCommand.getGlmList()){
			if (obj instanceof IxPoi){
				IxPoi poi = (IxPoi) obj;
				//需要判断POI的状态不为删除
				LogReader logRead=new LogReader(this.getConn());
				int poiState = logRead.getObjectState(poi.getPid(), "IX_POI");
				//state=2为删除
				if (poiState == 2){
					return ;
				}
				//获取通用深度信息的联系方式
				List<IRow> contacts = poi.getContacts();
				if (contacts.size() > 0){
					//CONTACT_TYPE=11 传真个数
					int countTel = 0;
					for(IRow contact: contacts){
						IxPoiContact ixPoiContact = (IxPoiContact) contact;
						if (ixPoiContact.getContactType() == 11){
							countTel += 1;
						}
					}
					//判断是否超过3个
					if (countTel > 3){
						this.setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(), "传真个数超过三个；");
					}
				}
			}
		}
	}
}
