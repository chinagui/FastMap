package com.navinfo.navicommons.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

import com.navinfo.navicommons.config.SystemGlobals;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.OracleConnectionManager;
import com.navinfo.navicommons.database.sql.PersistenceException;
import com.navinfo.navicommons.database.sql.SQLQuery;
import com.navinfo.navicommons.database.sqlite.SqliteManager;
import com.navinfo.navicommons.exception.VersionNotFoundException;
import com.navinfo.navicommons.net.ftp.FTPUtils;
import com.navinfo.navicommons.zip.ZipUtils;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-11
 */
public class GdbVersionManager {
    public static String CURRENT_VERSION;
    private static String versionSql;

    public static String getVersion(Connection con) throws VersionNotFoundException {
        SQLQuery sqlQuery = new SQLQuery(con);
        try {
            List<Map<String, String>> verMaps = sqlQuery.queryMap(versionSql);
            if (verMaps == null || verMaps.size() != 1) {
                throw new VersionNotFoundException("没有找到版本");
                //return CURRENT_VERSION;
            }
            return verMaps.get(0).get("GDB_VER_NO");
        } catch (PersistenceException e) {
            //return CURRENT_VERSION;
            throw new VersionNotFoundException("查找版本时发生sql错误", e);
        }
    }

    public static String getVersion(DataSource ds) throws VersionNotFoundException {
        Connection con = null;
        try {
            con = ds.getConnection();
            return getVersion(con);
        } catch (SQLException e) {
            throw new VersionNotFoundException("获取连接时出错", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }

    public static String getSqliteVersion(String sqiliteFile) throws VersionNotFoundException {
        Connection con = null;
        try {
            con = SqliteManager.getConnection(sqiliteFile);
            return getVersion(con);
        } catch (SQLException e) {
            throw new VersionNotFoundException("获取连接时出错", e);
        } catch (ClassNotFoundException e) {
            throw new VersionNotFoundException("没有找到sqlite驱动", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }

    public static String getSqliteVersion(String sqiliteFileName, String path, String ftpUser, String ftpPass)
            throws VersionNotFoundException {
        //下载sqlite文件到本地
        String tempDir = SystemGlobals.getValue("download.ftp.tmpdir");
        tempDir = tempDir + UUID.randomUUID().toString().replace("-", "");
        File tempDirectory = new File(tempDir);
        String dbName;
        try {
            tempDirectory.mkdirs();
            FTPUtils.downloadFile(SystemGlobals.getValue("dms.ftp.host"),
                    Integer.parseInt(SystemGlobals.getValue("dms.ftp.port", "21")),
                    ftpUser, ftpPass, path,
                    new String[]{sqiliteFileName}, tempDir);
            File sqlite = new File(tempDirectory, sqiliteFileName);
            dbName = tempDir + File.separator + sqiliteFileName;
            if (!sqlite.exists())
                throw new VersionNotFoundException("未找到下载的sqlite文件");
            if (sqiliteFileName.toLowerCase().endsWith("zip"))
                dbName = unzipFileAndSearchSqlite(tempDir, sqiliteFileName);
            Assert.notNull(dbName, "没有找到sqlite文件");
            return getSqliteVersion(dbName);
        } catch (IOException e) {
            throw new VersionNotFoundException("创建临时工作目录从ftp下载文件时出错", e);
        } finally {
            if (tempDirectory.exists())
                try {
                    FileUtils.deleteDirectory(tempDirectory);
                } catch (IOException e) {
                    //do nothing
                }
        }
    }

    private static String unzipFileAndSearchSqlite(String workDir, String fileName) throws VersionNotFoundException {
        try {
            ZipUtils.unZip(workDir + File.separator + fileName, workDir);
        } catch (Exception e) {
            throw new VersionNotFoundException("解压文件时出错", e);
        }
        Collection<File> files = FileUtils.listFiles(new File(workDir), new String[]{"db", "DB"}, true);
        if (files.size() == 1)
            return files.iterator().next().getAbsolutePath();
        for (File file : files) {
            if (file.getName().toLowerCase().replace(".db", "").equalsIgnoreCase(fileName.toLowerCase().replace(".zip", "")))
                return file.getAbsolutePath();
        }
        throw new VersionNotFoundException("解压缩后没有找到sqlite文件");
    }

    public static String getVersion(String dataSourceName) throws VersionNotFoundException {
        return getVersion(DBConnectionFactory.getInstance().getDataSource(dataSourceName));
    }

    public static String getOracleVersion(String url, String user, String password) throws VersionNotFoundException {
        Connection con = null;
        try {
            con = OracleConnectionManager.getConnection(url, user, password);
            return getVersion(con);
        } catch (SQLException e) {
            throw new VersionNotFoundException("获取连接时出错", e);
        } catch (ClassNotFoundException e) {
            throw new VersionNotFoundException("没有找到oracle驱动", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }

    public static String getOracleVersion(String ip, String sid, String user, String password) throws VersionNotFoundException {
        Connection con = null;
        try {
            con = OracleConnectionManager.getConnection(ip, sid, user, password);
            return getVersion(con);
        } catch (SQLException e) {
            throw new VersionNotFoundException("获取连接时出错", e);
        } catch (ClassNotFoundException e) {
            throw new VersionNotFoundException("没有找到oracle驱动", e);
        } finally {
            DBUtils.closeConnection(con);
        }
    }

    private static String update_version = "update m_parameter set parameter=?  where name='GLM_VERSION'";

    public static void updateOracleGDBVersion(DataSource ds, String version) throws SQLException {
        QueryRunner runner = new QueryRunner();
        runner.update(ds, update_version, version);
    }

    public static void updateSqliteGDBVersion(String sqliteFile, String version) throws Exception {
        Connection conn = null;
        try {
            conn = SqliteManager.getConnection(sqliteFile);
            QueryRunner runner = new QueryRunner();
            runner.update(conn, update_version, version);
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    static {
        CURRENT_VERSION = SystemGlobals.getValue("gdb.current.version", "1.6");
        versionSql = "select parameter gdb_ver_no from m_parameter where name='GLM_VERSION'";
    }
}
