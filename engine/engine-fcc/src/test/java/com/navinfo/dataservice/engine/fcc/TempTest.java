package com.navinfo.dataservice.engine.fcc;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.script.TipsIndexCreateScript;

/** 
 * @ClassName: TempTest.java
 * @author y
 * @date 2017-8-28 下午5:00:52
 * @Description: TODO
 *  
 */
public class TempTest extends InitApplication {
	
	private static final Logger log = Logger.getLogger(TipsIndexCreateScript.class);
	
	@Test
	public  void test() {
		
		long t = System.currentTimeMillis();
		
		initContext();
		
		try{
			
		final String tableName = HBaseConstant.tipTab;

		log.debug("start query oracle already exists tips list ");

		final List<String> hasIndexRowkeyList = getExistsRowkey(); // 查询已经有rowkey的数据

		log.debug("oracle already exists count:" + hasIndexRowkeyList.size());
		
		}catch (Exception e) {
			// TODO: handle exception
		}finally{
			
		}
			
		
	}
	
	
	
	
	/**
	 * @Description:查询oracle已经有的数据
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-8-28 下午2:54:32
	 */
	private static List<String> getExistsRowkey() throws Exception {

		java.sql.Connection conn = null;
		PreparedStatement pst = null;
		java.sql.ResultSet rs = null;
		List<String> existIndexRowkeyList = new ArrayList<String>();

		String sql = "SELECT  ID FROM  TIPS_INDEX";

		try {
			conn = DBConnector.getInstance().getTipsIdxConnection();
			pst = conn.prepareStatement(sql);
			rs = pst.executeQuery();
			rs.setFetchSize(10000); 
			while (rs.next()) {
				existIndexRowkeyList.add(rs.getString("id"));
				System.out.println(existIndexRowkeyList.contains("111101102447"));
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("查询oracle已有索引报错：" + e.getMessage());
			throw new Exception("查询oracle已有索引报错：" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pst);
			DbUtils.commitAndCloseQuietly(conn);
		}

		return existIndexRowkeyList;
	}



	@Override
	@Before
	public void init() {
		initContext();
	}

}
