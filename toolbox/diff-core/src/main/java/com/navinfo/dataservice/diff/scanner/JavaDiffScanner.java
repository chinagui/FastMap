package com.navinfo.dataservice.diff.scanner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.Logger;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.glm.GlmColumn;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.dataservice.diff.job.DiffJob;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-13 下午12:20
 */
public class JavaDiffScanner implements DiffScanner 
{
	protected static  Logger log = Logger
		.getLogger(DiffJob.class);
    protected OracleSchema diffServer;
    protected QueryRunner runner;
    

    public JavaDiffScanner(OracleSchema diffServer){
        this.diffServer = diffServer;
        runner = new QueryRunner();
    }

    @Override
    public int scan(GlmTable table,String leftTableFullName,String rightTableFullName)throws DiffException{
    	Connection conn = null;
    	try{
        	conn = diffServer.getPoolDataSource().getConnection();
        	int ca = scanLeftAddData(conn,table,leftTableFullName,rightTableFullName);
        	int cd = scanRightAddData(conn,table,leftTableFullName,rightTableFullName);
        	int cu = scanUpdateData(conn,table,leftTableFullName,rightTableFullName);
        	conn.commit();
        	return ca+cd+cu;
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    		DbUtils.rollbackAndCloseQuietly(conn);
    		throw new DiffException(e.getMessage(),e);
    	}finally{
    		DbUtils.closeQuietly(conn);
    	}
    }
    
    //数据源在差分库
    //数据源在别的schema
    //数据源在dblink中
    /**
     * 扫描左表中有右表中没有的数据
     * @param leftTable  左表
     * @param rightTable 右表
     */
    public int scanLeftAddData(Connection conn,GlmTable table,String leftTableFullName,String rightTableFullName)
    		throws DiffException
    {
        try
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("INSERT INTO LOG_DETAIL(ROW_ID,OP_ID, TB_NM, OP_TP, TB_ROW_ID,OB_NM,OB_PID)\n SELECT S.S_GUID,S.S_GUID,'");
        	sb.append(table.getName());
        	sb.append("',1,ROW_ID,'");
        	sb.append(table.getObjName());
        	sb.append("',");
        	sb.append(StringUtils.isEmpty(table.getObjPidCol())?"0":table.getObjPidCol());
        	sb.append(" FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L,(SELECT SYS_GUID() S_GUID FROM DUAL) S WHERE NOT EXISTS \n (SELECT 1 FROM ");
        	sb.append(rightTableFullName);
        	sb.append(" R WHERE L.ROW_ID=R.ROW_ID)");
        	return runner.update(conn,sb.toString());
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("扫描左表有右表没有的数据时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }

    /**
     * 扫描左表中没有右表中有的数据
     *
     * @param leftTable  左表
     * @param rightTable 右表
     */
    public int scanRightAddData(Connection conn,GlmTable table,String leftTableFullName,String rightTableFullName)
    		throws DiffException
    {
        try
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("INSERT INTO LOG_DETAIL(ROW_ID,OP_ID, TB_NM, OP_TP, TB_ROW_ID,OB_NM,OB_PID)\n SELECT S.S_GUID,S.S_GUID,'");
        	sb.append(table.getName());
        	sb.append("',2,ROW_ID,'");
        	sb.append(table.getObjName());
        	sb.append("',");
        	sb.append(StringUtils.isEmpty(table.getObjPidCol())?"0":table.getObjPidCol());
        	sb.append(" FROM ");
        	sb.append(rightTableFullName);
        	sb.append(" R,(SELECT SYS_GUID() S_GUID FROM DUAL) S WHERE NOT EXISTS \n (SELECT 1 FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L WHERE L.ROW_ID=R.ROW_ID)");
        	return runner.update(conn,sb.toString());
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("扫描左表没有右表有的数据时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }

    /**
     * 扫描左右两表都有但是不相同的数据
     *
     * @param leftTable  左表
     * @param rightTable 右表
     */
    public int scanUpdateData(Connection conn,GlmTable table,String leftTableFullName,String rightTableFullName)
    throws DiffException
    {
        try
        {
        	List<String> pkConditions = new ArrayList<String>();
        	List<String> conditions = new ArrayList<String>();
        	for(GlmColumn col:table.getColumns()){
        		if(col.isPk()){
        			pkConditions.add(getEqualsString(col));
        		}else{
            		conditions.add(getEqualsString(col));
        		}
        	}
        	StringBuilder sb = new StringBuilder();
        	sb.append("INSERT INTO LOG_DETAIL(ROW_ID,OP_ID, TB_NM, OP_TP, TB_ROW_ID,OB_NM,OB_PID)\n SELECT S.S_GUID,S.S_GUID,'");
        	sb.append(table.getName());
        	sb.append("',3,L.ROW_ID,'");
        	sb.append(table.getObjName());
        	sb.append("',");
        	sb.append((StringUtils.isEmpty(table.getObjPidCol()))?"0":("L."+table.getObjPidCol()));
        	sb.append(" FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L, ");
        	sb.append(rightTableFullName);
        	sb.append(" R,(SELECT SYS_GUID() S_GUID FROM DUAL) S WHERE ");
        	sb.append(StringUtils.join(pkConditions," AND "));
        	sb.append(" AND NOT (");
        	sb.append(StringUtils.join(conditions," AND "));
        	sb.append(")");
        	
        	return runner.update(conn,sb.toString());
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("扫描左表有右表没有的数据时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }
    private String getEqualsString(GlmColumn col){
    	if(GlmColumn.TYPE_CLOB.equals(col.getDataType())
    			||GlmColumn.TYPE_SDO_GEOMETRY.equals(col.getDataType())
    			||col.isBlobColumn()){
    		return "EQUALS.EQUAL(L.\""+col.getName()+"\",R.\""+col.getName()+"\")=1";
    	}else{
    		return "L.\""+col.getName()+"\" = "+"R.\""+col.getName()+"\"";
    	}
    }
}
