package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.engine.fcc.tips.model.TipsTrack;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;

/**
 * @ClassName: BaseTipsOperate.java
 * @author y
 * @date 2016-12-27 下午8:00:31
 * @Description: tips操作基础类（放一些公共方法）
 *
 */
public class BaseTipsOperate {

	public static int TIP_STATUS_EDIT = 1;
	public static int TIP_STATUS_INIT = 0;
	public static int TIP_STATUS_COMMIT = 2;

    public static int TIP_LIFECYCLE_DELETE = 1;
    public static int TIP_LIFECYCLE_UPDATE = 2;
    public static int TIP_LIFECYCLE_ADD = 3;
	public static int TIP_LIFECYCLE_INIT = 0;//初始化

    public static int TIP_NOT_DELETE = 2;//不用执行删除
    public static int TIP_PHYSICAL_DELETE = 1;//物理删除
    public static int TIP_LOGICAL_DELETE = 0;//逻辑删除

	protected SolrController solr = new SolrController();

	private static final Logger logger = Logger
			.getLogger(BaseTipsOperate.class);


	public void updateFeedbackMemo(String rowkey, int user, String memo) throws Exception {
        Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection oracleConn = null;
        try {

			hbaseConn = HBaseConnector.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

            oracleConn = DBConnector.getInstance().getTipsIdxConnection();
            TipsIndexOracleOperator operator = new TipsIndexOracleOperator(oracleConn);
			//获取solr数据
			TipsDao tipsDao = operator.getById(rowkey);

			int stage=2;
			//如果是预处理的tips则stage=5

			if(tipsDao.getS_sourceType().equals(PretreatmentTipsOperator.FC_SOURCE_TYPE)){
				stage=5;
			}


			// 获取到改钱的 feddback和track
			JSONObject oldTip = getOldTips(rowkey, htab);

			// 1.更新feddback和track
			JSONObject track = oldTip.getJSONObject("track");

			JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

			String date = DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

			// 新增一个trackInfo
			JSONObject jsonTrackInfo = new JSONObject();

			jsonTrackInfo.put("stage", stage);

			jsonTrackInfo.put("date", date);

			jsonTrackInfo.put("handler", user);

			trackInfoArr.add(jsonTrackInfo);

			track.put("t_trackInfo", trackInfoArr);

			track.put("t_lifecycle", 2);

			// 2.更新feedback

			// 新增一个f_array type=3的是文字
			JSONObject feedBack = oldTip.getJSONObject("feedback");

			JSONArray f_array = feedBack.getJSONArray("f_array");


			for (Object object : f_array) {

				JSONObject obj = JSONObject.fromObject(object);

				//先删掉

				if (obj.getInt("type") == 3) {

					f_array.remove(obj);

					break;
				}
			}

			// 如果count=0,则说明原来没有备注，则，增加一条

			int type = 3; // 文字

			JSONObject newFeedback = TipsUtils.newFeedback(user, memo, type,
					date);

			f_array.add(newFeedback);

			// 更新feedback
			feedBack.put("f_array", f_array);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedBack
					.toString().getBytes());

			// 同步更新solr
            tipsDao.setStage(stage);
            tipsDao.setT_date(date);
            tipsDao.setT_lifecycle(TIP_LIFECYCLE_UPDATE);
            tipsDao.setHandler(user);

			operator.updateOne(tipsDao);

			htab.put(put);
		} catch (IOException e) {
            DbUtils.rollbackAndCloseQuietly(oracleConn);
			logger.error(e.getMessage(), e);

			throw new Exception("改备注信息出错：rowkey:"+rowkey+"原因：" + e.getMessage(), e);
		}finally {
            DbUtils.commitAndCloseQuietly(oracleConn);
            if(htab != null) {
                htab.close();
            }
        }

	}

	 /* @Description:获取到tips改前的信息
	 * @param rowkey
	 * @param htab
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 下午2:16:44
	 */
	protected JSONObject getOldTips(String rowkey, Table htab) throws Exception {
		JSONObject oldTip = null;
		List<Get> gets = new ArrayList<Get>();

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		get.addColumn("data".getBytes(), "feedback".getBytes());

		get.addColumn("data".getBytes(), "deep".getBytes());

		gets.add(get);

		Result[] results = htab.get(gets);

		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			// String rowkey = new String(result.getRow());

			try {
				JSONObject jo = new JSONObject();

				String track = new String(result.getValue("data".getBytes(),
						"track".getBytes()));
				jo.put("track",track);


				if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
				}


				byte[] deepByte=result.getValue("data".getBytes(),
						"deep".getBytes());

				String deep=null;

				if(deepByte!=null){
					deep = new String(deepByte);
				}

				jo.put("deep",deep);

				oldTip = jo;
			} catch (Exception e) {
				logger.error("根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e.getCause());

				throw new Exception(
						"根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e);
			}
		}
		return oldTip;
	}


	/**
	 * @Description:删除tips
	 * @param rowkey
	 * @author: y
	 * @param delType :0 逻辑删除，1：物理删除
	 * @throws Exception
	 * @time:2016-11-16 下午5:21:09
	 */
	public void deleteByRowkey(String rowkey, int delType) throws Exception {
		try {
			//物理删除
			if(delType == TIP_PHYSICAL_DELETE){
				physicalDel(rowkey);
			}
			//逻辑删除
			else{
				logicDel(rowkey);
			}

		} catch (SolrServerException e) {

			logger.error("删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);

			throw new Exception(
					"删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);
		}

	}





	/**
	 * @Description:逻辑删除tips(将t_lifecycle改为1：删除)
	 * @param rowkey：被删除的tips的rowkey
	 * @author: y
	 * @throws Exception
	 * @time:2017-4-8 下午4:14:57
	 */
	private void logicDel(String rowkey) throws Exception {
        Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection oracleConn = null;
        try {
            //修改hbase
            hbaseConn = HBaseConnector.getInstance().getConnection();

            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

            Get get = new Get(rowkey.getBytes());

            get.addColumn("data".getBytes(), "track".getBytes());

            Result result = htab.get(get);

            if (result.isEmpty()) {
                throw new Exception("根据rowkey,没有找到需要删除的tips信息，rowkey：" + rowkey);
            }

            Put put = new Put(rowkey.getBytes());

            JSONObject trackJson = JSONObject.fromObject(new String(result.getValue(
                    "data".getBytes(), "track".getBytes())));

            TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
            track = this.tipSaveUpdateTrack(track, BaseTipsOperate.TIP_LIFECYCLE_DELETE);
            put.addColumn("data".getBytes(), "track".getBytes(), JSONObject.fromObject(track).toString()
                    .getBytes());

            htab.put(put);

            //同步更新index
            oracleConn = DBConnector.getInstance().getTipsIdxConnection();
            TipsIndexOracleOperator operator = new TipsIndexOracleOperator(oracleConn);
            TipsDao tipsIndex = operator.getById(rowkey);
            tipsIndex = this.tipSaveUpdateTrackSolr(track, tipsIndex);
            List<TipsDao> tipsIndexList = new ArrayList<TipsDao>();
            tipsIndexList.add(tipsIndex);
            operator.update(tipsIndexList);
        }catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(oracleConn);
            e.printStackTrace();
            logger.error("逻辑删除失败"+rowkey+":", e);
        }finally {
            if(htab != null) {
                htab.close();
            }
            DbUtils.commitAndCloseQuietly(oracleConn);
        }

	}

	/**
	 * @Description:TOOD
	 * @param rowkey
	 * @throws SolrServerException
	 * @throws IOException
	 * @author: y
	 * @time:2017-4-8 下午4:14:15
	 */
	private void physicalDel(String rowkey) throws Exception {
		Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection oracleConn = null;
        try {
            hbaseConn = HBaseConnector.getInstance().getConnection();
            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

            List list = new ArrayList();
            Delete d1 = new Delete(rowkey.getBytes());
            list.add(d1);

            // delete index
             //solr.deleteByRowkey(rowkey);
            oracleConn = DBConnector.getInstance().getTipsIdxConnection();
            TipsIndexOracleOperator operator = new TipsIndexOracleOperator(oracleConn);
            operator.delete(rowkey);

            htab.delete(list);
        }catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(oracleConn);
            logger.error("物理删除失败:", e);
            throw new Exception("物理删除失败rowkey:" + rowkey, e);
        }finally {
            if(htab != null) {
                htab.close();
            }
            DbUtils.commitAndCloseQuietly(oracleConn);
        }
    }

    /**
     * FC预处理，情报矢量化
     * 20170718 Tips新增或修改是维护Track,t_tipStatus=1，t_dEditStatus=0，
     *t_dEditMeth=0,t_mEditStatus=0,t_mEditMeth=0
     *不维护t_trackinfo
     * @param track
     * @param lifecycle
     * @return
     */
    public TipsTrack tipSaveUpdateTrack(TipsTrack track, int lifecycle) {
        String date = DateUtils.dateToString(new Date(),
                DateUtils.DATE_COMPACTED_FORMAT);
        track.setT_date(date);
        track.setT_lifecycle(lifecycle);
        track.setT_tipStatus(PretreatmentTipsOperator.TIP_STATUS_EDIT);
        track.setT_dEditStatus(PretreatmentTipsOperator.TIP_STATUS_INIT);
        track.setT_mEditStatus(PretreatmentTipsOperator.TIP_STATUS_INIT);
        track.setT_dEditMeth(PretreatmentTipsOperator.TIP_STATUS_INIT);
        track.setT_mEditMeth(PretreatmentTipsOperator.TIP_STATUS_INIT);
        return track;
    }

    /**
     * FC预处理，情报矢量化
     * 20170718 Tips提交维护Track,t_tipStatus=2，t_dEditStatus=0，
     *t_dEditMeth=0,t_mEditStatus=0,t_mEditMeth=0
     *同时维护t_trackinfo
     * @param track
     * @return
     */
    public TipsTrack tipSubmitTrack(TipsTrack track, int handler, int stage) {
        String date = DateUtils.dateToString(new Date(),
                DateUtils.DATE_COMPACTED_FORMAT);
        track.setT_date(date);
        track.setT_tipStatus(PretreatmentTipsOperator.TIP_STATUS_COMMIT);
        track.setT_dEditStatus(PretreatmentTipsOperator.TIP_STATUS_INIT);
        track.setT_mEditStatus(PretreatmentTipsOperator.TIP_STATUS_INIT);
        track.setT_dEditMeth(PretreatmentTipsOperator.TIP_STATUS_INIT);
        track.setT_mEditMeth(PretreatmentTipsOperator.TIP_STATUS_INIT);
        //新增一个trackInfo
        TipsTrack.TrackInfo trackInfo = new TipsTrack.TrackInfo();
        trackInfo.setDate(date);
        trackInfo.setHandler(handler);
        trackInfo.setStage(stage);
        List<TipsTrack.TrackInfo> trackInfoList = track.getT_trackInfo();
        trackInfoList.add(trackInfo);
        return track;
    }

    /**
     * FC预处理，情报矢量化
     * 20170718 Tips新增或修改是维护Track,t_tipStatus=1，t_dEditStatus=0，
     *t_dEditMeth=0,t_mEditStatus=0,t_mEditMeth=0
     *不维护t_trackinfo
     * @param track
     * @param tipsIndex
     * @return
     */
    public TipsDao tipSaveUpdateTrackSolr(TipsTrack track, TipsDao tipsIndex) {
        tipsIndex.setT_date(track.getT_date());
        tipsIndex.setT_lifecycle(track.getT_lifecycle());
        tipsIndex.setT_tipStatus(track.getT_tipStatus());
        tipsIndex.setT_dEditStatus(track.getT_dEditStatus());
        tipsIndex.setT_dEditMeth(track.getT_dEditMeth());
        tipsIndex.setT_mEditStatus(track.getT_mEditStatus());
        tipsIndex.setT_mEditMeth(track.getT_mEditMeth());
        return tipsIndex;
    }

    

    /**
     * FC预处理，情报矢量化
     * 20170718 Tips提交维护Track,t_tipStatus=2，t_dEditStatus=0，
     *t_dEditMeth=0,t_mEditStatus=0,t_mEditMeth=0
     *同时维护t_trackinfo
     * @param track
     * @param tipsDao
     * @return
     */
    public TipsDao tipSubmitTrackOracle(TipsTrack track, TipsDao tipsDao) {
        tipsDao.setT_date(track.getT_date());
        tipsDao.setT_tipStatus(track.getT_tipStatus());
        tipsDao.setT_dEditStatus(track.getT_dEditStatus());
        tipsDao.setT_dEditMeth(track.getT_dEditMeth());
        tipsDao.setT_mEditStatus(track.getT_mEditStatus());
        tipsDao.setT_mEditMeth(track.getT_mEditMeth());
        List<TipsTrack.TrackInfo> trackInfoList = track.getT_trackInfo();
        TipsTrack.TrackInfo lastTrack = trackInfoList.get(trackInfoList.size() - 1);
        tipsDao.setStage(lastTrack.getStage());
        tipsDao.setT_operateDate(lastTrack.getDate());
        tipsDao.setHandler(lastTrack.getHandler());
		return tipsDao;
    }
}
