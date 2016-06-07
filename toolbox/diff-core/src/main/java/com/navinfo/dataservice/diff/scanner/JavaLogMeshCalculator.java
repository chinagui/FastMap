package com.navinfo.dataservice.diff.scanner;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: JavaLogMeshCalculator 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午2:32:22 
* @Description: 道路第三迭代暂时实现，逻辑不完整，第四迭代转为计算grid号码。
*/
@Deprecated
public class JavaLogMeshCalculator implements LogMeshCalculator {
	protected static  Logger log = LoggerRepos
			.getLogger(JavaLogMeshCalculator.class);
    protected OracleSchema diffServer;
    protected QueryRunner runner;
    public JavaLogMeshCalculator(OracleSchema diffServer){
        this.diffServer = diffServer;
        runner = new QueryRunner();
    }

	@Override
	public void calc(GlmTable table, String leftTableFullName, String rightTableFullName) throws DiffException {

    	fillLeftAddUpdateLogMesh(table,leftTableFullName,rightTableFullName);
    	fillLeftDeleteLogMesh(table,leftTableFullName,rightTableFullName);
	}

    private void fillLeftAddUpdateLogMesh(GlmTable table,String leftTableFullName,String rightTableFullName)
    		throws DiffException{
        try
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("SELECT M.LINK_PID PID, M.MESH_ID,L.ROW_ID FROM RD_LINK M,");
        	sb.append(leftTableFullName);
        	sb.append(" L,LOG_DETAIL D WHERE M.LINK_PID=L.LINK_PID AND L.ROW_ID=D.TB_ROW_ID AND D.TB_NM = '");
        	sb.append(table.getName());
        	sb.append("' AND D.OP_TP in (1,3)");
        	runner.query(diffServer.getPoolDataSource(),sb.toString(),1000,new FillLogMeshDetail(table,diffServer),new Object[0]);
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("填充左表有右表没有的履历字段时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }
    private void fillLeftDeleteLogMesh(GlmTable table,String leftTableFullName,String rightTableFullName)
    		throws DiffException{
        try
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("SELECT M.LINK_PID PID, M.MESH_ID,R.ROW_ID FROM RD_LINK M,");
        	sb.append(rightTableFullName);
        	sb.append(" R,LOG_DETAIL D WHERE M.LINK_PID=R.LINK_PID AND R.ROW_ID=D.TB_ROW_ID AND D.TB_NM = '");
        	sb.append(table.getName());
        	sb.append("' AND D.OP_TP = 2");
        	runner.query(diffServer.getPoolDataSource(),sb.toString(),1000,new FillLogMeshDetail(table,diffServer),new Object[0]);
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("填充左表有右表没有的履历字段时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }

}
