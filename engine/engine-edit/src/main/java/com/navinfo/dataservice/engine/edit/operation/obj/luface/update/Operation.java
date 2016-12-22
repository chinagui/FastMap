package com.navinfo.dataservice.engine.edit.operation.obj.luface.update;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;

import com.navinfo.dataservice.engine.edit.utils.batch.UrbanBatchUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;

/**
 * @author zhangyt
 * @Title: Operation.java
 * @Description: TODO
 * @date: 2016年8月30日 上午9:49:23
 * @version: v1.0
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        JSONObject content = command.getContent();
        LuFace face = command.getFace();

        if (content.containsKey("objStatus")) {
            boolean isChanged = face.fillChangeFields(content);
            if (isChanged) {
                result.insertObject(face, ObjStatus.UPDATE, face.pid());
                int sourcrKind = face.getKind();
                Geometry faceGeometry = GeoTranslator.transform(face.getGeometry(), 0.00001, 5);
                // 修改LuFace形态为21时对面内道路赋Urban属性，由21修改为其他时删除Urban属性
                if (face.changedFields().containsKey("kind")) {
                    int kind = (int) face.changedFields().get("kind");
                    if (kind == 21) {
                        UrbanBatchUtils.updateUrban(faceGeometry, faceGeometry, face.getMeshId(), conn, result);
                    } else if (sourcrKind == 21) {
                        UrbanBatchUtils.updateUrban(faceGeometry, null, face.getMeshId(), conn, result);
                    }
                }
            }
        }

        if (content.containsKey("faceNames")) {
            JSONArray names = content.getJSONArray("faceNames");
            this.updateNames(result, names, face);
        }
        return null;
    }

    private void updateNames(Result result, JSONArray names, LuFace face) throws Exception {
        for (int i = 0; i < names.size(); i++) {
            JSONObject nameJson = names.getJSONObject(i);
            if (nameJson.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(nameJson.getString("objStatus"))) {
                    LuFaceName name = face.luFaceNameMap.get(nameJson.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(nameJson.getString("objStatus"))) {
                        result.insertObject(name, ObjStatus.DELETE, face.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(nameJson.getString("objStatus"))) {
                        boolean isChanged = name.fillChangeFields(nameJson);
                        if (isChanged) {
                            result.insertObject(name, ObjStatus.UPDATE, face.pid());
                        }
                    }
                } else {
                    LuFaceName name = new LuFaceName();
                    name.Unserialize(nameJson);
                    name.setPid(PidUtil.getInstance().applyLuFaceNamePid());
                    name.setFacePid(face.pid());
                    result.insertObject(name, ObjStatus.INSERT, face.pid());
                }
            }
        }
    }

}
