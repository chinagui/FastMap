package com.navinfo.dataservice.engine.fcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.client.Connection;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.fcc.tips.EdgeMatchTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.PretreatmentTipsOperator;
import com.navinfo.nirobot.common.storage.HbaseOperator;
import com.navinfo.nirobot.core.tipsprocess.BaseTipsProcessor;
import com.navinfo.nirobot.core.tipsprocess.TipsProcessorFactory;

/** 
 * @ClassName: TipsExportTest.java
 * @author y  
 * @date 2016-11-1 上午10:37:59
 * @Description: tips差分测试类
 *  
 */
public class TipsDiffTest extends InitApplication{
	
	
	@Override
	@Before
	public void init() {
		initContext();
	}


	public TipsDiffTest() throws Exception {
	}

    
    @Test
    public void testTipsDiff() throws Exception {

    	 java.sql.Connection  oraConnection=null;
 		try 
 		{
 		
 			oraConnection = DBConnector.getInstance().getTipsIdxConnection();

           /* String rowkey = "021507db652277f4ee4f358ea3aa17b5c1968f";

            String code = "1507";*/
            
            String rowkey = "02150764d15b1b7ebf4e4ab449770639bf4c25";

            String code = "1507";

            
            Connection hbaseConn = HbaseOperator.getConnection();

            BaseTipsProcessor processor = TipsProcessorFactory.getInstance()
                    .createProcessor(code);

            processor.setTipsOracleConn(oraConnection);

            processor.diff(rowkey, hbaseConn);

            System.out.println("差分成功");

        }catch (Exception ex){
			DbUtils.rollbackAndCloseQuietly(oraConnection);
			throw ex;
		}finally {
			DbUtils.commitAndCloseQuietly(oraConnection);
		}

    }

	
	


}
