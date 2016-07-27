/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.sqlhelper;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.xiaolong.invoke.Dao;

/** 
* @ClassName: SqlHelperTest 
* @author Zhang Xiaolong
* @date 2016年7月15日 上午11:00:25 
* @Description: TODO
*/
public class SqlHelperTest extends InitApplication{

	@Before
	@Override
	public void init() {
		initContext();
	}
	
	@Test
	public void testGetByPid()
	{
		SqlParameter obj[] = new SqlParameter[2];
		obj[0] = new SqlParameter("link_pid", 694);
		obj[1] = new SqlParameter("u_record", 2);
		try {
			RwLink link = SqlHelper.executeEntity(RwLink.class, "select * from rw_link where link_pid = ? and u_record != ?", obj);
			try {
				if(link == null)
				{
					System.out.println("pid为"+obj[0].Value+"的对象不存在");
				}
				else
				{
					System.out.println(link.Serialize(ObjLevel.FULL));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
