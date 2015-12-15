package com.navinfo.navicommons.database.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.sql.DataSource;

import org.apache.log4j.Logger;


public class ProcedureBase {

    protected Logger log = Logger.getLogger(this.getClass());
    protected Connection externalConnection;
    protected DataSource dataSource;
    protected boolean useDataSource = false;

    public ProcedureBase(Connection externalConnection) {
        super();
        this.externalConnection = externalConnection;
    }

    public ProcedureBase(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
        useDataSource = true;
    }

    /**
     * <pre>
     *  根据存储过程调用字符串（形如{call pk_vm.undo(123,'abc')}或begin pk_vm.undo(123,'abc');end;）调用存储过程
     *  注意：不支持返回值
     * </pre>
     *
     * @param proc 过程名，带参数值，例如：call pk_vm_reverse_log.undo(123,'abc')
     * @throws SQLException
     */
    public void callProcedure(String proc) throws SQLException {
        if (proc == null || proc.trim().length() == 0) {
            throw new SQLException("存储过程调用字符串不能为空");
        }
        long start = System.currentTimeMillis();
        CallableStatement cs = null;
        Connection conn = null;
        try {
            if (useDataSource) {
                conn = dataSource.getConnection();
            } else {
                conn = this.externalConnection;
            }
            cs = conn.prepareCall(proc);
            cs.execute();           
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            close(conn, cs);
        }
        long end = System.currentTimeMillis();
        log.debug(proc + ",cost :" + (end - start) + " ms.");
    }

    /**
     * <pre>
     * 根据存储过程调用字符串（形如{call pk_vm.undo(task_id=>?,?)}或begin pk_vm.undo(task_id=>?,?);end;）、参数列表调用存储过程
     * 支持多个输入参数，类型支持Integer,Long,String，各输入参数不能为空，否则无法判断类型
     * 注意：不支持存储过程返回值
     * </pre>
     *
     * @param proc     存储过程名，如pk_vm_imp.imp_data
     * @param inParams 输入参数列表，必须按照顺序排列
     * @throws SQLException
     */
    public void callProcedure(String proc, Object... inParams) throws SQLException {
        if (inParams == null) {
            callProcedure(proc);
            return;
        }
        long start = System.currentTimeMillis();
        CallableStatement cs = null;
        Connection conn = null;
        try {
            if (useDataSource) {
                conn = dataSource.getConnection();
            } else {
                conn = this.externalConnection;
            }
            cs = conn.prepareCall(proc);
            for (int i = 0; i < inParams.length; i++) {
                Object value = inParams[i];
                if (value == null) {
                    cs.setNull(i + 1, Types.OTHER);
                } else if (value instanceof String) {
                    cs.setString(i + 1, (String) value);
                } else if (value instanceof Integer) {
                    cs.setInt(i + 1, (Integer) value);
                } else if (value instanceof Long) {
                    cs.setLong(i + 1, (Long) value);
                } else {
                    cs.setObject(i + 1, value);
                }
            }
            cs.execute();
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            close(conn, cs);
        }
        long end = System.currentTimeMillis();
        log.debug(proc + ",cost :" + (end - start) + " ms.");
    }

    protected void rollback(Connection conn) {
        if (useDataSource) {
            try {
                conn.rollback();
            } catch (SQLException e) {

            }
        }
    }

    protected void close(Connection conn, Statement st) {

        try {
            if (st != null && !st.isClosed()) {
                st.close();
            }
        } catch (Exception e) {
        }


        if (useDataSource) {// 使用数据源，自动关闭连接
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.commit();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        } else {
            // 外部传入的连接，由外部程序关闭
        }
    }

}
