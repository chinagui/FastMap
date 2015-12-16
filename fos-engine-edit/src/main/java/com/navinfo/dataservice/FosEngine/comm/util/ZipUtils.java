package com.navinfo.dataservice.FosEngine.comm.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 压缩指定的文件或目录 解压指定的压缩文件(仅限ZIP格式). 采用JDK1.6,不依赖其它开源项目
 * 
 * @author zyh
 */
public class ZipUtils {
	/**
	 * 将指定文件或者指定目录下的所有文件压缩并生成指定路径的压缩文件. 如果压缩文件的路径或父路径不存在, 将会自动创建.
	 * 
	 * @param srcFile
	 *            将要进行压缩的文件或目录
	 * @param destfFile
	 *            最终生成的压缩文件的路径
	 * @throws IOException
	 */
	public static void zipFile(String srcFile, String destFile)
			throws IOException {
		zipFile(new File(srcFile), new File(destFile));
	}

	/**
	 * 将指定文件或目录下的所有文件压缩并生成指定路径的压缩文件. 如果压缩文件的路径或父路径不存在, 将会自动创建.
	 * 
	 * @param srcFile
	 *            将要进行压缩的文件或目录
	 * @param destFile
	 *            最终生成的压缩文件的路径
	 * @throws IOException
	 */
	public static void zipFile(File srcFile, File destFile) throws IOException {
		zipFile(srcFile, FileUtils.openOutputStream(destFile));
	}

	/**
	 * 将指定文件或目录下的所有文件压缩并将流写入指定的输出流中.
	 * 
	 * @param srcFile
	 *            将要进行压缩的目录
	 * @param outputStream
	 *            用于接收压缩产生的文件流的输出流
	 */
	private static void zipFile(File srcFile, OutputStream outputStream) {
		zipFile(srcFile, new ZipOutputStream(outputStream));
	}

	/**
	 * 将指定目录下的所有文件压缩并将流写入指定的ZIP输出流中.
	 * 
	 * @param srcFile
	 *            将要进行压缩的目录
	 * @param zipOut
	 *            用于接收压缩产生的文件流的ZIP输出流
	 */
	private static void zipFile(File srcFile, ZipOutputStream zipOut) {
		try {
			doZipFile(srcFile, zipOut, "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(zipOut);
		}
	}

	/**
	 * 压缩文件或目录到指定ZipOutputStream
	 * 
	 * @param srcFile
	 *            指定文件或者目录
	 * @param zipOut
	 *            指定ZipOutputStream输出流
	 * @param ns
	 *            文件组织到ZIP文件时的路径
	 * @throws IOException
	 */
	private static void doZipFile(File srcFile, ZipOutputStream zipOut,
			String ns) throws IOException {
		if (srcFile.isFile()) {
			zipOut.putNextEntry(new ZipEntry(ns+srcFile.getName()));
			InputStream is = FileUtils.openInputStream(srcFile);
			try {
				IOUtils.copy(is, zipOut);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(is);
			}
			return;
		}
		for (File file : srcFile.listFiles()) {
			String entryName = ns + file.getName();

			if (file.isDirectory()) {
				entryName = ns + file.getName();
				entryName += File.separator;
				zipOut.putNextEntry(new ZipEntry(entryName));
			}
			else{
				entryName = ns;
			}
			doZipFile(file, zipOut, entryName);
		}
	}

	/**
	 * 将指定的压缩文件解压到指定的目标目录下. 如果指定的目标目录不存在或其父路径不存在, 将会自动创建.
	 * 
	 * @param zipFile
	 *            将会解压的压缩文件
	 * @param destFile
	 *            解压操作的目录
	 */
	public static void unzipFile(String zipFile, String destFile)
			throws IOException {
		unzipFile(new File(zipFile), new File(destFile));
	}

	/**
	 * 将指定的压缩文件解压到指定的目标目录下. 如果指定的目标目录不存在或其父路径不存在, 将会自动创建.
	 * 
	 * @param zipFile
	 *            将会解压的压缩文件
	 * @param destFile
	 *            解压操作的目录目录
	 */
	public static void unzipFile(File zipFile, File destFile)
			throws IOException {
		unzipFile(FileUtils.openInputStream(zipFile), destFile);
		// doUnzipFile(new ZipFile(zipFile), destFile);
	}

	public static void doUnzipFile(ZipFile zipFile, File dest)
			throws IOException {
		if (!dest.exists()) {
			FileUtils.forceMkdir(dest);
		}
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile
				.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File file = new File(dest, entry.getName());
			if (entry.getName().endsWith(File.separator)) {
				FileUtils.forceMkdir(file);
			} else {
				OutputStream out = FileUtils.openOutputStream(file);
				InputStream in = zipFile.getInputStream(entry);
				try {
					IOUtils.copy(in, out);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
		}
		zipFile.close();
	}

	/**
	 * 将指定的输入流解压到指定的目标目录下.
	 * 
	 * @param is
	 *            将要解压的输入流
	 * @param destFile
	 *            解压操作的目标目录
	 * @throws IOException
	 */
	public static void unzipFile(InputStream is, File destFile)
			throws IOException {
		try {
			if (is instanceof ZipInputStream) {
				doUnzipFile((ZipInputStream) is, destFile);
			} else {
				doUnzipFile(new ZipInputStream(is), destFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * 
	 * @param zipInput
	 * @param dest
	 * @throws IOException
	 */
	private static void doUnzipFile(ZipInputStream zipInput, File dest)
			throws IOException {
		if (!dest.exists()) {
			FileUtils.forceMkdir(dest);
		}
		for (ZipEntry entry; (entry = zipInput.getNextEntry()) != null;) {
			String entryName = entry.getName();

			File file = new File(dest, entry.getName());
			if (entryName.endsWith("\\") || entryName.endsWith("/")) {
				FileUtils.forceMkdir(file);
			} else {
				OutputStream out = FileUtils.openOutputStream(file);
				try {
					IOUtils.copy(zipInput, out);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
			zipInput.closeEntry();
		}
	}

	// 测试
	public static void main(String[] args) throws IOException {
		//zipFile("C:/20151027110432.txt", "C:/1.zip");
		 unzipFile("C:/2131/1.zip", "C:/233");
	}
}