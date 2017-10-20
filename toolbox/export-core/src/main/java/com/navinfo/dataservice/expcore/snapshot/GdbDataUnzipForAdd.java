package com.navinfo.dataservice.expcore.snapshot;

import java.io.File;

import org.apache.uima.pear.util.FileUtil;

import com.fastmap.NdsSqliteEncryptor;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;

/**
 * 
 * @ClassName GdbDataUnzipForAdd
 * @author Han Shaoming
 * @date 2017年10月18日 上午10:03:35
 * @Description TODO
 */
public class GdbDataUnzipForAdd {

	/**
	 * 解压缩及解密
	 * @author Han Shaoming
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static String unzip(String dir) throws Exception{
		
		//判断相应省份的文件是否存在
		File provinceFile = new File(dir);
		if (!provinceFile.exists() || provinceFile.listFiles().length < 1) {
			return null;
		}else{
			boolean flag =false;
			for (File file : provinceFile.listFiles()) {
				if(file.isFile() && file.getName().endsWith(".zip")){flag = true;}
			}
			if(!flag){return null;}
		}
		//查询最新的压缩文件
		String localZipFile = getLastestInfo(dir,null);
		System.out.println("最新文件所在目录:"+localZipFile);
		//解压
		String localUnzipDir = dir+File.separator+"tmp";
		File unzipDir = new File(localUnzipDir);
		if (unzipDir.exists()) {
			FileUtil.deleteDirectory(unzipDir);
		}
		unzipDir.mkdirs();
		ZipUtils.unzipFile(localZipFile,localUnzipDir);
		String sqliteFile = null;
		File unzipFile = new File(localUnzipDir);
		if(unzipFile.exists() && unzipFile.listFiles().length >0){
			sqliteFile = localUnzipDir+File.separator+unzipFile.listFiles()[0].getName();
		}
		if(StringUtils.isEmpty(sqliteFile)){return null;}
		System.out.println("更新文件所在目录:"+sqliteFile);
		//获取 NdsSqliteEncryptor 实例
		NdsSqliteEncryptor encryptor = NdsSqliteEncryptor.getInstance();
		//解密
		System.out.println("......Start...解密...");
		try {
			//进行加密，参数1：源数据库文件名 参数2：加密后数据库文件吗 参数3：加密密码
			String gdbmm = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbSqlitePassword);
			encryptor.decryptDataBase(sqliteFile ,localUnzipDir+File.separator+"gdbdata_une.sqlite", gdbmm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("....解密成功..End......");
		System.out.println("解密成功的文件:"+localUnzipDir+File.separator+"gdbdata_une.sqlite");
		//删除原有sqlite 数据库
		File sqliteOld = new File(sqliteFile);
		if(sqliteOld.exists() && sqliteOld.isFile()){
			sqliteOld.delete();
			System.out.println(" 删除解压的加密sqlite 数据库成功!");
		}
		return localUnzipDir+File.separator+"gdbdata_une.sqlite";
	}
	
	/**
	 * 压缩及加密
	 * @author Han Shaoming
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static String zip(String dir) throws Exception{
		String zipfile = null;
		try {
			//判断相应省份的文件是否存在
			File provinceFile = new File(dir);
			if (!provinceFile.exists() || provinceFile.listFiles().length < 1) {
				return null;
			}else{
				boolean flag =false;
				for (File file : provinceFile.listFiles()) {
					if(file.isFile() && file.getName().endsWith(".zip")){flag = true;}
				}
				if(!flag){return null;}
			}
			//判断是否有文件
			String localUnzipDir = dir+File.separator+"tmp";
			String sqliteFile = null;
			File unzipFile = new File(localUnzipDir);
			if(unzipFile.exists() && unzipFile.listFiles().length >0){
				sqliteFile = localUnzipDir+File.separator+unzipFile.listFiles()[0].getName();
			}
			if(StringUtils.isEmpty(sqliteFile)){return null;}
			
			String operateDate = StringUtils.getCurrentTime();
			System.out.println("......Start......");
			//获取 NdsSqliteEncryptor 实例
			NdsSqliteEncryptor encryptor = NdsSqliteEncryptor.getInstance();
			try {
				//进行加密，参数1：源数据库文件名 参数2：加密后数据库文件吗 参数3：加密密码
				String gdbmm = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbSqlitePassword);
				encryptor.encryptDataBase(sqliteFile,dir + "/tmp/gdbdata.sqlite", gdbmm);
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			System.out.println("....加密成功..End......");
			
			//删除原有sqlite 数据库
			File fileOld = new File(dir + "/tmp/gdbdata_une.sqlite");
			if(fileOld.exists() && fileOld.isFile()){
				fileOld.delete();
				System.out.println(" 删除未加密sqlite 数据库成功!");
			}
			
			zipfile = dir + File.separator + operateDate + ".zip";
			// 压缩文件
			ZipUtils.zipFile(dir + "/tmp/", zipfile);
			
			FileUtil.deleteDirectory(new File(dir + "/tmp/"));
		} catch (Exception e) {
			System.err.println("压缩及加密数据报错:"+e.getMessage());	
		}
		return zipfile;
	}
	
	
	private static String getLastestInfo(String dir, String subdir) throws Exception{
		
		File file = new File(dir);
		
		File[] files = file.listFiles();
		
		long version = 0;
		
		for(File f:files){
			
			if(!f.isFile()){
				continue;
			}
			
			String name = f.getName();
			if(!name.endsWith(".zip")){continue;}
			
			int index= name.indexOf(".");
			
			if(index==-1){
				continue;
			}
			
			long tmpVersion = Long.parseLong(name.substring(0, index));
			
			if (tmpVersion > version){
				version = tmpVersion;
			}
		}
		
		if(subdir != null){
			return dir+File.separator+subdir+File.separator+version+".zip";
		}
		else{
			return dir+File.separator+version+".zip";
		}
		
	}
}
