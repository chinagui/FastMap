package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlag;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 
 * @Title:FMBATD20007
 * @Package:com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule
 * @Description: 
 *  批处理原则：
	(1)若POI的官方原始中文名称与SC_POINT_MINGAN_LIST中COMEFROM为“POI”且STATE为“D”或“T”的记录的NAME相同，则标识位日出品敏感数据，即IX_POI_FLAG新增一条记录，FLAG_CODE为110009010001，若该POI已存在（非删除）此FLAG_CODE，则不处理；
	(2)若不满足(1),则判断该POI的PID和官方原始中文名称名称同时关联SC_POINT_NOMINGAN_LIST中PID和NAME，若记录匹配，且该POI的FLAG_CODE为110009010001（非删除记录），则逻辑删除该ix_poi_flag记录，否则不处理；
	(3)若不满足(1)和(2),则判断如下：
	①SC_SENSITIVE_WORDS敏感关键字配置表中type=4的记录，读取KIND_CODE字段，若POI的分类在这些分类中，且该POI的FLAG_CODE为110009010001（非删除记录），则逻辑删除该ix_poi_flag记录，否则不处理；
	②SC_SENSITIVE_WORDS敏感关键字配置表中type=3的记录，若poi的分类在(230126、230127、230128)且POI的行政区划与ADMINCODE（必然有值）相同，并且poi名称包含SENSITIVE_WORD、SENSITIVE_WORD2其中之一（SENSITIVE_WORD2可能为空）且该POI的FLAG_CODE为110009010001（非删除记录），则逻辑删除该ix_poi_flag记录，否则不处理；
	③不满足①和②时，则读取SC_SENSITIVE_WORDS敏感关键字配置表中type=1、2的记录，
	若官方原始中文名称包含敏感字（见备注），则IX_POI_FLAG新增一条记录，FLAG_CODE为110009010001，若该POI已存在（非删除）此FLAG_CODE，则不处理；
	备注：
	敏感关键字配置表判断方式：
	①SENSITIVE_WORD字段中若为“%机场%”的记录，不使用即不参与判断；
	②敏感关键字配置表中SENSITIVE_WORD、SENSITIVE_WORD2、KIND_CODE、ADMINCODE四个字段记录敏感字信息，四个字段可能部分有值，多个有值时，表示需要同时满足，才算包含敏感字，SENSITIVE_WORD和SENSITIVE_WORD2为具体的敏感词字段，均对应POI的官方原始中文名称；KIND_CODE、ADMINCODE为条件字段，分别对应POI的kindCode、regiong_id对应的行政区划。四个字段内均可能有通配符%，表示“包含”，及敏感字出现的位置，没有通配符的字段值，表示“相等”详见示例。
	③单独对应一：包含连续三位及三位以上数字（全半角阿拉伯数字0~9、汉字零、一、二、三~十、还有表示零的“〇”），并且包含“工厂”；
	④单独对应二：末尾包含“炼油厂”，但官方原始中文名称不包含“精”；
 * @author:Jarvis 
 * @date: 2017年9月20日
 */
public class FMBATD20007 extends BasicBatchRule {
	
	private static MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	
	private List<String> scPointMinganNameList = new ArrayList<String>();
	
	private List<String> scSensitiveWordsType4KindList = new ArrayList<String>();
	
	private Map<Long,Long> pidAdminId = new HashMap<Long, Long>();
	
	private Map<Integer, List<ScSensitiveWordsObj>> scSensitiveWordsMap = new HashMap<>();

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		log.info("FMBATD20007 start loadReferDatas ---");
		
		log.info("FMBATD20007 start getScPointMinganNameList  ---");
		scPointMinganNameList = getScPointMinganNameList();
		log.info("FMBATD20007 end getScPointMinganNameList  ---");
		
		log.info("FMBATD20007 start scSensitiveWordsType4KindList  ---");
		scSensitiveWordsType4KindList = getScSensitiveWordsType4KindList();
		log.info("FMBATD20007 end scSensitiveWordsType4KindList  ---");
		
