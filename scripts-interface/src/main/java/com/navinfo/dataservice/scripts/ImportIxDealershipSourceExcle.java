package com.navinfo.dataservice.scripts;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.scripts.model.IxDealershipSource;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 代理店全国一览表IX_DEALERSHIP_SOURCE初始化
 * @author zhangli5174
 *
 */
public class ImportIxDealershipSourceExcle {

//	private static QueryRunner runner = new QueryRunner();
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

			String ext = filePath.substring(filePath.lastIndexOf("."));
			if(!".xls".equals(ext) && !".xlsx".equals(ext)){
				System.out.println("文件格式错误"+filePath);
				throw new IllegalArgumentException("文件格式错误"+filePath);
			}
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
			excelHeader.put("一览表X坐标", "XGuide");
			excelHeader.put("一览表Y坐标", "YGuide");//XGuide,YGuide
			excelHeader.put("已采纳POI分类", "poiKindCode");
			excelHeader.put("已采纳POI品牌", "poiChain");
			excelHeader.put("已采纳POI名称", "poiName");
			excelHeader.put("已采纳POI简称", "poiNameShort");
			excelHeader.put("已采纳POI地址", "poiAddress");
			excelHeader.put("已采纳POI电话", "poiTel");
			excelHeader.put("已采纳POI邮编", "poiPostCode");
			excelHeader.put("已采纳POI显示坐标X", "poiXDisplay");//double
			excelHeader.put("已采纳POI显示坐标Y", "poiYDisplay");//double
			excelHeader.put("已采纳POI引导坐标X", "poiXGuide");//double
			excelHeader.put("已采纳POI引导坐标Y", "poiYGuide");//double
			/*excelHeader.put("", "");
			excelHeader.put("", "");*/
			
			List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
		
