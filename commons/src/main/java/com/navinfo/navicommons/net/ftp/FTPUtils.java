package com.navinfo.navicommons.net.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.config.SystemGlobals;
import com.navinfo.navicommons.config.consts.ServiceConst;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.net.http.Response;
import com.navinfo.navicommons.springmvc.ResponseFormat;
import com.navinfo.navicommons.utils.JacksonUtils;
import com.navinfo.navicommons.utils.ServiceInvokeUtil;

/**
 * @author liuqing
 */
public class FTPUtils {

	private static Logger log = Logger.getLogger(FTPUtils.class);

	/**
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 */
	public static FTPClient connect(String host, int port, String username, String password) throws SocketException,
			IOException {
		FTPClient ftp = new FTPClient();
		int reply;
		ftp.connect(host, port);
		ftp.login(username, password);
		reply = ftp.getReplyCode();
		// 以2开头的返回值就会为真
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new RuntimeException("login failed");
		}
		ftp.enterLocalPassiveMode();// 客户端采用被动模式，被动连接ftp服务器分配的端口
		return ftp;
	}

	/**
	 * @param host
	 * @param username
	 * @param password
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 */
	public static FTPClient connect(String host, String username, String password) throws SocketException, IOException {
		return connect(host, 21, username, password);
	}

	/**
	 * @param ftp
	 * @throws IOException
	 */
	public static void disConnect(FTPClient ftp) {
		try {
			ftp.logout();
			if (ftp.isConnected())
				ftp.disconnect();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	/**
	 * @param ftp
	 * @param path
	 *            相对（相对用户默认目录）目录
	 * @param filename
	 * @param input
	 * @throws IOException
	 */
	public static void uploadFile(FTPClient ftp, String path, String filename, InputStream input) throws IOException {
		try {
			int inCount = changeWorkingDirectory(ftp, path);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.setBufferSize(1024 * 1024);
			ftp.setControlEncoding("UTF-8");
			ftp.storeFile(filename, input);
			// 恢复默认目录
			restoreWorkingDirectory(ftp, inCount);
		} catch (IOException e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	/**
	 * @param ftp
	 * @param remotePath
	 * @param localPath
	 * @throws IOException
	 */
	public static void downloadFile(FTPClient ftp, String remotePath, String[] fileNames, String localPath)
			throws IOException {
		try {
			int inCount = changeWorkingDirectory(ftp, remotePath);
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (ArrayUtils.contains(fileNames, ff.getName())) {
					File localPathFile = new File(localPath);
					if (!localPathFile.exists())
						localPathFile.mkdirs();
					File localFile = new File(localPath + File.separator + ff.getName());
					OutputStream os = new FileOutputStream(localFile);
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
					ftp.retrieveFile(ff.getName(), os);
					os.close();
				}
			}
			// 恢复默认目录
			restoreWorkingDirectory(ftp, inCount);

		} catch (IOException e) {
			throw e;
		}
	}	
	/**
	 * @param ftp
	 * @param remotePath
	 * @param fileNames
	 * @param localPath
	 * @param fromEncode 文件名编码方式"ISO-8859-1"
	 * @param toEncdoe 文件名编码方式 "GBK"
	 * @throws IOException
	 */
	public static void downloadFile(FTPClient ftp, String remotePath, String[] fileNames, String localPath,String fromEncode,String toEncdoe)
			throws IOException {
		try {
			int inCount = changeWorkingDirectory(ftp, remotePath);
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				String encodeFile = new String(ff.getName().getBytes(fromEncode),toEncdoe);
				if (ArrayUtils.contains(fileNames, encodeFile)) {
					File localPathFile = new File(localPath);
					if (!localPathFile.exists())
						localPathFile.mkdirs();
					File localFile = new File(localPath + File.separator + encodeFile);
					OutputStream os = new FileOutputStream(localFile);
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
					ftp.retrieveFile(ff.getName(), os);
					os.close();
				}
			}
			// 恢复默认目录
			restoreWorkingDirectory(ftp, inCount);

		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * @param ftp
	 * @param remotePath
	 * @param localPath
	 * @throws IOException
	 */
	public static void downloadFile(FTPClient ftp, String remotePath, String[] fileNames, OutputStream out)
			throws IOException {
		try {
			int inCount = changeWorkingDirectory(ftp, remotePath);
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (ArrayUtils.contains(fileNames, ff.getName())) {
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
					ftp.retrieveFile(ff.getName(), out);

				}
			}
			// 恢复默认目录
			restoreWorkingDirectory(ftp, inCount);

		} catch (IOException e) {
			throw e;
		} 
	}
	public static void downloadFile(FTPClient ftp, String remotePath, String[] fileNames, OutputStream out,String fromEncode,String toEncode)
			throws IOException {
		try {
			int inCount = changeWorkingDirectory(ftp, remotePath);
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				String encodeFile = new String(ff.getName().getBytes(fromEncode),toEncode);
				if (ArrayUtils.contains(fileNames, encodeFile)) {
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
					ftp.retrieveFile(ff.getName(), out);

				}
			}
			// 恢复默认目录
			restoreWorkingDirectory(ftp, inCount);

		} catch (IOException e) {
			throw e;
		} 
	}

	public static void restoreWorkingDirectory(FTPClient ftp, int inCount) throws IOException {
		for (int i = 0; i < inCount; i++) {
			ftp.changeToParentDirectory();
		}
	}

	public static int changeWorkingDirectory(FTPClient ftp, String remotePath) throws IOException {
		int inCount = 0;
		if (StringUtils.isNotBlank(remotePath)) {
			String dirs[] = remotePath.split("/");
			for (int i = 0; i < dirs.length; i++) {
				String dir = dirs[i];
				if (StringUtils.isNotBlank(dir)) {
					ftp.makeDirectory(dir);
					ftp.changeWorkingDirectory(dir);
					inCount++;
				}
			}
		}
		return inCount;
	}

	public static void downloadFile(String host, int port, String username, String password, String remotePath,
			String[] fileName, String localPath) throws IOException {
		FTPClient ftpClient = null;
		try {
			ftpClient = connect(host, port, username, password);
			downloadFile(ftpClient, remotePath, fileName, localPath);
		} finally {
			if (ftpClient != null)
				disConnect(ftpClient);
		}
	}
	public static void downloadFile(String host, int port, String username, String password, String remotePath,
			String[] fileName, String localPath,String fromEncode,String toEncode) throws IOException {
		FTPClient ftpClient = null;
		try {
			ftpClient = connect(host, port, username, password);
			downloadFile(ftpClient, remotePath, fileName, localPath, fromEncode, toEncode);
		} finally {
			if (ftpClient != null)
				disConnect(ftpClient);
		}
	}

	public static void downloadFile(String host, int port, String username, String password, String remotePath,
			String[] fileName, OutputStream out) throws IOException {
		FTPClient ftpClient = null;
		try {
			ftpClient = connect(host, port, username, password);
			downloadFile(ftpClient, remotePath, fileName, out);
		} finally {
			if (ftpClient != null)
				disConnect(ftpClient);
		}
	}
	public static void downloadFile(String host, int port, String username, String password, String remotePath,
			String[] fileName, OutputStream out,String fromEncode,String toEncode) throws IOException {
		FTPClient ftpClient = null;
		try {
			ftpClient = connect(host, port, username, password);
			downloadFile(ftpClient, remotePath, fileName, out, fromEncode, toEncode);
		} finally {
			if (ftpClient != null)
				disConnect(ftpClient);
		}
	}

	public static String[] listFileNames(FTPClient ftp, String remotePath) throws IOException {
		changeWorkingDirectory(ftp, remotePath);
		return ftp.listNames();
	}

	public static String[] listFileNames(String host, int port, String username, String password, String remotePath)
			throws IOException {
		String[] names = null;
		FTPClient ftpClient = null;
		try {
			ftpClient = connect(host, port, username, password);
			names = listFileNames(ftpClient, remotePath);
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
		return names;
	}

	public static AccountMeta createFtpAccount(String clientId, String extTaskId, String type) throws ServiceException {
		return createFtpAccount(clientId, extTaskId, type, null);
	}

	/**
	 * 根据FTP路径及参数MAP 返回一个FTP帐户元数据,把这个抽象为一个公共的方法，出错时抛出异常
	 * 
	 * @return
	 * @author 杨小军
	 * @modified by liuqing 20100722
	 * @author 杨小军
	 * @modified by liuqing 20100722
	 */
	public static AccountMeta createFtpAccount(String clientId, String extTaskId, String type, AccountMeta meta)
			throws ServiceException {
		AccountMeta accountMeta = null;
		Map resMap = null;
		try {
			// 创建帐号
			// 创建ftp帐号服务的url
			String ftpUrl = SystemGlobals.getValue(ServiceConst.URL_SWITCHAREA_FTP_CREATE);
			Map<String, String> parsMap = new HashMap<String, String>();
			// clientId 唯一id
			parsMap.put(ServiceConst.PARAMETER_SWITCHAREA_FTP_CREATE_CLIENTID, clientId);
			// 任务类型
			parsMap.put(ServiceConst.PARAMETER_SWITCHAREA_FTP_CREATE_TASKTYPE, type);
			// 请求返回格式，此处返回json格式
			parsMap.put(ServiceConst.FORMAT_RESPONSE, ResponseFormat.json.name());
			// 外部任务编号
			if (StringUtils.isNotEmpty(extTaskId))
				parsMap.put(ServiceConst.PARAMETER_SWITCHAREA_FTP_CREATE_TASKID, extTaskId);
			if (meta != null) {
				if (meta.getQuotaSize() > 0)
					parsMap.put(ServiceConst.PARAMETER_SWITCHAREA_FTP_CREATE_QUOTESIZE,
							Integer.toString(meta.getQuotaSize()));
			}
			String responseJson = ServiceInvokeUtil.invoke(ftpUrl, parsMap);
			if (responseJson != null) {
				try {
					resMap = JacksonUtils.toMap(responseJson);
					int code = (Integer) resMap.get("code");
					if (code == Response.SUCCESS.getCode()) {
						accountMeta = new AccountMeta();
						accountMeta.setUser((String) resMap.get("username"));
						accountMeta.setPassword((String) resMap.get("password"));
						accountMeta.setDir((String) resMap.get("dir"));
						log.debug("FTP返回路径为：" + accountMeta.getDir());
						// 验证帐户的完整性
						if (StringUtils.isEmpty(accountMeta.getUser())
								|| StringUtils.isEmpty(accountMeta.getPassword())
								|| StringUtils.isEmpty(accountMeta.getDir())) {
							throw new ServiceException("创建的帐户无效");
						}
					} else {
						throw new ServiceException("创建ftp帐号失败，原因为" + resMap.get("desc"));
					}
				} catch (Exception e) {
					throw new ServiceException("解析ftp创建接口返回值错误", e);
				}
			}
			// 创建失败：提示
			else {
				throw new ServiceException("创建ftp帐号失败");
			}

		} catch (Exception ex) {
			// 创建失败，抛出异常
			throw new ServiceException("创建ftp帐号失败");
		}
		return accountMeta;
	}

	/**
	 * 根据参数返回FTP 路径下所有的文件名
	 * 
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param remotePath
	 * @return
	 * @throws IOException
	 * @author 杨小军
	 * @author 杨小军
	 */

	// static List<String> arFiles = new ArrayList<String>();
	public static List<String> IteratorFileNames(String host, int port, String username, String password,
			String remotePath, List<String> arFiles)
			throws IOException {
		FTPFile[] files = null;
		FTPClient ftpClient = null;

		try {
			ftpClient = connect(host, port, username, password);

			ftpClient.changeWorkingDirectory(remotePath);
			files = ftpClient.listFiles();

			for (FTPFile file : files) {
				// 去掉本层跟上层目录
				if (!(".".equals(file.getName()) || "..".equals(file.getName()))) {
					if (file.isFile()) {
						arFiles.add(remotePath + "/" + file.getName());
					} else if (file.isDirectory()) {
						IteratorFileNames(host, port, username, password, remotePath + "/" + file.getName(), arFiles);
					}
				}

			}
		} finally {
			if (ftpClient != null) {
				disConnect(ftpClient);
			}
		}
		return arFiles;
	}

	public static void main(String[] args) {
		try {
			/*
			 * String[] fileNames =
			 * listFileNames(SystemGlobals.getValue("dms.ftp.host"),
			 * Integer.parseInt(SystemGlobals.getValue("dms.ftp.port", "21")),
			 * "test", "test", "/20100719");
			 */

			// List<String> fileNames =
			// IteratorFileNames(SystemGlobals.getValue("dms.ftp.host"),
			// Integer.parseInt(SystemGlobals.getValue("dms.ftp.port", "21")),
			// "test",
			// "test", "/20100719", new ArrayList<String>());

			// System.out.println(fileNames);

			downloadFile("192.168.3.228", 21, "gdb_exp", "gdbExp@@*", "/2011-02-25/4f1c92ab4ae74500b489008669001a74/", new String[] { "gdb_1.zip" }, "d:\\gdb_1.zip");
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