		log.info("FMBATD20007 start scSensitiveWordsMap  ---");
		scSensitiveWordsMap	= metadataApi.scSensitiveWordsMap();
		log.info("FMBATD20007 end scSensitiveWordsMap  ---");
		
		Set<Long> pidList = new HashSet<>();

        for (BasicObj obj : batchDataList) {

            pidList.add(obj.objPid());
        }
        
		pidAdminId = IxPoiSelector.getAdminIdByPids(getBatchRuleCommand().getConn(), pidList);
		
		log.info("FMBATD20007 end loadReferDatas ---");
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHIName();
			
			log.info("FMBATD20007 start runBatch pid---" +poi.getPid());
			
			if(poi.getOpType().equals(OperationType.INSERT)||ixPoiName.getOpType().equals(OperationType.UPDATE)){
				long pid = poi.getPid();
			
				String name = ixPoiName.getName();
				
				String kindCode = poi.getKindCode();
				
				String adminCode = null;
				if(pidAdminId != null && pidAdminId.containsKey(pid)){
					adminCode = pidAdminId.get(pid).toString();
				}
				
				IxPoiFlag ixPoiFlag = getFlagCode(poiObj);
				
				log.info("FMBATD20007 start suitContidion1 pid---" +poi.getPid());
				if(suitContidion1(name)){
					if(ixPoiFlag == null){
						addIxPoiFlag(poiObj);
						log.info("FMBATD20007 end suitContidion1 pid---" +poi.getPid());
					}else{
						return;
					}
				}
				
				log.info("FMBATD20007 start suitContidion2 pid---" +poi.getPid());
				if(suitContidion2(pid,name)){
					if(ixPoiFlag != null){
						ixPoiFlag.setOpType(OperationType.DELETE);
						log.info("FMBATD20007 end suitContidion2 pid---" +poi.getPid());
					}else{
						return;
					}
				}
				
				log.info("FMBATD20007 start suitContidion3 pid---" +poi.getPid());
				if(suitContidion3(kindCode)){
					if(ixPoiFlag != null){
						ixPoiFlag.setOpType(OperationType.DELETE);
						log.info("FMBATD20007 end suitContidion3 pid---" +poi.getPid());
					}else{
						return;
					}
				}
				
				log.info("FMBATD20007 start suitContidion4 pid---" +poi.getPid());
				if(suitContidion4(name,kindCode,adminCode)){
					if(ixPoiFlag != null){
						ixPoiFlag.setOpType(OperationType.DELETE);
						log.info("FMBATD20007 end suitContidion4 pid---" +poi.getPid());
					}else{
						return;
					}
				}
				
				log.info("FMBATD20007 start suitContidion5 pid---" +poi.getPid());
				if(suitContidion5(name, kindCode,adminCode)){
					if(ixPoiFlag == null){
						addIxPoiFlag(poiObj);
						log.info("FMBATD20007 end suitContidion5 pid---" +poi.getPid());
					}else{
						return;
					}
				}
				
			}
			
