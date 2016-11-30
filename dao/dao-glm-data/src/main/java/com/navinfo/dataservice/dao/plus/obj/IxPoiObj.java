package com.navinfo.dataservice.dao.plus.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;


/** 
 * @ClassName: IxPoi
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoi.java
 */
public class IxPoiObj extends AbstractIxObj {
	
	protected String parentFid;
	protected List<Map<Long,Object>> childFids;
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
	public List<Map<Long, Object>> getChildFids() {
		return childFids;
	}
	public void setChildFid(List<Map<Long, Object>> childFids) {
		this.childFids = childFids;
	}
	
	
	public IxPoiObj(BasicRow mainrow) {
		super(mainrow);
	}
	public List<IxPoiName> getIxPoiNames(){
		return (List)subrows.get("IX_POI_NAME");
	}
	public IxPoiName createIxPoiName()throws Exception{
		IxPoiName ixPoiName = (IxPoiName)(ObjFactory.getInstance().createRow("IX_POI_NAME", this.objPid()));
		if(subrows.containsKey("IX_POI_NAME")){
			subrows.get("IX_POI_NAME").add(ixPoiName);
		}else{
			List<BasicRow> ixPoiNameList = new ArrayList<BasicRow>();
			ixPoiNameList.add(ixPoiName);
			subrows.put("IX_POI_NAME", ixPoiNameList);
		}
		return ixPoiName;
//		return (IxPoiName)(ObjFactory.getInstance().createRow("IX_POI_NAME", this.objPid()));
	}
	public List<IxPoiAddress> getIxPoiAddresses(){
		return (List)subrows.get("IX_POI_ADDRESS");
	}
	public IxPoiAddress createIxPoiAddress()throws Exception{
		IxPoiAddress ixPoiAddress = (IxPoiAddress)(ObjFactory.getInstance().createRow("IX_POI_ADDRESS", this.objPid()));
		if(subrows.containsKey("IX_POI_ADDRESS")){
			subrows.get("IX_POI_ADDRESS").add(ixPoiAddress);
		}else{
			List<BasicRow> ixPoiAddressList = new ArrayList<BasicRow>();
			ixPoiAddressList.add(ixPoiAddress);
			subrows.put("IX_POI_ADDRESS", ixPoiAddressList);
		}
		return ixPoiAddress;
//		return (IxPoiAddress)(ObjFactory.getInstance().createRow("IX_POI_ADDRESS", this.objPid()));
	}
	public List<IxPoiContact> getIxPoiContacts(){
		return (List)subrows.get("IX_POI_CONTACT");
	}
	public IxPoiContact createIxPoiContact()throws Exception{
		IxPoiContact ixPoiContact = (IxPoiContact)(ObjFactory.getInstance().createRow("IX_POI_CONTACT", this.objPid()));
		if(subrows.containsKey("IX_POI_CONTACT")){
			subrows.get("IX_POI_CONTACT").add(ixPoiContact);
		}else{
			List<BasicRow> ixPoiContactList = new ArrayList<BasicRow>();
			ixPoiContactList.add(ixPoiContact);
			subrows.put("IX_POI_CONTACT", ixPoiContactList);
		}
		return ixPoiContact;
//		return (IxPoiContact)(ObjFactory.getInstance().createRow("IX_POI_CONTACT", this.objPid()));
	}
	public List<IxPoiRestaurant> getIxPoiRestaurants(){
		return (List)subrows.get("IX_POI_RESTAURANT");
	}
	public IxPoiRestaurant createIxPoiRestaurant()throws Exception{
		IxPoiRestaurant ixPoiRestaurant = (IxPoiRestaurant)(ObjFactory.getInstance().createRow("IX_POI_RESTAURANT", this.objPid()));
		if(subrows.containsKey("IX_POI_RESTAURANT")){
			subrows.get("IX_POI_RESTAURANT").add(ixPoiRestaurant);
		}else{
			List<BasicRow> ixPoiRestaurantList = new ArrayList<BasicRow>();
			ixPoiRestaurantList.add(ixPoiRestaurant);
			subrows.put("IX_POI_RESTAURANT", ixPoiRestaurantList);
		}
		return ixPoiRestaurant;
//		return (IxPoiRestaurant)(ObjFactory.getInstance().createRow("IX_POI_RESTAURANT", this.objPid()));
	}
	public List<IxPoiHotel> getIxPoiHotels(){
		return (List)subrows.get("IX_POI_HOTEL");
	}
	public IxPoiHotel createIxPoiHotel()throws Exception{
		IxPoiHotel ixPoiHotel = (IxPoiHotel)(ObjFactory.getInstance().createRow("IX_POI_DETAIL", this.objPid()));
		if(subrows.containsKey("IX_POI_DETAIL")){
			subrows.get("IX_POI_DETAIL").add(ixPoiHotel);
		}else{
			List<BasicRow> ixPoiHotelList = new ArrayList<BasicRow>();
			ixPoiHotelList.add(ixPoiHotel);
			subrows.put("IX_POI_DETAIL", ixPoiHotelList);
		}
		return ixPoiHotel;
//		return (IxPoiHotel)(ObjFactory.getInstance().createRow("IX_POI_HOTEL", this.objPid()));
	}
	public List<IxPoiDetail> getIxPoiDetails(){
		return (List)subrows.get("IX_POI_DETAIL");
	}
	public IxPoiDetail createIxPoiDetail()throws Exception{
		IxPoiDetail ixPoiDetail = (IxPoiDetail)(ObjFactory.getInstance().createRow("IX_POI_DETAIL", this.objPid()));
		if(subrows.containsKey("IX_POI_DETAIL")){
			subrows.get("IX_POI_DETAIL").add(ixPoiDetail);
		}else{
			List<BasicRow> ixPoiDetailList = new ArrayList<BasicRow>();
			ixPoiDetailList.add(ixPoiDetail);
			subrows.put("IX_POI_DETAIL", ixPoiDetailList);
		}
		return ixPoiDetail;
//		return (IxPoiDetail)(ObjFactory.getInstance().createRow("IX_POI_DETAIL", this.objPid()));
	}
	public List<IxPoiChildren> getIxPoiChildrens(){
		return (List)subrows.get("IX_POI_CHILDREN");
	}
	public IxPoiChildren createIxPoiChildren()throws Exception{
		IxPoiChildren ixPoiChildren = (IxPoiChildren)(ObjFactory.getInstance().createRow("IX_POI_CHILDREN", this.objPid()));
		if(subrows.containsKey("IX_POI_CHILDREN")){
			subrows.get("IX_POI_CHILDREN").add(ixPoiChildren);
		}else{
			List<BasicRow> ixPoiChildrenList = new ArrayList<BasicRow>();
			ixPoiChildrenList.add(ixPoiChildren);
			subrows.put("IX_POI_CHILDREN", ixPoiChildrenList);
		}
		return ixPoiChildren;
//		return (IxPoiChildren)(ObjFactory.getInstance().createRow("IX_POI_CHILDREN", this.objPid()));
	}
	public List<IxPoiParent> getIxPoiParents(){
		return (List)subrows.get("IX_POI_PARENT");
	}
	public IxPoiParent createIxPoiParent()throws Exception{
		IxPoiParent ixPoiParent = (IxPoiParent)(ObjFactory.getInstance().createRow("IX_POI_PARENT", this.objPid()));
		if(subrows.containsKey("IX_POI_PARENT")){
			subrows.get("IX_POI_PARENT").add(ixPoiParent);
		}else{
			List<BasicRow> ixPoiParentList = new ArrayList<BasicRow>();
			ixPoiParentList.add(ixPoiParent);
			subrows.put("IX_POI_PARENT", ixPoiParentList);
		}
		return ixPoiParent;
//		return (IxPoiParent)(ObjFactory.getInstance().createRow("IX_POI_PARENT", this.objPid()));
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
	
	
	@Override
	public String objType() {
		return ObjectType.IX_POI;
	}

}
