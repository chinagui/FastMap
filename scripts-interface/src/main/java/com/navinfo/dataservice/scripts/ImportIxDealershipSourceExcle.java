package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.database.sql.SqlExec;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

/**
 * 代理店全国一览表IX_DEALERSHIP_SOURCE初始化
 * @author zhangli5174
 *
 */
public class ImportIxDealershipSourceExcle {

	private static QueryRunner runner = new QueryRunner();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(args==null||args.length!=2){
				System.out.println("ERROR:need args:filePath");
				return;
			}

			String filePath = args[0];
//			String blockFile = args[1];

			imp(filePath);
			
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
	public static void imp(String filePath)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{

			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmMan");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			
			File file = new File(filePath);
			InputStream in = new FileInputStream(file);
			ExcelReader excleReader = new ExcelReader(filePath);
			Map<String,String> excelHeader = new HashMap<String,String>();
			excelHeader.put("FID", "cfmPoiNum");
			excelHeader.put("省份", "province");
			excelHeader.put("城市", "city");
			excelHeader.put("项目", "project");
			excelHeader.put("代理店分类", "kindCode");
			excelHeader.put("代理店品牌", "chain");
			excelHeader.put("厂商提供名称", "name");
			excelHeader.put("厂商提供简称", "nameShort");
			excelHeader.put("厂商提供地址", "address");
			excelHeader.put("厂商提供电话（销售）", "telSale");
			excelHeader.put("厂商提供电话（维修）", "telService");
			excelHeader.put("厂商提供电话（其他）", "telOther");
			excelHeader.put("厂商提供邮编", "postCode");
			excelHeader.put("厂商提供英文名称", "nameEng");
			excelHeader.put("厂商提供英文地址", "addressEng");
			excelHeader.put("一览表提供时间", "provideDate");
			excelHeader.put("一览表确认时间", "dealCfmDate");
			excelHeader.put("四维确认备注", "cfmMemo");
			/*excelHeader.put("负责人反馈结果", "");
			excelHeader.put("解决人", "");
			excelHeader.put("解决时间", "");
			excelHeader.put("四维差分结果", "");
			excelHeader.put("一览表作业状态", "");
			excelHeader.put("变更履历", "");*/
			excelHeader.put("是否删除记录", "isDeleted");//数字类型
			excelHeader.put("已采纳POI分类新值", "poiKindCode");
			excelHeader.put("已采纳POI品牌新值", "poiChain");
			excelHeader.put("已采纳POI名称新值", "poiName");
			excelHeader.put("已采纳POI简称新值", "poiNameShort");
			excelHeader.put("已采纳POI地址新值", "poiAddress");
			excelHeader.put("已采纳POI电话新值", "poiTel");
			excelHeader.put("已采纳POI邮编新值", "poiPostCode");
			excelHeader.put("已采纳POI显示坐标X新值", "poiXDisplay");//double
			excelHeader.put("已采纳POI显示坐标Y新值", "poiYDisplay");//double
			excelHeader.put("已采纳POI引导坐标X新值", "poiXGuide");//double
			excelHeader.put("已采纳POI引导坐标Y新值", "poiYGuide");//double
			excelHeader.put("一览表X坐标|一览表Y坐标", "geometry");
			/*excelHeader.put("", "");
			excelHeader.put("", "");*/
			
			
			
//			Object[] sourceObjs = ExcelReader.readDealerdhipSourceExcel(in);
			List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
		
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
