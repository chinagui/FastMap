package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * 同一根link上不同的道路名称中不允许有相同的NAME值
 * 
 * @author fhx
 * @since 2017/4/14
 */
public class CHECK_RWLINK_NAME_REPEAT extends baseRule {
	private Set<Integer> nameLinkPidSet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareChangeDataSet(checkCommand);

		for (Integer linkPid : nameLinkPidSet) {
			List<RdLinkName> nameList = loadNameByLinkPid(linkPid);

			boolean flag = compareSameName(nameList);
			if (flag == true)
				continue;

			RwLinkSelector linkSelector = new RwLinkSelector(getConn());
			RwLink link = (RwLink) linkSelector.loadById(linkPid, false);
			this.setCheckResult(link.getGeometry(), "[RW_LINK," + link.pid() + "]", link.mesh());
		}
	}

	/**
	 * 比对同一link上的名称是否相同
	 * 
	 * @param names
	 * @return
	 */
	private boolean compareSameName(List<RdLinkName> names) {
		boolean flag = true;
		for (int i = 0; i < names.size() - 1; i++) {
			for (int j = i + 1; j < names.size(); j++) {
				if (names.get(i).getName().equals(names.get(j).getName())) {
					flag = false;
					break;
				}
			}
			if (flag == false)
				break;
		}

		return flag;
	}

	private void prepareChangeDataSet(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RwLinkName) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RwLinkName rwlinkName = (RwLinkName) row;

			nameLinkPidSet.add(rwlinkName.getLinkPid());
		}
	}

	/**
	 * 获取中文名称
	 * 
	 * @param linkPid
	 * @return 名称集
	 * @throws Exception
	 */
	public List<RdLinkName> loadNameByLinkPid(int linkPid) throws Exception {

		List<RdLinkName> map = new ArrayList<RdLinkName>();

		String str = String.format(
				"SELECT B.*, A.NAME FROM RD_NAME A, RW_LINK_NAME B WHERE A.NAME_GROUPID = B.NAME_GROUPID AND B.U_RECORD != 2 AND (A.LANG_CODE = 'CHI'  OR A.LANG_CODE = 'CHT' ) AND B.LINK_PID = %d",
				linkPid);

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = this.getConn().prepareStatement(str);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdLinkName linkName = new RdLinkName();
				ReflectionAttrUtils.executeResultSet(linkName, resultSet);

				String name = resultSet.getString("name");
				linkName.setName(name);
				map.add(linkName);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return map;
	}

}
