/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.invoke;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.InitApplication;

/** 
* @ClassName: TestDao 
* @author Zhang Xiaolong
* @date 2016年7月14日 下午4:30:40 
* @Description: TODO
*/
public class TestDao extends InitApplication{
	
	private Connection conn;
	
	@Override
	@Before
	public void init() {
		initContext();
		try {
			this.conn = DBConnector.getInstance().getConnectionById(42);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetByPid()
	{
		Dao<RdLink> dao = new Dao<RdLink>();
		Object obj[] = new Object[2];
		obj[0] = 564862;
		obj[1] = 2;
		try {
			RdLink link = dao.query(RdLink.class, "select * from rd_link where link_pid = ? and u_record != ?", conn, obj);
			try {
				System.out.println(link.Serialize(ObjLevel.FULL));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
