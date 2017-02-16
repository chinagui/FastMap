package com.navinfo.dataservice.impcore;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.Mesh4Partition;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogSelector;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogFlusher;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogByFilterGridsSelector;

public class Day2MonPoiLogSelectorTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer-datahub-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void testSelect() throws Exception {
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
		int cityId = 17;
		DbInfo dailyDbInfo = datahubApi.getDbById(cityId);
		OracleSchema schema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo .getConnectParam()));
		Day2MonPoiLogSelector selector = new Day2MonPoiLogSelector(schema );
		ManApi manApi = (ManApi)ApplicationContextUtil
				.getBean("manApi");
		selector.setGrids(manApi.queryGridOfCity(cityId));
		selector.setStopTime(new Date());
		String tempTable = selector.select();
		System.out.println(tempTable);
	}
	@Test
	public void testSelectLog() throws Exception {
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
		int cityId = 17;
		DbInfo dailyDbInfo = datahubApi.getDbById(cityId);
		DbInfo monthDbInfo = datahubApi.getDbById(19);
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo.getConnectParam()));
		Connection dailyConn = dailyDbSchema.getPoolDataSource().getConnection();
		OracleSchema monthDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(monthDbInfo.getConnectParam()));
		Connection monthConn = monthDbSchema.getPoolDataSource().getConnection();
		Day2MonPoiLogByFilterGridsSelector selector = new Day2MonPoiLogByFilterGridsSelector(dailyDbSchema);
		List<Integer> filterGrids= new ArrayList<Integer>();
		filterGrids.add(60563511);
		selector.setFilterGrids(filterGrids);
		selector.setStopTime(new Date());
		try{
			String tempTable = selector.select();
			System.out.println(tempTable);
			//测试刷履历
			FlushResult flushResult= new Day2MonLogFlusher(dailyDbSchema,dailyConn,monthConn,true,tempTable).flush();
			if(0==flushResult.getTotal()){
				return;
			}
		}catch(Exception e){
			if(monthConn!=null)monthConn.rollback();
			if(dailyConn!=null)dailyConn.rollback();
			throw e;
			
		}finally{
			DbUtils.commitAndCloseQuietly(monthConn);
			DbUtils.commitAndCloseQuietly(dailyConn);	
		}

	}
	@Test
	public void testGetMesh() throws Exception {
		ManApi manApi = (ManApi)ApplicationContextUtil
				.getBean("manApi");
		MetadataApi metaApi = (MetadataApi)ApplicationContextUtil
				.getBean("metadataApi");
		
		
		//确定需要日落月的大区
		List<Region> regions = null;
		regions = manApi.queryRegionList();
		System.out.println("确定日落月大区库个数："+regions.size()+"个。");
		//获取region对应的省份
		List<CpRegionProvince> regionProvs = manApi.listCpRegionProvince();
		Map<Integer,Set<Integer>> adminMap = new HashMap<Integer,Set<Integer>>();
		for(CpRegionProvince cp:regionProvs){
			if(adminMap.containsKey(cp.getRegionId())){
				adminMap.get(cp.getRegionId()).add(cp.getAdmincode());
			}else{
				Set<Integer> codes = new HashSet<Integer>();
				codes.add(cp.getAdmincode());
				adminMap.put(cp.getRegionId(), codes);
			}
		}
		//开始分配
		for(Region region:regions){
			//获取region包含的省份
			Set<Integer> admins = adminMap.get(region.getRegionId());
			//过去大区库内的关闭图幅并转换成girds
			List<Mesh4Partition> meshes = metaApi.queryMeshes4PartitionByAdmincodes(admins);
			List<Integer> filterGrids = new ArrayList<Integer>();
			for(Mesh4Partition m:meshes){
				if(m.getDay2monSwitch()==0){
					int mId = m.getMesh();
					for(int i=0;i<4;i++){
						for(int j=0;j<4;j++){
							filterGrids.add(mId*100 + i*10+ j);
						}
					}
				}
			}
			System.out.println(filterGrids);
		}
		
	}

}
