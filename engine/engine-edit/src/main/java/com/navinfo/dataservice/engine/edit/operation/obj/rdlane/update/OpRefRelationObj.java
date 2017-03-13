package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;

/**
 * 详细车道被动维护
 *
 * @author Zhang Xiaolong
 * @ClassName: OpRefRelationObj
 * @date 2016年12月20日 上午11:14:02
 */
public class OpRefRelationObj {

    // 新增link优先级号码
    public static final int LINK_CREATE_11 = 11;

    // link种别优先级号码
    public static final int LINK_KIND_12 = 12;

    // link方向优先级号码
    public static final int LINK_DIRECT_13 = 13;

    // 收费站优先级号码
    public static final int LINK_TOLLGATE_21 = 21;

    // 车信进入线优先级号码
    public static final int LINK_CONNEXITY_22 = 22;

    // 交叉口内link属性优先级号码
    public static final int LINK_CROSS_FORM_23 = 23;

    // 分歧模式图优先级号码
    public static final int BRANCH_PATTERN_CODE_24 = 24;

    // link车道数优先级号码
    public static final int LINK_LANE_NUM_25 = 25;

    // link车辆类型优先级号码
    public static final int LINK_LANE_VEHICLE_31 = 31;

    // link形态优先级号码
    public static final int LINK_FORM_32 = 32;

    /**
     * levelMap:将level按等级划分为1、2、3开头，越小代表优先级越高
     * 11：link新增或者删除；12：link种别变更；13：link方向变更
     * 21：收费站；22：车信；23：交叉口内link属性变更；24：高速分歧模式图；25：link车道数
     * 31：link车辆类型；32：link形态属性
     * <p>
     * key:linkPid； Map<Integer,List<IRow>>
     * linkpid对应的所有需要修改详细车道的要素,其中的key代表级别，value代表对应的要素对象
     */
    private Map<Integer, Map<Integer, List<IRow>>> updateLevelMap = new HashMap<>();

    private Map<Integer, Map<Integer, List<IRow>>> delLevelMap = new HashMap<>();

    /**
     * 更新的限制信息集合
     */
    private Map<Integer, Map<Integer, List<IRow>>> updateLimitMap = new HashMap<>();

    /**
     * 删除的限制信息集合
     */
    private Map<Integer, Map<Integer, List<IRow>>> delLimitMap = new HashMap<>();

    private Connection conn = null;

    private AbstractSelector abstractSelector;

    private RdLaneSelector rdLaneSelector;

    private int laneDirect = 1;

    private Result result;

    public OpRefRelationObj(Connection conn, Result result) {
        this.conn = conn;

        this.result = result;

        if (abstractSelector == null) {
            abstractSelector = new AbstractSelector(conn);
        }
        if (rdLaneSelector == null) {
            rdLaneSelector = new RdLaneSelector(conn);
        }
    }

    public RdLaneSelector getRdlaneSelector() {
        if (rdLaneSelector == null) {
            rdLaneSelector = new RdLaneSelector(conn);
        }

        return rdLaneSelector;
    }

    /**
     * 详细车道被动维护
     *
     * @param command
     * @throws Exception
     */
    public void updateRdLane(ObjType objType) throws Exception {
        switch (objType) {
            case RDLINK:
            case RDTOLLGATE:
            case RDLANECONNEXITY:
            case RDBRANCH:
            case RDBRANCHDETAIL:
                // 将影响详细车道要素进行排序
                calLevelMapData(result);
                // 按照level优先级排序后进行详细车道的维护
                updateRdLaneByLevel();
                // 维护详细车道的限制信息
                updateRdLaneCondition();
                break;
            default:
                return;
        }
    }

