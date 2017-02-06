package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * GLM60282
 * 检查条件：
 * (1)IX_POI表中“STATE(状态)”字段为非1（删除）
 * 检查原则：
 * 1）如果联系方式为普通固话，以下情况报出：
 * a.电话号码不包含“-”；
 * b.包含“-”，但“-”后数字第一位为0或1；
 * c.存在数字（-,0，1，2，3，4，5，6，7，8，9）以外的，报log；
 * d.电话位数必须为12位或者13位，否则报log
 * e.电话区号第1位必须为0，否则报log
 * f.电话区号相同，电话位数不同，报log
 * 3）如果联系方式为移动电话，以下情况报出
 * a.必须以1开头，必须11位数，否则报出
 * b.存在数字（0，1，2，3，4，5，6，7，8，9）以外的，报log；
 * 4）如果电话号码为空，报LOG
 * @author zhangxiaoyi
 */
public class GLM60282 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();			
			List<IxPoiContact> contactList = poiObj.getIxPoiContacts();
			if(contactList==null||contactList.isEmpty()){return;}
			Map<String,Integer> contactMap=new HashMap<String,Integer>();
			for(IxPoiContact contactObj:contactList){
				String contactStr=contactObj.getContact();
				int type = contactObj.getContactType();
				if(contactStr==null||contactStr.isEmpty()){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "电话号码为空");
					return;
				}
				if(type==1){//普通固话
					//a.电话号码不包含“-”；
					if(!contactStr.contains("-")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "普通固话不包含“-”");
						return;
					}
					//b.包含“-”，但“-”后数字第一位为0或1；
					String[] contacts = contactStr.split("-");
					if(contacts[1]!=null&&!contacts[1].isEmpty()
							&&(contacts[1].startsWith("1")||contacts[1].startsWith("0"))){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "普通固话“-”后不能为0或1");
						return;
					}
					//c.存在数字（-,0，1，2，3，4，5，6，7，8，9）以外的，报log；
					Pattern p = Pattern.compile("^[0-9]+$");
					if(!p.matcher(contacts[1]).matches()||!p.matcher(contacts[0]).matches()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "电话存在非法字符");
						return;
					}
					//d.电话位数必须为12位或者13位，否则报log
					if(contactStr.length()!=12&&contactStr.length()!=13){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "普通电话长度不足位错误");
						return;
					}					
					//e.电话区号第1位必须为0，否则报log
					if(!contacts[0].substring(0, 1).equals("0")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "普通电话区号第1位不为0错误");
						return;
					}
					//f.电话区号相同，电话位数不同，报log
					if(contactMap.containsKey(contacts[0])&&contactMap.get(contacts[0])!=contactStr.length()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "电话位数错误");
						return;
					}
					contactMap.put(contacts[0], contactStr.length());
				}else if(type==2){//移动电话
					//a.必须以1开头，必须11位数，否则报出
					if(!contactStr.startsWith("1")||contactStr.length()!=11){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "移动电话必须以“1”开头");
						return;
					}
					//b.存在数字（0，1，2，3，4，5，6，7，8，9）以外的，报log；
					Pattern p = Pattern.compile("^[0-9]+$");
					if(!p.matcher(contactStr).matches()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "电话存在非法字符");
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {}

}
