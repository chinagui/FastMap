package com.navinfo.dataservice.diff.scanner;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.diff.DiffEngine;
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
    public void scan(GlmTable table,String leftTableFullName,String rightTableFullName)throws DiffException{
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
    public void scanLeftAddData(GlmTable table,String leftTableFullName,String rightTableFullName)
    		throws DiffException
    {
    }

    /**
     * 扫描左表中没有右表中有的数据
     *
     * @param leftTable  左表
     * @param rightTable 右表
     */
    public void scanRightAddData(GlmTable table,String leftTableFullName,String rightTableFullName)
    		throws DiffException
    {
        
    }

    /**
     * 扫描左右两表都有但是不相同的数据
     *
     * @param leftTable  左表
     * @param rightTable 右表
     */
    public void scanUpdateData(GlmTable table,String leftTableFullName,String rightTableFullName)
    throws DiffException
    {
    }

    public void fillLogDetail(GlmTable table,String leftTableFullName,String rightTableFullName)
    throws DiffException
    {
    	fillLeftAddLogDetail(table,leftTableFullName,rightTableFullName);
    }
    
    private void fillLeftAddLogDetail(GlmTable table,String leftTableFullName,String rightTableFullName)throws DiffException{

    	
    }
    
}