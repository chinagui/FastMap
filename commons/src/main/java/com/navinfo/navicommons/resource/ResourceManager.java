package com.navinfo.navicommons.resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.DBConnectionFactory;
import com.navinfo.navicommons.database.DataSourceType;
import com.navinfo.navicommons.database.GdbVersionManager;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.PersistenceException;
import com.navinfo.navicommons.database.sql.SQLQuery;
import com.navinfo.navicommons.utils.DateUtilsEx;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-11-15
 */
public class ResourceManager {
    private static ResourceManager instance;
    private Logger log = Logger.getLogger(ResourceManager.class);

    public ResourceLock takeResourceLock(ResourceCategory category, String version) throws ResourceLockException {
        return takeResourceLock(category, "anonym", version);
    }

    public ResourceLock takeResourceLock(String parentId, ResourceCategory category, String version) throws ResourceLockException {
        return takeResourceLock(parentId, category, "anonym", version);
    }


    public ResourceLock takeResourceLock(ResourceCategory category) throws ResourceLockException {
        return takeResourceLock(category, "anonym", GdbVersionManager.CURRENT_VERSION);
    }

    public ResourceLock takeResourceLock(String parentId, ResourceCategory category) throws ResourceLockException {
        return takeResourceLock(parentId, category, "anonym", GdbVersionManager.CURRENT_VERSION);
    }

