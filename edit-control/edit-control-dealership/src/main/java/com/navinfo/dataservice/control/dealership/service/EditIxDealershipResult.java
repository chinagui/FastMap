package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.control.dealership.service.excelModel.AddChainDataEntity;
import com.navinfo.dataservice.engine.editplus.diff.BaiduGeocoding;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * 增量更新数据处理
 * 
 * 
 * */
public class EditIxDealershipResult {
	
	private static Logger log = LoggerRepos.getLogger(EditIxDealershipResult.class);
	private Geometry geo = null;
	
	/**
	 * 增量数据更新
	 * @param AddChainDataEntity 
	 * @param identification 标识，新增还是更新处理数据
	 * @param Map<String, Object> map  要加到实体中的数据
	 * @param userId
	 * @throws Exception 
	 * 
	 * */
	public void editIxDealershipResult(Connection conn, AddChainDataEntity addChainDataEntity, String identification, Map<String, Object> map, Long userId) throws Exception{
		String resultId = map.get("number").toString();
		Map<String, Object> sourceMap = new HashMap<>();
		if(StringUtils.isNotBlank(resultId)){
			sourceMap = getSourceData(conn, resultId);
		}
		if("insert".equals(identification)){
			addChainDataEntity.setWorkflowStatus(0);
			addChainDataEntity.setDealStatus(0);
			addChainDataEntity.setUserId(0);
			addChainDataEntity.setProvince(map.get("province").toString());
			addChainDataEntity.setCity(map.get("city").toString());
			addChainDataEntity.setProject(map.get("project").toString());
			addChainDataEntity.setKindCode(map.get("kindCode").toString());
			if(StringUtils.isNotBlank(map.get("chain").toString())){
				addChainDataEntity.setChain(map.get("chain").toString());
			}else{
				if(sourceMap.get("chain") != null && StringUtils.isNotBlank(sourceMap.get("chain").toString())){
					addChainDataEntity.setChain(sourceMap.get("chain").toString());
				}
			}
			addChainDataEntity.setName(map.get("name").toString());
			addChainDataEntity.setNameShort(map.get("nameShort").toString());
			addChainDataEntity.setAddress(map.get("address").toString());
			addChainDataEntity.setTelSale(map.get("telSale").toString());
			addChainDataEntity.setTelService(map.get("telService").toString());
			addChainDataEntity.setTelOther(map.get("telOther").toString());
			addChainDataEntity.setPostCode(map.get("postCode").toString());
			addChainDataEntity.setNameEng(map.get("nameEng").toString());
			addChainDataEntity.setAddressEng(map.get("addressEng").toString());
			addChainDataEntity.setProvideDate(DateUtils.longToString(System.currentTimeMillis(), DateUtils.DATE_COMPACTED_FORMAT));
			addChainDataEntity.setIsDeleted(0);
			int matchMethod = 0;
			String cfmPoiNum = "";
			if(sourceMap.get("cfmPoiNum") != null && StringUtils.isNotBlank(sourceMap.get("cfmPoiNum").toString())){
				matchMethod = 1;
				cfmPoiNum = sourceMap.get("cfmPoiNum").toString();
			}
			addChainDataEntity.setMatchMethod(matchMethod);
			addChainDataEntity.setPoiNum1(cfmPoiNum);
			addChainDataEntity.setFbSource(0);
			addChainDataEntity.setCfmStatus(0);
			addChainDataEntity.setCfmPoiNum(cfmPoiNum);
			addChainDataEntity.setCfmIsAdopted(0);
			if(sourceMap.get("sourceId") != null && StringUtils.isNotBlank(sourceMap.get("sourceId").toString())){
				addChainDataEntity.setSourceId(Integer.parseInt(sourceMap.get("sourceId").toString()));
			}
			if(StringUtils.isNotBlank(map.get("history").toString())){
				addChainDataEntity.setDealSrcDiff(Integer.valueOf(map.get("history").toString()));
			}
			if(sourceMap.get("deaCfmDate") != null && StringUtils.isNotBlank(sourceMap.get("deaCfmDate").toString())){
				addChainDataEntity.setDealCfmDate(sourceMap.get("deaCfmDate").toString());
			}
			if(matchMethod == 1){
				if(sourceMap.get("poiKindCode") != null && StringUtils.isNotBlank(sourceMap.get("poiKindCode").toString())){
					addChainDataEntity.setPoiKindCode(sourceMap.get("poiKindCode").toString());
				}
				if(sourceMap.get("poiChain") != null && StringUtils.isNotBlank(sourceMap.get("poiChain").toString())){
					addChainDataEntity.setPoiChain(sourceMap.get("poiChain").toString());
				}
				if(sourceMap.get("poiName") != null && StringUtils.isNotBlank(sourceMap.get("poiName").toString())){
					addChainDataEntity.setPoiName(sourceMap.get("poiName").toString());
				}
				if(sourceMap.get("poiNameShort") != null && StringUtils.isNotBlank(sourceMap.get("poiNameShort").toString())){
					addChainDataEntity.setPoiNameShort(sourceMap.get("poiNameShort").toString());
				}
				if(sourceMap.get("poiAddress") != null && StringUtils.isNotBlank(sourceMap.get("poiAddress").toString())){
					addChainDataEntity.setPoiAddress(sourceMap.get("poiAddress").toString());
				}
				if(sourceMap.get("poiTel") != null && StringUtils.isNotBlank(sourceMap.get("poiTel").toString())){
					addChainDataEntity.setPoiTel(sourceMap.get("poiTel").toString());
				}
				if(sourceMap.get("poiPostCode") != null && StringUtils.isNotBlank(sourceMap.get("poiPostCode").toString())){
					addChainDataEntity.setPoiPostCode(sourceMap.get("poiPostCode").toString());
				}
				if(sourceMap.get("poiXDis") != null && StringUtils.isNotBlank(sourceMap.get("poiXDis").toString())){
					addChainDataEntity.setPoiXDisplay(Double.parseDouble(sourceMap.get("poiXDis").toString()));
				}
				if(sourceMap.get("poiYDis") != null && StringUtils.isNotBlank(sourceMap.get("poiYDis").toString())){
					addChainDataEntity.setPoiYDisplay(Double.parseDouble(sourceMap.get("poiYDis").toString()));
				}
				if(sourceMap.get("poiXGui") != null && StringUtils.isNotBlank(sourceMap.get("poiXGui").toString())){
					addChainDataEntity.setPoiXGuide(Double.parseDouble(sourceMap.get("poiXGui").toString()));
				}
				if(sourceMap.get("poiYGui") != null && StringUtils.isNotBlank(sourceMap.get("poiYGui").toString())){
					addChainDataEntity.setPoiYGuide(Double.parseDouble(sourceMap.get("poiYGui").toString()));
				}
			}
			
			//处理省份信息
			String province = provideProvince(map, sourceMap);
			
			if(sourceMap.get("geometry") == null || StringUtils.isBlank(sourceMap.get("geometry").toString())){
				String addr = addChainDataEntity.getProvince()+addChainDataEntity.getCity()+addChainDataEntity.getAddress();
				Geometry poiGeo=BaiduGeocoding.geocoder(addr);
				if(poiGeo!=null){
					addChainDataEntity.setGeometry(poiGeo);
				}else{
					throw new Exception("无法获取geometry");
				}
			}else{
				addChainDataEntity.setGeometry(geo);
			}
			int regionId = getRegionIdByProvince(province);
			addChainDataEntity.setRegionId(regionId);
			insertIxDealershipResult(conn, addChainDataEntity);
			log.info("新增数据成功");
		}
		if("update".equals(identification)){
			addChainDataEntity.setProvince(map.get("province").toString());
			addChainDataEntity.setCity(map.get("city").toString());
			addChainDataEntity.setProject(map.get("project").toString());
			log.info("更新项目值为：" + map.get("project").toString());
			addChainDataEntity.setKindCode(map.get("kindCode").toString());
			addChainDataEntity.setChain(map.get("chain").toString());
			addChainDataEntity.setName(map.get("name").toString());
			addChainDataEntity.setNameShort(map.get("nameShort").toString());
			addChainDataEntity.setAddress(map.get("address").toString());
			addChainDataEntity.setTelSale(map.get("telSale").toString());
			addChainDataEntity.setTelService(map.get("telService").toString());
			addChainDataEntity.setTelOther(map.get("telOther").toString());
			addChainDataEntity.setPostCode(map.get("postCode").toString());
			addChainDataEntity.setNameEng(map.get("nameEng").toString());
			addChainDataEntity.setAddressEng(map.get("addressEng").toString());
			addChainDataEntity.setProvideDate(DateUtils.longToString(System.currentTimeMillis(), DateUtils.DATE_COMPACTED_FORMAT));
			//处理省份信息
			if(!map.get("province").toString().equals(sourceMap.get("pro").toString())){
				String province = provideProvince(map, sourceMap);
				Integer regionId = getRegionIdByProvince(province);
				addChainDataEntity.setRegionId(regionId);
			} else {
				addChainDataEntity.setRegionId(null);
			}
			addChainDataEntity.setResultId(Integer.valueOf(map.get("number").toString()));
			updateIxDealershipResult(conn, addChainDataEntity, userId);
			log.info("更新成功");
		}
	}
	
