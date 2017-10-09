package com.navinfo.dataservice.engine.limit.operation;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * 操作控制器
 */
public class Transaction {

    private static Logger logger = Logger.getLogger(Transaction.class);

    /**
     * 请求参数
     */
    private String requester;

    /**
     * 操作类型
     */
    private OperType operType;

    /**
     * 对象类型
     */
    private LimitObjType objType;

    /**
     * 数据库链接
     */
    private Connection conn;

    /**
     * 用户Id
     */
    private long userId;

    /**
     * 子任务Id
     */
    private int subTaskId;

    /**
     * 数据库类型
     */
    private int dbType;

    /**
     * 主要操作
     */
    private AbstractProcess process;

    /**
     * 命令对象
     */
    private AbstractCommand command;

    /**
     * 删除标识
     * 1：提示，0：删除
     */
    private int infect = 0;

    public Transaction(String requester) {
        this.requester = requester;
    }

    public Transaction(String requester, Connection conn) {
        this.requester = requester;
        this.conn = conn;
    }


    public String getRequester() {
        return requester;
    }


    public void setRequester(String requester) {
        this.requester = requester;
    }


    public OperType getOperType() {
        return operType;
    }


    public LimitObjType getObjType() {
        return objType;
    }


    public void setObjType(LimitObjType objType) {
        this.objType = objType;
    }


    public Connection getConn() {
        return conn;
    }


    public void setConn(Connection conn) {
        this.conn = conn;
    }


    public long getUserId() {
        return userId;
    }


    public void setUserId(long userId) {
        this.userId = userId;
    }


    public int getSubTaskId() {
        return subTaskId;
    }


    public void setSubTaskId(int subTaskId) {
        this.subTaskId = subTaskId;
    }


    public void setDbType(int dbType) {
        this.dbType = dbType;
    }


    public int getInfect() {
        return infect;
    }


    public void setInfect(int infect) {
        this.infect = infect;
    }

    /**
     * 创建操作命令
     *
     * @return 命令
     */
    public AbstractCommand createCommand(String requester) throws Exception {
        // 修改net.sf.JSONObject的bug：string转json对象损失精度问题（解决方案目前有两种，一种替换新的jar包以及依赖的包，第二种先转fastjson后再转net.sf）
        com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject.parseObject(requester);
        JSONObject json = JsonUtils.fastJson2netJson(fastJson);

        operType = Enum.valueOf(OperType.class, json.getString("command"));
        objType = Enum.valueOf(LimitObjType.class, json.getString("type"));
        if (json.containsKey("infect")) {
            infect = json.getInt("infect");
        }

        switch (objType) {
            case SCPLATERESGROUP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.create.Command(json,
                                requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.update.Command(json,
                                requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.delete.Command(json,
                                requester);
                    case RELATION:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.relation.Command(json,
                                requester);

                }
                break;
            case SCPLATERESMANOEUVRE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.create.Command(json,
                                requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.update.Command(json,
                                requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Command(json,
                                requester);
                }
                break;
            case SCPLATERESINFO:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.create.Command(json,
                                requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.update.Command(json,
                                requester);
                }
                break;
            case SCPLATERESRDLINK:
                switch (operType) {
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.rdlink.update.Command(json, requester);
                }
            case SCPLATERESGEOMETRY:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.create.Command(json,
                                requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.update.Command(json,
                                requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete.Command(json,
                                requester);
                }
                break;
        }
        throw new Exception("不支持的操作类型");
    }

    /**
     * 创建操作进程
     *
     * @param command 操作命令
     * @return 操作进程
     * @throws Exception
     */
    public AbstractProcess createProcess(AbstractCommand command) throws Exception {
        switch (objType) {
            case SCPLATERESGROUP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.delete.Process(
                                command);
                    case RELATION:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.relation.Process(
                                command);
                }
                break;
            case SCPLATERESMANOEUVRE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Process(
                                command);
                }
                break;
            case SCPLATERESINFO:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.update.Process(
                                command);
                }
                break;
            case SCPLATERESRDLINK:
                switch (operType) {
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.rdlink.update.Process(command);
                }
            case SCPLATERESGEOMETRY:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete.Process(
                                command);
                }
                break;
        }
        throw new Exception("不支持的操作类型");
    }


    /**
     * 执行操作
     */
    public String run() throws Exception {

        command = this.createCommand(requester);

        process = this.createProcess(command);

        return process.run();
    }

    /**
     * 执行操作
     */
    public String innerRun() throws Exception {
        return null;
    }


    /**
     * Getter method for property <tt>logs</tt>.
     *
     * @return property value of logs
     */
    public String getLogs() {
        return process.getResult().getLogs();
    }


    public JSONArray getCheckLog() {
        return process.getResult().getCheckResults();

    }

    public String getId() {
        return process.getResult().getPrimaryId();
    }

    public int getDbType() {
        return dbType;
    }
}
