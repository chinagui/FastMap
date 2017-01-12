package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.service.RticService;
import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimitTruck;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSidewalk;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkWalkstair;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkZone;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.engine.edit.utils.batch.SpeedLimitUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * RLINK 修改和属性维护
 * 
 * @author zhaokk
 * 
 */
public class Operation implements IOperation {

    private Command command;
    private RdLink updateLink;
    private Connection conn;
    private RdLinkForm form;
    private int formCrossFlag;// 交叉口link判断 1 新增 2 删除
    private int kindFlag;// 种别维护 1 新增 2 删除

    public int getKindFlag() {
        return kindFlag;
    }

    public void setKindFlag(int kindFlag) {
        this.kindFlag = kindFlag;
    }

    public int getFormCrossFlag() {
        return formCrossFlag;
    }

    public void setFormCrossFlag(int formCrossFlag) {
        this.formCrossFlag = formCrossFlag;
    }

    public RdLinkForm getForm() {
        return form;
    }

    public void setForm(RdLinkForm form) {
        this.form = form;
    }

    public Operation() {
    }

    public Operation(Command command, RdLink updateLink) {
        this.command = command;
        this.updateLink = updateLink;
    }

    public Operation(Command command, RdLink updateLink, Connection conn) {
        this.command = command;
        this.conn = conn;
        this.updateLink = updateLink;
    }

    public final int KIND = 7;

    @Override
    public String run(Result result) throws Exception {
        // 修改rdlink
        if (null != updateLink)
            updateLink(result);
        // 批量修改
        else {
            for (int i = 0; i < command.getLinks().size(); i++) {
                updateLink = command.getLinks().get(i);
                command.setUpdateContent(command.getUpdateContents().getJSONObject(i));
                updateLink(result);
            }
        }
        return null;
    }

    /***
     * 修改rdkink
     * 
     * @param result
     * @return
     * @throws Exception
     */
    private boolean updateLink(Result result) throws Exception {
        JSONObject content = command.getUpdateContent();
        if (content.containsKey("objStatus")) {
            if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
                result.insertObject(updateLink, ObjStatus.DELETE, updateLink.pid());
                return true;
            } else {
                boolean isChanged = updateLink.fillChangeFields(content);
                if (isChanged) {
                    result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
                }
            }
        }
        // 子表LINK 形态表维护
        if (content.containsKey("forms")) {
            JSONArray forms = content.getJSONArray("forms");
            this.saveForms(result, forms);
        }
        // 子表:LINK 限制表维护
        if (content.containsKey("limits")) {
            JSONArray limits = content.getJSONArray("limits");
            this.saveLimits(result, limits);
        }
        // 子表 LINK 名称表
        if (content.containsKey("names")) {
            JSONArray names = content.getJSONArray("names");
            this.saveNames(result, names);
        }
        // 子表 LINK 卡车限制表
        if (content.containsKey("limitTrucks")) {
            JSONArray array = content.getJSONArray("limitTrucks");
            this.saveLimitTrucks(result, array);
        }
        // 子表 LINK 限速表
        if (content.containsKey("speedlimits")) {
            JSONArray array = content.getJSONArray("speedlimits");
            this.saveSpeedlimits(result, array);
        } // 子表 LINK 人行便道表
        if (content.containsKey("sidewalks")) {
            JSONArray array = content.getJSONArray("sidewalks");
            this.saveSidewalks(result, array);
        }
        // 子表 LINK 人行阶梯表
        if (content.containsKey("walkstairs")) {
            JSONArray array = content.getJSONArray("walkstairs");
            this.saveWalkstairs(result, array);
        }
        // 子表 LINK RTIC 客户表
        if (content.containsKey("rtics")) {
            JSONArray array = content.getJSONArray("rtics");
            this.saveRtics(result, array);
        }
        // 子表 LINK RTIC 互联网表
        if (content.containsKey("intRtics")) {
            JSONArray array = content.getJSONArray("intRtics");
            this.saveIntRtics(result, array);
        }
        // 子表 LINK ZONE表
        if (content.containsKey("zones")) {
            JSONArray array = content.getJSONArray("zones");
            this.saveZones(result, array);
        }
        // 属性关系维护
        this.updataRelationObj(result);