    public ResourceLock takeResourceLock(String parentId, ResourceCategory category, String clientId, String version) throws ResourceLockException {
        Connection con = null;
        ResourceLock resourceLock = null;
        try {
            con = getConnection();
            String sql = "select * from RESOURCE_POOL where CATEGORY = ? and STATUS = ? " +
                    "and PARENT_RESOURCE_ID =?";
            if (StringUtils.isNotBlank(version)) {
                sql += " and VER_NO = ?";
            }
            sql += " and ROWNUM = 1 for update nowait";
            SQLQuery sqlQuery = new SQLQuery(con);
            List<ResourcePool> pools = null;
            log.debug(sql);
            if (StringUtils.isNotBlank(version)) {
                pools = sqlQuery.query(ResourcePool.class, sql, category.name(),
                        ResourceStatus.free.getCode(), parentId, version);
            } else
                pools = sqlQuery.query(ResourcePool.class, sql, category.name(),
                        ResourceStatus.free.getCode(), parentId);
            if (pools != null && pools.size() == 1) {
                resourceLock = lockResource(clientId, con, pools);
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


    protected ResourceLock lockResource(String clientId, Connection con, List<ResourcePool> pools) {
        ResourceLock resourceLock;
        resourceLock = new ResourceLock(pools.get(0));
        resourceLock.getPysicalPool().setClientId(clientId);
        resourceLock.getPysicalPool().setLockTime(DateUtilsEx.getCurTime());
        updateResourcePool(resourceLock.getPysicalPool(), con, ResourceStatus.mainLocked);
        return resourceLock;
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
                    "and STATUS = ? and VER_NO = ? and ROWNUM = 1 for update";
            SQLQuery sqlQuery = new SQLQuery(con);
            List<ResourcePool> pools = sqlQuery.query(ResourcePool.class, sql, category.name(),
                    ResourceStatus.free.getCode(), version);
            log.debug(sql);
            if (pools != null && pools.size() == 1) {
                resourceLock = new ResourceLock(pools.get(0));
                resourceLock.getPysicalPool().setClientId(clientId);
                resourceLock.getPysicalPool().setLockTime(DateUtilsEx.getCurTime());
                updateResourcePool(resourceLock.getPysicalPool(), con, ResourceStatus.mainLocked);
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

    /**
     * 释放锁到空闲状态
     *
     * @param resourceLock 资源锁
     * @throws ResourceLockException
     */
    public void releaseResourceLock(ResourceLock resourceLock) throws ResourceLockException {
        updateResourceLock(resourceLock, ResourceStatus.free);
    }

    /**
     * 释放锁到空闲状态
     *
     * @param resourceId 资源ID
     * @throws ResourceLockException
     */
    public void releaseResourceLock(String resourceId) throws ResourceLockException {
        updateResourceLock(resourceId, ResourceStatus.free);
    }

    /**
     * 释放锁到空闲状态
     *
     * @param resourceId 资源ID
     * @param status     状态
     * @throws ResourceLockException
     */
    public void updateResourceLock(String resourceId, ResourceStatus status) throws ResourceLockException {
        Connection con = null;
        try {
            con = getConnection();
            String sql = "select * from RESOURCE_POOL where RESOURCE_ID = ?";
            SQLQuery sqlQuery = new SQLQuery(con);
            List<ResourcePool> pools = sqlQuery.query(ResourcePool.class, sql, resourceId);
            if (pools != null && pools.size() > 0)
                updateResourcePool(pools.get(0), con, status);
            else
                throw new ResourceLockException("释放锁失败:" + resourceId);
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollBack(con);
            throw new ResourceLockException("释放资源锁时发生异常", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }

    /**
     * 释放锁到指定状态
     *
     * @param resourceLock 资源锁
     * @param status       状态
     * @throws ResourceLockException
     */
    public void updateResourceLock(ResourceLock resourceLock, ResourceStatus status) throws ResourceLockException {
        Connection con = null;
        try {
            con = getConnection();
            updateResourcePool(resourceLock.getPysicalPool(), con, status);
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollBack(con);
            throw new ResourceLockException("释放资源锁时发生异常", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }

    public ResourcePool getResourcePool(String resourceId) {
        Connection con = null;
        try {
            con = getConnection();
            String sql = "select * from RESOURCE_POOL where RESOURCE_ID = ?";
            SQLQuery sqlQuery = new SQLQuery(con);
            List<ResourcePool> pools = sqlQuery.query(ResourcePool.class, sql, resourceId);
            if (pools != null && pools.size() > 0)
                return pools.get(0);
        } catch (PersistenceException e) {
            throw new ResourceLockException("获取父资源时出错", e);
        } finally {
            DBUtils.closeConnection(con);
        }
        return null;
    }

    public void updateResourceVersion(String resourceId, String version) {
        Connection con = null;
        try {
            con = getConnection();
            String sql = "update RESOURCE_POOL set VER_NO=? where RESOURCE_ID = ?";
            SQLQuery sqlQuery = new SQLQuery(con);
            sqlQuery.execute(sql, version, resourceId);
        } catch (PersistenceException e) {
            throw new ResourceLockException("更新资源版本号出错", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }


    public List<ResourcePool> getFreeResourcePool() {
        Connection con = null;
        try {
            con = getConnection();
            String sql = "select * from RESOURCE_POOL where status=?";
            SQLQuery sqlQuery = new SQLQuery(con);
            List<ResourcePool> pools = sqlQuery.query(ResourcePool.class, sql, ResourceStatus.free);

            return pools;
        } catch (PersistenceException e) {
            throw new ResourceLockException("查询所有资源出错", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }


    public ResourceLock getResourceAsResourceLock(String resourceId) {
        ResourcePool basePool = getResourcePool(resourceId);
        return new ResourceLock(basePool);
    }

    public List<ResourcePool> getChildrenResourcePool(String resourceUuid) {
        Connection con = null;
        try {
            con = getConnection();
            String sql = "select * from \n" +
                    "(\n" +
                    "select * from resource_pool t \n" +
                    "connect by prior t.resource_id = t.parent_resource_id\n" +
                    "start with t.resource_id = ?\n" +
                    ")where resource_id <> 1";
            SQLQuery sqlQuery = new SQLQuery(con);
            List<ResourcePool> pools = sqlQuery.query(ResourcePool.class, sql, resourceUuid);
            return pools;
        } catch (PersistenceException e) {
            throw new ResourceLockException("获取父资源时出错", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }

    private void updateResourcePool(ResourcePool pool, Connection con, ResourceStatus status) {
        pool.setStatus(status.getCode());
        SQLQuery sqlQuery = new SQLQuery(con);
        sqlQuery.update(pool, "resourceId");
    }

    private Connection getConnection() throws ResourceLockException {
        try {
            if (this.dataSource == null) {
                return DBConnectionFactory.getInstance().getDataSource(DataSourceType.DMS).getConnection();
            } else
                return this.dataSource.getConnection();

        } catch (SQLException e) {
            throw new ResourceLockException("获取DMS数据库连接时出错", e);
        }
    }


    private DataSource dataSource;

    public ResourceManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    protected ResourceManager() {
    }

    public static synchronized ResourceManager getInstance() {
        if (instance == null)
            instance = new ResourceManager(null);
        return instance;
    }
}
