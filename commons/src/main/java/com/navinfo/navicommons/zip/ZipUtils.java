package com.navinfo.navicommons.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * 
 * @author liuqing
 * 
 */
public abstract class ZipUtils {

	public static final Logger logger = Logger.getLogger(ZipUtils.class);

	private static void createDirectory(String directory, String subDirectory) {
		String dir[];
		File fl = new File(directory);
		if (subDirectory == "" && fl.exists() != true)
			fl.mkdir();
		else if (subDirectory != "") {
			dir = subDirectory.replace('\\', '/').split("/");
			for (int i = 0; i < dir.length; i++) {
				File subFile = new File(directory + File.separator + dir[i]);
				if (!subFile.exists()) {
					subFile.mkdir();
					// logger.debug("create Directory��" +directory +
					// File.separator + dir[i]);
				}
				directory += File.separator + dir[i];
			}
		}

	}

	/**
	 * 
	 * @param zipFileName
	 */
	public static void unZip(String zipFileName, String outputDirectory) throws Exception {
		try {
			ZipFile zipFile = new ZipFile(zipFileName);
			Enumeration e = zipFile.getEntries();
			ZipEntry zipEntry = null;
			createDirectory(outputDirectory, "");
			while (e.hasMoreElements()) {
				zipEntry = (ZipEntry) e.nextElement();
				// logger.debug("unziping: " + zipEntry.getName());
				if (zipEntry.isDirectory()) {
					String name = zipEntry.getName();
					name = name.substring(0, name.length() - 1);
					File f = new File(outputDirectory + File.separator + name);
					f.mkdir();
					// logger.debug("create Directory��" + outputDirectory +
					// File.separator + name);
				} else {
					String fileName = zipEntry.getName();
					fileName = fileName.replace('\\', '/');
					if (fileName.indexOf("/") != -1) {
						createDirectory(outputDirectory, fileName.substring(0, fileName.lastIndexOf("/")));
						fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
					}

					File f = new File(outputDirectory + File.separator + zipEntry.getName());

					f.createNewFile();
					InputStream in = zipFile.getInputStream(zipEntry);
					FileOutputStream out = new FileOutputStream(f);

					byte[] by = new byte[1024];
					int c;
					while ((c = in.read(by)) != -1) {
						out.write(by, 0, c);
					}
					out.close();
					in.close();
				}
			}
			zipFile.close();
		} catch (Exception ex) {
			logger.error(ex);
		}

	}

	public static File zip(File file) throws IOException {
		return doZip(new File[] { file }, file.getAbsolutePath());
	}
    public static File zip(File file,String zipFileName) throws IOException {
		return doZip(new File[] { file }, zipFileName);
	}

	public static File doZip(File[] files, String zipFileName) throws IOException {
		ZipOutputStream zipOut = null; // 压缩Zip
		int dotIndex = zipFileName.lastIndexOf(".");
		if (dotIndex > -1)
			zipFileName = zipFileName.substring(0, dotIndex) + ".zip";
		else {
			zipFileName = zipFileName + ".zip";
		}
		try {
			zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
			compressFiles(files, zipOut);

		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (zipOut != null)
				zipOut.close();
		}
		return new File(zipFileName);
	}

	private static void compressFiles(File[] files, ZipOutputStream zipOut) throws IOException {
		for (File file : files) {
			if (file == null)
				continue; // 空的文件对象
			if (file.isDirectory()) {// 是目录
				compressFolder(file, zipOut);
			} else {// 是文件
				compressFile(file, zipOut);
			}
		}
	}

	/**
	 *压缩文件或空目录。由compressFiles()调用。
	 * 
	 * @param file
	 *            需要压缩的文件
	 *@param zipOut
	 *            zip输出流
	 */
	public static void compressFile(File file, ZipOutputStream zipOut) throws IOException {

		String fileName = file.getName();
		/*
		 * 因为是空目录，所以要在结尾加一个"/"。 ? 不然就会被当作是空文件。 ?
		 * ZipEntry的isDirectory()方法中,目录以"/"结尾. ? org.apache.tools.zip.ZipEntry :
		 * public boolean isDirectory() { return getName().endsWith("/"); } ?
		 */
		if (file.isDirectory())
			fileName = fileName + "/";// 此处不能用"\\"

		zipOut.putNextEntry(new ZipEntry(fileName));

		// 如果是文件则需读;如果是空目录则无需读，直接转到zipOut.closeEntry()。
		byte[] buffer = new byte[1024];
		if (file.isFile()) {
			int readedBytes = 0;
			FileInputStream fileIn = new FileInputStream(file);
			while ((readedBytes = fileIn.read(buffer)) > 0) {
				zipOut.write(buffer, 0, readedBytes);
			}
			fileIn.close();
		}

		zipOut.closeEntry();
	}

	/**
	 *递归完成目录文件读取。由compressFiles()调用。
	 * 
	 * @param dir
	 *            需要处理的文件对象
	 *@param zipOut
	 *            zip输出流
	 */
	private static void compressFolder(File dir, ZipOutputStream zipOut) throws IOException {

		File[] files = dir.listFiles();

		if (files.length == 0)// 如果目录为空，则单独压缩空目录。
			compressFile(dir, zipOut);
		else
			// 如果目录不为空,则分别处理目录和文件.
			compressFiles(files, zipOut);
	}

	public static void main(String[] args) throws Exception {
		//ZipUtils.unZip("c:\\AdminDivision.war", "c:\\AdminDivision");
		ZipUtils.zip(new File("d:\\temp"));
	}
}
