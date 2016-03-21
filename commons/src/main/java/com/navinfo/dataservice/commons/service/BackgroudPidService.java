package com.navinfo.dataservice.commons.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;


public class BackgroudPidService {

	private static final Logger logger = Logger
			.getLogger(BackgroudPidService.class);

	public static final String sql = "call proc_transport_pid(?,?)";

	/**
	 * @param delay 後台任務啟動延遲時間 單位(分鐘)
	 * @param period 後台任務執行間隔 單位(分鐘)
	 * @param minRange
	 * @param range
	 * @param jsonConnMsg
	 */
	public static void startService(int delay, int period, int minRange,
			int range, JSONObject jsonConnMsg) {

		try {
			Class.forName("");
			
			String username = jsonConnMsg.getString("");
			
			String password = jsonConnMsg.getString("");
			
			String url = "";
			
			Connection conn = DriverManager.getConnection(url, username, password);
			
			Timer timer = new Timer();
			
			timer.schedule(new TransportTask(conn,minRange,range), delay *60 * 1000, period *60 * 1000);
			
		} catch (Exception e) {
		}
	}

	/**
	 * 創建從ID分配器庫搬運PID任務
	 * @author lilei3774
	 *
	 */
	public static class TransportTask extends TimerTask {
		private Connection conn;

		private int minRange;

		private int range;

		public TransportTask(Connection conn, int minRange, int range) {
			this.conn = conn;

			this.minRange = minRange;

			this.range = range;
		}

		@Override
		public void run() {

			CallableStatement proc = null;

			try {
				proc = conn.prepareCall(sql);

				proc.setInt(1, this.minRange);

				proc.setInt(2, this.range);

				proc.execute();
			} catch (Exception e) {

			} finally {

				try {
					proc.close();
				} catch (Exception e) {
				}
			}
		}

	}
}