			log.info("FMBATD20007 end runBatch pid---" +poi.getPid());
		}
		
	}
	
	/**
	 * 读取SC_SENSITIVE_WORDS敏感关键字配置表中type=1、2的记录，官方原始中文名称包含敏感字（见备注）
	 * @param name
	 * @param kindCode
	 * @param adminCode
	 * @return
	 */
	private boolean suitContidion5(String name,String kindCode,String adminCode){
		List<ScSensitiveWordsObj> list = scSensitiveWordsMap.get(1);
		List<ScSensitiveWordsObj> list1 = scSensitiveWordsMap.get(2);
		list.addAll(list1);
		boolean flag1 = false,flag2 = false;
		String regex = ".*[零一二三四五六七八九十〇0-9０-９]{3,}.*";
		if(Pattern.matches(regex, String.valueOf(name)) && name.contains("工厂")){
			flag1 = true;
		}
		if(name.endsWith("炼油厂") && !name.contains("精")){
			flag2 = true;
		}
		
		
		if(list!=null && !list.isEmpty()){
			for (ScSensitiveWordsObj sc : list) {
				String word = sc.getSensitiveWord();
				if(StringUtils.isNotBlank(word) && word.equals("%机场%")){
					continue;
				}
				
				if(flag1 || flag2){
					return true;
				}
				
				int count = 0;
				int reCount = 0;
				
				String word1 = sc.getRegexSensitiveWord();
				int wordType1 = sc.getRegexWordType();
				if(word1 != null && !word1.isEmpty()){
					count += 1;
					if((wordType1 == 0 && Pattern.matches(word1, String.valueOf(name)))||
							(wordType1 == 1 && !Pattern.matches(word1, String.valueOf(name)))){
						reCount += 1;
					}
				}
				
				String word2 = sc.getRegexSensitiveWord2();
				int wordType2 = sc.getRegexWordType2();
				if(word2 != null && !word2.isEmpty()){
					count += 1;
					if((wordType2 == 0 && Pattern.matches(word2, String.valueOf(name)))||
							(wordType2 == 1 && !Pattern.matches(word2, String.valueOf(name)))){
						reCount += 1;
					}
				}
				
				String kindTmp = sc.getRegexKindCode();
				if(kindTmp != null && !kindTmp.isEmpty()){
					count += 1;
					if(Pattern.matches(kindTmp, String.valueOf(kindCode))){
						reCount += 1;
					}
				}
				
				
				String adminTmp = sc.getRegexAdmincode();
				if(adminTmp != null && !adminTmp.isEmpty( )){
					count += 1;
					if(Pattern.matches(adminTmp, String.valueOf(adminCode))){
						reCount += 1;
					}
				}
				
				if(reCount == count){
					return true;
				}
				
			}
		}
		
		return false;
	}
	
	
	/**
	 * SC_SENSITIVE_WORDS敏感关键字配置表中type=3的记录，若poi的分类在(230126、230127、230128)且POI的行政区划与ADMINCODE（必然有值）相同，
	 * 并且poi名称包含SENSITIVE_WORD、SENSITIVE_WORD2其中之一（SENSITIVE_WORD2可能为空）且该POI的FLAG_CODE为110009010001（非删除记录），
	 * 则逻辑删除该ix_poi_flag记录，否则不处理
	 * @param name
	 * @param kindCode
	 * @param adminCode
	 * @return
	 */
	private boolean suitContidion4(String name,String kindCode,String adminCode){
		if((!kindCode.equals("230126")) && (!kindCode.equals("230127")) && (!kindCode.equals("230128"))){
			return false;
		}
		List<ScSensitiveWordsObj> list = scSensitiveWordsMap.get(3);
		if(list!=null && !list.isEmpty()){
			for (ScSensitiveWordsObj sc : list) {
				String adminTmp=sc.getRegexAdmincode();
				if(adminTmp!=null&&!adminTmp.isEmpty()&&!Pattern.matches(adminTmp, String.valueOf(adminCode))){
					continue;
				}
				String word1 = sc.getRegexSensitiveWord();
				if(word1!=null&&!word1.isEmpty()){
					if(Pattern.matches(word1, String.valueOf(name))){
						return true;
					}
				}
				String word2 = sc.getRegexSensitiveWord2();
				if(word2!=null&&!word2.isEmpty()){
					if(Pattern.matches(word2, String.valueOf(name))){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * SC_SENSITIVE_WORDS敏感关键字配置表中type=4的记录，读取KIND_CODE字段，若POI的分类在这些分类中，
	 * 且该POI的FLAG_CODE为110009010001（非删除记录），则逻辑删除该ix_poi_flag记录，否则不处理；
	 * @param kindCode
	 * @param ixPoiFlag
	 * @return
	 */
	private boolean suitContidion3(String kindCode){
		return scSensitiveWordsType4KindList.contains(kindCode);
	}	

	
	/**
	 * 判断该POI的PID和官方原始中文名称名称同时关联SC_POINT_NOMINGAN_LIST中PID和NAME，
	 * 若记录匹配，且该POI的FLAG_CODE为110009010001（非删除记录），则逻辑删除该ix_poi_flag记录，否则不处理
	 * @param pid
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	private boolean suitContidion2(long pid, String name) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql  = "SELECT COUNT(1) FROM SC_POINT_NOMINGAN_LIST WHERE pid = "+pid+" AND NAME = '"+name+"'";
			
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			if(rs.next() && rs.getInt(1)>0){
				return true;
			}
			
			return false;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}

	/**
	 * POI的官方原始中文名称与SC_POINT_MINGAN_LIST中COMEFROM为“POI”且STATE为“D”或“T”的记录的NAME相同，
	 * 则标识位日出品敏感数据，即IX_POI_FLAG新增一条记录，FLAG_CODE为110009010001，若该POI已存在（非删除）此FLAG_CODE，则不处理
	 * @param name
	 * @param ixPoiFlag
	 * @return
	 */
	private boolean suitContidion1(String name){
		return scPointMinganNameList.contains(name);
	}	

	
	/**
	 * 新增flagCode为110009010001的IxPoiFlag
	 * @param poiObj
	 * @param flagCode
	 * @throws Exception
	 */
	private void addIxPoiFlag(IxPoiObj poiObj) throws Exception {
		IxPoiFlag ixPoiFlag=poiObj.createIxPoiFlag();
		ixPoiFlag.setFlagCode("110009010001");
		ixPoiFlag.setPoiPid(poiObj.getMainrow().getObjPid());
	}
	
	/**
	 * 判断该POI是否存在FLAG_CODE为110009010001（非删除记录）
	 * @param poiObj
	 * @return
	 * @throws Exception
	 */
	public IxPoiFlag getFlagCode(IxPoiObj poiObj) throws Exception {

        List<IxPoiFlag> ixPoiFlags = poiObj.getIxPoiFlags();

        if (ixPoiFlags != null && ixPoiFlags.size()>0) {

            for (IxPoiFlag flag : ixPoiFlags) {

                if (!flag.getOpType().equals(OperationType.DELETE) && flag.getFlagCode().equals("110009010001")) {

                    return flag;
                }
            }
            
        }
		return null;
     }
	
	
	/**
	 * 获取SC_POINT_MINGAN_LIST中COMEFROM为“POI”且STATE为“D”或“T”的记录的NAME集合
	 * @return
	 * @throws Exception
	 */
	private List<String> getScPointMinganNameList() throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql  = "SELECT NAME FROM SC_POINT_MINGAN_LIST WHERE COMEFROM = 'POI' AND (STATE = 'D' OR STATE = 'T')";
			
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				log.info("FMBATD20007 addScPointMinganNameList  name---"+rs.getString(1));
				scPointMinganNameList.add(rs.getString(1));
			}
			
			
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
		return scPointMinganNameList;
	}
	
	/**
	 * 获取SC_SENSITIVE_WORDS敏感关键字配置表中type=4的KIND_CODE集合
	 * @return
	 * @throws Exception
	 */
	private List<String> getScSensitiveWordsType4KindList() throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql  = "SELECT DISTINCT KIND_CODE FROM SC_SENSITIVE_WORDS WHERE TYPE = 4 AND KIND_CODE IS NOT NULL";
			
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
		
			while(rs.next()){
				log.info("FMBATD20007 add scSensitiveWordsType4KindList  kindCode---"+rs.getString(1));
				scSensitiveWordsType4KindList.add(rs.getString(1));
			}
			
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
		return scSensitiveWordsType4KindList;
	}
	

}
