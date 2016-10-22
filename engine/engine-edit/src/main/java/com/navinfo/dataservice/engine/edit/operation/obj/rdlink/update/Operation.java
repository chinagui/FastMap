package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class Operation implements IOperation {

    private Command command;

    private RdLink updateLink;
    private Connection conn;
    
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
        JSONObject content = command.getUpdateContent();

        if (content.containsKey("objStatus")) {

            if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
                result.insertObject(updateLink, ObjStatus.DELETE, updateLink.pid());

                return null;
            } else {

                boolean isChanged = updateLink.fillChangeFields(content);

                if (isChanged) {
                    result.insertObject(updateLink, ObjStatus.UPDATE, updateLink.pid());
                }
            }
        }

        if (content.containsKey("forms")) {
            JSONArray forms = content.getJSONArray("forms");

            this.saveForms(result, forms);
        }

        if (content.containsKey("limits")) {
            JSONArray limits = content.getJSONArray("limits");

            this.saveLimits(result, limits);
        }

        if (content.containsKey("names")) {

            JSONArray names = content.getJSONArray("names");

            this.saveNames(result, names);
        }

        if (content.containsKey("limitTrucks")) {

            JSONArray array = content.getJSONArray("limitTrucks");

            this.saveLimitTrucks(result, array);
        }

        if (content.containsKey("speedlimits")) {

            JSONArray array = content.getJSONArray("speedlimits");

            this.saveSpeedlimits(result, array);
        }

        if (content.containsKey("sidewalks")) {

            JSONArray array = content.getJSONArray("sidewalks");

            this.saveSidewalks(result, array);
        }

        if (content.containsKey("walkstairs")) {

            JSONArray array = content.getJSONArray("walkstairs");

            this.saveWalkstairs(result, array);
        }

        if (content.containsKey("rtics")) {

            JSONArray array = content.getJSONArray("rtics");

            this.saveRtics(result, array);
        }

        if (content.containsKey("intRtics")) {

            JSONArray array = content.getJSONArray("intRtics");

            this.saveIntRtics(result, array);
        }

        if (content.containsKey("zones")) {

            JSONArray array = content.getJSONArray("zones");

            this.saveZones(result, array);
        }
        this.refRdLaneForRdlinkKind(result);
        this.calSpeedLimit(updateLink, command.getUpdateContent(), result);
        this.updateRdTraffic(result);
        return null;
    }

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
                        this.refRdLaneForRdlinkForm(result, form, 2);

                    } else if (ObjStatus.UPDATE.toString().equals(formJson.getString("objStatus"))) {

                        boolean isChanged = form.fillChangeFields(formJson);

                        if (isChanged) {
                            result.insertObject(form, ObjStatus.UPDATE, updateLink.pid());
                            RdLinkForm linkForm = new RdLinkForm();
                            linkForm.copy(form);
                            if (formJson.containsKey("formOfWay")) {
                                linkForm.setFormOfWay(formJson.getInt("formOfWay"));
                                this.refRdLaneForRdlinkForm(result, linkForm, 1);
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

    private void saveLimits(Result result, JSONArray limits) throws Exception {

        for (int i = 0; i < limits.size(); i++) {

            JSONObject limitJson = limits.getJSONObject(i);

            if (limitJson.containsKey("objStatus")) {

                if (!ObjStatus.INSERT.toString().equals(limitJson.getString("objStatus"))) {

                    RdLinkLimit limit = updateLink.limitMap.get(limitJson.getString("rowId"));

                    if (ObjStatus.DELETE.toString().equals(limitJson.getString("objStatus"))) {
                        result.insertObject(limit, ObjStatus.DELETE, updateLink.pid());
                        this.refRdLaneForRdlinkLimit(result, limit, 2);

                    } else if (ObjStatus.UPDATE.toString().equals(limitJson.getString("objStatus"))) {

                        boolean isChanged = limit.fillChangeFields(limitJson);

                        if (isChanged) {
                            result.insertObject(limit, ObjStatus.UPDATE, updateLink.pid());

                            if (limitJson.containsKey("type") && limit.getType() == 2) {
                                limit.setLimitDir(1);
                                this.refRdLaneForRdlinkLimit(result, limit, 2);
                                continue;
                            }
                            RdLinkLimit linkLimit = new RdLinkLimit();
                            linkLimit.copy(limit);
                            if (limitJson.containsKey("vehicle")) {
                                linkLimit.setVehicle(limitJson.getLong("vehicle"));
                            }
                            if (limitJson.containsKey("timeDmain")) {
                                linkLimit.setTimeDomain(limitJson.getString("timeDmain"));
                            }
                            if (limitJson.containsKey("limitDir")) {
                                linkLimit.setLimitDir(limitJson.getInt("limitDir"));
                            }
                            this.refRdLaneForRdlinkLimit(result, linkLimit, 1);
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

    private void saveRtics(Result result, JSONArray array) throws Exception {

        for (int i = 0; i < array.size(); i++) {

            JSONObject json = array.getJSONObject(i);

            if (json.containsKey("objStatus")) {

                if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

                    RdLinkRtic obj = updateLink.rticMap.get(json.getString("rowId"));

                    if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

                    } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

                        boolean isChanged = obj.fillChangeFields(json);

                        if (isChanged) {
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkRtic obj = new RdLinkRtic();

                    obj.Unserialize(json);

                    obj.setLinkPid(this.updateLink.getPid());

                    obj.setMesh(this.updateLink.getMeshId());

                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

                }
            }

        }

    }

    private void saveIntRtics(Result result, JSONArray array) throws Exception {

        for (int i = 0; i < array.size(); i++) {

            JSONObject json = array.getJSONObject(i);

            if (json.containsKey("objStatus")) {

                if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

                    RdLinkIntRtic obj = updateLink.intRticMap.get(json.getString("rowId"));

                    if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                        result.insertObject(obj, ObjStatus.DELETE, updateLink.pid());

                    } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

                        boolean isChanged = obj.fillChangeFields(json);

                        if (isChanged) {
                            result.insertObject(obj, ObjStatus.UPDATE, updateLink.pid());
                        }
                    }
                } else {
                    RdLinkIntRtic obj = new RdLinkIntRtic();

                    obj.Unserialize(json);

                    obj.setLinkPid(this.updateLink.getPid());

                    obj.setMesh(this.updateLink.getMeshId());

                    result.insertObject(obj, ObjStatus.INSERT, updateLink.pid());

                }
            }

        }

    }

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
     * 种类变更维护车道信息 zhaokk
     *
     * @param result
     * @throws Exception
     */
    private void refRdLaneForRdlinkKind(Result result) throws Exception {
        if (this.command.getUpdateContent().containsKey("kind")) {
            int kind = this.command.getUpdateContent().getInt("kind");
            if (this.updateLink.getKind() != kind) {
                com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
                        conn);
                operation.setLink(this.updateLink);
                if (this.updateLink.getKind() <= KIND && kind > KIND) {
                    operation.setFlag(1);

                    operation.caleRdLinesForRdLinkKind(result);
                }
                if (this.updateLink.getKind() > KIND && kind <= KIND) {
                    operation.setFlag(0);
                    operation.caleRdLinesForRdLinkKind(result);
                }
            }
        }
    }

    /***
     * link形态表属性变更维护车道信息
     *
     * @param result
     * @throws Exception
     */
    private void refRdLaneForRdlinkForm(Result result, RdLinkForm form, int flag) throws Exception {
        if (form.getFormOfWay() == 22 || form.getFormOfWay() == 20) {
            com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
                    conn);
            operation.setForm(form);
            operation.setFlag(flag);
            operation.setLink(this.updateLink);
            operation.refRdLaneForRdlinkForm(result);
        }

    }

    /****
     * link车辆类型限制变更 1、 当link上添加或删除车辆类型限制时，或进行车辆类型或时间段变更时，需要进行详细车道维护 2、
     * 当link上添加车辆类型及时间段时，则该link上对应方向上所有车道均添加该车辆类型限制及时间
     * 3、当link上删除车辆类型及时间时，则该link上对应方向上所有车道均删除该车辆类型限制及时间
     * 4、当link车辆类型或时间段变更时，则该link上对应该方向上所有车道更新为link上车辆类型及时间 说明：
     * ①在进行车道车辆类型更新时，如果Rd_lane_Condtion中不存在记录的，需要先增加对应车道的记录，再添加车辆类型及时间
     * ②在进行车道类型删除更新时，如果删除车辆类型或时间段后，RD_LANE_CONDTION中该车道的方向时间段及车辆类型均为空，
     * 则该RD_LANE_CONDTION记录需要删除。
     *
     * @throws Exception
     */
    private void refRdLaneForRdlinkLimit(Result result, RdLinkLimit limit, int flag) throws Exception {
        if (limit.getType() == 2
                && (limit.getLimitDir() == 1 || limit.getLimitDir() == 2 || limit.getLimitDir() == 3)) {
            com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
                    conn);
            operation.setLimit(limit);
            operation.setFlag(flag);
            operation.setLink(this.updateLink);
            operation.refRdLaneForRdlinkLimit(result);
        }

    }

    /**
     * 更新车道限速信息
     *
     * @param link   原始RdLink
     * @param json   待修改属性JSON
     * @param result 结果集
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
            
            for(RdTrafficsignal signal : trafficsignalList)
            {
            	result.insertObject(signal, ObjStatus.DELETE, signal.pid());
            }

            com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Operation eleceye = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Operation(conn);
            eleceye.updateRdElectroniceyeWithDirect(updateLink, result);
        }
        return "";
    }
	
	/**
	 * 获取更新link
	 * @param updateLink 需要更新的link
	 * @return 跟新link提示
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateRdLinkAlertData(RdLink updateLink,JSONObject jsonObj) throws Exception {
		
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