package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

import java.util.*;

/**
 * 查询条件：
 * (1)不存在官方原始中文名称记录或官方原始中文名称name字段为空；
 * (2)新增POI或修改官方标准中文名称；
 * (3)新增POI且存在别名，或修改别名；
 * 批处理：
 * 满足条件(1)时，批处理：
 * 根据官方标准中文名称批处理赋值官方原始中文名称，如果官方原始中文名称记录不存在，则新增一条记录，NAME_ID申请赋值，NAME_TYPE=2，NAME_CLASS=1，LANG_CODE=CHI或CHT，NAME赋值官方标准中文名称；如果存在记录但NAME字段为空，则直接将官方标准中文名称赋值赋值给官方原始中文名称NAME字段；并官方原始中文名称转拼音；
 * 满足条件(2)时，批处理：
 * 官方标准中文名称转拼音；
 * 满足条件(3)时，批处理：
 * 标准中文别名转拼音；
 * 以上生成批处理履历；
 * Created by ly on 2017/7/9.
 */
public class FMBATD20005 extends BasicBatchRule {

    private Map<Long, Long> pidAdminId;

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

        Set<Long> pidList = new HashSet<>();

        for (BasicObj obj : batchDataList) {

            pidList.add(obj.objPid());
        }

        pidAdminId = IxPoiSelector.getAdminIdByPids(getBatchRuleCommand().getConn(), pidList);
    }

    @Override
    public void runBatch(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        IxPoi poi = (IxPoi) obj.getMainrow();

        if (poi.getHisOpType().equals(OperationType.DELETE)) {

            return;
        }

        //需要转拼音的name
        List<IxPoiName> convertNames = new ArrayList<>();

        getPyConvertName(poiObj, poi, convertNames);

        handOriginName(poiObj, convertNames);

        setPy(convertNames);
    }

    /**
     * 获取需要转拼音的name
     * @param poiObj
     * @param poi
     * @param convertNames
     */
    private void getPyConvertName(IxPoiObj poiObj, IxPoi poi, List<IxPoiName> convertNames) {

        HashSet<String> langCodeCH = new HashSet<String>() {{
            add("CHT");
            add("CHI");
        }};

        List<IxPoiName> names = poiObj.getIxPoiNames();

        if (names == null || names.size() == 0) {

            return;
        }

        for (IxPoiName name : names) {

            //不是标准化或不是中文continue
            if (name.getNameType() != 1 || !langCodeCH.contains(name.getLangCode())) {

                continue;
            }

            //不是官方名且不是别名
            if (name.getNameClass() != 1 && name.getNameClass() != 3) {

                continue;
            }

            //poi新增、修改或新增名称
            if (poi.getHisOpType().equals(OperationType.INSERT)
                    || (name.getHisOpType().equals(OperationType.UPDATE)
                    || name.getHisOpType().equals(OperationType.INSERT))) {

                convertNames.add(name);
            }

        }
    }

    /**
     * 处理官方原始中文名称
     * @param poiObj
     * @param convertNames
     * @throws Exception
     */
    private void handOriginName(IxPoiObj poiObj, List<IxPoiName> convertNames) throws Exception {

        IxPoiName officeOriginCHName = poiObj.getOfficeOriginCHName();

        IxPoiName officeStandardCHName = poiObj.getOfficeStandardCHName();

        if (officeOriginCHName == null) {

            officeOriginCHName = poiObj.createIxPoiName();

            officeOriginCHName.setNameClass(1);

            officeOriginCHName.setNameType(2);

            officeOriginCHName.setNameGroupid(poiObj.getMaxGroupIdFromNames() + 1);

            officeOriginCHName.setLangCode("CHI");

            if (officeStandardCHName != null) {

                officeOriginCHName.setLangCode(officeStandardCHName.getLangCode());
            }
        }

        if (officeOriginCHName.getName() == null || officeOriginCHName.getName().isEmpty()) {

            if (officeStandardCHName != null) {

                officeOriginCHName.setName(officeStandardCHName.getName());
            }

            convertNames.add(officeOriginCHName);
        }
    }


    /**
     * 转拼音
     * @param convertNames
     * @throws Exception
     */
    private void setPy(List<IxPoiName> convertNames) throws Exception {

        if (convertNames.size() < 1) {

            return;
        }

        long poiPid = convertNames.get(0).getPoiPid();

        if (pidAdminId == null || !pidAdminId.containsKey(poiPid)) {

            return;
        }

        MetadataApi apiService = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

        String adminCode = pidAdminId.get(poiPid).toString();

        for (IxPoiName name : convertNames) {

            if (name.getName() != null && !name.getName().isEmpty()) {

                String namePy = apiService.pyConvert(name.getName(), adminCode, null);

                name.setNamePhonetic(namePy);
            }
        }
    }

}
