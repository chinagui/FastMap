package com.navinfo.dataservice.diff.scanner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.diff.DiffEngine;
import com.navinfo.dataservice.diff.dataaccess.Column;
import com.navinfo.dataservice.diff.dataaccess.Table;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-13 下午12:20
 */
public class PLSQLDiffScanner implements DiffScanner 
{
	protected static  Logger log = Logger
		.getLogger(DiffEngine.class);
    protected OracleSchema diffServer;
    protected QueryRunner runner;
    

    public PLSQLDiffScanner(OracleSchema diffServer){
        this.diffServer = diffServer;
        runner = new QueryRunner();
    }


    @Override
    public void scan(Table table,String leftTableFullName,String rightTableFullName)throws DiffException{
    	scanLeftAddData(table,leftTableFullName,rightTableFullName);
    	scanRightAddData(table,leftTableFullName,rightTableFullName);
    	scanUpdateData(table,leftTableFullName,rightTableFullName);
    }
    
    //数据源在差分库
    //数据源在别的schema
    //数据源在dblink中
    /**
     * 扫描左表中有右表中没有的数据
     * @param leftTable  左表
     * @param rightTable 右表
     */
    public void scanLeftAddData(Table table,String leftTableFullName,String rightTableFullName)
    		throws DiffException
    {
        try
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("INSERT INTO LOG_DETAIL(OP_ID, OP_DT, TB_NM, OP_TP, ROW_ID)\n SELECT SYS_GUID(),SYSDATE,'");
        	sb.append(table.getTableName());
        	sb.append("',3,ROW_ID FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L WHERE NOT EXISTS \n (SELECT 1 FROM ");
        	sb.append(rightTableFullName);
        	sb.append(" R WHERE L.ROW_ID=R.ROW_ID)");
        	runner.execute(diffServer.getPoolDataSource(), sb.toString());
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
    public void scanRightAddData(Table table,String leftTableFullName,String rightTableFullName)
    		throws DiffException
    {
        try
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("INSERT INTO LOG_DETAIL(OP_ID, OP_DT, TB_NM, OP_TP, ROW_ID)\n SELECT SYS_GUID(),SYSDATE,'");
        	sb.append(table.getTableName());
        	sb.append("',1,ROW_ID FROM ");
        	sb.append(rightTableFullName);
        	sb.append(" R WHERE NOT EXISTS \n (SELECT 1 FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L WHERE L.ROW_ID=R.ROW_ID)");
        	runner.execute(diffServer.getPoolDataSource(), sb.toString());
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
    public void scanUpdateData(Table table,String leftTableFullName,String rightTableFullName)
    throws DiffException
    {
        try
        {
        	List<String> pkConditions = new ArrayList<String>();
        	List<String> conditions = new ArrayList<String>();
        	for(Column col:table.getDiffCols()){
        		if(col.isPk()){
        			pkConditions.add(getEqualsString(col));
        		}else{
            		conditions.add(getEqualsString(col));
        		}
        	}
        	StringBuilder sb = new StringBuilder();
        	sb.append("INSERT INTO LOG_DETAIL(OP_ID, OP_DT, TB_NM, OP_TP, ROW_ID)\n SELECT SYS_GUID(),SYSDATE,'");
        	sb.append(table.getTableName());
        	sb.append("',2,ROW_ID FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L, ");
        	sb.append(rightTableFullName);
        	sb.append(" R WHERE ");
        	sb.append(StringUtils.join(pkConditions," AND "));
        	sb.append(" AND NOT (");
        	sb.append(StringUtils.join(conditions," AND "));
        	sb.append(")");
        	
        	runner.execute(diffServer.getPoolDataSource(), sb.toString());
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("扫描左表有右表没有的数据时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }
    private String getEqualsString(Column col){
    	if(Column.TYPE_CLOB.equals(col.getType())
    			||Column.TYPE_SDO_GEOMETRY.equals(col.getType())){
    		return "pk_vm_utils.equal(L."+col.getName()+",R."+col.getName()+")=1";
    	}else{
    		return "L."+col.getName()+" = "+"R."+col.getName();
    	}
    }

    public void fillLogDetail(Table table,String leftTableFullName,String rightTableFullName)
    throws DiffException
    {
        try
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("INSERT INTO LOG_DETAIL(OP_ID, OP_DT, TB_NM, OP_TP, ROW_ID)\n SELECT SYS_GUID(),SYSDATE,'");
        	sb.append(table.getTableName());
        	sb.append("',3,ROW_ID FROM ");
        	sb.append(leftTableFullName);
        	sb.append(" L WHERE NOT EXISTS \n (SELECT 1 FROM ");
        	sb.append(rightTableFullName);
        	sb.append(" R WHERE L.ROW_ID=R.ROW_ID)");
        	runner.execute(diffServer.getPoolDataSource(), sb.toString());
        } catch (SQLException e){
        	log.error(e.getMessage(),e);
        	throw new DiffException("扫描左表有右表没有的数据时出错：" + e.getMessage() 
				+","+leftTableFullName
				+","+rightTableFullName,e);
        }
    }
    
    
}
