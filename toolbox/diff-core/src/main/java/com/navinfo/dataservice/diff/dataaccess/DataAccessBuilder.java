package com.navinfo.dataservice.diff.dataaccess;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.diff.config.DiffConfig;
import com.navinfo.dataservice.diff.exception.DiffException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-9 下午1:40
 */
public class DataAccessBuilder
{
	protected Logger log = Logger.getLogger(this.getClass());
    private DiffConfig diffConfig;

    public DataAccessBuilder(DiffConfig diffConfig)
    {
        this.diffConfig = diffConfig;
    }

    /**
     * 需要重写
     * 创建到右数据源访问包装器
     * @return 右数据源访问包装器
     */
    public DataAccess builderRightDataAccess()throws DiffException
    {
        DataAccess dataAccess = null;
        //local,schema,dblink,migrate
        //可以加一些策略判断，如果都是oracle且都是同一台服务，则走crossschema
        //如果不同服务器则走db link
        //如果有一个不是oracle，则将数据拉至oracle服务器
        //如果都不是oracle，则需要一天差分服务器
        OracleSchema rightSchema = null;
        try{
        	rightSchema = 
        	        (OracleSchema)new DbManager().getDbById(diffConfig.getRightDbId());
        }catch(Exception e){
        	log.error("datahub中未获取右库的连接方式出错。"+e.getMessage(),e);
        	throw new DiffException("datahub中未获取右库的连接方式出错。"+e.getMessage(),e);
        }
        dataAccess = new CrossSchemaDataAccess(rightSchema);
        return dataAccess;
    }
    
}
