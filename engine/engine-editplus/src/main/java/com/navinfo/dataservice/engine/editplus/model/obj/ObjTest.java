package com.navinfo.dataservice.engine.editplus.model.obj;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;

/** 
 * @ClassName: ObjTest
 * @author xiaoxiaowen4127
 * @date 2016年11月24日
 * @Description: ObjTest.java
 */
public class ObjTest {
	protected List<BasicRow> rows=new ArrayList<BasicRow>();
	public ObjTest(){
		rows.add(new IxPoiName(0));
		rows.add(new IxPoiName(0));
	}
	
	public List<IxPoiName> getNames(){
		return (List)rows;
	}
	
	public static void main(String[] args) {
		ObjTest t = new ObjTest();
		List<IxPoiName> names = t.getNames();
		for(IxPoiName name:names){
			System.out.println(name.getObjPid());
		}
	}
}
