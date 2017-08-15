package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlag;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlagMethod;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 批处理对象：代理店提交的POI数据（新增、修改(包含鲜度验证)、删除）；
 * 批处理原则：
 * 总原则，按照PID查询是否存在记录，如果存在，则更新POI_FLAG记录，否则插入一条记录；
 * 1.1 若提交POI数据为新增，则批处理如下：
 * 1.1.1 记录级来源标识POI_FLAG.SRC_RECORD有值且不为0则不处理，否则POI_FLAG.SRC_RECORD批处理赋值为5；
 * 1.1.2 记录级验证标识POI_FLAG.VER_RECORD赋值为5；
 * 1.1.3 源是否被外业已验证POI_FLAG.FIELD_VERIFIED赋值0；
 * 1.1.4 若记录级来源标识POI_FLAG.SRC_RECORD=5，则IX_POI_FLAG表插入一条flag记录，flag_code=110000340000(若存在此flag_code，则不处理)
 * 以上，批处理生成履历；
 * 1.2 若提交POI数据为修改(包含鲜度验证)或删除，则批处理如下：
 * 1.2.1 记录级验证标识POI_FLAG.VER_RECORD赋值为5；
 * 以上均生成履历；
 * Created by ly on 2017/7/9.
 */
public class FMBATD20006 extends BasicBatchRule {


    Map<Long, Integer> srcRecordMap = new HashMap<>();

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
        // TODO Auto-generated method stub

    }


    @Override
    public void runBatch(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        List<IxPoiFlagMethod> poiFlags = poiObj.getIxPoiFlagMethods();

        IxPoi poi = (IxPoi) obj.getMainrow();

        //经确认不处理删除数据
        if(poi.getHisOpType().equals(OperationType.DELETE))
        {
            return;
        }

        if (poiFlags == null || poiFlags.isEmpty()) {

            poiObj.createIxPoiFlagMethod();
        }

        boolean hadSrcRecord5 = false;

        for (IxPoiFlagMethod flag : poiObj.getIxPoiFlagMethods()) {

            if (poi.getHisOpType().equals(OperationType.INSERT)) {

                if (flag.getSrcRecord() == 0) {

                    flag.setSrcRecord(5);
                }

                flag.setFieldVerified(0);

                if (flag.getSrcRecord() == 5) {

                    hadSrcRecord5 = true;
                }

            }
            flag.setVerRecord(5);
        }
        if (hadSrcRecord5) {

            setIxPoiFlag(poiObj);
        }
    }

    private void setIxPoiFlag(IxPoiObj poiObj) throws Exception {

        List<IxPoiFlag> ixPoiFlags = poiObj.getIxPoiFlags();

        if (ixPoiFlags != null) {

            for (IxPoiFlag flag : ixPoiFlags) {

                if (flag.getFlagCode().equals("110000340000")) {

                    return;
                }
            }
        }

        IxPoiFlag newFlag = poiObj.createIxPoiFlag();

        newFlag.setFlagCode("110000340000");
    }
}


