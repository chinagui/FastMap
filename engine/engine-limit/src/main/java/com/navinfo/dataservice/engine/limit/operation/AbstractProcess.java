package com.navinfo.dataservice.engine.limit.operation;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.glm.iface.OperType;

import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.IProcess;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;

public abstract class AbstractProcess<T extends AbstractCommand> implements IProcess {
    private T command;
    private Result result;
    private Connection conn;

    public static Logger log = Logger.getLogger(AbstractProcess.class);

    /**
     * @return the conn
     */
    public Connection getConn() {
        return conn;
    }

    public void setCommand(T command) {
        this.command = command;

    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    private String postCheckMsg;

    public AbstractProcess(AbstractCommand command) throws Exception {
        this.log = LoggerRepos.getLogger(this.log);
        this.command = (T) command;
        this.result = new Result();

        if (command.getDbType().equals(DbType.LIMITDB)) {
            this.conn = DBConnector.getInstance().getLimitConnection();
            this.conn.setAutoCommit(false);
        }
        if (command.getDbType().equals(DbType.METADB)) {
            this.conn = DBConnector.getInstance().getMetaConnection();
            this.conn.setAutoCommit(false);
        }
        if (command.getDbType().equals(DbType.REGIONDB)) {
            this.conn = DBConnector.getInstance().getConnectionById(this.command.getDbId());
            this.conn.setAutoCommit(false);
        }
        // 初始化检查参数
        this.initCheckCommand();
    }

    // 初始化检查参数
    public void initCheckCommand() throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IProcess#getCommand()
     */
    @Override
    public T getCommand() {
        return command;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IProcess#getResult()
     */
    @Override
    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IProcess#prepareData()
     */
    @Override
    public boolean prepareData() throws Exception {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IProcess#preCheck()
     */
    @Override
    public String preCheck() throws Exception {

        return null;
    }

    // 构造前检查参数。前检查，如果command中的构造不满足前检查参数需求，则需重写该方法，具体可参考createPostCheckGlmList
    public void createPreCheckGlmList() {

    }

    public abstract String exeOperation() throws Exception;

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IProcess#run()
     */
    @Override
    public String run() throws Exception {

        String msg;
        try {
            conn.setAutoCommit(false);

            this.prepareData();

            msg = exeOperation();

            checkResult();

            if (!this.getCommand().getOperType().equals(OperType.DELETE) ) {
                this.handleResult(this.getCommand().getObjType(), this.getCommand().getOperType(), result);
            }

            String preCheckMsg = this.preCheck();

            if (preCheckMsg != null) {
                throw new Exception(preCheckMsg);
            }

            this.recordData();

            this.postCheck();

            conn.commit();

            System.out.print("操作成功\r\n");

        } catch (Exception e) {

            conn.rollback();

            throw e;
        } finally {
            try {
                conn.close();

                System.out.print("结束\r\n");
            } catch (Exception e) {

            }
        }

        return msg;
    }

    public String innerRun() throws Exception {
        return null;
    }


    /**
     * 检查请求是否执行了某些操作
     *
     * @throws Exception
     */
    private void checkResult() throws Exception {
        if (CollectionUtils.isEmpty(result.getAddObjects()) && CollectionUtils.isEmpty(result.getUpdateObjects()) &&
                CollectionUtils.isEmpty(result.getDelObjects())) {
            throw new DataNotChangeException("属性值未发生变化");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IProcess#postCheck()
     */
    @Override
    public void postCheck() throws Exception {


    }

    // 构造后检查参数
    public void createPostCheckGlmList() {

    }

    @Override
    public String getPostCheck() throws Exception {
        return postCheckMsg;
    }

    @Override
    public boolean recordData() throws Exception {

        OperatorFactory.recordData(conn, result);

        return true;
    }


    public void handleResult(LimitObjType objType, OperType operType, Result result) {
//        switch (operType) {
//            case CREATE:
//            case CREATESIDEROAD:
//            case BREAK:
//                List<Integer> addObjPidList = result.getListAddIRowObPid();
//                for (int i = 0; i < result.getAddObjects().size(); i++) {
//                    IRow row = result.getAddObjects().get(i);
//                    if (objType.equals(row.objType())) {
//                        if (addObjPidList.get(i) != null) {
//                            result.setPrimaryPid(addObjPidList.get(i));
//                            break;
//                        }
//                    }
//                }
//                break;
//            case UPDATE:
//                for (IRow row : result.getAddObjects()) {
//                    result.setPrimaryPid(row.parentPKValue());
//                    return;
//                }
//                for (IRow row : result.getUpdateObjects()) {
//                    result.setPrimaryPid(row.parentPKValue());
//                    return;
//                }
//                for (IRow row : result.getDelObjects()) {
//                    result.setPrimaryPid(row.parentPKValue());
//                    return;
//                }
//                break;
//            case REPAIR:
//            case MOVE:
//                List<Integer> allObjPidList = new ArrayList<>();
//                List<IRow> allIRows = new ArrayList<>();
//                allIRows.addAll(result.getUpdateObjects());
//                allObjPidList.addAll(result.getListUpdateIRowObPid());
//                if (CollectionUtils.isEmpty(allObjPidList)) {
//                    allIRows.addAll(result.getAddObjects());
//                    allObjPidList.addAll(result.getListAddIRowObPid());
//                }
//                for (int i = 0; i < allIRows.size(); i++) {
//                    IRow row = allIRows.get(i);
//                    if (objType.equals(row.objType())) {
//                        if (allObjPidList.get(i) != null) {
//                            result.setPrimaryPid(allObjPidList.get(i));
//                            break;
//                        }
//                    }
//                }
//                break;
//            default:
//                break;
//        }
    }

    public static void main(String[] args) {


    }
}
