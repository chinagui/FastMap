package com.navinfo.dataservice.dao.glm.operator;

import java.sql.Connection;
import java.sql.Statement;

import com.navinfo.dataservice.dao.glm.iface.IOperator;

/**
 * @ClassName: AbstractOperator.java
 * @author zhaokaikai
 * @date 上午10:54:43
 * @Description: AbstractOperator.java
 */
public abstract class AbstractOperator implements IOperator {
	public Connection conn;
	private boolean isChanged = false;

	public boolean isChanged() {
		return isChanged;
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public AbstractOperator(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insertRow() throws Exception {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.insertRow2Sql(stmt);

			stmt.executeBatch();

		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {

			}
		}

	}

	@Override
	public void updateRow() throws Exception {

		
			Statement stmt = null;
			try {
				stmt = conn.createStatement();

				this.updateRow2Sql(stmt);
				if (isChanged) {
					stmt.executeBatch();
				}

			} catch (Exception e) {

				throw e;

			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
				} catch (Exception e) {

				}
			}

		

	}

	@Override
	public void deleteRow() throws Exception {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			this.deleteRow2Sql(stmt);

			stmt.executeBatch();

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {

			}
		}

	}

	public abstract void insertRow2Sql(Statement stmt) throws Exception;

	public abstract void updateRow2Sql(Statement stmt) throws Exception;

	public abstract void deleteRow2Sql(Statement stmt) throws Exception;

}
