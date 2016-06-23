package com.navinfo.dataservice.engine.edit.datalock;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.database.QueryRunner;

/*
 * @author MaYunFei
 * 2016年6月16日
 * 描述：engine-editTestGridLockManager.java
 */
public class GridLockManagerTest {
	@Before
	public void setUp() throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		
	}

	@Test
	public void testLock() throws Exception {
		Set<Integer> grids=queryGrid(1500);
		int lockSeq = GridLockManager.getInstance().lock(2, FmEditLock.LOCK_OBJ_POI, grids, FmEditLock.TYPE_BORROW,FmEditLock.DB_TYPE_DAY, 0);
		System.out.println(lockSeq);
		Assert.assertTrue(lockSeq>0);
	}
//	@Test
//	public void testUnLock() throws Exception {
//		int unlockCount = GridLockManager.getInstance().unlock(24, FmEditLock.TYPE_BORROW);
//		Assert.assertTrue(unlockCount==10);
//	}
	private static Set<Integer> queryGrid(int limit) throws Exception{
		String sql = "select grid_id from grid g where rownum<=?";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			ResultSetHandler<Set<Integer>> rsh = new ResultSetHandler<Set<Integer>>(){

				@Override
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					if (rs!=null){
						Set<Integer> grids = new HashSet<Integer>();
						while(rs.next()){
							int gridId = rs.getInt("grid_id");
							grids.add(gridId);
						}
						return grids;
					}
					return null;
				}};
			return queryRunner.query(conn, sql, limit, rsh);
			
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}

