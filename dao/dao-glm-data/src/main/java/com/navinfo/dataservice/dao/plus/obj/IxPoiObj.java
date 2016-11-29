package com.navinfo.dataservice.dao.plus.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;


/** 
 * @ClassName: IxPoi
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoi.java
 */
public class IxPoiObj extends AbstractIxObj {
	
	protected String parentFid;
	protected String childFid;
	protected long adminId=0L;
	public long getAdminId() {
		return adminId;
	}
	public void setAdminId(long adminId) {
		this.adminId = adminId;
	}
	public String getParentFid() {
		return parentFid;
	}
	public void setParentFid(String parentFid) {
		this.parentFid = parentFid;
	}
	public String getChildFid() {
		return childFid;
	}
	public void setChildFid(String childFid) {
		this.childFid = childFid;
	}
	
	
	public IxPoiObj(BasicRow mainrow) {
		super(mainrow);
	}
	public List<IxPoiName> getIxPoiNames(){
		return (List)subrows.get("IX_POI_NAME");
	}
	public IxPoiName createIxPoiName()throws Exception{
		return (IxPoiName)(ObjFactory.getInstance().createRow("IX_POI_NAME", this.objPid()));
	}
	public List<IxPoiAddress> getIxPoiAddresses(){
		return (List)subrows.get("IX_POI_ADDRESS");
	}
	public IxPoiAddress createIxPoiAddress()throws Exception{
		return (IxPoiAddress)(ObjFactory.getInstance().createRow("IX_POI_ADDRESS", this.objPid()));
	}
	public List<IxPoiContact> getIxPoiContacts(){
		return (List)subrows.get("IX_POI_CONTACT");
	}
	public IxPoiContact createIxPoiContact()throws Exception{
		return (IxPoiContact)(ObjFactory.getInstance().createRow("IX_POI_CONTACT", this.objPid()));
	}
	public List<IxPoiRestaurant> getIxPoiRestaurants(){
		return (List)subrows.get("IX_POI_RESTAURANT");
	}
	public IxPoiRestaurant createIxPoiRestaurant()throws Exception{
		return (IxPoiRestaurant)(ObjFactory.getInstance().createRow("IX_POI_RESTAURANT", this.objPid()));
	}
	public List<IxPoiHotel> getIxPoiHotels(){
		return (List)subrows.get("IX_POI_HOTEL");
	}
	public IxPoiHotel createIxPoiHotel()throws Exception{
		return (IxPoiHotel)(ObjFactory.getInstance().createRow("IX_POI_HOTEL", this.objPid()));
	}
	public List<IxPoiDetail> getIxPoiDetails(){
		return (List)subrows.get("IX_POI_DETAIL");
	}
	public IxPoiDetail createIxPoiDetail()throws Exception{
		return (IxPoiDetail)(ObjFactory.getInstance().createRow("IX_POI_DETAIL", this.objPid()));
	}
	public List<IxPoiChildren> getIxPoiChildrens(){
		return (List)subrows.get("IX_POI_CHILDREN");
	}
	public IxPoiChildren createIxPoiChildren()throws Exception{
		return (IxPoiChildren)(ObjFactory.getInstance().createRow("IX_POI_CHILDREN", this.objPid()));
	}
	public List<IxPoiParent> getIxPoiParents(){
		return (List)subrows.get("IX_POI_PARENT");
	}
	public IxPoiParent createIxPoiParent()throws Exception{
		return (IxPoiParent)(ObjFactory.getInstance().createRow("IX_POI_PARENT", this.objPid()));
	}
	
	/**
	 * 根据名称分类,名称类型,语言代码获取名称内容
	 * @author Han Shaoming
	 * @param langCode
	 * @param nameClass
	 * @param nameType
	 * @return
	 */
	public IxPoiName getNameByLct(String langCode,int nameClass,int nameType){
		List<BasicRow> rows = getRowsByName("IX_POI_NAME");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				//
				IxPoiName name=(IxPoiName)row;
				if(langCode.equals(name.getLangCode())
						&&name.getNameClass()==nameClass
						&&name.getNameType()==nameType){
					return name;
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据名称组号,语言代码获取地址全称
	 * @author Han Shaoming
	 * @param nameGroupId
	 * @param langCode
	 * @return
	 */
	public IxPoiAddress getFullNameByLg(String langCode,int nameGroupId){
		List<BasicRow> rows = getRowsByName("IX_POI_ADDRESS");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				//
				IxPoiAddress name=(IxPoiAddress)row;
				if(langCode.equals(name.getLangCode())
						&&name.getNameGroupid()==nameGroupId){
					return name;
				}
			}
		}
		return null;
	}

	
	
	/**
	 * 根据语言代码获取楼层
	 * @author Han Shaoming
	 * @return
	 */
	public IxPoiAddress getFloorByLangCode(String langCode){
		List<BasicRow> rows = getRowsByName("IX_POI_ADDRESS");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				IxPoiAddress floor = (IxPoiAddress) row;
				if(langCode.equals(floor.getLangCode())){
					return floor;
				}	
			}
		}
		return null;
	}
	
	/**
	 * 子列表
	 * @author Han Shaoming
	 * @return
	 */
	public List<Map<String,Object>> getChildrens(){
		List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
		List<BasicRow> rows = getRowsByName("IX_POI_CHILDREN");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				Map<String,Object> msg = new HashMap<String, Object>();
				IxPoiChildren children = (IxPoiChildren) row;
				msg.put("type", children.getRelationType());
				msg.put("childPid", children.getChildPoiPid());
				msg.put("childFid", this.getChildFid());
				msg.put("rowId", children.getRowId());
				msgs.add(msg);
			}
		}
		return msgs;
	}
	
	@Override
	public String objType() {
		return ObjectType.IX_POI;
	}

}
