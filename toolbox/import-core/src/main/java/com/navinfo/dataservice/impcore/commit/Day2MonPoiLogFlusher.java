package com.navinfo.dataservice.impcore.commit;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;

/*
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-coreDay2MonLogFlusher.java
 */
public class Day2MonPoiLogFlusher extends LogFlusher {
	public Day2MonPoiLogFlusher(int regionId, DbInfo sourceDbInfo,
			DbInfo targetDbInfo, List<Integer> grids, String stopTime,
			String featureType, int lockType) {
		super(regionId, sourceDbInfo, targetDbInfo, grids, stopTime, featureType,
				lockType);
		// TODO Auto-generated constructor stub
	}

	public  String getPrepareSql() throws Exception{
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(this.getTempTable());
			sb.append(" SELECT DISTINCT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DETAIL_GRID T  WHERE P.OP_ID=L.OP_ID AND L.ROW_ID=T.LOG_ROW_ID AND P.COM_STA = 0 ");
			sb.append(this.getFeatureFilter());
			sb.append(" AND EXISTS(SELECT 1 FROM POI_EDIT_STATUS I WHERE L.ROW_ID=L.ROW_ID AND I.STATUS=3)");
			return sb.toString();
	}
	public String getFeatureFilter(){
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		Set<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getTableNames(featureType);
		return " AND L.TB_NM IN ('"+StringUtils.join(tableNames,"','")+"')";
		
	}
}

