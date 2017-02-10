package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01167
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 检查对象：含有SA\PA属性的link，且含有IC\JCT和匝道属性。
				   检查原则：检查对象上不能含有“POI连接路”属性，否则报err
 */
public class GLM01167 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01167.class);

	/**
	 * sa/pa形态的link
	 */
	private Set<Integer> saPaLinkPidSet = new HashSet<>();
	
	/**
	 * ic/jct形态的link
	 */
	private Set<Integer> icJctLinkPidSet = new HashSet<>();
	
	/**
	 * poi连接路形态的link
	 */
	private Set<Integer> poiFormSet = new HashSet<>();
	
	/**
	 * 最终报检查log的link
	 */
	private Set<Integer> resultLinkPidSet = new HashSet<>();
	
	public GLM01167() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : resultLinkPidSet) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK      L, RD_LINK_FORM F, RD_LINK_FORM F1, RD_LINK_FORM F2, RD_LINK_FORM F3 WHERE L.LINK_PID = F.LINK_PID AND L.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND L.U_RECORD <> 2 AND F.U_RECORD <> 2 AND F1.U_RECORD <> 2 AND F2.U_RECORD <> 2 AND F3.U_RECORD <> 2 AND F.FORM_OF_WAY IN (12, 13) AND F1.LINK_PID = F.LINK_PID AND F1.FORM_OF_WAY = 15 AND F2.LINK_PID = F.LINK_PID AND F2.FORM_OF_WAY IN (10, 11) AND F3.LINK_PID = F.LINK_PID AND F3.FORM_OF_WAY = 36 ");

			
			log.info("RdLink后检查GLM01167 SQL:" + sb.toString());
			
			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.status() == ObjStatus.UPDATE && form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if(formOfWay == 10|| formOfWay == 11)
				{
					if (form.status() != ObjStatus.DELETE) {
						icJctLinkPidSet.add(form.getLinkPid());
					}
					else
					{
						icJctLinkPidSet.remove(form.getLinkPid());
					}
				}
				else if(formOfWay == 12|| formOfWay == 13)
				{
					if (form.status() != ObjStatus.DELETE) {
						saPaLinkPidSet.add(form.getLinkPid());
					}
					else
					{
						saPaLinkPidSet.remove(form.getLinkPid());
					}
				}
				else if(formOfWay == 36)
				{
					if (form.status() != ObjStatus.DELETE) {
						poiFormSet.add(form.getLinkPid());
					}
					else
					{
						poiFormSet.remove(form.getLinkPid());
					}
				}
			}
		}
		//获取满足sa/pa,ic/jct,poi连接路的link
		resultLinkPidSet.addAll(poiFormSet);
		resultLinkPidSet.addAll(saPaLinkPidSet);
		resultLinkPidSet.addAll(icJctLinkPidSet);
	}

}
