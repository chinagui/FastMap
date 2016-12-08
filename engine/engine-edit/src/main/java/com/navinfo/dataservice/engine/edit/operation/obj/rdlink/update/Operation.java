package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update;

import com.navinfo.dataservice.bizcommons.service.RticService;
import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.*;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.engine.edit.utils.batch.SpeedLimitUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // 子表 LINK TMC表
        if (content.containsKey("tmclocations")) {
            JSONArray array = content.getJSONArray("tmclocations");
            this.saveTmcLocations(result, array);
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
     * 子表LINK TMC表维护
     * 
     * @param result
     * @param array
     * @throws Exception
     */
    private void saveTmcLocations(Result result, JSONArray array) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            RdTmclocation obj = updateLink.locationMap.get(json.getString("rowId"));
            if (json.containsKey("objStatus")) {
                if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                    result.insertObject(obj, ObjStatus.DELETE, obj.pid());
                } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                    boolean isChanged = obj.fillChangeFields(json);
                    if (isChanged) {
                        result.insertObject(obj, ObjStatus.UPDATE, obj.pid());
                    }
                }
            }
            // 修改子表links
            if (json.containsKey("links")) {
                JSONArray linkArray = json.getJSONArray("links");
                saveTmcLocationLinks(result, linkArray, obj);
            }
        }
        result.setPrimaryPid(this.command.getLinkPid());
    }

    /***
     * 子表LINK TMC表维护
     * 
     * @param result
     * @param array
     * @param obj
     * @throws Exception
     */
    private void saveTmcLocationLinks(Result result, JSONArray array, RdTmclocation obj) throws Exception {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            RdTmclocationLink link = obj.linkMap.get(json.getString("rowId"));
            if (json.containsKey("objStatus")) {
                if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
                    result.insertObject(link, ObjStatus.DELETE, obj.pid());
                } else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {
                    // 修改目前只允许修改位置方向
                    boolean isChanged = link.fillChangeFields(json);
                    if (isChanged) {
                        result.insertObject(link, ObjStatus.UPDATE, obj.pid());
                    }
                }
                // 新增
                else if (ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
                    RdTmclocationLink insertLink = new RdTmclocationLink();
                    insertLink.Unserialize(json);
                    insertLink.setGroupId(obj.getPid());
                    result.insertObject(insertLink, ObjStatus.INSERT, insertLink.getGroupId());
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
        this.refRdLaneForRdlinkKind(result);
        this.calSpeedLimit(updateLink, command.getUpdateContent(), result);
        // this.updateRdLane(result);
        // 信号灯维护
        this.updateRdTraffic(result);

    }

    /**
     * 修改属性维护详细车道信息 但当一个link发生多信息变更时，按如下要素优先级顺序进行判断，当且仅当该变更要素为该对象的最高优先级要素且发生变更时，
     * 才需要按该要素启动自动维护。 Level1：link种别变更 Level2：交叉口内link属性变更>link车道数变更。
     * 说明：Level1影响详细车道有无有的判断
     * ，当其不发生变更时，不影响level2的判断，当其发生变化时，如果变为无，则不用判断level2，如果变更为有
     * ，则最终车道数依据level2进行内容进行判断。
     * Level2为影响详细车道记录变更的要素优先级，level1不变更时，不考虑level1要素的影响。
     * 其中，link车辆类型变更与link属性变更，
     * 不会影响详细车道记录，仅会影响到详细车道中的车辆类型限制信息，故两者优先级如下：link车辆类型变更>link属性变更
     * 当同一link的车道记录与车辆类型均需要变更维护时，先维护车道记录，再维护车道上的车辆类型限制信息。
     * 
     * @param result
     * @throws Exception
     */
    private void updateRdLane(Result result) throws Exception {
        // link 种类修改维护详细车道
        this.refRdLaneForRdlinkKind(result);
        if (this.getKindFlag() == 2) {
            return;
        }
        // link 方向修改维护详细车道
        this.refRdLaneForRdlinkDirect(result);
        //
        this.refRdLaneForRdLinkCross(result);
        // 车道数变更维护车道信息
        this.refRdLaneForRdLinkLaneNum(result);

    }

    /***
     * 方向修改维护详细车道信息
     * 
     * @param result
     * @throws Exception
     */
    private void refRdLaneForRdlinkDirect(Result result) throws Exception {
        if (this.command.getUpdateContent().containsKey("direct")) {
            int direct = this.command.getUpdateContent().getInt("direct");
            com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
                    conn);
            operation.setLinkDirect(direct);
            operation.setLink(this.updateLink);
            operation.caleRdlinesForRdlinkDirect(result);
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
                // 新增详细车道
                if (this.updateLink.getKind() <= KIND && kind > KIND) {
                    this.setKindFlag(1);
                    operation.setKindFlag(1);
                    operation.caleRdLinesForRdLinkKind(result);
                }
                // 删除详细车道
                if (this.updateLink.getKind() > KIND && kind <= KIND) {
                    this.setKindFlag(2);
                    operation.setKindFlag(2);
                    operation.caleRdLinesForRdLinkKind(result);
                }
            }
        }
    }

    /***
     * link车道数变更
     * 
     * @throws Exception
     */

    private void refRdLaneForRdLinkLaneNum(Result result) throws Exception {
        JSONObject content = this.command.getUpdateContent();
        int laneNum = 0;
        int laneLeft = 0;
        int laneRight = 0;
        if (content.containsKey("laneNum") || content.containsKey("laneLeft") || content.containsKey("laneRight")) {
            if (content.containsKey("laneNum")) {
                laneNum = content.getInt("laneNum");
            }
            if (content.containsKey("laneLeft")) {
                laneLeft = content.getInt("laneLeft");
            }
            if (content.containsKey("laneRight")) {
                laneRight = content.getInt("laneNum");
            }
            com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
                    conn);
            operation.setLink(this.updateLink);
            operation.setLaneNum(laneNum);
            operation.setLaneLeft(laneLeft);
            operation.setLaneRight(laneRight);
            operation.caleRdLinesForLaneNum(result);
        }
    }

    /***
     * 交叉口内link属性变更
     * 
     * @throws Exception
     */
    private void refRdLaneForRdLinkCross(Result result) throws Exception {
        if (this.getForm() != null) {
            if (this.getForm().getFormOfWay() == 50) {
                com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
                        conn);
                operation.setFormCrossFlag(this.getFormCrossFlag());
                operation.setLink(this.updateLink);
                operation.caleRdLinesForRdLinkCross(result);

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
            // operation.setFormCrossFlag();
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
            // operation.setFlag(flag);
            operation.setLink(this.updateLink);
            operation.refRdLaneForRdlinkLimit(result);
        }

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