    /**
     * @param result
     * @throws Exception
     */
    private void calLevelMapData(Result result) throws Exception {

        // 新增记录
        List<IRow> addObjList = result.getAddObjects();

        for (IRow row : addObjList) {
            ObjType objType = row.objType();

            switch (objType) {
                // 新增收费站
                case RDTOLLGATE:
                    RdTollgate rdTollgate = (RdTollgate) row;
                    handleRowList(LINK_TOLLGATE_21, rdTollgate.getInLinkPid(), row, updateLevelMap);
                    handleRowList(LINK_TOLLGATE_21, rdTollgate.getOutLinkPid(), row, updateLevelMap);
                    break;
                // 新增车信
                case RDLANECONNEXITY:
                    RdLaneConnexity connexity = (RdLaneConnexity) row;
                    handleRowList(LINK_CONNEXITY_22, connexity.getInLinkPid(), row, updateLevelMap);
                    break;
                // 修改link形态，新增其他形态（公交车道或者步行街）
                case RDLINKFORM:
                    RdLinkForm form = (RdLinkForm) row;
                    if (form.getFormOfWay() == 20 || form.getFormOfWay() == 22) {
                        handleRowList(LINK_FORM_32, form.getLinkPid(), form, updateLimitMap);
                    }
                    break;
                // link上新增限制信息
                case RDLINKLIMIT:
                    RdLinkLimit limit = (RdLinkLimit) row;
                    if (limit.getType() == 2) {
                        handleRowList(LINK_LANE_VEHICLE_31, limit.getLinkPid(), limit, updateLimitMap);
                    }
                    break;
                default:
                    break;
            }
        }

        // 更新记录
        List<IRow> updateObjList = result.getUpdateObjects();

        for (IRow row : updateObjList) {
            ObjType objType = row.objType();

            switch (objType) {
                // 更新link种别和方向
                case RDLINK:
                    if (row.changedFields().containsKey("kind")) {
                        RdLink link = (RdLink) row;

                        int sourceKind = link.getKind();

                        int kind = (int) row.changedFields().get("kind");

                        if (sourceKind <= 7 && kind > 7 || sourceKind > 7 && kind <= 7) {
                            handleRowList(LINK_KIND_12, row.parentPKValue(), row, updateLevelMap);
                        }
                        break;
                    }
                    if (row.changedFields().containsKey("direct")) {
                        handleRowList(LINK_DIRECT_13, row.parentPKValue(), row, updateLevelMap);
                        break;
                    }
                    // link车道数变更
                    if (row.changedFields().containsKey("laneNum") || row.changedFields().containsKey("laneLeft") ||
                            row.changedFields().containsKey("laneRight")) {
                        handleRowList(LINK_LANE_NUM_25, row.parentPKValue(), row, updateLevelMap);
                    }
                    break;
                // 交叉口内link属性
                case RDLINKFORM:
                    RdLinkForm linkForm = (RdLinkForm) row;
                    int sourceFormOfWay = linkForm.getFormOfWay();
                    if (row.changedFields().containsKey("formOfWay")) {
                        int formOfWay = (int) row.changedFields().get("formOfWay");

                        if ((sourceFormOfWay != 50 && formOfWay == 50) || (sourceFormOfWay == 50 && formOfWay != 50)) {
                            handleRowList(LINK_CROSS_FORM_23, row.parentPKValue(), row, updateLevelMap);
                        }
                        // 修改link形态，新增其他形态（公交车道或者步行街）
                        if (sourceFormOfWay == 20 || sourceFormOfWay == 22) {
                            if (formOfWay != 20 && formOfWay != 22) {
                                handleRowList(LINK_FORM_32, row.parentPKValue(), row, delLimitMap);
                            }
                        } else if (formOfWay == 20 || formOfWay == 22) {
                            handleRowList(LINK_FORM_32, row.parentPKValue(), row, updateLimitMap);
                        }
                    }
                    break;
                // 修改收费站
                case RDTOLLGATE:
                    RdTollgate rdTollgate = (RdTollgate) row;
                    if (row.changedFields().containsKey("passageNum")) {
                        handleRowList(LINK_TOLLGATE_21, rdTollgate.getInLinkPid(), row, updateLevelMap);
                        // handleRowList(LINK_TOLLGATE_21,
                        // rdTollgate.getOutLinkPid(), row, updateLevelMap);
                    }
                    break;
                // 修改车信
                case RDLANECONNEXITY:
                    RdLaneConnexity connexity = (RdLaneConnexity) row;
                    if (row.changedFields().containsKey("laneNum") || row.changedFields().containsKey("laneInfo")) {
                        handleRowList(LINK_CONNEXITY_22, connexity.getInLinkPid(), row, updateLevelMap);
                    }
                    break;
                // 修改高速分歧模式图
                case RDBRANCHDETAIL:
                    RdBranchDetail detail = (RdBranchDetail) row;
                    abstractSelector.setCls(RdBranch.class);
                    RdBranch branch = (RdBranch) abstractSelector.loadById(detail.getBranchPid(), true, true);
                    // 判断是高速分歧并且模式图变更
                    if (detail.getBranchType() == 0 && row.changedFields().containsKey("patternCode")) {
                        String patternCode = (String) row.changedFields().get("patternCode");
                        if (patternCode.equals("80261009") || patternCode.equals("80271009") || patternCode.equals
                                ("80361009")) {
                            return;
                        }
                        branch.getDetails().add(detail);
                        handleRowList(BRANCH_PATTERN_CODE_24, branch.getInLinkPid(), branch, updateLevelMap);
                        handleRowList(BRANCH_PATTERN_CODE_24, branch.getOutLinkPid(), branch, updateLevelMap);
                    }
                    break;
                // link上更新限制信息：1.由车辆类型限制修改为非车辆类型限制2.由非车辆类型限制修改为车辆类型限制
                case RDLINKLIMIT:
                    RdLinkLimit limit = (RdLinkLimit) row;

                    int type = limit.getType();

                    if (limit.changedFields().containsKey("type")) {
                        int newType = (int) limit.changedFields().get("type");
                        // 1.由车辆类型限制修改为非车辆类型限制
                        if (type == 2 && newType != 2) {
                            handleRowList(LINK_LANE_VEHICLE_31, limit.getLinkPid(), limit, delLimitMap);
                        }
                        // 2.由非车辆类型限制修改为车辆类型限制
                        if (type != 2 && newType == 2) {
                            handleRowList(LINK_LANE_VEHICLE_31, limit.getLinkPid(), limit, updateLimitMap);
                        }
                    }
                    // link的限制信息为车辆限制，修改限制的时间和限制方向也需要维护link上详细车道的限制信息
                    else if (type == 2) {
                        RdLinkLimit linkLimit = new RdLinkLimit();
                        linkLimit.copy(limit);
                        // 更新标志
                        boolean updateFlag = false;
                        // 修改车辆类型、限制类型、限制方向也需要维护详细车道的限制信息
                        if (limit.changedFields().containsKey("vehicle")) {
                            linkLimit.setVehicle(Long.valueOf(limit.changedFields().get("vehicle").toString()));
                            updateFlag = true;
                        }
                        if (limit.changedFields().containsKey("timeDmain")) {
                            linkLimit.setTimeDomain((String) limit.changedFields().get("timeDmain"));
                            updateFlag = true;
                        }
                        if (limit.changedFields().containsKey("limitDir")) {
                            linkLimit.setLimitDir((int) limit.changedFields().get("limitDir"));
                            updateFlag = true;
                        }
                        if (updateFlag) {
                            handleRowList(LINK_LANE_VEHICLE_31, linkLimit.getLinkPid(), linkLimit, updateLimitMap);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        // 根据删除对象计算维护的详细车道
        List<IRow> deleteRows = result.getDelObjects();

        for (IRow row : deleteRows) {
            ObjType objType = row.objType();

            switch (objType) {
                // 删除收费站
                case RDTOLLGATE:
                    RdTollgate rdTollgate = (RdTollgate) row;
                    handleRowList(LINK_TOLLGATE_21, rdTollgate.getInLinkPid(), row, delLevelMap);
                    handleRowList(LINK_TOLLGATE_21, rdTollgate.getOutLinkPid(), row, delLevelMap);
                    break;
                // 删除车信
                case RDLANECONNEXITY:
                    RdLaneConnexity connexity = (RdLaneConnexity) row;
                    handleRowList(LINK_CONNEXITY_22, connexity.getInLinkPid(), row, delLevelMap);
                    break;
                // 删除link形态（公交车道或者步行街）
                case RDLINKFORM:
                    RdLinkForm form = (RdLinkForm) row;
                    if (form.getFormOfWay() == 20 || form.getFormOfWay() == 22) {
                        handleRowList(LINK_FORM_32, form.getLinkPid(), form, delLimitMap);
                    }
                    break;
                // 删除高速分歧，分歧上包含影响详细车道的模式图
                case RDBRANCH:
                    RdBranch branch = (RdBranch) row;
                    RdBranchDetail detail = null;
                    if (CollectionUtils.isNotEmpty(branch.getDetails())) {
                        detail = (RdBranchDetail) branch.getDetails().get(0);
                        if (detail.getBranchType() == 0) {
                            String patternCode = detail.getPatternCode();
                            // 必须是高速分歧和3个例外模式图号的删除才走通道进行更新
                            if (patternCode == null || detail.getBranchType() != 0 || patternCode.equals("80261009")
                                    || patternCode.equals("80271009") || patternCode.equals("80361009")) {
                                return;
                            }

                            handleRowList(BRANCH_PATTERN_CODE_24, branch.getInLinkPid(), branch, delLevelMap);
                            handleRowList(BRANCH_PATTERN_CODE_24, branch.getOutLinkPid(), branch, delLevelMap);
                        }
                    }
                    break;
                // link上删除车辆类型限制信息
                case RDLINKLIMIT:
                    RdLinkLimit limit = (RdLinkLimit) row;
                    if (limit.getType() == 2) {
                        handleRowList(LINK_LANE_VEHICLE_31, limit.getLinkPid(), limit, delLimitMap);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 对所有link的详细车道影响要素进行优先级排序 车信的进入线不可能是交叉口内link
     * 收费站进入link挂接的退出link只能有一条，分歧要求进入线挂接的link至少要3条，所以有收费站的link上不会有分歧。
     * 有收费站的link上如果有车信，那么link不能为交叉口内link
     *
     * @throws Exception
     */
    private void updateRdLaneByLevel() throws Exception {

        // 删除的要素
        for (Map.Entry<Integer, Map<Integer, List<IRow>>> entry : delLevelMap.entrySet()) {
            int linkPid = entry.getKey();

            Map<Integer, List<IRow>> levelMap = entry.getValue();

            levelMap = sortMapByKey(levelMap);

            for (Map.Entry<Integer, List<IRow>> levelEntry : levelMap.entrySet()) {
                int level = levelEntry.getKey();

                List<IRow> rowList = levelEntry.getValue();

                List<Integer> pidList = getPidListFromRowList(rowList);

                // 获取除了删除要素后该link上其他影响详细车道维护的要素。key：要素的优先级号码 value:影响的要素的pid
                Map<Integer, List<Integer>> laneInfoList = rdLaneSelector.getLaneInfoByLinkPid(linkPid, level, pidList);

                updateByLevel(level, laneInfoList, rowList.get(0));

                break;
            }
        }

        // 新增或者修改的要素
        for (Map.Entry<Integer, Map<Integer, List<IRow>>> entry : updateLevelMap.entrySet()) {

            int linkPid = entry.getKey();

            Map<Integer, List<IRow>> levelMap = entry.getValue();

            for (Map.Entry<Integer, List<IRow>> levelEntry : levelMap.entrySet()) {
                int level = levelEntry.getKey();

                List<IRow> rowList = levelEntry.getValue();

                List<Integer> pidList = getPidListFromRowList(rowList);

                Map<Integer, List<Integer>> laneInfoList = rdLaneSelector.getLaneInfoByLinkPid(linkPid, level, null);

                // 对应新增的要素还没入库需要手动添加进laneInfoList
                List<Integer> levelList = laneInfoList.get(level);

                if (levelList == null) {
                    levelList = new ArrayList<>();
                }
                levelList.addAll(pidList);

                laneInfoList.put(level, levelList);

                // 维护详细车道的增删该
                updateByLevel(level, laneInfoList, rowList.get(0));

                break;
            }
        }
    }

    /**
     * 根据level和link上存在的影响详细车道的要素对象判断
     *
     * @param level
     * @param laneInfoList
     * @throws Exception
     */
    public void updateByLevel(int level, Map<Integer, List<Integer>> laneInfoList, IRow row) throws Exception {
        // 递归调用超出范围的直接返回
        if (level > LINK_LANE_NUM_25) {
            return;
        }
        // 如果link上存在优先级高的要素，则不需要重新维护详细车道信息
        if (laneInfoList.get(level - 1) != null) {
            return;
        }
        List<Integer> selfLevelInfoPidList = laneInfoList.get(level);
        if (CollectionUtils.isNotEmpty(selfLevelInfoPidList)) {
            // 是否继续按照下一个level进行详细车道维护
            boolean runNextLevelFlag = updateRdLane(level, selfLevelInfoPidList.get(0), row);
            if (runNextLevelFlag) {
                // 该level等级维护规则已经失效，需要删除该leve等级防止递归判断上级level直接return
                laneInfoList.remove(Integer.valueOf(level));
                updateByLevel(level + 1, laneInfoList, null);
            }
        } else {
            updateByLevel(level + 1, laneInfoList, null);
        }
    }

    /**
     * 根据level维护详细车道
     *
     * @param level 优先级level
     * @param pid   要素的pid
     * @param row   要素对象
     * @throws Exception
     */
    private boolean updateRdLane(int level, int pid, IRow row) throws Exception {
        // flag代表是否要继续按照其他原则进行维护
        boolean runNextLevelFlag = false;
        switch (level) {
            case LINK_KIND_12:
                updateByRdLinkKind((RdLink) row);
                break;
            case LINK_DIRECT_13:
                updateByRdLinkDirect((RdLink) row);
                break;
            case LINK_TOLLGATE_21:
                runNextLevelFlag = updateByTollgate(pid, (RdTollgate) row);
                break;
            case LINK_CONNEXITY_22:
                updateByRdLaneConnexity(pid, (RdLaneConnexity) row);
                break;
            case LINK_CROSS_FORM_23:
                updateByRdCrossLink(pid, (RdLinkForm) row);
                break;
            case BRANCH_PATTERN_CODE_24:
                runNextLevelFlag = updateByRdBranchPattern(pid, (RdBranch) row);
                break;
            case LINK_LANE_NUM_25:
                updateByRdLinkLaneNum(pid, (RdLink) row);
                break;
            default:
                break;
        }
        return runNextLevelFlag;
    }

    /**
     * 维护详细车道的限制信息
     *
     * @throws Exception
     */
    private void updateRdLaneCondition() throws Exception {
        // 删除的要素
        for (Map.Entry<Integer, Map<Integer, List<IRow>>> entry : delLimitMap.entrySet()) {
            int linkPid = entry.getKey();

            Map<Integer, List<IRow>> levelMap = entry.getValue();

            levelMap = sortMapByKey(levelMap);

            for (Map.Entry<Integer, List<IRow>> levelEntry : levelMap.entrySet()) {
                int level = levelEntry.getKey();
                // 删除车辆类型后需要判断link的形态属性是否是公交车道或者步行街，如果是要按照leve32维护，不是的直接删除详细车道限制信息
                if (level == LINK_LANE_VEHICLE_31) {
                    RdLinkLimit limit = (RdLinkLimit) levelEntry.getValue().get(0);
                    updateByRdLinkVehicle(linkPid, limit, null);
                    updateByRdLinkForm(linkPid, null, null);
                } else if (level == LINK_FORM_32) {
                    // 公交车形态和步行街形态不会共存，只会存在一个
                    RdLinkForm form = (RdLinkForm) levelEntry.getValue().get(0);
                    updateByRdLinkForm(linkPid, form, null);
                }
                break;
            }
        }
        // 更新的要素
        for (Map.Entry<Integer, Map<Integer, List<IRow>>> entry : updateLimitMap.entrySet()) {
            int linkPid = entry.getKey();

            Map<Integer, List<IRow>> levelMap = entry.getValue();

            // 按照level排序，level小的在前
            levelMap = sortMapByKey(levelMap);

            for (Map.Entry<Integer, List<IRow>> levelEntry : levelMap.entrySet()) {
                int level = levelEntry.getKey();
                // 更新车辆类型
                if (level == LINK_LANE_VEHICLE_31) {
                    RdLinkLimit limit = (RdLinkLimit) levelEntry.getValue().get(0);
                    updateByRdLinkVehicle(linkPid, limit, null);
                    break;
                } else if (level == LINK_FORM_32) {
                    // 公交车形态和步行街形态不会共存，只会存在一个
                    RdLinkForm form = (RdLinkForm) levelEntry.getValue().get(0);
                    updateByRdLinkForm(linkPid, form, null);
                    break;
                }
                break;
            }
        }
        // 处理新增的详细车道的限制信息
        for (IRow row : result.getAddObjects()) {
            ObjType objType = row.objType();

            if (objType == ObjType.RDLANE) {
                // 新增的详细车道维护限制信息
                RdLane rdLane = (RdLane) row;

                int linkPid = rdLane.getLinkPid();

                Map<Integer, List<IRow>> limitMap = updateLimitMap.get(linkPid);

                sortMapByKey(limitMap);

                RdLinkLimit limit = null;

                RdLinkForm form = null;

                //获取修改或者新增的level3要素，这个要素还没入库，需要从内存中获取
                if (limitMap != null) {
                    List<IRow> limitRowsList = limitMap.get(LINK_LANE_VEHICLE_31);

                    if (CollectionUtils.isNotEmpty(limitRowsList)) {
                        limit = (RdLinkLimit) limitRowsList.get(0);
                    } else {
                        List<IRow> formRowsList = limitMap.get(LINK_FORM_32);

                        form = (RdLinkForm) formRowsList.get(0);
                    }
                }

                boolean flag = updateByRdLinkVehicle(rdLane.getLinkPid(), limit, null);

                if (flag) {
                    updateByRdLinkForm(rdLane.getLinkPid(), form, rdLane);
                }
            }
        }
    }

    private boolean updateByTollgate(int pid, RdTollgate rdTollgate) throws Exception {
        boolean runNextLevelFlag = false;

        RdTollgate tollgate = rdTollgate;

        if (rdTollgate == null) {
            abstractSelector.setCls(RdTollgate.class);

            tollgate = (RdTollgate) abstractSelector.loadAllById(pid, true, true);
        }

        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);

        operation.setTollgate(tollgate);

        operation.setLaneDirect(laneDirect);

        int passNum = tollgate.getPassageNum();

        if (tollgate.changedFields.containsKey("passageNum")) {
            passNum = (int) tollgate.changedFields.get("passageNum");
        }

        if (passNum > 0) {
            operation.setPassageNum(passNum);

            operation.refRdLaneForTollgate(result);
        } else if (rdTollgate == null) {
            runNextLevelFlag = true;
        }

        return runNextLevelFlag;
    }

    private void updateByRdLaneConnexity(int pid, RdLaneConnexity rdLaneConnexity) throws Exception {
        RdLaneConnexity connexity = rdLaneConnexity;

        if (rdLaneConnexity == null) {
            abstractSelector.setCls(RdLaneConnexity.class);

            connexity = (RdLaneConnexity) abstractSelector.loadAllById(pid, true, true);
        }

        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);

        String laneInfos = null;

        //修改车信车道信息
        if (connexity.changedFields().containsKey("laneInfo")) {
            laneInfos = connexity.changedFields().get("laneInfo").toString();
        } else {
            //新增场景
            laneInfos = connexity.getLaneInfo();
        }

        List<String> laneInfoList = new ArrayList<>();

        for (String info : laneInfos.split(",")) {
            //去除公交车道车道信息数据
            info = StringUtils.removeSpeLetter(info);
            if (StringUtils.isEmpty(info)) {
                continue;
            }
            if (info.contains("[")) {
                // 理论值带[]
                laneInfoList.add(info.substring(1, 2));
            } else {
                // 实际值不带
                laneInfoList.add(info);
            }
        }

        operation.setLanInfos(laneInfoList);

        operation.setConnexity(connexity);

        operation.refRdLaneForRdLaneconnexity(result, laneDirect);
    }

    private void updateByRdCrossLink(int pid, RdLinkForm form) throws Exception {
        // link的交叉口形态状态：新增是1，删除是2
        int formCrossFlag = 0;
        // form为null说明是通过递归level进行维护，则按新增处理
        if (form == null) {
            formCrossFlag = 1;
        } else if (form.status() == ObjStatus.UPDATE) {
            int sourceFormOfWay = form.getFormOfWay();
            // 判断是否是删除了交叉口形态
            if (form.changedFields().containsKey("formOfWay")) {
                int formOfWay = (int) form.changedFields().get("formOfWay");
                if (sourceFormOfWay == 50 && formOfWay != 50) {
                    formCrossFlag = 2;
                } else if (sourceFormOfWay != 50 && formOfWay == 50) {
                    formCrossFlag = 1;
                }
            }
        } else if (form.status() == ObjStatus.DELETE) {
            formCrossFlag = 2;
        }
        if (formCrossFlag != 0) {
            abstractSelector.setCls(RdLink.class);

            RdLink link = (RdLink) abstractSelector.loadAllById(pid, true, true);

            com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com
                    .navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);

            operation.setLink(link);

            operation.setFormCrossFlag(formCrossFlag);

            operation.caleRdLinesForRdLinkCross(result);
        }
    }

    private boolean updateByRdBranchPattern(int pid, RdBranch rdBranch) throws Exception {
        // 是否需要走下一个优先级的维护
        boolean flag = false;

        RdBranch branch = rdBranch;

        if (rdBranch == null) {
            RdBranchSelector selector = new RdBranchSelector(conn);

            branch = (RdBranch) selector.loadByDetailId(pid, 0, null, true);
        }

        com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdlane.update.Operation(conn);

        RdBranchDetail detail = (RdBranchDetail) branch.getDetails().get(0);

        if (detail.changedFields().containsKey("patternCode")) {
            Map<ObjStatus, List<RdLane>> rdLaneMap = operation.autoHandleByRdBranch(branch, String.valueOf(detail
                    .changedFields().get("patternCode")), result);
            if (rdLaneMap.isEmpty()) {
                flag = true;
            }
        }
        // 通过降级需要根据高速分歧模式图维护详细车道
        else if (rdBranch == null) {
            String patternCode = detail.getPatternCode();

            if (patternCode == null || patternCode.equals("80261009") || patternCode.equals("80271009") ||
                    patternCode.equals("80361009")) {
                flag = true;
            } else {
                Map<ObjStatus, List<RdLane>> rdLaneMap = operation.autoHandleByRdBranch(branch, patternCode, result);
                if (rdLaneMap.isEmpty()) {
                    flag = true;
                }
            }
        } else {
            flag = true;
        }

        return flag;
    }

    private void updateByRdLinkLaneNum(int pid, RdLink rdLink) throws Exception {
        RdLink link = rdLink;

        if (rdLink == null) {
            abstractSelector.setCls(RdLink.class);

            link = (RdLink) abstractSelector.loadAllById(pid, true, true);
        }

        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);

        int laneNum = link.getLaneNum();

        int laneRight = link.getLaneRight();

        int laneLeft = link.getLaneLeft();

        if (link.changedFields().containsKey("laneNum")) {
            laneNum = (int) link.changedFields().get("laneNum");
        } else if (link.changedFields().containsKey("laneRight")) {
            laneRight = (int) link.changedFields().get("laneRight");
        } else if (link.changedFields().containsKey("laneLeft")) {
            laneLeft = (int) link.changedFields().get("laneLeft");
        }

        operation.setLink(link);

        operation.setLaneNum(laneNum);

        operation.setLaneLeft(laneLeft);

        operation.setLaneLeft(laneRight);

        operation.setLaneDirect(laneDirect);

        operation.caleRdLinesForLaneNum(result);
    }

    private void updateByRdLinkKind(RdLink link) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);
        // 根据原link的种别判断是否新增还是删除详细车道
        if (link.getKind() <= 8) {
            // 删除
            operation.setKindFlag(2);
        } else {
            // 新增
            operation.setKindFlag(1);
        }

        operation.setLink(link);

        operation.caleRdLinesForRdLinkKind(result);
    }

    private void updateByRdLinkDirect(RdLink link) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);
        int direct = (int) link.changedFields().get("direct");

        operation.setLinkDirect(direct);

        operation.setLink(link);

        operation.caleRdlinesForRdlinkDirect(result);
    }

