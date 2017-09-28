package com.navinfo.dataservice.engine.script;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.navinfo.dataservice.commons.util.DateUtils;
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
	static String savePath;		
	static String rootPath="PhotoFetch"; 
	static String tpPath;
	static String ptnPath;
	static String filePath;
	static String rowkey;
	static String t_operateDate;
	static String photoName;
	static String dl_t_operateDate;	
	static String download_time;
	static String meshId;
	static String input_s_sourcetype;
	static int count;
	
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
	        
	        // mesh范围
	        String meshIds = props.getProperty("meshIds");
	        
	        // s_sourcetype取值范围，1401代表方向看板，1406代表实景图
	        String input_s_sourcetype = props.getProperty("input_s_sourcetype");
	        
	        	        
	        //用户指定的照片保存路径
	        String savePath = props.getProperty("savePath");	       	       

	        if (StringUtils.isEmpty(meshIds)) {
	            log.error("没有输入图符号，请输入图符号");
	            return;
	        }
	        
	        if (StringUtils.isEmpty(input_s_sourcetype)) {
	            log.error("没有输入提取类型，请输入提取类型");
	            return;
	        }
	        
	        if (StringUtils.isEmpty(savePath)) {
	            log.error("没有输入指定路径，请输入指定路径");
	            return;
	        }
	        	        	        
	        List<String> meshList = new ArrayList<>(Arrays.asList(meshIds
                  .split(",")));
	        
	        List<String> typeList = new ArrayList<>(Arrays.asList(input_s_sourcetype
                  .split(",")));
	        
	        // 从配置信息中获取meshId、input_s_sourcetype,按照图幅范围及type类型提取照片。
	        for (String mid : meshList) {
				for (String type : typeList) {
					downloadPhoto(mid,type,savePath);
				}
								
			}
	        if (count == 0) {
	        	 log.error("你要下载的图幅上，没有未下载过的符合要求的数据。请确认你要下载的图幅中有上次下载之后，更新过的tips，以及配置的meshIds中是否有input_s_sourcetype类型的数据");				
			}else{
				log.debug("本次照片下载任务已完成。共下载了" + count + "条tips数据");
			}
	        System.exit(0);
						
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
	
	public static void downloadPhoto(String meshId,String sourceType,String savePath){
		
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
				
				String rowkey = tips.getId();
				String t_operateDate = tips.getT_operateDate();
				
				//查询将要下载的照片是否已经下载，没有下载过，才能正常下载，否则提示已经下载过，下载出错。						
				QueryRunner run = new QueryRunner(); 
				String selectSql1 = "select dl_t_operateDate from tips_photo_downloaded where rowkey = ?";
				String selectSql2 = "select download_time from tips_photo_downloaded where rowkey = ?";
				
				Object[] params1 = new Object[] { rowkey };
				dl_t_operateDate = run.queryForString(oracleConn, selectSql1, params1);
				download_time = run.queryForString(oracleConn, selectSql2, params1);
												
				//已下载过的tips的rowkey和t_operateDate记录在临时表中
				//如果要下载的tips在临时表中不存在，则直接下载
					if (StringUtils.isEmpty(dl_t_operateDate)) {
						//生成照片下载时间
						String downloadTime = getDownloadTime();  
						//将下载成功的照片对应rowkey、a_uuid、a_uploadUser、a_uploadDate插入到tips_photo_downloaded表。
						String sql8 = "insert into tips_photo_downloaded(ROWKEY,DL_T_OPERATEDATE,DOWNLOAD_TIME) values(?, ?, ?)";  					
						oracleConn.setAutoCommit(false);
						PreparedStatement psInsert = oracleConn.prepareStatement(sql8);  
						psInsert.setString(1,rowkey);  
						psInsert.setString(2,t_operateDate); 
						psInsert.setString(3, downloadTime);
						psInsert.executeUpdate(); 
						oracleConn.commit();	
						
						download(tips,savePath);
						count++;
					}
								
				//如果要下载的tips在临时表中存在，判断该tips的t_operateDate时间是否比临时表中的dl_t_operateDate时间晚
				//如果晚，则下载，否则提示已经下载过。
						else {
							if (StrToDate(t_operateDate).after(StrToDate(dl_t_operateDate))) {	
								
								String downloadTime = getDownloadTime(); 
								String sql9 = "update tips_photo_downloaded set dl_t_operateDate = ?,download_time = ? where rowkey = ?";  
								
								oracleConn.setAutoCommit(false);
								PreparedStatement psInsert = oracleConn.prepareStatement(sql9);  
								psInsert.setString(1,t_operateDate);  
								psInsert.setString(2,downloadTime); 
								psInsert.setString(3,rowkey);
								psInsert.executeUpdate(); 
								oracleConn.commit();		
								
								download(tips,savePath);
								count++;
								
								log.debug("照片下载成功,tips的Rowkey是：" + rowkey);						
							}else{								
								log.error("下载出错：rowkey为" + rowkey + "的tips的照片已经于" + download_time + "下载过。");
								continue;
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
	
	private static void download(TipsDao tips,String savePath) throws Exception {
		
		JSONObject deep = JSONObject.fromObject(tips.getDeep());
		if(deep.isEmpty() || deep.isNullObject()){
			return;
			}
		ptnPath= (String)deep.get("ptn");
		if(StringUtils.isEmpty(ptnPath)){
			return;
			}
		
		String  s_sourcetype = tips.getS_sourceType();
		if(StringUtils.isEmpty(s_sourcetype)){
			return;
			}
										
		if ("1401".equals(s_sourcetype)) {
			//SIGNBOARD：方向看板
			tpPath="SIGNBOARD";				
		}
		
		if("1406".equals(s_sourcetype)){
			Integer integer = (Integer)deep.get("tp");
			int deep_tips_tp = integer.intValue();
			//deep_tips中tp值：1是  普通道路路口实景图，2 是 高速出口实景图， 3 是 高速入口实景图。
			if (1==deep_tips_tp) {
				//REALIMAGE1：普通路口实景图
				tpPath="REALIMAGE1";
			} else if (2==deep_tips_tp) {
				//REALIMAGE2：高速出口实景图
				tpPath="REALIMAGE2";
			} else if (3==deep_tips_tp) {
				//REALIMAGE3：高速入口实景图
				tpPath="REALIMAGE3";
			}
			
		}
		
		
		
		JSONObject feedback = JSONObject.fromObject(tips.getFeedback());
		if(feedback.isEmpty() || feedback.isNullObject()){
			return;
			}
		JSONArray farray = feedback.getJSONArray("f_array");
		if(farray.isEmpty()){
			return;
			}
		for (int i = 0; i < farray.size(); i++) {
			JSONObject jo = farray.getJSONObject(i);
			if(jo.isEmpty() || jo.isNullObject()){
				return;
				}
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
				
				filePath = savePath + "/" + rootPath + "/" + tpPath + "/" + ptnPath;
				log.debug("照片将提取到: " + filePath + "文件夹下");
				
				download2LocalFilePath(photoInputStream,filePath,photoName);																													
			}								
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
	
	public static Date StrToDate(String str) throws ParseException{  
	     
        return DateUtils.stringToDate(str, DateUtils.DATE_COMPACTED_FORMAT);
    }  
	
	public static String getDownloadTime() {
		Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.DATE_COMPACTED_FORMAT);
		String curdate = simpleDateFormat.format(date);
		return curdate;
	}
	 

}
