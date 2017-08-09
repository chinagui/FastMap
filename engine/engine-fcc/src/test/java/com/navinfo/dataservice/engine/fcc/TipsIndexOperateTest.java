package com.navinfo.dataservice.engine.fcc;
 
import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;

/** 
 * @ClassName: TipsIndexOperateTest.java
 * @author y
 * @date 2017-8-2 上午9:42:01
 * @Description: TODO
 *  
 */
public class TipsIndexOperateTest extends InitApplication{
	
	
	@Override
	@Before
	public void init() {
		initContext();
	}


    
    @Test
    public void testUpdate() throws Exception {

      	Connection conn = null;
    	
    	String rowkey="02120103a629e5246f47f9a6f22d7521b8a5b1";
		
 		try 
 		{ 
    		conn = DBConnector.getInstance().getTipsIdxConnection();
    		
    		TipsIndexOracleOperator r=new TipsIndexOracleOperator(conn);
    		
    		
        	TipsDao dao=r.getById(rowkey);
        	
        	
        	System.out.println(dao.getDeep());
        	
        	System.out.println(dao.getRelate_links());
        	
        	dao.setStage(2);
        	
        	r.updateOne(dao);
        	
        	System.out.println("修改成功");
 		}catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollback(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
    }
	
	

}