package com.navinfo.dataservice.datahub.glm;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/** 
* @ClassName: GlmGrid 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午4:28:47 
* @Description: TODO
*/
public class GlmGridRefInfo {
	private String tableName;
	private String gridRefCol;
	private List<String[]> refInfo;//{"第一层参考表","关联的参考表的字段，一般为主键","参考表本身参考其他表的参考字段,如果没有，则为空字符串"}
	private String selectSqlPart;
	private String conditionSqlPart;
	private String meshIdTable;//
	
	public GlmGridRefInfo(String tableName){
		this.tableName=tableName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getGridRefCol() {
		return gridRefCol;
	}
	public void setGridRefCol(String gridRefCol) {
		this.gridRefCol = gridRefCol;
	}
	public List<String[]> getRefInfo() {
		return refInfo;
	}
	public void setRefInfo(List<String[]> refInfo) {
		this.refInfo = refInfo;
		generateSqlPart();
	}
	public String getSelectSqlPart(){
		return selectSqlPart;
	}
	public String getConditionSqlPart(){
		return conditionSqlPart;
	}
	/**
	 * 
	 * @param name:cross user name
	 * @return
	 */
	public String replaceSelectSqlPartByCrossUser(String crossUserName){
		String s = null;
		if(StringUtils.isNotEmpty(selectSqlPart)){
			s = selectSqlPart.replaceAll(tableName, crossUserName+"."+tableName);
			if(refInfo!=null){
				for(String[] arr:refInfo){
					s=s.replaceAll(arr[0], crossUserName+"."+arr[0]);
				}
			}
		}
		return s;
	}
	public String replaceSelectSqlPartByDbLink(String dbLinkName){
		String s = null;
		if(StringUtils.isNotEmpty(selectSqlPart)){
			s = selectSqlPart.replaceAll(tableName, tableName+"@"+dbLinkName);
			if(refInfo!=null){
				for(String[] arr:refInfo){
					s=s.replaceAll(arr[0], arr[0]+"@"+dbLinkName);
				}
			}
		}
		return s;
	}
	private void generateSqlPart(){
		if(refInfo!=null&&refInfo.size()>0){
			int size = refInfo.size();
			StringBuilder sb4S = new StringBuilder();
			sb4S.append("SELECT P.ROW_ID,R1.GEOMETRY FROM ");
			sb4S.append(tableName+" P");
			StringBuilder sb4C = new StringBuilder();
			sb4C.append(" WHERE P."+gridRefCol);
			for(int i=0;i<size;i++){
				String[] s = refInfo.get(i);
				//
				sb4S.append(","+s[0]+" R"+(size-i));
				//
				sb4C.append("=R"+(size-i)+"."+s[1]);
				if(i<(size-1)&&StringUtils.isNotEmpty(s[2])){
					sb4C.append(" AND R"+(size-i)+"."+s[2]);
				}
			}
			this.selectSqlPart=sb4S.toString();
			this.conditionSqlPart=sb4C.toString();
		}else{
			this.selectSqlPart="SELECT P.ROW_ID,P.GEOMETRY FROM "+ tableName +" P ";
			this.conditionSqlPart = " WHERE 1=1 ";
		}
	}
	
	public static String parseMeshIdTable(String mainTable){
		GlmMainTable glmMainTable = GlmMainTable.getGlmMainTable(mainTable);
		if(glmMainTable!=null){
			switch(glmMainTable){
			case RD_NODE:
				return  "RD_NODE_MESH";
			case RD_LINK:
				return null;
			}
		}
		return null;
	}
}
