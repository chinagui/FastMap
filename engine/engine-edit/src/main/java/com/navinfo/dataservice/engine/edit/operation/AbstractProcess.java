package com.navinfo.dataservice.engine.edit.operation;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.check.CheckEngine;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MaYunFei
 * @ClassName: Abstractprocess
 * @date 上午10:54:43
 * @Description: Abstractprocess.java
 */
public abstract class AbstractProcess<T extends AbstractCommand> implements IProcess {
    private T command;
    private Result result;
    private Connection conn;
    protected CheckCommand checkCommand = new CheckCommand();
    protected CheckEngine checkEngine = null;
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

    public AbstractProcess() {
        this.log = LoggerRepos.getLogger(this.log);
    }

    public AbstractProcess(AbstractCommand command, Result result, Connection conn) throws Exception {
        this.command = (T) command;
        if (conn != null) {
            this.conn = conn;
        }
        if (result != null) {
            this.result = result;
        } else {
            this.result = new Result();
        }
        // 初始化检查参数
        this.initCheckCommand();
    }

    public AbstractProcess(AbstractCommand command) throws Exception {
        this.command = (T) command;
        this.result = new Result();
        if (!command.isHasConn()) {
            this.conn = DBConnector.getInstance().getConnectionById(this.command.getDbId());
            this.conn.setAutoCommit(false);
        }
        // 初始化检查参数
        this.initCheckCommand();
    }

    // 初始化检查参数
    public void initCheckCommand() throws Exception {
        this.checkCommand.setObjType(this.command.getObjType());
        this.checkCommand.setOperType(this.command.getOperType());
        // this.checkCommand.setGlmList(this.command.getGlmList());
        ManApi manApi = (ManApi) ApplicationContextUtil
				.getBean("manApi");
        UserInfo userinfo =manApi.getUserInfoByUserId(this.command.getUserId());
        String worker = "";
        if(userinfo != null){
        	worker = userinfo.getUserRealName();
        }
        this.checkEngine = new CheckEngine(checkCommand, this.conn ,this.command.getTaskId(),worker);
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
        // TODO Auto-generated method stub
        // createPreCheckGlmList();
        List<IRow> glmList = new ArrayList<IRow>();
        glmList.addAll(this.getResult().getAddObjects());
        glmList.addAll(this.getResult().getUpdateObjects());
        glmList.addAll(this.getResult().getDelObjects());
        this.checkCommand.setGlmList(glmList);
        String msg = checkEngine.preCheck();
        return msg;
    }

    // 构造前检查参数。前检查，如果command中的构造不满足前检查参数需求，则需重写该方法，具体可参考createPostCheckGlmList
    public void createPreCheckGlmList() {
        List<IRow> resultList = new ArrayList<IRow>();
        Result resultObj = this.getResult();
        if (resultObj.getAddObjects().size() > 0) {
            resultList.addAll(resultObj.getAddObjects());
        }
        if (resultObj.getUpdateObjects().size() > 0) {
            resultList.addAll(resultObj.getUpdateObjects());
        }
        if (resultObj.getDelObjects().size() > 0) {
            resultList.addAll(resultObj.getDelObjects());
        }
        this.checkCommand.setGlmList(resultList);
    }

    public abstract String exeOperation() throws Exception;

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IProcess#run()
     */
    @Override
    public String run() throws Exception {
        this.prepareData();

        long startPostCheckTime = System.currentTimeMillis();

        String msg = exeOperation();

        long endPostCheckTime = System.currentTimeMillis();

        log.info("exeOperation use time   " + String.valueOf(endPostCheckTime - startPostCheckTime));

        checkResult();

        if (!this.getCommand().getOperType().equals(OperType.DELETE) && !this.getCommand().getObjType().equals(ObjType.RDBRANCH) &&
                !this.getCommand().getObjType().equals(ObjType.RDELECEYEPAIR) && !this.getCommand().getObjType().equals(ObjType.LUFACE)
                && !this.getCommand().getObjType().equals(ObjType.LCFACE)) {
            this.handleResult(this.getCommand().getObjType(), this.getCommand().getOperType(), result);
        }

        log.info("BEGIN  PRECHECK ");
        String preCheckMsg = this.preCheck();
        if (preCheckMsg != null) {
            throw new Exception(preCheckMsg);
        }

        log.info("END  PRECHECK ");
        // 维护车道信息
        this.updateRdLane();

        startPostCheckTime = System.currentTimeMillis();

        //this.recordData();

        endPostCheckTime = System.currentTimeMillis();

        log.info("recordData use time   " + String.valueOf(endPostCheckTime - startPostCheckTime));

        //startPostCheckTime = System.currentTimeMillis();

        //log.info("BEGIN  POSTCHECK ");

        //this.postCheck();

        //endPostCheckTime = System.currentTimeMillis();

        //log.info("BEGIN  POSTCHECK ");

        //log.info("post check use time   " + String.valueOf(endPostCheckTime - startPostCheckTime));

        log.info("操作成功");


        return msg;
    }

