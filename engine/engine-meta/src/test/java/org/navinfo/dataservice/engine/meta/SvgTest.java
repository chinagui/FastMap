/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.svg.SvgImageSelector;

import net.sf.json.JSONObject;

/** 
* @ClassName: SvgTest 
* @author Zhang Xiaolong
* @date 2016年12月23日 上午9:31:06 
* @Description: TODO
*/
public class SvgTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testUpdateSvgData() throws Exception
	{
		String sql = "update SC_VECTOR_MATCH set file_content = ? where file_id = 151110155635000173";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);
			
			InputStream in = new FileInputStream(new File("D:\\Stest.svg"));

			pstmt.setBinaryStream(1, in);

			pstmt.executeUpdate();
			
			conn.commit();

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}
	
	@Test
	public void testGetSvgData()
	{
		SvgImageSelector selector = new SvgImageSelector();
		
		try {
			JSONObject obj = selector.searchByName("S", 5, 0);
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