	/**
	 * 查询赋值用的source数据
	 * @throws Exception 
	 * 
	 * 
	 * */
	public Map<String, Object> getSourceData(Connection conn, String resultId) throws Exception{
		try {
			QueryRunner run = new QueryRunner();
			String sql = "select s.*,r.deal_src_diff,r.province AS pro from IX_DEALERSHIP_SOURCE s,IX_DEALERSHIP_RESULT r where r.result_id = "+resultId+" and r.source_id = s.source_id ";
			ResultSetHandler<Map<String, Object>> rs = new ResultSetHandler<Map<String, Object>>() {
				@Override
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						Map<String, Object> result = new HashMap<>();
						result.put("sourceId", rs.getInt("SOURCE_ID"));
						result.put("cfmPoiNum", rs.getString("CFM_POI_NUM"));
						result.put("deaCfmDate", rs.getString("DEAL_CFM_DATE"));
						result.put("poiKindCode", rs.getString("POI_KIND_CODE"));
						result.put("poiChain", rs.getString("POI_CHAIN"));
						result.put("poiName", rs.getString("POI_NAME"));
						result.put("poiNameShort", rs.getString("POI_NAME_SHORT"));
						result.put("poiAddress", rs.getString("POI_ADDRESS"));
						result.put("poiPostCode", rs.getString("POI_POST_CODE"));
						result.put("poiXDis", rs.getString("POI_X_DISPLAY"));
						result.put("poiYDis", rs.getString("POI_Y_DISPLAY"));
						result.put("poiXGui", rs.getString("POI_X_GUIDE"));
						result.put("poiYGui", rs.getString("POI_Y_GUIDE"));
						result.put("poiTel", rs.getString("POI_TEL"));
						result.put("chain", rs.getString("CHAIN"));
						result.put("dealSrcDiff", rs.getString("DEAL_SRC_DIFF"));
						result.put("province", rs.getString("PROVINCE"));
						result.put("deaCfmDate", rs.getString("DEAL_CFM_DATE"));
						result.put("pro", rs.getString("PRO"));
						
						//GEOMETRY
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							geo = GeoTranslator.struct2Jts(struct);
							result.put("geometry",GeoTranslator.struct2Wkt(struct));
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						return result;
					}
					return null;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			log.error(e);
			throw e;
		}
	}
	
