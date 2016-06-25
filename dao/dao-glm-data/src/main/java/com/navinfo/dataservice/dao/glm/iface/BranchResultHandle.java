package com.navinfo.dataservice.dao.glm.iface;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BranchResultHandle {

	/**
	 * 处理分歧的结果集，针对RdBranchRealimage和RdSeriesbranch：log中添加子表返回的rowId
	 * @param result
	 */
	public static void handleResult(List<IRow> listAddIRow,JSONArray logs) {
		for (IRow row : listAddIRow) {
			if (row instanceof RdBranch) {
				RdBranch branch = (RdBranch) row;

				List<IRow> realimages = branch.getRealimages();

				List<IRow> seriesbranches = branch.getSeriesbranches();

				if (CollectionUtils.isNotEmpty(realimages)) {
					IRow real = realimages.get(0);
					setRowId(real, logs);
				}
				if (CollectionUtils.isNotEmpty(seriesbranches)) {
					IRow series = seriesbranches.get(0);
					setRowId(series, logs);
				}
			} else if (row instanceof RdBranchRealimage || row instanceof RdSeriesbranch) {
				setRowId(row, logs);
			}
		}
	}

	private static void setRowId(IRow row, JSONArray logs) {
		for (int i = 0; i < logs.size(); i++) {
			JSONObject obj = (JSONObject) logs.get(i);
			if (obj.containsKey("pid") && obj.getInt("pid") == row.parentPKValue()) {
				obj.put("rowId", row.rowId());
			}
		}
	}
}
