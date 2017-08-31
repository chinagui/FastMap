package com.navinfo.dataservice.engine.script;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.engine.fcc.photo.HBaseController;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DownloadPhotoFromHbase {
	private static Logger log = LoggerRepos.getLogger(DownloadPhotoFromHbase.class);
	static ClassPathXmlApplicationContext context =null;
	static String rootPath="图形提取";
	static String tpPath;
	static String ptnPath;
	static String filePath;
	static String a_uuid;
	static int a_uploadUser;
	static String photoName;
	static String a_uploadDate;	
	static String last_a_uploadDate;
	static String meshId;
	static String input_s_sourcetype;
	
	static {
		//初始化
		initContext();
	}	
	
	public static void main(String[] args) throws IOException{
		
		  if (args.length == 0) {
	            System.out.println("请输入配置文件路径！");
	            System.exit(0);
	        }
	        Properties props = new Properties();
	        props.load(new FileInputStream(args[0]));
	        
	        // s_sourcetype取值范围，1401代表方向看板，1406代表实景图
	        String input_s_sourcetype = props.getProperty("input_s_sourcetype");
	        
	        // mesh范围
	        String meshIds = props.getProperty("meshIds");

	        if (StringUtils.isEmpty(input_s_sourcetype)) {
	            System.out.println("input_s_sourcetype is  null");
	            log.error("input_s_sourcetype is  null");
	            return;
	        }	       

	        if (StringUtils.isEmpty(meshIds)) {
	            System.out.println("meshIds is null");
	            log.error("meshIds is null");
	            return;
	        }
	        	        	        
	        List<String> meshList = new ArrayList<>(Arrays.asList(meshIds
                  .split(",")));
	        
	        List<String> typeList = new ArrayList<>(Arrays.asList(input_s_sourcetype
                  .split(",")));
	        
	        // 从配置信息中获取meshId、input_s_sourcetype,按照图幅范围及type类型提取照片。
	        for (String mid : meshList) {
				for (String type : typeList) {
					downloadPhoto(mid,type);
				}
								
			}
						
	}	
	
		
	//初始化	
	private static void initContext() {
		
		context = new ClassPathXmlApplicationContext(
				new String[] {"dubbo-app-scripts.xml","dubbo-scripts.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		
	}	
	
	/**
	 * 根据输入的图幅号和对应的sourceType类型提取对应照片
	 * @param meshId
	 */
	
	public static void downloadPhoto(String meshId,String sourceType){
		
		String wkt=MeshUtils.mesh2WKT(meshId);		
		 if (StringUtils.isEmpty(wkt)) {
	            log.error("meshId(" + meshId + ")转换为wkt时出错");
	            return;
	        }	       
		Connection oracleConn = null;
		try {
			oracleConn = DBConnector.getInstance().getTipsIdxConnection();
			String isWkt = " sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' ";
			TipsIndexOracleOperator tipsOp = new TipsIndexOracleOperator(oracleConn);			
						
			//	根据wkt从Oracle中查询tips_index表，并同时查hbase，最终获取到对应的tipsDao集合。		
			List<TipsDao> tis = tipsOp.query(
					"select * from tips_index where " + isWkt + "and s_sourcetype = ?"
					+ " and stage=1 and (t_lifecycle=2 or t_lifecycle=3)", ConnectionUtil.createClob(oracleConn, wkt),sourceType);			
			
			for (TipsDao tips : tis) {
				
				JSONObject deep = JSONObject.fromObject(tips.getDeep());
				if(deep.isEmpty() || deep.isNullObject()) continue;
				ptnPath= (String)deep.get("ptn");
				if(StringUtils.isEmpty(ptnPath)) continue;
				
				String  s_sourcetype = tips.getS_sourceType();
				if(StringUtils.isEmpty(s_sourcetype)) continue;
												
				if ("1401".equals(s_sourcetype)) {
					tpPath="方向看板";				
				}
				
				if("1406".equals(s_sourcetype)){
					Integer integer = (Integer)deep.get("tp");
					int deep_tips_tp = integer.intValue();
					//deep_tips中tp值：1是  普通道路路口实景图，2 是 高速出口实景图， 3 是 高速入口实景图。
					if (1==deep_tips_tp) {
						tpPath="普通路口实景图";
					} else if (2==deep_tips_tp) {
						tpPath="高速出口实景图";
					} else if (3==deep_tips_tp) {
						tpPath="高速入口实景图";
					}
					
				}
				
				JSONObject feedback = JSONObject.fromObject(tips.getFeedback());
				if(feedback.isEmpty() || feedback.isNullObject()) continue;
				JSONArray farray = feedback.getJSONArray("f_array");
				if(farray.isEmpty()) continue;
				for (int i = 0; i < farray.size(); i++) {
					JSONObject jo = farray.getJSONObject(i);
					if(jo.isEmpty() || jo.isNullObject()) continue;
					int type = jo.getInt("type");
					
					// type含义：1 照片；2 语音；3 文字; 5 图片；6 草图。
					if (type == 1) {
						
						//该id对应的是Hbase库中photo表的rowkey
						String id = jo.getString("content");
						if(StringUtils.isEmpty(id)) continue;
						
						//通过rowkey从Hbase库的photo表中获取到照片
						byte[] photoInputStream = new HBaseController().getPhotoByRowkey(id);
						
						//获取该photo对应的具体信息	
			            String[] queryColNames={"attribute"};
						JSONObject tipsObject = HbaseTipsQuery.getHbaseTipsByRowkey(HBaseConnector.getInstance().getConnection().getTable(TableName
			                    .valueOf(HBaseConstant.photoTab)),id,queryColNames);
												
						JSONObject attributeObject = tipsObject.getJSONObject("attribute");
						if(attributeObject.isEmpty() || attributeObject.isNullObject()) continue;
						photoName = (String) attributeObject.get("a_fileName");
						if(StringUtils.isEmpty(photoName)) continue;

						log.debug("本次提取的照片名为: " + photoName );
						
						filePath = rootPath + "/" + tpPath + "/" + ptnPath;
						log.debug("照片将提取到: " + filePath + "文件夹下");
						
						//每次下载前，使用要下载的图片rowkey到该表查询，看对应的a_uploadDate值和last_a_uploadDate值是否相等
						//如果相等，说明已经下载过，则提示：已经下载，不能重复下载。如果不相等，则直接下载。						
							  a_uuid = attributeObject.getString("a_uuid");
						a_uploadUser = attributeObject.getInt("a_uploadUser");						
						a_uploadDate = attributeObject.getString("a_uploadDate");
						
						log.debug("照片上传的时间为: " + a_uploadDate);
						if(StringUtils.isEmpty(a_uploadDate)) continue;
												
						//查询将要下载的照片是否已经下载，没有下载过，才能正常下载，否则提示已经下载过，下载出错。						
						QueryRunner run = new QueryRunner(); 
						String selectSql = "select last_a_uploadDate from tips_photo_downloaded where rowkey = ?";
						Object[] params1 = new Object[] { id };
						last_a_uploadDate = run.queryForString(oracleConn, selectSql, params1);
						
						if (!StringUtils.isEmpty(a_uploadDate)) {
							if (!a_uploadDate.equals(last_a_uploadDate)) {	
								log.debug("照片可以正常下载");
								download2LocalFilePath(photoInputStream,filePath,photoName);
								log.debug("照片下载成功：" + id);
								
								//将下载成功的照片对应rowkey、a_uuid、a_uploadUser、a_uploadDate插入到tips_photo_downloaded表。
								String sql8 = "insert into tips_photo_downloaded(ROWKEY,LAST_A_UUID,LAST_A_UPLOADUSER,LAST_A_UPLOADDATE) values(?, ?, ?, ?)";  
								oracleConn.setAutoCommit(false);
								PreparedStatement psInsert = oracleConn.prepareStatement(sql8);  
								psInsert.setString(1,id);  
								psInsert.setString(2,a_uuid); 
								psInsert.setInt(3, a_uploadUser);
								psInsert.setString(4, a_uploadDate);
								psInsert.executeUpdate(); 
								oracleConn.commit();
								
							}else{								
								log.error("下载出错：该照片已下载过，不能重复下载。ID：" + id);
							}							
						}																										
					}								
			    }
			}
						
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		
		} finally {
			DbUtils.closeQuietly(oracleConn);
		}		
	}
	
	private static void download2LocalFilePath(byte[] inputStream,String filePath,String photoName)throws Exception{
		
		BufferedOutputStream bos = null;
		File f = new File(filePath);
		if (!f.exists()) {
			f.mkdirs();
		}
		try{
			
			bos = new BufferedOutputStream(new FileOutputStream(f + "/" + photoName));			
			bos.write(inputStream);			
			bos.flush();
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			try{
				if(bos!=null)bos.close();
			}catch(Exception e2){
				log.error(e2.getMessage(),e2);
			}
		}
	}

}
