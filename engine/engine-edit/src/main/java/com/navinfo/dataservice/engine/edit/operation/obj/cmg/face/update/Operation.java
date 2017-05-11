package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import net.sf.json.JSONObject;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public class Operation implements IOperation {

    /**
     * 参数
     */
    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    /**
     * 执行操作
     *
     * @param result 操作结果
     * @return 操作后的对象
     * @throws Exception
     */
    @Override
    public String run(Result result) throws Exception {
        JSONObject content = command.getContent();
        if (content.containsKey("objStatus")) {
            if (ObjStatus.UPDATE.toString().equals(content.getString("objStatus"))) {
                boolean isChanged = command.getCmgface().fillChangeFields(content);
                if (isChanged) {
                    result.insertObject(command.getCmgface(), ObjStatus.UPDATE, command.getCmgface().pid());
                }
            }
        }

        return null;
    }
}