	public void insertIxDealershipResult(Connection conn,AddChainDataEntity bean)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "insert into IX_DEALERSHIP_RESULT ";		
			
			List<String> columns = new ArrayList<String>();
			List<String> placeHolder = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			
			columns.add(" RESULT_ID ");
			placeHolder.add("RESULT_SEQ.NEXTVAL");
			
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getWorkflowStatus()))){
				columns.add(" WORKFLOW_STATUS ");
				placeHolder.add("?");
				values.add(bean.getWorkflowStatus());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getDealStatus()))){
				columns.add(" DEAL_STATUS ");
				placeHolder.add("?");
				values.add(bean.getDealStatus());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getUserId()))){
				columns.add(" USER_ID ");
				placeHolder.add("?");
				values.add(bean.getUserId());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getToInfoDate())){
				columns.add(" TO_INFO_DATE ");
				placeHolder.add("?");
				values.add(bean.getToInfoDate());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getToClientDate())){
				columns.add(" TO_CLIENT_DATE ");
				placeHolder.add("?");
				values.add(bean.getToClientDate());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getProvince())){
				columns.add(" PROVINCE ");
				placeHolder.add("?");
				values.add(bean.getProvince());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getCity())){
				columns.add(" CITY ");
				placeHolder.add("?");
				values.add(bean.getCity());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getProject())){
				columns.add(" PROJECT ");
				placeHolder.add("?");
				values.add(bean.getProject());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getKindCode())){
				columns.add(" KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getKindCode());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getChain())){
				columns.add(" CHAIN ");
				placeHolder.add("?");
				values.add(bean.getChain());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getName())){
				columns.add(" NAME ");
				placeHolder.add("?");
				values.add(bean.getName());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getNameShort())){
				columns.add(" NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getNameShort());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getAddress())){
				columns.add(" ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getAddress());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getTelSale())){
				columns.add(" TEL_SALE ");
				placeHolder.add("?");
				values.add(bean.getTelSale());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getTelService())){
				columns.add(" TEL_SERVICE ");
				placeHolder.add("?");
				values.add(bean.getTelService());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getTelOther())){
				columns.add(" TEL_OTHER ");
				placeHolder.add("?");
				values.add(bean.getTelOther());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPostCode())){
				columns.add(" POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPostCode());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getNameEng())){
				columns.add(" NAME_ENG ");
				placeHolder.add("?");
				values.add(bean.getNameEng());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getAddressEng())){
				columns.add(" ADDRESS_ENG ");
				placeHolder.add("?");
				values.add(bean.getAddressEng());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getProvideDate())){
				columns.add(" PROVIDE_DATE ");
				placeHolder.add("?");
				values.add(bean.getProvideDate());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getIsDeleted()))){
				columns.add(" IS_DELETED ");
				placeHolder.add("?");
				values.add(bean.getIsDeleted());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getMatchMethod()))){
				columns.add(" MATCH_METHOD ");
				placeHolder.add("?");
				values.add(bean.getMatchMethod());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum1())){
				columns.add(" POI_NUM_1 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum1());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum2())){
				columns.add(" POI_NUM_2 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum2());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum3())){
				columns.add(" POI_NUM_3 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum3());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum4())){
				columns.add(" POI_NUM_4 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum4());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum5())){
				columns.add(" POI_NUM_5 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum5());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getSimilarity())){
				columns.add(" SIMILARITY ");
				placeHolder.add("?");
				values.add(bean.getSimilarity());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getFbSource()))){
				columns.add(" FB_SOURCE ");
				placeHolder.add("?");
				values.add(bean.getFbSource());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getFbContent())){
				columns.add(" FB_CONTENT ");
				placeHolder.add("?");
				values.add(bean.getFbContent());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getFbAuditRemark())){
				columns.add(" FB_AUDIT_REMARK ");
				placeHolder.add("?");
				values.add(bean.getFbAuditRemark());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getFbDate())){
				columns.add(" FB_DATE ");
				placeHolder.add("?");
				values.add(bean.getFbDate());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getCfmStatus()))){
				columns.add(" CFM_STATUS ");
				placeHolder.add("?");
				values.add(bean.getCfmStatus());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getCfmPoiNum())){
				columns.add(" CFM_POI_NUM ");
				placeHolder.add("?");
				values.add(bean.getCfmPoiNum());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getCfmMemo())){
				columns.add(" CFM_MEMO ");
				placeHolder.add("?");
				values.add(bean.getCfmMemo());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getSourceId()))){
				columns.add(" SOURCE_ID ");
				placeHolder.add("?");
				values.add(bean.getSourceId());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getDealSrcDiff()))){
				columns.add(" DEAL_SRC_DIFF ");
				placeHolder.add("?");
				values.add(bean.getDealSrcDiff());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getDealCfmDate())){
				columns.add(" DEAL_CFM_DATE ");
				placeHolder.add("?");
				values.add(bean.getDealCfmDate());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiKindCode())){
				columns.add(" POI_KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiKindCode());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiChain())){
				columns.add(" POI_CHAIN ");
				placeHolder.add("?");
				values.add(bean.getPoiChain());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiName())){
				columns.add(" POI_NAME ");
				placeHolder.add("?");
				values.add(bean.getPoiName());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNameShort())){
				columns.add(" POI_NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getPoiNameShort());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiAddress())){
				columns.add(" POI_ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getPoiAddress());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiTel())){
				columns.add(" POI_TEL ");
				placeHolder.add("?");
				values.add(bean.getPoiTel());
			};
			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiPostCode())){
				columns.add(" POI_POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiPostCode());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiXDisplay()))){
				columns.add(" POI_X_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiXDisplay());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiYDisplay()))){
				columns.add(" POI_Y_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiYDisplay());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiXGuide()))){
				columns.add(" POI_X_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiXGuide());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiYGuide()))){
				columns.add(" POI_Y_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiYGuide());
			};
			if (bean!=null&&bean.getGeometry()!=null){
				columns.add(" GEOMETRY ");
				placeHolder.add("?");
				STRUCT struct;
//				struct = JGeometry.store(conn, geo);
				struct = GeoTranslator.wkt2Struct(conn,  GeoTranslator.jts2Wkt(bean.getGeometry()));
				values.add(struct);
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getRegionId()))){
				columns.add(" REGION_ID ");
				placeHolder.add("?");
				values.add(bean.getRegionId());
			};
			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getCfmIsAdopted()))){
				columns.add(" CFM_IS_ADOPTED ");
				placeHolder.add("?");
				values.add(bean.getCfmIsAdopted());
			};
			
			if(!columns.isEmpty()){
				String columsStr = "(" + StringUtils.join(columns.toArray(),",") + ")";
				String placeHolderStr = "(" + StringUtils.join(placeHolder.toArray(),",") + ")";
				createSql = createSql + columsStr + " values " + placeHolderStr;
			}
			log.info("createSql:"+createSql);
			run.update(conn, createSql, values.toArray() );
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	public void updateIxDealershipResult(Connection conn,AddChainDataEntity bean, Long userId)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			List<String> columns = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			
			String updateSql = "update IX_DEALERSHIP_RESULT set ";
