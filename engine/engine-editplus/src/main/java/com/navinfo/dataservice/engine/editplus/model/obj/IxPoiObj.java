package com.navinfo.dataservice.engine.editplus.model.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

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

	//子对象
	protected List<BasicObj> names=null;
	protected List<BasicObj> addresses=null;
	//...
	//子表
	protected List<BasicRow> contacts=null;

	
	@Override
	public Map<Class<? extends BasicRow>, List<BasicRow>> childRows() {
		if(childrows==null){
			childrows=new HashMap<Class<? extends BasicRow>, List<BasicRow>>();
			childrows.put(IxPoiContact.class,contacts);
			//...
		}
		return childrows;
	}

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



	@Override
	public String objType() {
		return ObjectType.IX_POI;
	}

}