    public String innerRun() throws Exception {

        this.prepareData();

        String msg = exeOperation();

        checkResult();

        if (!this.getCommand().getOperType().equals(OperType.DELETE) && !this.getCommand().getObjType().equals(ObjType.RDBRANCH) &&
                !this.getCommand().getObjType().equals(ObjType.RDELECEYEPAIR) && !this.getCommand().getObjType().equals(ObjType.LUFACE)
                && !this.getCommand().getObjType().equals(ObjType.LCFACE)) {
            handleResult(this.getCommand().getObjType(), this.getCommand().getOperType(), result);
        }

        String preCheckMsg = this.preCheck();

        if (preCheckMsg != null) {
            throw new Exception(preCheckMsg);
        }
        this.updateRdLane();
        //// 处理提示信息
        //if (this.command.getInfect() == 1) {
        //    return this.delPrompt(this.getResult(), this.getCommand());
        //}
        //this.recordData();

        this.postCheck();


        return msg;
    }

    /**
     * 被动维护详细车道
     *
     * @throws Exception
     */
    public void updateRdLane() throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.OpRefRelationObj operation = new com.navinfo.dataservice.engine
                .edit.operation.obj.rdlane.update.OpRefRelationObj(this.getConn(), getResult());

        operation.updateRdLane(getCommand().getObjType());
    }

    /**
     * 检查请求是否执行了某些操作
     *
     * @throws Exception
     */
    private void checkResult() throws Exception {
        if (this.getCommand().getObjType().equals(ObjType.IXPOI)) {
            return;
        } else if (CollectionUtils.isEmpty(result.getAddObjects()) && CollectionUtils.isEmpty(result.getUpdateObjects()) &&
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
        // TODO Auto-generated method stub
        // this.createPostCheckGlmList();
        List<IRow> glmList = new ArrayList<IRow>();
        glmList.addAll(this.getResult().getAddObjects());
        glmList.addAll(this.getResult().getUpdateObjects());
        // glmList.addAll(this.getResult().getDelObjects());
        this.checkCommand.setGlmList(glmList);
        this.checkEngine.postCheck();

    }

    // 构造后检查参数
    public void createPostCheckGlmList() {
        List<IRow> resultList = new ArrayList<IRow>();
        Result resultObj = this.getResult();
        if (resultObj.getAddObjects().size() > 0) {
            resultList.addAll(resultObj.getAddObjects());
        }
        if (resultObj.getUpdateObjects().size() > 0) {
            resultList.addAll(resultObj.getUpdateObjects());
        }
        this.checkCommand.setGlmList(resultList);
    }

    @Override
    public String getPostCheck() throws Exception {
        return postCheckMsg;
    }

    @Override
    public boolean recordData() throws Exception {
        LogWriter lw = new LogWriter(conn);
        lw.setUserId(command.getUserId());
        lw.setTaskId(command.getTaskId());
        lw.generateLog(command, result);
        OperatorFactory.recordData(conn, result);
        lw.recordLog(command, result);
        try {
            PoiMsgPublisher.publish(result);
        } catch (Exception e) {
            log.error(e, e);
        }
        return true;
    }

    public CheckCommand getCheckCommand() {
        return checkCommand;
    }

    public void setCheckCommand(CheckCommand checkCommand) {
        this.checkCommand = checkCommand;
    }

    public void handleResult(ObjType objType, OperType operType, Result result) {
        switch (operType) {
            case CREATE:
            case CREATESIDEROAD:
            case BREAK:
                List<Integer> addObjPidList = result.getListAddIRowObPid();
                for (int i = 0; i < result.getAddObjects().size(); i++) {
                    IRow row = result.getAddObjects().get(i);
                    if (objType.equals(row.objType())) {
                        if (addObjPidList.get(i) != null) {
                            result.setPrimaryPid(addObjPidList.get(i));
                            break;
                        }
                    }
                }
                break;
            case UPDATE:
                for (IRow row : result.getAddObjects()) {
                    result.setPrimaryPid(row.parentPKValue());
                    return;
                }
                for (IRow row : result.getUpdateObjects()) {
                    result.setPrimaryPid(row.parentPKValue());
                    return;
                }
                for (IRow row : result.getDelObjects()) {
                    result.setPrimaryPid(row.parentPKValue());
                    return;
                }
                break;
            case REPAIR:
            case MOVE:
                List<Integer> allObjPidList = new ArrayList<>();
                List<IRow> allIRows = new ArrayList<>();
                allIRows.addAll(result.getUpdateObjects());
                allObjPidList.addAll(result.getListUpdateIRowObPid());
                if (CollectionUtils.isEmpty(allObjPidList)) {
                    allIRows.addAll(result.getAddObjects());
                    allObjPidList.addAll(result.getListAddIRowObPid());
                }
                for (int i = 0; i < allIRows.size(); i++) {
                    IRow row = allIRows.get(i);
                    if (objType.equals(row.objType())) {
                        if (allObjPidList.get(i) != null) {
                            result.setPrimaryPid(allObjPidList.get(i));
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        if (ObjStatus.INSERT.equals(ObjStatus.INSERT)) {
            System.out.println(12423);
        }

    }
}