//			produceSqlBody(bean, conn);

//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getWorkflowStatus()))){
//				columns.add(" WORKFLOW_STATUS=?  ");
//				values.add(bean.getWorkflowStatus());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getDealStatus()))){
//				columns.add(" DEAL_STATUS=?  ");
//				values.add(bean.getDealStatus());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getUserId()))){
//				columns.add(" USER_ID=?  ");
//				values.add(bean.getUserId());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getToInfoDate())){
//				columns.add(" TO_INFO_DATE=? ");
//				values.add(bean.getToInfoDate());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getToClientDate())){
//				columns.add(" TO_CLIENT_DATE=?  ");
//				values.add(bean.getToClientDate());
//			};
			columns.add(" PROVINCE=?  ");
			values.add(bean.getProvince());
			columns.add(" CITY=?  ");
			values.add(bean.getCity());
			columns.add(" PROJECT=?  ");
			values.add(bean.getProject());
			log.info("项目值为:" + bean.getProject());
			columns.add(" KIND_CODE=?  ");
			values.add(bean.getKindCode());
			columns.add(" CHAIN=?  ");
			values.add(bean.getChain());
			columns.add(" NAME=?  ");
			values.add(bean.getName());
			columns.add(" NAME_SHORT=?  ");
			values.add(bean.getNameShort());
			columns.add(" ADDRESS=?  ");
			values.add(bean.getAddress());
			columns.add(" TEL_SALE=?  ");
			values.add(bean.getTelSale());
			columns.add(" TEL_SERVICE=?  ");
			values.add(bean.getTelService());
			columns.add(" TEL_OTHER=?  ");
			values.add(bean.getTelOther());
			columns.add(" POST_CODE=?  ");
			values.add(bean.getPostCode());
			columns.add(" NAME_ENG=?  ");
			values.add(bean.getNameEng());
			columns.add(" ADDRESS_ENG=?  ");
			values.add(bean.getAddressEng());
			columns.add(" PROVIDE_DATE=?  ");
			values.add(bean.getProvideDate());
			if(bean.getRegionId() != null){
				columns.add(" REGION_ID=?  ");
				values.add(bean.getRegionId());
			}
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getIsDeleted()))){
//				columns.add(" IS_DELETED=?  ");
//				values.add(bean.getIsDeleted());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getMatchMethod()))){
//				columns.add(" MATCH_METHOD=?  ");
//				values.add(bean.getMatchMethod());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum1())){
//				columns.add(" POI_NUM_1=?  ");
//				values.add(bean.getPoiNum1());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum2())){
//				columns.add(" POI_NUM_2=?  ");
//				values.add(bean.getPoiNum2());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum3())){
//				columns.add(" POI_NUM_3=?  ");
//				values.add(bean.getPoiNum3());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum4())){
//				columns.add(" POI_NUM_4=?  ");
//				values.add(bean.getPoiNum4());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNum5())){
//				columns.add(" POI_NUM_5=?  ");
//				values.add(bean.getPoiNum5());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getSimilarity())){
//				columns.add(" SIMILARITY=?  ");
//				values.add(bean.getSimilarity());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getFbSource()))){
//				columns.add(" FB_SOURCE=?  ");
//				values.add(bean.getFbSource());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getFbContent())){
//				columns.add(" FB_CONTENT=?  ");
//				values.add(bean.getFbContent());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getFbAuditRemark())){
//				columns.add(" FB_AUDIT_REMARK=?  ");
//				values.add(bean.getFbAuditRemark());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getFbDate())){
//				columns.add(" FB_DATE=?  ");
//				values.add(bean.getFbDate());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getCfmStatus()))){
//				columns.add(" CFM_STATUS=?  ");
//				values.add(bean.getCfmStatus());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getCfmPoiNum())){
//				columns.add(" CFM_POI_NUM=?  ");
//				values.add(bean.getCfmPoiNum());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getCfmMemo())){
//				columns.add(" CFM_MEMO=?  ");
//				values.add(bean.getCfmMemo());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getSourceId()))){
//				columns.add(" SOURCE_ID=?  ");
//				values.add(bean.getSourceId());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getDealSrcDiff()))){
//				columns.add(" DEAL_SRC_DIFF=?  ");
//				values.add(bean.getDealSrcDiff());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getDealCfmDate())){
//				columns.add(" DEAL_CFM_DATE=?  ");
//				values.add(bean.getDealCfmDate());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiKindCode())){
//				columns.add(" POI_KIND_CODE=?  ");
//				values.add(bean.getPoiKindCode());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiChain())){
//				columns.add(" POI_CHAIN=? ");
//				values.add(bean.getPoiChain());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiName())){
//				columns.add(" POI_NAME=?  ");
//				values.add(bean.getPoiName());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiNameShort())){
//				columns.add(" POI_NAME_SHORT=?  ");
//				values.add(bean.getPoiNameShort());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiAddress())){
//				columns.add(" POI_ADDRESS=?  ");
//				values.add(bean.getPoiAddress());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiTel())){
//				columns.add(" POI_TEL=?  ");
//				values.add(bean.getPoiTel());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(bean.getPoiPostCode())){
//				columns.add(" POI_POST_CODE=?  ");
//				values.add(bean.getPoiPostCode());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiXDisplay()))){
//				columns.add(" POI_X_DISPLAY=?  ");
//				values.add(bean.getPoiXDisplay());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiYDisplay()))){
//				columns.add(" POI_Y_DISPLAY=?  ");
//				values.add(bean.getPoiYDisplay());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiXGuide()))){
//				columns.add(" POI_X_GUIDE=?  ");
//				values.add(bean.getPoiXGuide());
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getPoiYGuide()))){
//				columns.add(" POI_Y_GUIDE=?  ");
//				values.add(bean.getPoiYGuide());
//			};
//			if (bean!=null&&bean.getGeometry()!=null){
//				columns.add(" GEOMETRY=?  ");
//				STRUCT struct;
//				struct = JGeometry.store(conn, geo);
////				struct = GeoTranslator.wkt2Struct(conn,  GeoTranslator.jts2Wkt(bean.getGeometry()));
//				values.add(struct);
//			};
//			if (bean!=null&&StringUtils.isNotBlank(String.valueOf(bean.getCfmIsAdopted()))){
//				columns.add(" CFM_IS_ADOPTED=?  ");
//				values.add(bean.getCfmIsAdopted());
//			};

			if(!columns.isEmpty()){
				String columsStr = StringUtils.join(columns.toArray(),",");
				updateSql = updateSql + columsStr + "  where RESULT_ID=" + bean.getResultId();
				log.info("updateSql:"+updateSql);
				run.update(conn, updateSql, values.toArray() );
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 根据省份查询region_id
	 * @throws SQLException 
	 * @throws Exception 
	 * 
	 * */
	public Integer getRegionIdByProvince(String province) throws SQLException{
		Connection conn = null;
		try{
			if(StringUtils.isBlank(province)){
				log.info("省份信息为空，无法获取大区库regionId");
				return null;
			}
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String sql = "select t.region_id from CP_REGION_PROVINCE t where t.province = '"+province+"'";
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					Integer regionId = null;
					if(rs.next()){
						regionId = rs.getInt("REGION_ID");
					}
					return regionId;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			log.error("根据省份名称查询regionId异常："+e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}
	
	/**
	 * 根据查分结果判断新增或更新数据时省份的取值
	 * 
	 * 
	 * */
	public String provideProvince(Map<String, Object> map, Map<String, Object> sourceMap){
		//根据IX_DEALERSHIP_RESULT.PROVINCE关联cp_region_province.province,查找对应的region_id赋值；
		//针对差分结果为删除的记录即deal_src_diff＝2，取source.province关联
		String province = null;
		if(map.containsKey("province")){
			province = map.get("province").toString();
		}
		if(sourceMap.get("dealSrcDiff") != null && StringUtils.isNotBlank(sourceMap.get("dealSrcDiff").toString()) && "2".equals(sourceMap.get("dealSrcDiff").toString())){
			province = sourceMap.get("province").toString();
		}
		return province;
	}
	
}
