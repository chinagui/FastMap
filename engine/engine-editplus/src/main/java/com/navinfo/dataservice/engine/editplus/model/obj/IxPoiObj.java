package com.navinfo.dataservice.engine.editplus.model.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjSelector;

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

	@Override
	public String objType() {
		return ObjectType.IX_POI;
	}

}
