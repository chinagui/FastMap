package com.navinfo.dataservice.engine.fcc.tile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * 构建瓦片类
 */
public class RdLinkTileBuilder {

	public static class TileMapper extends
			Mapper<LongWritable, Text, Text, Text> {

		/**
		 * 根据道路种别，输出显示的颜色
		 * 
		 * @param kind
		 *            道路种别
		 * @return
		 */
		public static int kind2Color(int kind) {
			if (kind == 13) {
				return 13;
			} else if (kind == 15) {
				return 14;
			} else {
				return kind + 1;
			}
		}

		private WKTReader reader = new WKTReader();

		private WKTWriter writer = new WKTWriter();

		private Text keyText = new Text();

		private Text valueText = new Text();

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			int mod = Integer.parseInt(value.toString());

			// String sql =
			// "select a.link_pid,a.kind,sdo_util.to_wktgeometry(geometry) geometry,direct,name from rd_link_beijing a, (select b.link_pid,c.name from  rd_link_name b,rd_name c where b.name_groupid = c.name_groupid and b.name_class=1 and b.seq_num =1 and c.lang_code='CHI' ) b where a.link_pid = b.link_pid(+) and mod(a.link_pid,280)="
			// + mod;

			String sql = null;

//			if (!"1".equals(context.getConfiguration().get("isgdb"))) {
				sql = "select a.link_pid,        a.kind,        a.s_node_pid,        a.e_node_pid,        sdo_util.to_wktgeometry(geometry) geometry,        direct,        name,        c.limits,        d.forms   from rd_link a,        (select b.link_pid, c.name           from rd_link_name b, rd_name c          where b.name_groupid = c.name_groupid            and b.name_class = 1            and b.seq_num = 1            and c.lang_code = 'CHI') b,(select    t1.link_pid, listagg(t1.type, ';') within group(order by t1.link_pid) limits     from rd_link_limit t1    where t1.u_record != 2    group by t1.link_pid) c,    (select     a.link_pid,    listagg(a.form_of_way, ';') within group(order by a.link_pid) forms     from rd_link_form a    where a.u_record != 2    group by a.link_pid) d      where a.link_pid = b.link_pid(+)        and a.link_pid = c.link_pid(+)        and a.link_pid=d.link_pid(+) and a.u_record!=2 and mod(a.link_pid,50)="
						+ mod;
//			} else {
//				sql = "select a.link_pid,        a.kind,        a.s_node_pid,        a.e_node_pid,        sdo_util.to_wktgeometry(geometry) geometry,        direct,        name,        c.limits,        d.forms   from rd_link a,        (select b.link_pid, c.name           from rd_link_name b, rd_name c          where b.name_groupid = c.name_groupid            and b.name_class = 1            and b.seq_num = 1            and c.lang_code = 'CHI') b,(select    t1.link_pid, listagg(t1.type, ';') within group(order by t1.link_pid) limits     from rd_link_limit t1    where t1.u_record != 2    group by t1.link_pid) c,    (select     a.link_pid,    listagg(a.form_of_way, ';') within group(order by a.link_pid) forms     from rd_link_form a    where a.u_record != 2    group by a.link_pid) d      where a.link_pid = b.link_pid(+)        and a.link_pid = c.link_pid(+)        and a.link_pid=d.link_pid(+) and a.u_record!=2 and mod(a.link_pid,280)="
//						+ mod;
//			}
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			try {

				Class.forName("oracle.jdbc.driver.OracleDriver");

				String username = context.getConfiguration().get("username");

				String password = context.getConfiguration().get("password");

				String serviceName = context.getConfiguration().get(
						"serviceName");

				String ip = context.getConfiguration().get("ip");

				int maxDegree = Integer.parseInt(context.getConfiguration()
						.get("maxDegree"));

				int minDegree = Integer.parseInt(context.getConfiguration()
						.get("minDegree"));

				minDegree = minDegree >= 7 ? minDegree : 7;

				int port = Integer.parseInt(context.getConfiguration().get(
						"port"));
				conn = DriverManager.getConnection(
						"jdbc:oracle:thin:@" + ip + ":" + port + ":"
								+ serviceName, username, password);

				stmt = conn.createStatement();

				rs = stmt.executeQuery(sql);

				rs.setFetchSize(1000);

				int num = 0;

				while (rs.next()) {
					num++;

					String geomStr = rs.getString("geometry");

					int linkPid = rs.getInt("link_pid");

					int kind = rs.getInt("kind");

					int direct = rs.getInt("direct");

					String name = rs.getString("name");
					
					String forms= rs.getString("forms");
					
					String limits = rs.getString("limits");
					
					int sNodePid = rs.getInt("s_node_pid");
					
					int eNodePid = rs.getInt("e_node_pid");

					for (byte degree = (byte) minDegree; degree <= (byte) maxDegree; degree++) {

						if (degree <= 7) {
							if (kind <= 1) {
								try {
									writeLine(geomStr, linkPid, context,
											keyText, valueText, degree, kind,
											direct, name,sNodePid,eNodePid,forms,limits);
								} catch (Exception e) {
									e.printStackTrace();
									throw new InterruptedException("错误："
											+ e.getMessage());
								}
							}
						} else if (degree <= 10) {
							if (kind <= 3) {
								try {
									writeLine(geomStr, linkPid, context,
											keyText, valueText, degree, kind,
											direct, name,sNodePid,eNodePid,forms,limits);
								} catch (Exception e) {
									e.printStackTrace();
									throw new InterruptedException("错误："
											+ e.getMessage());
								}
							}
						} else if (degree <= 12) {
							if (kind <= 6) {
								try {
									writeLine(geomStr, linkPid, context,
											keyText, valueText, degree, kind,
											direct, name,sNodePid,eNodePid,forms,limits);
								} catch (Exception e) {
									e.printStackTrace();
									throw new InterruptedException("错误："
											+ e.getMessage());
								}
							}
						} else if (degree <= 14) {
							if (kind <= 7) {
								try {
									writeLine(geomStr, linkPid, context,
											keyText, valueText, degree, kind,
											direct, name,sNodePid,eNodePid,forms,limits);
								} catch (Exception e) {
									e.printStackTrace();
									throw new InterruptedException("错误："
											+ e.getMessage());
								}
							}
						} else {
							try {
								writeLine(geomStr, linkPid, context, keyText,
										valueText, degree, kind, direct, name,sNodePid,eNodePid,forms,limits);
							} catch (Exception e) {
								e.printStackTrace();
								throw new InterruptedException("错误："
										+ e.getMessage());
							}
						}
					}

					if (num % 1000 == 0) {
						context.getCounter("map", "records").increment(1000);

						num = 0;
					}
				}

				context.getCounter("map", "records").increment(num);
			} catch (Exception e) {
				e.printStackTrace();

				throw new IOException(e);
			}finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(stmt);
				DbUtils.closeQuietly(conn);
			}

		}

		public void writeLine(String wktLine, int linkPid, Context context,
				Text keyText, Text valueText, byte degree, int kind,
				int direct, String name,int sNodePid,int eNodePid,String forms,String limits) throws Exception {

			// for (byte degree = 8; degree <= 12; degree++) {
			Set<String> set = new HashSet<String>();
			String[] splits = wktLine.replace("LINESTRING (", "")
					.replace(")", "").split(", ");

			long minTileX = 99999999;

			long minTileY = 9999999;

			long maxTileX = 0;

			long maxTileY = 0;

			StringBuilder wktMerLine = new StringBuilder("LINESTRING (");

			boolean isFirst = true;
			for (String s : splits) {
				double lon = Double.parseDouble(s.split(" ")[0]);

				double lat = Double.parseDouble(s.split(" ")[1]);

				long tx = MercatorProjection.longitudeToTileX(lon, degree);

				long ty = MercatorProjection.latitudeToTileY(lat, degree);

				if (minTileX > tx) {
					minTileX = tx;
				}

				if (minTileY > ty) {
					minTileY = ty;
				}

				if (maxTileX < tx) {
					maxTileX = tx;
				}

				if (maxTileY < ty) {
					maxTileY = ty;
				}

				if (!isFirst) {
					wktMerLine.append(", "
							+ MercatorProjection.longitudeToMetersX(lon));

					wktMerLine.append(" ");

					wktMerLine
							.append(MercatorProjection.latitudeToMetersY(lat));
				} else {
					wktMerLine.append(MercatorProjection
							.longitudeToMetersX(lon));

					wktMerLine.append(" ");

					wktMerLine
							.append(MercatorProjection.latitudeToMetersY(lat));

					isFirst = false;
				}

			}

			wktMerLine.append(")");

			for (long x = minTileX; x <= maxTileX; x++) {
				for (long y = minTileY; y <= maxTileY; y++) {
					set.add(String.format("%02d", degree)
							+ String.format("%08d", x)
							+ String.format("%07d", y));
				}
			}

			Iterator<String> it = set.iterator();

			while (it.hasNext()) {
				int prePx = 1000;

				int prePy = 1000;

				String str = it.next();

				long tx = Long.parseLong(str.substring(2, 10));

				long ty = Long.parseLong(str.substring(10));

				double startLon = MercatorProjection.tileXToLongitude(tx,
						degree);

				double endLon = MercatorProjection.tileXToLongitude(tx + 1,
						degree);

				double startLat = MercatorProjection
						.tileYToLatitude(ty, degree);

				double endLat = MercatorProjection.tileYToLatitude(ty + 1,
						degree);

				double startMerX = MercatorProjection
						.longitudeToMetersX(startLon);

				double startMerY = MercatorProjection
						.latitudeToMetersY(startLat);

				double endMerX = MercatorProjection.longitudeToMetersX(endLon);

				double endMerY = MercatorProjection.latitudeToMetersY(endLat);

				StringBuilder sb = new StringBuilder("POLYGON ((");

				sb.append(startMerX + " " + startMerY);

				sb.append(", ");

				sb.append(endMerX + " " + startMerY);

				sb.append(", ");

				sb.append(endMerX + " " + endMerY);

				sb.append(", ");

				sb.append(startMerX + " " + endMerY);

				sb.append(", ");

				sb.append(startMerX + " " + startMerY);

				sb.append("))");

				Geometry geomLine = reader.read(wktMerLine.toString());

				Geometry geomTile = reader.read(sb.toString());

				if (!geomLine.intersects(geomTile)) {

					continue;
				}

				String interWkt = writer.write(geomLine.intersection(geomTile));

				// System.out.println(interWkt);

				if (interWkt.startsWith("LINESTRING")) {
					splits = interWkt.replace("LINESTRING (", "")
							.replace(")", "").split(", ");

					Tile tile = new Tile();

					tile.setT(4);

					tile.setI(String.valueOf(linkPid));

					JSONArray jap = new JSONArray();

					double tmpPx = MercatorProjection.tileXToPixelX(tx);

					double tmpPy = MercatorProjection.tileYToPixelY(ty);

					int[][] points = new int[splits.length][];

					int tmpNum = 0;

					for (String s : splits) {

						int px = (int) Math.round(MercatorProjection
								.longitudeToPixelX(MercatorProjection
										.metersXToLongitude(Double
												.parseDouble(s.split(" ")[0])),
										degree)
								- tmpPx);

						int py = (int) Math.round(MercatorProjection
								.latitudeToPixelY(MercatorProjection
										.metersYToLatitude(Double.parseDouble(s
												.split(" ")[1])), degree)
								- tmpPy);

						if (degree >= 1) {
							if (px != prePx || py != prePy) {

								JSONArray array = new JSONArray();

								array.add(px);

								array.add(py);

								jap.add(array);

							}
							prePx = px;
							prePy = py;
						} else {
							// points[tmpNum][0] = px;
							//
							// points[tmpNum++][1] = py;

							points[tmpNum++] = new int[] { px, py };
						}
					}

					// if (degree < 1) {
					// try {
					// compress(points, 1, jap);
					// } catch (Exception e) {
					// e.printStackTrace();
					//
					// throw new Exception(interWkt);
					// }
					// }

					tile.setG(jap);

					JSONObject jsonM = new JSONObject();

					jsonM.put("a", String.valueOf(kind));

					jsonM.put("b", name != null ? name : JSONNull.getInstance());

					jsonM.put("c", limits != null ? limits : JSONNull.getInstance());

					jsonM.put("d", String.valueOf(direct));
					
					jsonM.put("e", String.valueOf(sNodePid));
					
					jsonM.put("f", String.valueOf(eNodePid));
					
					jsonM.put("h", forms!= null ? forms : JSONNull.getInstance());

					tile.setM(jsonM);

					keyText.set(str);

					valueText.set(tile.Serialize(ObjLevel.FULL).toString());

					context.write(keyText, valueText);

				} else if (interWkt.startsWith("MULTILINESTRING")) {
					interWkt = interWkt.replace("MULTILINESTRING ((", "")
							.replace("))", "");

					String[] ss = interWkt.split("\\), \\(");

					for (int i = 0; i < ss.length; i++) {

						splits = ss[i].split(", ");

						Tile tile = new Tile();

						tile.setT(4);

						tile.setI(String.valueOf(linkPid));

						JSONArray jap = new JSONArray();

						double tmpPx = MercatorProjection.tileXToPixelX(tx);

						double tmpPy = MercatorProjection.tileYToPixelY(ty);

						int[][] points = new int[splits.length][];

						int tmpNum = 0;

						for (String s : splits) {
							int px = (int) Math
									.round(MercatorProjection.longitudeToPixelX(
											MercatorProjection
													.metersXToLongitude(Double.parseDouble(s
															.split(" ")[0])),
											degree)
											- tmpPx);

							int py = (int) Math
									.round(MercatorProjection.latitudeToPixelY(
											MercatorProjection
													.metersYToLatitude(Double.parseDouble(s
															.split(" ")[1])),
											degree)
											- tmpPy);

							if (degree >= 1) {
								if (px != prePx || py != prePy) {

									JSONArray array = new JSONArray();

									array.add(px);

									array.add(py);

									jap.add(array);
								}
								prePx = px;
								prePy = py;
							} else {
								// points[tmpNum][0] = px;
								//
								// points[tmpNum++][1] = py;

								points[tmpNum++] = new int[] { px, py };
							}
						}

						if (degree < 1) {
							compress(points, 1, jap);
						}

						tile.setG(jap);

						JSONObject jsonM = new JSONObject();

						jsonM.put("a", String.valueOf(kind));

						jsonM.put("b", name != null ? name : JSONNull.getInstance());

						jsonM.put("c", limits != null ? limits : JSONNull.getInstance());

						jsonM.put("d", String.valueOf(direct));
						
						jsonM.put("e", String.valueOf(sNodePid));
						
						jsonM.put("f", String.valueOf(eNodePid));
						
						jsonM.put("h", forms!= null ? forms : JSONNull.getInstance());

						tile.setM(jsonM);

						keyText.set(str);

						valueText.set(tile.Serialize(ObjLevel.FULL).toString());

						context.write(keyText, valueText);

						prePx = 1000;

						prePy = 1000;
					}
				}
			}
		}

		public void compress(int[][] points, double d, JSONArray jap) {

			int len = points.length;

			int startX = points[0][0];

			int startY = points[0][1];

			jap.add(startX);

			jap.add(startY);

			if (len == 2) {

				jap.add(points[1][0]);

				jap.add(points[1][1]);

				return;
			}

			int endX = points[len - 1][0];

			int endY = points[len - 1][1];

			// Ax + By + C =0;

			// double A = (from.getY() - to.getY())
			// / Math.sqrt(Math.pow((from.getY() - to.getY()), 2)
			// + Math.pow((from.getX() - to.getX()), 2));

			double A = (startY - endY)
					/ Math.sqrt(Math.pow((startY - endY), 2)
							+ Math.pow((startX - endX), 2));

			// double B = (to.getX() - from.getX())
			// / Math.sqrt(Math.pow((from.getY() - to.getY()), 2)
			// + Math.pow((from.getX() - to.getX()), 2));

			double B = (endX - startX)
					/ Math.sqrt(Math.pow((startY - endY), 2)
							+ Math.pow((endX - startX), 2));

			// double C = (from.getX() * to.getY() - to.getX() * from.getY())
			// / Math.sqrt(Math.pow((from.getY() - to.getY()), 2)
			// + Math.pow((from.getX() - to.getX()), 2));

			double C = (startX * endY - endX * startX)
					/ Math.sqrt(Math.pow((startY - endY), 2)
							+ Math.pow((endX - startX), 2));

			int num = 1;

			// double distance = Math.abs(A * points[num][0] + B *
			// points[num][1] + C)/Math.sqrt(Math.pow(A, 2) + Math.pow(B, 2));

			while (num < (len - 1)
					&& (Math.abs(A * points[num][0] + B * points[num][1] + C) / Math
							.sqrt(Math.pow(A, 2) + Math.pow(B, 2))) <= d) {

				num++;
			}

			if (num == (len - 1)) {
				jap.add(points[num][0]);

				jap.add(points[num][1]);
			} else {
				int lenSon = len - num;

				int sonPoints[][] = new int[lenSon][];

				int tmpNum = 0;

				for (int i = num; i < len; i++) {
					sonPoints[tmpNum++] = points[i];

				}

				compress(sonPoints, d, jap);
			}

		}

	}

	public static class TileReducer extends
			TableReducer<Text, Text, ImmutableBytesWritable> {

		@Override
		protected void reduce(Text arg0, Iterable<Text> arg1, Context arg2)
				throws IOException, InterruptedException {

			Put put = new Put(arg0.toString().getBytes());

			List<String> list1 = new ArrayList<String>();

			List<String> list2 = new ArrayList<String>();

			List<String> list3 = new ArrayList<String>();

			List<String> list4 = new ArrayList<String>();

			List<String> list5 = new ArrayList<String>();

			for (Text value : arg1) {

				JSONObject json = JSONObject.fromObject(value.toString());

				int a = json.getJSONObject("m").getInt("a");

				if (a == 1) {
					list1.add(json.toString());
				} else if (a == 2) {
					list2.add(json.toString());
				} else if (a == 3) {
					list3.add(json.toString());
				} else if (a == 4) {
					list4.add(json.toString());
				} else {
					list5.add(json.toString());
				}
			}

			StringBuilder sb = new StringBuilder("[");

			boolean isFirst = true;

			for (String value : list5) {
				if (!isFirst) {
					sb.append("," + value);
				} else {
					sb.append(value);
					isFirst = false;
				}
			}

			for (String value : list4) {
				if (!isFirst) {
					sb.append("," + value);
				} else {
					sb.append(value);
					isFirst = false;
				}
			}

			for (String value : list3) {
				if (!isFirst) {
					sb.append("," + value);
				} else {
					sb.append(value);
					isFirst = false;
				}
			}

			for (String value : list2) {
				if (!isFirst) {
					sb.append("," + value);
				} else {
					sb.append(value);
					isFirst = false;
				}
			}

			for (String value : list1) {
				if (!isFirst) {
					sb.append("," + value);
				} else {
					sb.append(value);
					isFirst = false;
				}
			}

			sb.append("]");

			put.addColumn("index".getBytes(), null, sb.toString().getBytes());

			arg2.write(new ImmutableBytesWritable(put.getRow()), put);

			arg2.getCounter("reduce", "records").increment(1);
		}
	}

	public static class TilePartitioner extends Partitioner<Text, Text> {

		@Override
		public int getPartition(Text arg0, Text arg1, int arg2) {

			return (int) (Long.parseLong(arg0.toString()) % arg2);
		}

	}

	public static class ProgressThread implements Runnable {

		private Job job;

		public Job getJob() {
			return job;
		}

		public void setJob(Job job) {
			this.job = job;
		}

		public int getLinkNum() {
			return linkNum;
		}

		public void setLinkNum(int linkNum) {
			this.linkNum = linkNum;
		}

		public int getTileNum() {
			return tileNum;
		}

		public void setTileNum(int tileNum) {
			this.tileNum = tileNum;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		private int linkNum;

		private int tileNum;

		private String uuid;

		private Connection conn;

		public Connection getConn() {
			return conn;
		}

		public void setConn(Connection conn) {
			this.conn = conn;
		}

		public ProgressThread(Job job, int linkNum, int tileNum, String uuid,
				Connection conn) {

			this.job = job;

			this.linkNum = linkNum;

			this.tileNum = tileNum;

			this.uuid = uuid;

			this.conn = conn;

		}

		public void run() {

			boolean isExists = false;
			Statement stmt =null;
			try {

				stmt = conn.createStatement();

				while (job.getConfiguration().getBoolean("isover", false) == false) {

					Thread.sleep(10000);

					long mapRecord = 0;

					Counter mapCounter = job.getCounters().findCounter("map",
							"records");

					if (mapCounter != null) {
						mapRecord = mapCounter.getValue();
					}

					long reduceRecord = 0;

					Counter reduceCounter = job.getCounters().findCounter(
							"reduce", "records");

					if (reduceCounter != null) {
						reduceRecord = reduceCounter.getValue();
					}

					double progress = (mapRecord / linkNum) / 2
							+ (reduceRecord / tileNum) / 2;

					progress = progress * 100;

					String progressInfo = "总进度为：" + (int) progress
							+ "%,道路总条数为:" + linkNum + ",map 完成条数：" + mapRecord
							+ ";瓦片总数：" + tileNum + ",reduce完成条数："
							+ reduceRecord;

					String sql = null;

					if (isExists) {
						sql = "update task_progress set progress_info='"
								+ progressInfo + "' where uuid='" + uuid + "'";
					} else {

						sql = "insert into task_progress(progress_info,uuid) values ('"
								+ progressInfo + "','" + uuid + "')";
						isExists = true;
					}

					stmt.executeUpdate(sql);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				DbUtils.closeQuietly(stmt);
			}
		}
	}
	
	public static void run(Configuration conf) throws Exception{

		String tabName = "RDLINK_" + conf.get("dbId");
		
		createHBaseTab(conf, tabName);

		FileSystem fs = FileSystem.get(conf);

		String tmpDir = "/" + String.valueOf(new Date().getTime()) + "/";

		fs.mkdirs(new Path(tmpDir));

		int numTask = 50;

		for (int i = 0; i < numTask; i++) {

			OutputStream out = fs.create(new Path(tmpDir + i));

			out.write(String.valueOf(i).getBytes());

			out.flush();

			out.close();
		}

		Job job = Job.getInstance(conf, "split rdlink");

		job.setJarByClass(RdLinkTileBuilder.class);

		job.setNumReduceTasks(numTask);

		job.setMapOutputKeyClass(Text.class);

		job.setMapOutputValueClass(Text.class);

		job.setPartitionerClass(TilePartitioner.class);

		job.setMapperClass(TileMapper.class);

		job.setMapOutputKeyClass(Text.class);

		job.setMapOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path(tmpDir));

		TableMapReduceUtil.initTableReducerJob(tabName, TileReducer.class, job);
		job.waitForCompletion(true);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		Properties props = new Properties();

		props.load(new FileInputStream(args[0]));

		Configuration conf = new Configuration();

		// conf.set("fs.defaultFS", "hdfs://192.168.3.156:9000");

		conf.set("fs.defaultFS", props.getProperty("fs.defaultFS"));

		conf.setBoolean("dfs.permissions", false);

		// conf.set("hbase.zookeeper.quorum", "hadoop-01");

		conf.set("hbase.zookeeper.quorum",
				props.getProperty("hbase.zookeeper.quorum"));

		// conf.set("username", "fmgdb14");

		conf.set("username", props.getProperty("db.username"));

		// conf.set("password", "fmgdb14");

		conf.set("password", props.getProperty("db.password"));

		// conf.set("serviceName", "orcl");

		conf.set("serviceName", props.getProperty("db.service.name"));

		// conf.set("ip", "192.168.4.131");

		conf.set("ip", props.getProperty("db.ip"));

		// conf.set("port", "1521");

		conf.set("port", props.getProperty("db.port"));

		// conf.set("splits", "100");

		conf.set("isgdb", props.getProperty("isgdb"));

		// if (args.length>1){
		// conf.set("maxDegree", args[1]);
		// }else{
		// conf.set("maxDegree", "16");
		// }

		conf.set("minDegree", props.getProperty("min.degree"));

		conf.set("maxDegree", props.getProperty("max.degree"));

		String tabName = "RDLINK_" + props.getProperty("tab.name");

		createHBaseTab(conf, tabName);

		FileSystem fs = FileSystem.get(conf);

		String tmpDir = "/" + String.valueOf(new Date().getTime()) + "/";

		// fs.deleteOnExit(new Path("/lilei"));

		// fs.mkdirs(new Path("/lilei"));

		fs.mkdirs(new Path(tmpDir));

		int numTask = 50;

		if ("1".equals(props.getProperty("isgdb"))) {
			numTask = 280;
		}

		for (int i = 0; i < numTask; i++) {
			// OutputStream out = fs.create(new Path("/lilei/" + i));

			OutputStream out = fs.create(new Path(tmpDir + i));

			out.write(String.valueOf(i).getBytes());

			out.flush();

			out.close();
		}

		Job job = Job.getInstance(conf, "split link");

		// int linkNum = getLinkNum(job);
		//
		// int tileNum = 10290067;

		// ProgressThread pt = new ProgressThread(job, linkNum, tileNum,
		// args[0],
		// null);
		//
		// Thread thread = new Thread(pt);
		//
		// thread.setDaemon(true);
		//
		// thread.start();

		job.setJarByClass(RdLinkTileBuilder.class);

		job.setNumReduceTasks(numTask);

		job.setMapOutputKeyClass(Text.class);

		job.setMapOutputValueClass(Text.class);

		job.setPartitionerClass(TilePartitioner.class);

		job.setMapperClass(TileMapper.class);

		job.setMapOutputKeyClass(Text.class);

		job.setMapOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path(tmpDir));

		TableMapReduceUtil.initTableReducerJob(tabName, TileReducer.class, job);
		job.waitForCompletion(true);

		// thread.interrupt();

		// Thread.sleep(10000);

		// fillProgress(null, args[0], linkNum, tileNum);

	}

	public static int getLinkNum(Job job) throws Exception {
		int linkNum = 0;

		Class.forName("oracle.jdbc.driver.OracleDriver");

		String username = job.getConfiguration().get("username");

		String password = job.getConfiguration().get("password");

		String serviceName = job.getConfiguration().get("serviceName");

		String ip = job.getConfiguration().get("ip");

		int port = Integer.parseInt(job.getConfiguration().get("port"));
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip
					+ ":" + port + ":" + serviceName, username, password);
	
			stmt = conn.createStatement();
	
			String sql = "select count(*) from rd_link";
	
			rs = stmt.executeQuery(sql);
	
			rs.next();
	
			linkNum = rs.getInt(1);
		}catch (Exception e) {
			throw e;
		}finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		return linkNum;
	}

	public static void fillProgress(Connection conn, String uuid, int linkNum,
			int tileNum) throws Exception {

		Statement stmt = conn.createStatement();

		String progressInfo = "总进度为：100%,道路总条数为:" + linkNum + ",map 完成条数："
				+ linkNum + ";瓦片总数：" + tileNum + ",reduce完成条数：" + tileNum;

		String sql = "update task_progress set progress_info='" + progressInfo
				+ "' where uuid='" + uuid + "'";

		stmt.executeUpdate(sql);

		conn.close();
	}

	private static void createHBaseTab(Configuration conf, String tabName)
			throws Exception {
		org.apache.hadoop.hbase.client.Connection conn = ConnectionFactory
				.createConnection(conf);

		Admin admin = conn.getAdmin();

		TableName tableName = TableName.valueOf(tabName);

		if (admin.tableExists(tableName)) {
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
		}

		HTableDescriptor htd = new HTableDescriptor(tableName);

		HColumnDescriptor hcd = new HColumnDescriptor("index");

		htd.addFamily(hcd);

		admin.createTable(htd);
	}

}
