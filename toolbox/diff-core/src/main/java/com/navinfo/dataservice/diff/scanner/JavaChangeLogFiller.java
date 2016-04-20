package com.navinfo.dataservice.diff.scanner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.datahub.glm.GlmColumn;
import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: JavaChangeLogFiller 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午2:22:19 
* @Description: TODO
*/
public class JavaChangeLogFiller implements ChangeLogFiller {

	protected static  Logger log = LoggerRepos
		.getLogger(JavaChangeLogFiller.class);
    protected OracleSchema diffServer;
    protected QueryRunner runner;
    public JavaChangeLogFiller(OracleSchema diffServer){
        this.diffServer = diffServer;
        runner = new QueryRunner();
    }

    public void fill(GlmTable table,String leftTableFullName,String rightTableFullName)
    throws DiffException
    {
    	fillLeftAddLogDetail(table,leftTableFullName,rightTableFullName);
    	fillLeftDeleteLogDetail(table,leftTableFullName,rightTableFullName);
    	fillLeftUpdateLogDetail(table,leftTableFullName,rightTableFullName);
    	
    }

    private void fillLeftAddLogDetail(GlmTable table,String leftTableFullName,String rightTableFullName)throws DiffException{

        try
        {
        	List<String> colNames = new ArrayList<String>();
        	for(GlmColumn col:table.getColumns()){
        		colNames.add("L.\""+col.getName()+"\"");
        	}
        	StringBuilder sb = new StringBuilder();
        	sb.append("SELECT ");
        	sb.append(StringUtils.join(colNames,","));
        	sb.append(" FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L,LOG_DETAIL D WHERE L.ROW_ID=D.TB_ROW_ID AND D.TB_NM = '");
        	sb.append(table.getName());
        	sb.append("' AND D.OP_TP = 1");
        	runner.query(diffServer.getPoolDataSource(),sb.toString(),1000,new FillLeftAddLogDetail(table,diffServer),new Object[0]);
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("填充左表有右表没有的履历字段时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }
    private void fillLeftDeleteLogDetail(GlmTable table,String leftTableFullName,String rightTableFullName)throws DiffException{
    	//暂时不填充
    }
    private void fillLeftUpdateLogDetail(GlmTable table,String leftTableFullName,String rightTableFullName)throws DiffException{

        try
        {
        	List<String> colNames = new ArrayList<String>();
        	for(GlmColumn col:table.getColumns()){
        		colNames.add("L."+col.getName());
        	}
        	List<String> equalCols = new ArrayList<String>();
        	List<String> leftCols = new ArrayList<String>();
        	List<String> rightCols = new ArrayList<String>();
        	int colIndex=0;
        	for(GlmColumn col:table.getColumns()){
        		colIndex++;
        		equalCols.add("EQUALS.EQUAL(L.\""+col.getName()+"\",R.\""+col.getName()+"\") E"+colIndex);
        		leftCols.add("L.\""+col.getName()+"\" L"+colIndex);
        		rightCols.add("R.\""+col.getName()+"\" R"+colIndex);
        	}
        	StringBuilder sb = new StringBuilder();
        	sb.append("SELECT ");
        	sb.append(StringUtils.join(equalCols,","));
        	sb.append(" , ");
        	sb.append(StringUtils.join(leftCols,","));
        	sb.append(" , ");
        	sb.append(StringUtils.join(rightCols,","));
        	sb.append(" FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L, ");
        	sb.append(rightTableFullName);
        	sb.append(" R,LOG_DETAIL D WHERE L.ROW_ID=D.TB_ROW_ID AND R.ROW_ID=D.TB_ROW_ID AND D.TB_NM = '");
        	sb.append(table.getName());
        	sb.append("' AND D.OP_TP = 3");
        	runner.query(diffServer.getPoolDataSource(),sb.toString(),1000,new FillLeftUpdateLogDetail(table,diffServer),new Object[0]);
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("填充左表右表都有但字段不一致的履历字段时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }
}
