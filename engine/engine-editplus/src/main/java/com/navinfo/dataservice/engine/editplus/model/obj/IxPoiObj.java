package com.navinfo.dataservice.engine.editplus.model.obj;

import java.util.List;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiParent;

import net.sf.json.JSONObject;

/** 
 * @ClassName: IxPoi
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoi.java
 */
public class IxPoiObj extends AbstractIxObj {

	public IxPoiObj(BasicRow mainrow) {
		super(mainrow);
	}

//	//子对象
//	protected List<BasicObj> ixPoiName=null;
//	protected List<BasicObj> ixPoiAddress=null;
//	//...
//	//子表
//	protected List<BasicRow> ixPoiContact=null;

	
//	@Override
//	public Map<Class<? extends BasicRow>, List<BasicRow>> childRows() {
//		if(childrows==null){
//			childrows=new HashMap<Class<? extends BasicRow>, List<BasicRow>>();
//			childrows.put(IxPoiContact.class,contacts);
//			//...
//		}
//		return childrows;
//	}
	

//	@Override
//	public Map<Class<? extends BasicObj>, List<BasicObj>> childObjs() {
//		if(childobjs==null){
//			childobjs=new HashMap<Class<? extends BasicObj>, List<BasicObj>>();
//			childobjs.put(IxPoiName.class, names);
//			childobjs.put(IxPoiAddress.class, addresses);
//			//...
//		}
//		return childobjs;
//	}
	
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
		if(rows!=null){
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
		if(rows!=null){
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
	 * 根据关系类型获取父POI的Fid
	 * @author Han Shaoming
	 * @return
	 */
	public String getParentFidByType(){
		List<BasicRow> parentRows = getRowsByName("IX_POI_PARENT");
		List<BasicRow> childrenRows = getRowsByName("IX_POI_CHILDREN");
		if(rows != null){
			for (BasicRow row : rows) {
				IxPoiParent ixPoiParent = (IxPoiParent)row;
				if(ixPoi.getLinkPid()==0 
						&&ixPoi.getXGuide()==0 
						&&ixPoi.getYGuide()==0){
				return null;	
				}else{
					guide.put("linkPid", ixPoi.getLinkPid());
					guide.put("longitude", ixPoi.getXGuide());
					guide.put("latitude", ixPoi.getYGuide());
				}
			}
		}
		return guide.toString();
	}
	
	
	@Override
	public String objType() {
		return ObjectType.IX_POI;
	}

}
