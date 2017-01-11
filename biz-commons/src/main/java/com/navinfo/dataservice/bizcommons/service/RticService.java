package com.navinfo.dataservice.bizcommons.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.internal.OracleTypes;
import oracle.sql.ARRAY;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * @ClassName RticService
 * @author wuguangyao
 * @date 2016.11.23 Description: RticService.java
 */

public class RticService implements Observer {

    protected Logger log = LoggerRepos.getLogger(this.getClass());

    private volatile static RticService instance;

    public static RticService getInstance() {
        if (instance == null) {
            synchronized (RticService.class) {
                if (instance == null) {
                    instance = new RticService();
                }
            }
        }
        return instance;
    }

    private RticService() {
        refreshDataSource();
        SystemConfigFactory.getSystemConfig().addObserver(this);
    }

    ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private List<DataSource> dataSources = new ArrayList<DataSource>();

    // 刷新RticCode数据源
    private void refreshDataSource() {
        rwl.writeLock().lock();

        try {
            dataSources.clear();

            String config = SystemConfigFactory.getSystemConfig().getValue(PropConstant.rticServers);

            if (StringUtils.isEmpty(config)) {
                log.warn("******注意：RticCode Servers没有配置，RticCode服务将不可用******");
                return;
            }

            DbConnectConfig dc = DbConnectConfig.createConnectConfig(config, "rticServer");
            dataSources.add(MultiDataSourceFactory.getInstance().getDataSource(dc));

        } catch (Exception e) {
            log.warn("刷新Rtic数据库服务失败，Rtic服务将不可用");
            log.warn(e.getMessage(), e);
        } finally {
            if (rwl.isWriteLockedByCurrentThread()) {
                rwl.writeLock().unlock();
            }
        }
    }

    public int applyCode(int meshId, int rank) throws ServiceException {

        String meshIdStr = String.valueOf(meshId);
        String rankStr = String.valueOf(rank);

        return applyCode(meshIdStr, rankStr);
    }

    public int applyCode(String meshId, String rticClass) throws ServiceException {

        rwl.readLock().lock();
        try {
            int code = applyRticCodeFromDb(meshId, rticClass, "1", "fastmap-task", "fastmap", "127.0.0.1", "fastmap");
            if (code > 0) {
                return code;
            }
            throw new ServiceException("申请RticCode失败！");
        } finally {
            rwl.readLock().unlock();
        }
    }

    private int applyRticCodeFromDb(String meshId, String rticClass, String limit, String taskId, String clientId,
            String clientIp, String useFor) {
        Connection conn = null;
        //OracleCallableStatement cs = null;
        CallableStatement cs = null;
        
        int rticCode = 0;
        try {
            String sql = "{call DMS_RTICID_MAN.APPLY_RTICID(?,?,?,?,?,?,?,?,?)}";
            conn = dataSources.get(0).getConnection();
            //cs = (OracleCallableStatement) conn.prepareCall(sql);
            cs = (CallableStatement) conn.prepareCall(sql);

            cs.setString(1, meshId);
            cs.setLong(2, Long.parseLong(rticClass));
            cs.setInt(3, Integer.parseInt(limit));
            cs.registerOutParameter(4, OracleTypes.ARRAY, "T_VARCHAR_ARRAY");
            cs.setLong(5, 0L);
            cs.setString(6, taskId);
            cs.setString(7, clientId);
            cs.setString(8, clientIp);
            cs.setString(9, useFor);
            cs.execute();

            ARRAY meshARRAY = (ARRAY) cs.getArray(4);   // cs.getARRAY(4);

            String[] meshes = (String[]) meshARRAY.getArray();

            if (meshes.length > 0) {
                rticCode = Integer.parseInt(meshes[0]);
            }
            conn.commit();
        } catch (Exception e) {
            log.error(e);
            DbUtils.closeQuietly(cs);
            DbUtils.closeQuietly(conn);
        } finally {
            DbUtils.closeQuietly(cs);
            DbUtils.closeQuietly(conn);
        }
        return rticCode;
    }

    @Override
    public void update(Observable o, Object arg) {
        refreshDataSource();
    }
}