        return false;
    }

    /***
     * 子表LINK 形态表维护
     * 
     * @param result
     * @param forms
     * @throws Exception
     */
    private void saveForms(Result result, JSONArray forms) throws Exception {
        int deleteCount = 0;
        int insertCount = 0;
        for (int i = 0; i < forms.size(); i++) {
            JSONObject formJson = forms.getJSONObject(i);
            if (formJson.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(formJson.getString("objStatus"))) {
                    RdLinkForm form = updateLink.formMap.get(formJson.getString("rowId"));
                    if (form == null) {
                        throw new Exception("rowId为" + formJson.getString("rowId") + "的RdLinkForm不存在");
                    }
                    if (ObjStatus.DELETE.toString().equals(formJson.getString("objStatus"))) {
                        result.insertObject(form, ObjStatus.DELETE, updateLink.pid());
                        deleteCount++;
                        if (form.getFormOfWay() == 50) {
                            this.setForm(form);
                            this.setFormCrossFlag(2);// 标识删除状态
                        }
                        // this.refRdLaneForRdlinkForm(result, form, 2);
                    } else if (ObjStatus.UPDATE.toString().equals(formJson.getString("objStatus"))) {
                        boolean isChanged = form.fillChangeFields(formJson);
                        if (isChanged) {
                            result.insertObject(form, ObjStatus.UPDATE, updateLink.pid());
                            RdLinkForm linkForm = new RdLinkForm();
                            linkForm.copy(form);
                            if (formJson.containsKey("formOfWay")) {
                                linkForm.setFormOfWay(formJson.getInt("formOfWay"));
                                if (formJson.getInt("formOfWay") == 50) {
                                    this.setForm(linkForm);
                                    this.setFormCrossFlag(1);
                                }
                                linkForm.setFormOfWay(formJson.getInt("formOfWay"));
                                // this.refRdLaneForRdlinkForm(result, linkForm,
                                // 1);
                            }
                        }
                    }
                } else {
                    RdLinkForm form = new RdLinkForm();
                    form.Unserialize(formJson);
                    form.setLinkPid(this.updateLink.getPid());
                    form.setMesh(this.updateLink.getMeshId());
                    result.insertObject(form, ObjStatus.INSERT, updateLink.pid());
                    insertCount++;
                    if (formJson.containsKey("formOfWay")) {
                        if (formJson.getInt("formOfWay") == 50) {
                            this.setForm(form);
                            this.setFormCrossFlag(1);
                        }
                    }
                }
                if (insertCount == 0 && deleteCount == updateLink.getForms().size()) {
                    // rd_link_form被清空时，自动添加一条
                    RdLinkForm form = new RdLinkForm();
                    form.setLinkPid(this.updateLink.getPid());
                    form.setMesh(this.updateLink.getMeshId());
                    result.insertObject(form, ObjStatus.INSERT, this.updateLink.pid());
                }
            }
        }
    }

    /***
     * 子表LINK 限制表维护
     * 
     * @param result
     * @param limits
     * @throws Exception
     */
    private void saveLimits(Result result, JSONArray limits) throws Exception {
        for (int i = 0; i < limits.size(); i++) {
            JSONObject limitJson = limits.getJSONObject(i);
            if (limitJson.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(limitJson.getString("objStatus"))) {
                    RdLinkLimit limit = updateLink.limitMap.get(limitJson.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(limitJson.getString("objStatus"))) {
                        result.insertObject(limit, ObjStatus.DELETE, updateLink.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(limitJson.getString("objStatus"))) {
                        boolean isChanged = limit.fillChangeFields(limitJson);
                        if (isChanged) {
                            result.insertObject(limit, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkLimit limit = new RdLinkLimit();
                    limit.Unserialize(limitJson);
                    limit.setLinkPid(this.updateLink.getPid());
                    limit.setMesh(this.updateLink.getMeshId());
                    result.insertObject(limit, ObjStatus.INSERT, updateLink.pid());
                }
            }
        }
    }

    /***
     * 子表LINK 卡车限制表维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveLimitTrucks(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
                    RdLinkLimitTruck obj = updateLink.limitTruckMap.get(json.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                        boolean isChanged = obj.fillChangeFields(json);
                        if (isChanged) {
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkLimitTruck obj = new RdLinkLimitTruck();
                    obj.Unserialize(json);
                    obj.setLinkPid(this.updateLink.getPid());
                    obj.setMesh(this.updateLink.getMeshId());
                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());
                }
            }
        }
    }

    /***
     * 子表LINK 限速表维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveSpeedlimits(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
                    RdLinkSpeedlimit obj = updateLink.speedlimitMap.get(json.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                        boolean isChanged = obj.fillChangeFields(json);
                        if (isChanged) {
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkSpeedlimit obj = new RdLinkSpeedlimit();
                    obj.Unserialize(json);
                    obj.setLinkPid(this.updateLink.getPid());
                    obj.setMesh(this.updateLink.getMeshId());
                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());
                }
            }
        }
    }

    /***
     * 子表LINK 任性便道表维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveSidewalks(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
                    RdLinkSidewalk obj = updateLink.sidewalkMap.get(json.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                        boolean isChanged = obj.fillChangeFields(json);
                        if (isChanged) {
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkSidewalk obj = new RdLinkSidewalk();
                    obj.Unserialize(json);
                    obj.setLinkPid(this.updateLink.getPid());
                    obj.setMesh(this.updateLink.getMeshId());
                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());
                }
            }
        }
    }

    /***
     * 子表LINK人行阶梯表维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveWalkstairs(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
                    RdLinkWalkstair obj = updateLink.walkstairMap.get(json.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                        boolean isChanged = obj.fillChangeFields(json);
                        if (isChanged) {
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkWalkstair obj = new RdLinkWalkstair();
                    obj.Unserialize(json);
                    obj.setLinkPid(this.updateLink.getPid());
                    obj.setMesh(this.updateLink.getMeshId());
                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());
                }
            }
        }
    }

    /***
     * 子表LINK 阶梯表维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveRtics(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.containsKey("objStatus")) {

                if (ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

                    RdLinkRtic obj = new RdLinkRtic();
                    obj.Unserialize(json);
                    
                    if(obj.getCode() == 0){
                        int newCode = RticService.getInstance().applyCode(updateLink.getMeshId(), obj.getRank());
                        obj.setCode(newCode);    
                    }

                    obj.setLinkPid(this.updateLink.getPid());
                    obj.setMesh(this.updateLink.getMeshId());
                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

                } else {
                    RdLinkRtic obj = updateLink.rticMap.get(json.get("rowId"));
                    if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                        boolean isChanged = obj.fillChangeFields(json);
                        if (isChanged) {
                            if (obj.getCode() == 0) {
                                int newCode = RticService.getInstance().applyCode(updateLink.getMeshId(), obj.getRank());
                                obj.changedFields().put("code", newCode);
                            }
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.getPid());
                        }
                    } else if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {

                        result.insertObject(obj, ObjStatus.DELETE, updateLink.getPid());
                    }
                }
            }
        }
    }

    /***
     * 子表LINKRTIC维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveIntRtics(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.containsKey("objStatus")) {
                if (ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

                    RdLinkIntRtic obj = new RdLinkIntRtic();
                    obj.Unserialize(json);

                    if(obj.getCode() == 0){
                        int newCode = RticService.getInstance().applyCode(updateLink.getMeshId(), obj.getRank());
                        obj.setCode(newCode);    
                    }

                    obj.setLinkPid(this.updateLink.getPid());
                    obj.setMesh(this.updateLink.getMeshId());
                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

                } else {

                    RdLinkIntRtic obj = updateLink.intRticMap.get(json.getString("rowId"));

                    if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

                        boolean isChanged = obj.fillChangeFields(json);
                        if (isChanged) {
                            if (obj.getCode() == 0) {
                                int newCode = RticService.getInstance().applyCode(updateLink.getMeshId(), obj.getRank());
                                obj.changedFields().put("code", newCode);
                            }
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    } else if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());
                    }
                }
            }
        }
    }

    /***
     * 子表LINK ZONE表维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveZones(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
                    RdLinkZone obj = updateLink.zoneMap.get(json.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                        boolean isChanged = obj.fillChangeFields(json);
                        if (isChanged) {
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkZone obj = new RdLinkZone();
                    obj.Unserialize(json);
                    obj.setLinkPid(this.updateLink.getPid());
                    obj.setMesh(this.updateLink.getMeshId());
                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());
                }
            }
        }
    }

    /****
     * 子表LINK 名称表维护
     * 
     * @param result
     * @param names
     * @throws Exception
     */
    private void saveNames(Result result, JSONArray names) throws Exception {
        for (int i = 0; i < names.size(); i++) {
            JSONObject nameJson = names.getJSONObject(i);
            if (nameJson.containsKey("objStatus")) {
                if (!ObjStatus.INSERT.toString().equals(nameJson.getString("objStatus"))) {
                    RdLinkName name = updateLink.nameMap.get(nameJson.getString("rowId"));
                    if (ObjStatus.DELETE.toString().equals(nameJson.getString("objStatus"))) {
                        result.insertObject(name, ObjStatus.DELETE, updateLink.pid());
                    } else if (ObjStatus.UPDATE.toString().equals(nameJson.getString("objStatus"))) {
                        boolean isChanged = name.fillChangeFields(nameJson);
                        if (isChanged) {
                            result.insertObject(name, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkName name = new RdLinkName();
                    name.Unserialize(nameJson);
                    name.setLinkPid(this.updateLink.getPid());
                    result.insertObject(name, ObjStatus.INSERT, updateLink.pid());
                }
            }
        }
    }

    /***
     * 属性关系维护
     * 
     * @param result
     * @throws Exception
     */
    private void updataRelationObj(Result result) throws Exception {
        this.calSpeedLimit(updateLink, command.getUpdateContent(), result);
        // this.updateRdLane(result);
        // 信号灯维护
        this.updateRdTraffic(result);

    }

    /**
     * 更新车道限速信息
     * 
     * @param link
     *            原始RdLink
     * @param json
     *            待修改属性JSON
     * @param result
     *            结果集
     */
    private void calSpeedLimit(RdLink link, JSONObject json, Result result) {
        SpeedLimitUtils.updateRdLink(link, json, result);
    }

    /**
     * 修改link方向维护信号灯关系
     * 
     * @param result
     * @throws Exception
     */
    public String updateRdTraffic(Result result) throws Exception {
        Map<String, Object> changeFields = updateLink.changedFields();
        if (changeFields.containsKey("direct")) {
            com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation(
                    conn);
            List<RdTrafficsignal> trafficsignalList = operation.updateRdCrossByModifyLinkDirect(updateLink);
            for (RdTrafficsignal signal : trafficsignalList) {
                result.insertObject(signal, ObjStatus.DELETE, signal.pid());
            }
            com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Operation eleceye = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Operation(
                    conn);
            eleceye.updateRdElectroniceyeWithDirect(updateLink, result);
        }
        return "";
    }

    /**
     * 获取更新link
     * 
     * @param updateLink
     *            需要更新的link
     * @return 跟新link提示
     * @throws Exception
     */
    public List<AlertObject> getUpdateRdLinkAlertData(RdLink updateLink, JSONObject jsonObj) throws Exception {
        boolean flag = updateLink.fillChangeFields(jsonObj);
        List<AlertObject> alertList = new ArrayList<>();
        if (flag) {
            AlertObject alertObj = new AlertObject();
            alertObj.setObjType(updateLink.objType());
            alertObj.setPid(updateLink.getPid());
            alertObj.setStatus(ObjStatus.UPDATE);
            if (!alertList.contains(alertObj)) {
                alertList.add(alertObj);
            }
        }
        return alertList;
    }
}