			saveSources(sources);
		
	}
	private static void saveSources(List<Map<String, Object>> sources) throws Exception {
		if(sources != null && sources.size() > 0){
//			int id =0;
			for(Map<String, Object> source : sources){
				IxDealershipSource sourceObj = new IxDealershipSource();
				double x =0;
				double y =0;
				//XGuide,YGuide
				if(source.get("XGuide") != null && StringUtils.isNotEmpty((String) source.get("XGuide"))){
					x = Double.parseDouble((String) source.get("XGuide"));
				}
				source.remove("XGuide");
				if(source.get("YGuide") != null && StringUtils.isNotEmpty((String) source.get("YGuide"))){
					y = Double.parseDouble((String) source.get("YGuide"));
				}
				source.remove("YGuide");
				
				
				Geometry pointWkt = GeoTranslator.point2Jts(x, y);
				
				//特殊类型数据处理
				if(source.get("isDeleted") != null && StringUtils.isNotEmpty((String) source.get("isDeleted"))){
					source.put("isDeleted",Integer.parseInt((String) source.get("isDeleted")));
				}else{
					source.put("isDeleted",0);
				}
				if(source.get("poiXDisplay") != null && StringUtils.isNotEmpty((String) source.get("poiXDisplay"))){
					source.put("poiXDisplay",Double.parseDouble((String) source.get("poiXDisplay")));
				}else{
					source.put("poiXDisplay",0);
				}
				if(source.get("poiYDisplay") != null && StringUtils.isNotEmpty((String) source.get("poiYDisplay"))){
					source.put("poiYDisplay",Double.parseDouble((String) source.get("poiYDisplay")));
				}else{
					source.put("poiYDisplay",0);
				}
				if(source.get("poiXGuide") != null && StringUtils.isNotEmpty((String) source.get("poiXGuide"))){
					source.put("poiXGuide",Double.parseDouble((String) source.get("poiXGuide")));
				}else{
					source.put("poiXGuide",0);
				}
				if(source.get("poiYGuide") != null && StringUtils.isNotEmpty((String) source.get("poiYGuide"))){
					source.put("poiYGuide",Double.parseDouble((String) source.get("poiYGuide")));
				}else{
					source.put("poiYGuide",0);
				}
				//************************
				
				
				transMap2Bean(source, sourceObj);
				int id =applyPid();
				//id+=1;
//				System.out.println("fid: "+sourceObj.getCfmPoiNum());
				sourceObj.setSourceId(id);
				sourceObj.setGeometry(pointWkt);
				if(sourceObj.getProvince() != null && StringUtils.isNotEmpty(sourceObj.getProvince())){
					sourceObj.setProvince(h2f(sourceObj.getProvince()));
				}
				if(sourceObj.getCity() != null && StringUtils.isNotEmpty(sourceObj.getCity())){
					sourceObj.setCity(h2f(sourceObj.getCity()));
				}
				if(sourceObj.getProject() != null && StringUtils.isNotEmpty(sourceObj.getProject())){
					sourceObj.setProject(h2f(sourceObj.getProject()));
				}
				
				if(sourceObj.getName() != null && StringUtils.isNotEmpty(sourceObj.getName())){
					sourceObj.setName(h2f(sourceObj.getName()));
				}
				if(sourceObj.getNameShort() != null && StringUtils.isNotEmpty(sourceObj.getNameShort())){
					sourceObj.setNameShort(h2f(sourceObj.getNameShort()));
				}
				if(sourceObj.getAddress() != null && StringUtils.isNotEmpty(sourceObj.getAddress())){
					sourceObj.setAddress(h2f(sourceObj.getAddress()));
				}
				if(sourceObj.getCfmMemo() != null && StringUtils.isNotEmpty(sourceObj.getCfmMemo())){
					sourceObj.setCfmMemo(h2f(sourceObj.getCfmMemo()));
				}
				if(sourceObj.getPoiName() != null && StringUtils.isNotEmpty(sourceObj.getPoiName())){
					sourceObj.setPoiName(h2f(sourceObj.getPoiName()));
				}
				if(sourceObj.getPoiNameShort() != null && StringUtils.isNotEmpty(sourceObj.getPoiNameShort())){
					sourceObj.setPoiNameShort(h2f(sourceObj.getPoiNameShort()));
				}
				if(sourceObj.getPoiAddress() != null && StringUtils.isNotEmpty(sourceObj.getPoiAddress())){
					sourceObj.setPoiAddress(h2f(sourceObj.getPoiAddress()));
				}
					
				create(sourceObj);
			}
		}
		
	}
	
	
	
	 public static void transMap2Bean(Map<String, Object> map, Object obj) throws Exception {  
		  
	        try {  
	            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());  
	            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
	  
	            for (PropertyDescriptor property : propertyDescriptors) {  
	                String key = property.getName();  
	  
	                if (map.containsKey(key)) {  
	                    Object value = map.get(key);  
//	                    System.out.println(key+" : "+value);
	                    // 得到property对应的setter方法  
	                    Method setter = property.getWriteMethod();  
	                    setter.invoke(obj, value);  
	                }  
	  
	            }  
	  
	        } catch (Exception e) {  
	            System.out.println("transMap2Bean Error " + e);  
	            throw e;
	        }  
	  
	        return;  
	  
	    } 
	 
	 public static void create(IxDealershipSource  bean) throws Exception{
		 System.out.println("begin");
		 Connection conn = null;
			PreparedStatement stmt = null;
			String fid = "";
			try{

				//获取代理店数据库连接
				DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("dealership");

				OracleSchema manSchema = new OracleSchema(
						DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
				conn = manSchema.getPoolDataSource().getConnection();
				//持久化
				QueryRunner run = new QueryRunner();
				
				String createSql = "insert into IX_DEALERSHIP_SOURCE (SOURCE_ID, PROVINCE, CITY, PROJECT, KIND_CODE, CHAIN, NAME, NAME_SHORT, ADDRESS, TEL_SALE, TEL_SERVICE, TEL_OTHER, POST_CODE, NAME_ENG, ADDRESS_ENG, PROVIDE_DATE, IS_DELETED, FB_SOURCE, FB_CONTENT, FB_AUDIT_REMARK, FB_DATE, CFM_POI_NUM, CFM_MEMO, DEAL_CFM_DATE, POI_KIND_CODE, POI_CHAIN, POI_NAME, POI_NAME_SHORT, POI_ADDRESS, POI_POST_CODE, POI_X_DISPLAY, POI_Y_DISPLAY, POI_X_GUIDE, POI_Y_GUIDE, GEOMETRY, POI_TEL) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//						+ "SDO_GEOMETRY(?,8307),?)";			
						+ "?,?)";			
//				System.out.println("createSql: "+createSql);
				fid=bean.getCfmPoiNum();
				run.update(conn, 
						   createSql, 
						   bean.getSourceId() , bean.getProvince(), bean.getCity(), bean.getProject(), 
						   bean.getKindCode(), bean.getChain(), bean.getName(), bean.getNameShort(), 
						   bean.getAddress(), bean.getTelSale(), bean.getTelService(), bean.getTelOther(), 
						   bean.getPostCode(), bean.getNameEng(), bean.getAddressEng(), bean.getProvideDate(), 
						   bean.getIsDeleted(), bean.getFbSource(), bean.getFbContent(), bean.getFbAuditRemark(), 
						   bean.getFbDate(), bean.getCfmPoiNum(), bean.getCfmMemo(), bean.getDealCfmDate(), 
						   bean.getPoiKindCode(), bean.getPoiChain(), bean.getPoiName(), bean.getPoiNameShort(), 
						   bean.getPoiAddress(), bean.getPoiPostCode(), bean.getPoiXDisplay(), bean.getPoiYDisplay(), 
						   bean.getPoiXGuide(), bean.getPoiYGuide(), 
						   GeoTranslator.wkt2Struct(conn, GeoTranslator.jts2Wkt(bean.getGeometry())), 
						   bean.getPoiTel()
						   );
				 System.out.println("end");
			}catch(SQLException e){
				DbUtils.rollbackAndCloseQuietly(conn);
//				log.error(e.getMessage(), e);
				 System.out.println("fid:"+fid+"  创建失败，原因为:"+e.getMessage());
				throw new Exception("fid:"+fid+"  创建失败，原因为:"+e.getMessage(),e);
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
			}
		}
	 
	 public void createBatch(List<IxDealershipSource>  list,Connection conn){
			try{
				//持久化
				QueryRunner run = new QueryRunner();
				
				String sql = " INSERT INTO IX_DEALERSHIP_SOURCE ( sourceId , province, city, project, kindCode, chain, name, nameShort, address, telSale, telService, telOther, postCode, nameEng, addressEng, provideDate, isDeleted, fbSource, fbContent, fbAuditRemark, fbDate, cfmPoiNum, cfmMemo, dealCfmDate, poiKindCode, poiChain, poiName, poiNameShort, poiAddress, poiPostCode, poiXDisplay, poiYDisplay, poiXGuide, poiYGuide, geometry, poiTel ) "
						+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
				
				 // 关闭事务自动提交
				   conn.setAutoCommit(false);
				 
				   Long startTime = System.currentTimeMillis();
				   PreparedStatement pst = (PreparedStatement) conn.prepareStatement(sql.toString());
				   int id =0;
				   for (int i = 0; i < list.size(); i++) {
					IxDealershipSource sourceObj = new IxDealershipSource();
//					int id = applyPid();
					id+=1;
					pst.setInt(1, id);
//				    pst.setString(1, exLog.getExLogId());
//				    pst.setString(2, exLog.getExLogDate());
				    // 把一个SQL命令加入命令列表
				    pst.addBatch();
				   }
				   // 执行批量更新
				   pst.executeBatch();
				   // 语句执行完毕，提交本事务
				   conn.commit();
				   Long endTime = System.currentTimeMillis();
				   System.out.println("用时：" + (endTime - startTime));
				
				//将map 转bean
			}catch(Exception e){
//				DbUtils.rollbackAndCloseQuietly(conn);
//				log.error(e.getMessage(), e);
//				throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
			}finally{
//				DbUtils.commitAndCloseQuietly(conn);
			}
		}
	 private static int applyPid() throws Exception {
			return PidUtil.getInstance().applyIxDealershipSourceId();
		}
	 private static String h2f(String input) {
	        char c[] = input.toCharArray();
	        for (int i = 0; i < c.length; i++) {
	            if (c[i] == ' ') {
	                c[i] = '\u3000';
	            } else if (c[i] < '\177') {
	                c[i] = (char) (c[i] + 65248);

	            }
	        }
	        return new String(c);
	    }
}
