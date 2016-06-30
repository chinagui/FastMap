package com.navinfo.dataservice.engine.edit.comm.util;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;

/**
 * 编辑操作Utils
 * @author zhangxiaolong
 *
 */
public class EditUtils {
	
	public static void handleResult(Class<?> cls,Result result)
	{
		for(IRow row : result.getAddObjects())
		{
			if(isSameClass(cls,row.getClass()))
			{
				result.setPrimaryPid(row.parentPKValue());
				
				break;
			}
		}
	}
	
	public static boolean isSameClass(Class<?> cls1,Class<?> class1)
	{
		boolean flag =false;
		
		if(cls1.getName().equals(class1.getName()))
		{
			flag = true;
		}
		
		return flag;
	}
	
	public static void main(String[] args) {
		IRow row = new ZoneLink();
		System.out.println(isSameClass(new ZoneLink().getClass(), row.getClass()));
	}
}