    private void updateByRdLinkForm(int pid, RdLinkForm linkForm, RdLane rdLane) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);
        int formOfWay = -1;
        // link车辆类型限制没有维护详细车道限制，走形态维护
        if (linkForm == null) {
            List<IRow> forms = abstractSelector.loadRowsByClassParentId(RdLinkForm.class, pid, true, null, null);

            for (IRow irow : forms) {
                RdLinkForm form = (RdLinkForm) irow;
                if (form.getFormOfWay() == 20 || form.getFormOfWay() == 22) {
                    formOfWay = form.getFormOfWay();
                    operation.setFormCrossFlag(1);
                    break;
                }
            }
        } else {
            // 删除form
            if (linkForm != null && linkForm.status() == ObjStatus.DELETE) {
                formOfWay = linkForm.getFormOfWay();
                operation.setFormCrossFlag(3);
            } else if (linkForm.status() == ObjStatus.INSERT) {
                formOfWay = linkForm.getFormOfWay();
                // 1代表新增
                operation.setFormCrossFlag(1);
            } else {
                // 2代表修改
                formOfWay = (int) linkForm.changedFields().get("formOfWay");
                int sourceFormOfWay = linkForm.getFormOfWay();
                // 修改link形态，新增其他形态（公交车道或者步行街）
                if (sourceFormOfWay == 20 || sourceFormOfWay == 22) {
                    if (formOfWay != 20 && formOfWay != 22) {
                        operation.setFormCrossFlag(3);
                    }
                } else if (formOfWay == 20 || formOfWay == 22) {
                    operation.setFormCrossFlag(2);
                }
            }
        }

        if (formOfWay != -1) {
            abstractSelector.setCls(RdLink.class);

            RdLink link = (RdLink) abstractSelector.loadAllById(pid, true, true);

            operation.setLink(link);

            List<RdLane> lanes = new ArrayList<>();

            if (rdLane != null) {
                lanes.add(rdLane);
            }

            operation.refRdLaneForRdlinkForm(result, formOfWay, lanes);
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
    private boolean updateByRdLinkVehicle(int linkPid, RdLinkLimit limit, List<RdLane> lanes) throws Exception {
        // 是否继续执行下一个level标志；默认不执行
        boolean nextFlag = false;
        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(conn);
        int flag = 0;
        if (limit == null) {
            List<IRow> limits = abstractSelector.loadRowsByClassParentId(RdLinkLimit.class, linkPid, false, null, null);
            for (IRow row : limits) {
                RdLinkLimit tmpLimit = (RdLinkLimit) row;
                if (tmpLimit.getType() == 2) {
                    limit = tmpLimit;
                    flag = 1;
                    break;
                }
            }
        } else if (limit.status() == ObjStatus.INSERT) {
            // flag = 1为新增
            flag = 1;
        } else if (limit.status() == ObjStatus.UPDATE) {
            // flag = 1为修改
            flag = 2;
        } else if (limit.status() == ObjStatus.DELETE) {
            // flag = 3为删除
            flag = 3;
        }
        if (limit != null) {
            if (CollectionUtils.isEmpty(lanes)) {
                lanes = new RdLaneSelector(conn).loadByLink(linkPid, limit.getLimitDir(), true);
            }
            if (limit.getType() == 2 && (limit.getLimitDir() == 1 || limit.getLimitDir() == 2 || limit.getLimitDir() == 3)) {
                operation.refRdLaneForRdlinkLimit(result, lanes, flag);
            }
        } else {
            // 需要执行下一个levle
            nextFlag = true;
        }
        return nextFlag;
    }

    /**
     * 给levelMap填值
     *
     * @param levelKey
     * @param row
     */
    private void handleRowList(int levelKey, int linkPid, IRow row, Map<Integer, Map<Integer, List<IRow>>> map) {
        Map<Integer, List<IRow>> levelMap = map.get(linkPid);
        if (levelMap == null) {
            levelMap = new HashMap<>();

            List<IRow> rowList = new ArrayList<>();

            rowList.add(row);

            levelMap.put(levelKey, rowList);

            map.put(linkPid, levelMap);
        } else {
            List<IRow> rowList = levelMap.get(levelKey);

            if (rowList == null) {
                rowList = new ArrayList<>();

                levelMap.put(levelKey, rowList);
            }
            rowList.add(row);
        }
    }

    /**
     * 对Map按key进行排序
     *
     * @param map
     * @return
     */
    public static Map<Integer, List<IRow>> sortMapByKey(Map<Integer, List<IRow>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        // 排序map:key值小的排在前面
        Map<Integer, List<IRow>> sortMap = new TreeMap<Integer, List<IRow>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        sortMap.putAll(map);

        return sortMap;
    }

    private List<Integer> getPidListFromRowList(List<IRow> rowList) {
        List<Integer> pidList = new ArrayList<>();

        for (IRow row : rowList) {
            pidList.add(row.parentPKValue());
        }

        return pidList;
    }

    public int getLaneDirect() {
        return laneDirect;
    }

    public void setLaneDirect(int laneDirect) {
        this.laneDirect = laneDirect;
    }
}
