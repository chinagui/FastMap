package com.navinfo.navicommons.manager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.DBConnectionFactory;
import com.navinfo.navicommons.database.DataSourceType;
import com.navinfo.navicommons.database.SqlNamedQuery;
import com.navinfo.navicommons.exception.DAOException;

/**
 * User: liuqing
 * Date: 2010-10-29
 * Time: 8:46:48
 */
public class SharedResourceLockManager {
    protected Logger log = Logger.getLogger(getClass());
    private DataSource controlDataSource;

    public SharedResourceLockManager() {
        controlDataSource = DBConnectionFactory.getInstance().getDataSource(DataSourceType.DMS);
    }

    /**
     * 释放公共资源（导出数据共享资源为临时表的使用）
     *
     * @throws Exception
     */
    public void releaseSharedResourceLock(SharedResourceLock sharedResource) {
        log.debug("Release Shared Resource Lock" + sharedResource.getId());
        Connection conn = null;
        QueryRunner run = new QueryRunner();
        try {
            conn = controlDataSource.getConnection();
            String releaseSharedObjectSql = SqlNamedQuery.getInstance().getSql("SharedResourceLockManager.releaseSharedObject");
            run.update(conn, releaseSharedObjectSql, sharedResource.getId());
        }
        catch (Exception sqle) {
            DbUtils.rollbackAndCloseQuietly(conn);
            throw new DAOException("释放共享锁", sqle);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }


    }

    /**
     * 获取当前应用可以使用的共享对象(使用那些临时表)
     * 查询是必须加锁for update ,所以必须尽快释放锁
     *
     * @return 返回的是临时表的后缀
     * @throws Exception
     */
    public SharedResourceLock lockSharedResource(int type) {

        Connection conn = null;
        SharedResourceLock so = null;
        QueryRunner run = new QueryRunner();
        try {

            conn = controlDataSource.getConnection();
            String getSharedObjectSql = SqlNamedQuery.getInstance().getSql("SharedResourceLockManager.getSharedObject");
            so = run.query(conn, getSharedObjectSql, new ResultSetHandler<SharedResourceLock>() {

                public SharedResourceLock handle(ResultSet rs) throws SQLException {
                    SharedResourceLock so = null;
                    if (rs.next()) {
                        so = new SharedResourceLock();
                        String tempTableSuffix = rs.getString("VALUE");
                        //主键
                        Integer id = rs.getInt("ID");
                        Integer type = rs.getInt("TYPE");
                        Integer used = rs.getInt("USED");
                        so.setValue(tempTableSuffix);
                        so.setId(id);
                        so.setType(type);
                        so.setUsed(used);
                    }

                    return so;
                }
            }, type);
            //lock object
            if (so != null) {
                String useSharedObjectSql = SqlNamedQuery.getInstance().getSql("SharedResourceLockManager.useSharedObject");
                run.update(conn, useSharedObjectSql, so.getId());
            }

        }
        catch (Exception sqle) {
            DbUtils.rollbackAndCloseQuietly(conn);
            throw new DAOException("获取共享锁", sqle);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
        return so;

    }

    /**
     * 是否死亡（一直没占用超过一定时间）的共享资源
     *
     * @param
     * @throws Exception
     */
    public void releaseExpiredSharedLock(int type, int day) throws Exception {
        Connection conn = null;
        QueryRunner run = new QueryRunner();
        try {
            conn = controlDataSource.getConnection();
            String releaseSharedObjectSql = SqlNamedQuery.getInstance().getSql("SharedResourceLockManager.releaseExpiredSharedObject");
            run.update(conn, releaseSharedObjectSql, type, day);
        }
        catch (Exception sqle) {
            DbUtils.rollbackAndCloseQuietly(conn);
            throw new DAOException("释放共享锁", sqle);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }


    }


}
