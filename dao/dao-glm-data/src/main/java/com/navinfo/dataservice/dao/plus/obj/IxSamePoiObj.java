package com.navinfo.dataservice.dao.plus.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;



/** 
 * @ClassName: IxSamePoi
 * @author zl
 * @date 2017年1月9日
 * @Description: IxSamePoi.java
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class IxSamePoiObj extends AbstractIxObj {
	
	protected String sameFid;
	protected List<Map<Long,Object>> childFids;
	protected long adminId=0L;
	public long getAdminId() {
		return adminId;
	}
	public void setAdminId(long adminId) {
		this.adminId = adminId;
	}
	public String getSameFid() {
		return sameFid;
	}
	public void setSameFid(String sameFid) {
		this.sameFid = sameFid;
	}
	public List<Map<Long, Object>> getChildFids() {
		return childFids;
	}
	public void setChildFid(List<Map<Long, Object>> childFids) {
		this.childFids = childFids;
	}
	
	
	public IxSamePoiObj(BasicRow mainrow) {
		super(mainrow);
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 * 创建一个IxSamepoi对象，完成主键赋值，完成objPid赋值，并将其写入到IxPoi的subrows属性中。
	 * 暂时没有维护IxSamepoi对象的外键
	 */
	public IxSamepoiPart createIxSamepoiPart()throws Exception{
		IxSamepoiPart ixSamepoiPart = (IxSamepoiPart)(ObjFactory.getInstance().createRow("IX_SAMEPOI_PART", this.objPid()));
		if(subrows.containsKey("IX_SAMEPOI_PART")){
			subrows.get("IX_SAMEPOI_PART").add(ixSamepoiPart);
		}else{
			List<BasicRow> ixSamepoiPartList = new ArrayList<BasicRow>();
			ixSamepoiPartList.add(ixSamepoiPart);
			subrows.put("IX_SAMEPOI_PART", ixSamepoiPartList);
		}
		return ixSamepoiPart;
	}
	
	
	public List<IxSamepoiPart> getIxSamepoiParts(){
		return (List)subrows.get("IX_SAMEPOI_PART");
	}
	
	@Override
	public String objName() {
		return ObjectName.IX_SAMEPOI;
	}
	@Override
	public String objType() {
		return ObjType.RELATION;
	}
	
	/**
	 * 根据json中的key创建对象
	 * @throws Exception 
	 */
	@Override
	public BasicRow createSubRowByName(String subRowName) throws Exception {
		if("samepoiParts".equals(subRowName)){
			return this.createIxSamepoiPart();
		}else{
			throw new Exception("字段名为:"+subRowName+"的子表未创建");
		}
	}
	
	/**
	 * 根据json中的key创建三级对象
	 */
	@Override
	public BasicRow createSubSubRowByName(String subRowName, long subId) throws Exception {
		// TODO Auto-generated method stub
		
		return null;
	}
	
	/**
	 * 根据json中的key获取对象
	 */
	@Override
	public List<BasicRow> getSubRowByName(String subRowName) throws Exception {
		if("samepoiParts".equals(subRowName)){
			return (List)subrows.get("IX_SAMEPOI_PART");
		}else{
			throw new Exception("字段名为:"+subRowName+"的子表未找到");
		}
	}
	
	

}
