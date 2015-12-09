package com.navinfo.navicommons.resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.DBConnectionFactory;
import com.navinfo.navicommons.database.DataSourceType;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.SQLQuery;

/**
 * 扩展资源管理
 */
class ResourceManagerExt extends ResourceManager {
	
	private static ResourceManagerExt instance;
	private Logger log = Logger.getLogger(ResourceManagerExt.class);

	protected ResourceLock lockResource(String clientId, Connection con,
			List<ResourcePool> pools) {
		ResourceLock resourceLock = new ResourceLock(pools.get(0));
		return resourceLock;
	}

	public static synchronized ResourceManagerExt getInstance() {
		if (instance == null)
			instance = new ResourceManagerExt();
		return instance;
	}
	
	 /**
     * 获取资源锁
     *
     * @param category 类型
     * @return
     * @throws ResourceLockException
     */
    public ResourceLock takeResourceLock(ResourceCategory category, String clientId, String version) throws ResourceLockException {
        Connection con = null;
        ResourceLock resourceLock = null;
        try {
            con = getConnection();
            String sql = "select * from RESOURCE_POOL where CATEGORY = ? " +
                    "and VER_NO = ? and ROWNUM = 1";
            SQLQuery sqlQuery = new SQLQuery(con);
            List<ResourcePool> pools = sqlQuery.query(ResourcePool.class, sql, category.name(),version);
            if (pools != null && pools.size() == 1) {
            	 resourceLock = lockResource(clientId, con, pools);
            }
            log.debug(sql);
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollBack(con);
            throw new ResourceLockException("获取资源锁时发生异常", e);
        } finally {
            DBUtils.closeConnection(con);
        }
        return resourceLock;
    }
    
    private Connection getConnection() throws ResourceLockException {
        try {
            return DBConnectionFactory.getInstance().getDataSource(DataSourceType.DMS).getConnection();
        } catch (SQLException e) {
            throw new ResourceLockException("获取DMS数据库连接时出错", e);
        }
    }

	public ResourceLock takeResourceLock(String resourceId) {
		 Connection con = null;
	        ResourceLock resourceLock = null;
	        try {
	            con = getConnection();
	            String sql = "select * from RESOURCE_POOL where RESOURCE_ID = ? and STATUS = ?  and ROWNUM = 1 for update";
	            SQLQuery sqlQuery = new SQLQuery(con);
	            List<ResourcePool> pools = null;
	            pools = sqlQuery.query(ResourcePool.class, sql, resourceId,
	                        ResourceStatus.free.getCode());
	            log.debug(sql);
	            if (pools != null && pools.size() == 1) {
	                resourceLock = lockResource(pools.get(0));
	            }
	            con.commit();
	        } catch (SQLException e) {
	            DBUtils.rollBack(con);
	            throw new ResourceLockException("获取资源锁时发生异常", e);
	        } finally {
	            DBUtils.closeConnection(con);
	        }
	        return resourceLock;
	}

	private ResourceLock lockResource(ResourcePool pool) {
		return new ResourceLock(pool);
	}
}
