package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 查询条件：满足以下任一条件均执行批处理：
 * (1)存在官方原始中文名称或官方标准中文名称新增履历；
 * (2)存在官方原始中文名称或官方标准中文名称修改履历；
 * (3)存在KIND_CODE或CHAIN的修改履历
 * 批处理：当NAME_TYPE=1且NAME_CLASS=1时，进行如下批处理：
 * (1)当同组（NAME_GROUPID相同）名称中，没有原始英文名称（LANG_CODE="ENG",NAME_TYPE=2）时,新增一条原始英文名(组号一样，NAME_CLASS=1，NAME_TYPE=2，LANG_CODE="ENG"，NAME转拼音赋值，NAME_PHONETIC赋值空），并生成新增履历；
 * (2)当同组（NAME_GROUPID相同）名称中，有原始英文名称（LANG_CODE="ENG",NAME_TYPE=2）时,更新英文名NAME；有标准化英文名称时，清空标准化英文NAME，并生成履历；
 * NAME统一处理：统一处理No.中N和o的大小写问题：将“NO.”，“nO.”，“no.”修改成“No.”。
 * 注：标准化（type=1）中文名,当class={1,3,8,9}时，必须有原始英文名；标准化（type=1）中文名，当class<>{1,3,8,9}时，没有英文名；官方原始中文（type=2，class=1）名，没有英文名。
 *
 */
public class FMBAT20115 extends BasicBatchRule {

    private Map<Long,Long> pidAdminId;

    private MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
        //init(batchDataList);
    }

    private void init(Connection conn, Collection<BasicObj> batchDataList) throws Exception {
        Set<Long> pidList=new HashSet<>();
        for(BasicObj obj:batchDataList){
            pidList.add(obj.objPid());
        }
        log.debug("开始查询pidAdminId");
        pidAdminId = IxPoiSelector.getAdminIdByPids(conn, pidList);
        log.debug("查询pidAdminId结束");
    }

    @Override
    public void runBatch(BasicObj obj) throws Exception {
        //run20115(obj);
    }

    private void run20115(BasicObj obj) throws Exception {
        IxPoiObj poiObj = (IxPoiObj) obj;
        IxPoi poi = (IxPoi) obj.getMainrow();
        List<IxPoiName> names=poiObj.getIxPoiNames();
        String adminCode=null;
        if(pidAdminId!=null&&pidAdminId.containsKey(poi.getPid())){
            adminCode=pidAdminId.get(poi.getPid()).toString();
        }
        boolean isChanged = false;
        for (IxPoiName name:names) {
            if(name.getNameClass()==1&&name.getNameType()==2&&(name.getLangCode().equals("CHI")||name.getLangCode().equals("CHT"))){
                if (name.getHisOpType().equals(OperationType.INSERT) || name.getHisOpType().equals(OperationType.UPDATE)) {
                    isChanged = true;
                    break;
                }
            }
        }
        // 存在KIND_CODE或CHAIN的修改；
        if (poi.hisOldValueContains(IxPoi.KIND_CODE) || poi.hisOldValueContains(IxPoi.CHAIN)) {
            isChanged = true;
        }

        if (isChanged) {
            IxPoiName standarName = null;
            for (IxPoiName name:names) {
                if (name.getNameClass()==1 && name.getNameType()==1 && name.getLangCode().equals("CHI")) {
                    standarName = name;
                }
            }

            if (standarName != null) {
                IxPoiName engOfficialName = null;
                IxPoiName engStandarName = null;
                for (IxPoiName name:names) {
                    if (name.getNameType()==2 && name.getLangCode().equals("ENG") && standarName.getNameGroupid()==name.getNameGroupid()) {
                        engOfficialName = name;
                    }
                    if (name.getNameType()==1 && name.getLangCode().equals("ENG") && standarName.getNameGroupid()==name.getNameGroupid()) {
                        engStandarName = name;
                    }
                }

                if (engOfficialName == null) {
                    engOfficialName = poiObj.createIxPoiName();
                    engOfficialName.setNameType(2);
                    engOfficialName.setNameClass(1);
                    engOfficialName.setPoiPid(poi.getPid());
                    engOfficialName.setNameGroupid(standarName.getNameGroupid());
                    engOfficialName.setLangCode("ENG");
                    log.debug("开始convertEng");
                    engOfficialName.setName(metadata.convertEng(standarName.getName(),adminCode));
                    log.debug("结束convertEng");
                } else {
                    log.debug("开始convertEng");
                    engOfficialName.setName(metadata.convertEng(standarName.getName(),adminCode));
                    log.debug("结束convertEng");
                    if (engStandarName != null) {
                        engStandarName.setName("");
                    }
                }

            }

        }
    }

    //private final Logger logger = LoggerRepos.getLogger(this.getClass());

    private static final Integer BATCH_THREAD_SIZE = 20;

    private static ThreadLocal<FMBAT20115> threadLocal = new ThreadLocal<>();

    public static FMBAT20115 getInstance() {
        FMBAT20115 bat = threadLocal.get();
        if (null == bat) {
            bat = new FMBAT20115();
            threadLocal.set(bat);
        }
        return bat;
    }

    @Override
    public void run() throws Exception {
        log.info("FMBAT20115 start...");
        long startTime = System.currentTimeMillis();
        Map<Integer, List<BasicObj>> rows = loadData();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(BATCH_THREAD_SIZE, BATCH_THREAD_SIZE, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(20000), new ThreadPoolExecutor.DiscardOldestPolicy());

        for (Map.Entry<Integer, List<BasicObj>> entry : rows.entrySet()) {
            Task task = new Task(getBatchRuleCommand().getConn(), entry.getKey(), entry.getValue());
            executor.execute(task);
        }
        executor.shutdown();

        while (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            log.debug(String.format("executor.getPoolSize()：%d，executor.getQueue().size()：%d，executor.getCompletedTaskCo" +
                    "unt()：%d", executor.getPoolSize(), executor.getQueue().size(), executor.getCompletedTaskCount()));
        }

        log.info("FMBAT20115 end...");
        log.info(String.format("speedTime: %d", (System.currentTimeMillis() - startTime) >> 10));
    }

    private Map<Integer, List<BasicObj>> loadData() {
        Map<Integer, List<BasicObj>> map = new HashMap<>();

        IxPoi ixPoi;
        Integer meshId;
        for (BasicObj basicObj : getRowList().values()) {
            if (!(basicObj.getMainrow() instanceof IxPoi)) {
                continue;
            }
            ixPoi = (IxPoi) basicObj.getMainrow();
            meshId = ixPoi.getMeshId();
            if (map.containsKey(meshId)) {
                map.get(meshId).add(basicObj);
            } else {
                List<BasicObj> list = new ArrayList<>();
                list.add(basicObj);
                map.put(meshId, list);
            }
        }
        return map;
    }

    private void batchTranslate(Connection conn, List<BasicObj> list) throws Exception{
        this.init(conn, list);
        log.info(String.format("poiObjects.size() = %d", list.size()));
        log.info(String.format("pidAdminId.size() = %d", this.pidAdminId.size()));
        for (BasicObj basicObj : list) {
            try {
                this.run20115(basicObj);
            } catch (Exception e) {
                log.error(String.format("fmbat20115 has error (pid = %d)", basicObj.objPid()), e.fillInStackTrace());
                throw e;
            }
        }
    }

    class Task implements Runnable {
        private Connection conn;
        private Integer meshId;
        private List<BasicObj> list;

        protected Task(Connection conn, Integer meshId, List<BasicObj> list) {
            this.conn = conn;
            this.meshId = meshId;
            this.list = list;
        }

        @Override
        public void run() {
            try {
                FMBAT20115.getInstance().batchTranslate(conn, list);
                log.info(String.format("mesh %d translate success..", meshId));
            } catch (Exception e) {
                log.error(String.format("mesh %d translate error..", meshId), e.fillInStackTrace());
                e.fillInStackTrace();
            }
        }
    }
}

