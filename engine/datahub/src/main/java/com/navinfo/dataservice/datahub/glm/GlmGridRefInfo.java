package com.navinfo.dataservice.datahub.glm;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/** 
* @ClassName: GlmGridRefInfo 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午4:28:47 
* @Description: TODO
*/
public class GlmGridRefInfo {
	private String tableName;
	private String gridRefCol;
	private List<String[]> refInfo;//{"第一层参考表","关联的参考表的字段，一般为主键","参考表本身参考其他表的参考字段,如果没有，则为空字符串"}
	private boolean singleMesh;//在glm模型上，是否属于唯一一个图幅，标识是参考的主表中有mesh_id字段
	private String editQuerySql;//给编辑时查询数据记录所属grid使用的sql，row_id是数据记录的row_id
	private String diffQuerySql;//给履历记录查询所属grid使用的sql，row_id是履历表的row_id
	
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
		generateSql();
	}
	public boolean isSingleMesh() {
		return singleMesh;
	}
	public void setSingleMesh(boolean singleMesh) {
		this.singleMesh = singleMesh;
	}
	public String getEditQuerySql(){
		return editQuerySql;
	}
	public String getDiffQuerySql(){
		return diffQuerySql;
	}
	/**
	 * 
	 * @param name:cross user name
	 * @return
	 */
	public String replaceDiffSqlByCrossUser(String crossUserName){
		String s = null;
		if(StringUtils.isNotEmpty(diffQuerySql)){
			s = diffQuerySql.replaceAll(tableName+" P", crossUserName+"."+tableName+" P");
			if(refInfo!=null){
				for(String[] arr:refInfo){
					s=s.replaceAll(arr[0]+" R", crossUserName+"."+arr[0]+" R");
				}
			}
		}
		return s;
	}
	public String replaceDiffSqlByDbLink(String dbLinkName){
		String s = null;
		if(StringUtils.isNotEmpty(diffQuerySql)){
			s = diffQuerySql.replaceAll(tableName, tableName+"@"+dbLinkName);
			if(refInfo!=null){
				for(String[] arr:refInfo){
					s=s.replaceAll(arr[0], arr[0]+"@"+dbLinkName);
				}
			}
		}
		return s;
	}
	private void generateSql(){
		StringBuilder sb4E = new StringBuilder();
		StringBuilder sb4D = new StringBuilder();
		//先判断是否本身为主表，主表的refInfo为空
		if(refInfo!=null&&refInfo.size()>0){
			int size = refInfo.size();
			//...
			sb4E.append("SELECT P.ROW_ID,R1.GEOMETRY");
			sb4D.append("SELECT L.ROW_ID,R1.GEOMETRY");
			//
			String meshSql = null;
			if(singleMesh){
				meshSql = ",R1.MESH_ID FROM ";
			}else{
				meshSql = ",0 MESH_ID FROM ";
			}
			sb4E.append(meshSql);
			sb4D.append(meshSql);
			//
			sb4E.append(tableName+" P");
			sb4D.append(tableName+" P");
			//ref table part
			StringBuilder sb4S = new StringBuilder();
			StringBuilder sb4C = new StringBuilder();
			sb4C.append(" WHERE P."+gridRefCol);
			for(int i=0;i<size;i++){
				String[] s = refInfo.get(i);
				//
				sb4S.append(","+s[0]+" R"+(size-i));
				//
				sb4C.append("=R"+(size-i)+"."+s[1]);
				if(i<(size-1)&&(!("NULL".equals(s[2])))){
					sb4C.append(" AND R"+(size-i)+"."+s[2]);
				}
			}
			sb4E.append(sb4S.toString());
			sb4D.append(sb4S.toString());
			sb4D.append(",LOG_DETAIL L");
			//WHERE
			sb4E.append(sb4C.toString());
			sb4D.append(sb4C.toString());
			sb4D.append(" AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM = '"+tableName+"' ");
		}else{
			//...
			sb4E.append("SELECT P.ROW_ID,P.GEOMETRY");
			sb4D.append("SELECT L.ROW_ID,P.GEOMETRY");
			//
			String meshSql = null;
			if(singleMesh){
				meshSql = ",P.MESH_ID FROM ";
			}else{
				meshSql = ",0 MESH_ID FROM ";
			}
			sb4E.append(meshSql);
			sb4D.append(meshSql);
			//
			sb4E.append(tableName+" P");
			sb4D.append(tableName+" P,LOG_DETAIL L");
			//WHERE
			sb4E.append(" WHERE 1=1 ");
			sb4D.append(" WHERE P.ROW_ID=L.TB_ROW_ID AND L.TB_NM = '"+tableName+"' ");
		}
		editQuerySql = sb4E.toString();
		diffQuerySql = sb4D.toString();
	}
}
