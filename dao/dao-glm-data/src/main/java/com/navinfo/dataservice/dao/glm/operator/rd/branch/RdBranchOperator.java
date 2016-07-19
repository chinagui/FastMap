package com.navinfo.dataservice.dao.glm.operator.rd.branch;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.operator.AbstractOperator;

public class RdBranchOperator extends AbstractOperator {

	private static Logger logger = Logger.getLogger(RdBranchOperator.class);

	private RdBranch branch;

	public RdBranchOperator(Connection conn, RdBranch branch) {
		super(conn);

		this.branch = branch;
	}

	@Override
	public void insertRow2Sql(Statement stmt) throws Exception {

		branch.setRowId(UuidUtils.genUuid());

		StringBuilder sb = new StringBuilder("insert into ");

		sb.append(branch.tableName());

		sb.append("(branch_pid, in_link_pid, node_pid, out_link_pid, relationship_type, u_record, row_id) values (");

		sb.append(branch.getPid());

		sb.append("," + branch.getInLinkPid());

		sb.append("," + branch.getNodePid());

		sb.append("," + branch.getOutLinkPid());

		sb.append("," + branch.getRelationshipType());

		sb.append(",1,'" + branch.rowId() + "')");

		stmt.addBatch(sb.toString());

		for (IRow r : branch.getDetails()) {
			RdBranchDetailOperator op = new RdBranchDetailOperator(conn,
					(RdBranchDetail) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : branch.getSignboards()) {
			RdSignboardOperator op = new RdSignboardOperator(conn,
					(RdSignboard) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : branch.getSignasreals()) {
			RdSignasrealOperator op = new RdSignasrealOperator(conn,
					(RdSignasreal) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : branch.getSeriesbranches()) {
			RdSeriesbranchOperator op = new RdSeriesbranchOperator(conn,
					(RdSeriesbranch) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : branch.getRealimages()) {
			RdBranchRealimageOperator op = new RdBranchRealimageOperator(conn,
					(RdBranchRealimage) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : branch.getSchematics()) {
			RdBranchSchematicOperator op = new RdBranchSchematicOperator(conn,
					(RdBranchSchematic) r);

			op.insertRow2Sql(stmt);
		}

		for (IRow r : branch.getVias()) {
			RdBranchViaOperator op = new RdBranchViaOperator(conn,
					(RdBranchVia) r);

			op.insertRow2Sql(stmt);
		}
	}

	@Override
	public void updateRow2Sql(Statement stmt) throws Exception {
		StringBuilder sb = new StringBuilder("update " + branch.tableName()
				+ " set u_record=3,");

		Set<Entry<String, Object>> set = branch.changedFields().entrySet();

		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> en = it.next();

			String column = en.getKey();

			Object columnValue = en.getValue();

			Field field = branch.getClass().getDeclaredField(column);

			field.setAccessible(true);

			Object value = field.get(branch);

			column = StringUtils.toColumnName(column);

			if (value instanceof String || value == null) {

				if (!StringUtils.isStringSame(String.valueOf(value),
						String.valueOf(columnValue))) {

					if (columnValue == null) {
						sb.append(column + "=null,");
					} else {
						sb.append(column + "='" + String.valueOf(columnValue)
								+ "',");
					}
					this.setChanged(true);

				}

			} else if (value instanceof Double) {

				if (Double.parseDouble(String.valueOf(value)) != Double
						.parseDouble(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Double.parseDouble(String.valueOf(columnValue))
							+ ",");
					this.setChanged(true);
				}

			} else if (value instanceof Integer) {

				if (Integer.parseInt(String.valueOf(value)) != Integer
						.parseInt(String.valueOf(columnValue))) {
					sb.append(column + "="
							+ Integer.parseInt(String.valueOf(columnValue))
							+ ",");
					this.setChanged(true);
				}

			}
		}
		sb.append(" where branch_pid=" + branch.getPid());

		String sql = sb.toString();

		sql = sql.replace(", where", " where");

		stmt.addBatch(sql);

	}

	@Override
	public void deleteRow2Sql(Statement stmt) throws Exception {

		String sql = "update " + branch.tableName()
				+ " set u_record=2 where branch_pid=" + branch.getPid();

		stmt.addBatch(sql);

		for (IRow r : branch.getDetails()) {
			RdBranchDetailOperator op = new RdBranchDetailOperator(conn,
					(RdBranchDetail) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : branch.getSignboards()) {
			RdSignboardOperator op = new RdSignboardOperator(conn,
					(RdSignboard) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : branch.getSignasreals()) {
			RdSignasrealOperator op = new RdSignasrealOperator(conn,
					(RdSignasreal) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : branch.getSeriesbranches()) {
			RdSeriesbranchOperator op = new RdSeriesbranchOperator(conn,
					(RdSeriesbranch) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : branch.getRealimages()) {
			RdBranchRealimageOperator op = new RdBranchRealimageOperator(conn,
					(RdBranchRealimage) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : branch.getSchematics()) {
			RdBranchSchematicOperator op = new RdBranchSchematicOperator(conn,
					(RdBranchSchematic) r);

			op.deleteRow2Sql(stmt);
		}

		for (IRow r : branch.getVias()) {
			RdBranchViaOperator op = new RdBranchViaOperator(conn,
					(RdBranchVia) r);

			op.deleteRow2Sql(stmt);
		}
	}

}
