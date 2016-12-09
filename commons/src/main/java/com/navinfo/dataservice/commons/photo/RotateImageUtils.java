package com.navinfo.dataservice.commons.photo;

import java.awt.Dimension;
import java.awt.Graphics2D;  
import java.awt.Image;  
import java.awt.Rectangle;  
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

/**
 * 图片旋转的工具类
 * @author zhangli5174
 *
 */
public class RotateImageUtils {

	public static BufferedImage Rotate(Image src, int angel) {  
        int src_width = src.getWidth(null);  
        int src_height = src.getHeight(null);  
        // calculate the new image size  
        Rectangle rect_des = CalcRotatedSize(new Rectangle(new Dimension(  
                src_width, src_height)), angel);  
  
        BufferedImage res = null;  
        res = new BufferedImage(rect_des.width, rect_des.height,  
                BufferedImage.TYPE_INT_RGB);  
        Graphics2D g2 = res.createGraphics();  
        // transform  
        g2.translate((rect_des.width - src_width) / 2,  
                (rect_des.height - src_height) / 2);  
        g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);  
  
        g2.drawImage(src, null, null);  
        return res;  
    }  
  
    public static Rectangle CalcRotatedSize(Rectangle src, int angel) {  
        // if angel is greater than 90 degree, we need to do some conversion  
        if (angel >= 90) {  
            if(angel / 90 % 2 == 1){  
                int temp = src.height;  
                src.height = src.width;  
                src.width = temp;  
            }  
            angel = angel % 90;  
        }  
  
        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2;  
        double len = 2 * Math.sin(Math.toRadians(angel) / 2) * r;  
        double angel_alpha = (Math.PI - Math.toRadians(angel)) / 2;  
        double angel_dalta_width = Math.atan((double) src.height / src.width);  
        double angel_dalta_height = Math.atan((double) src.width / src.height);  
  
        int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha  
                - angel_dalta_width));  
        int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha  
                - angel_dalta_height));  
        int des_width = src.width + len_dalta_width * 2;  
        int des_height = src.height + len_dalta_height * 2;  
        return new java.awt.Rectangle(new Dimension(des_width, des_height));  
    }  
    
    /**
     * @Title: rotateImage
     * @Description: 传入图片路径,按角度旋转后,重新将图片存会原路径
     * @param path
     * @throws ImageProcessingException
     * @throws IOException  void
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2016年12月7日 下午7:59:18 
     */
    public static void rotateImage(String path) throws ImageProcessingException, IOException{
    	File jpegFile= new File(path);  
    	  
        Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);  
        Directory directory = (Directory) metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);  

        int orientation = 0;  
        if(directory != null){
        try {  
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);  
        } catch (MetadataException me) {  
           // logger.warn("Could not get orientation");  
        }  
  
        System.out.println("orientation: "+orientation);  
  
        BufferedImage src = ImageIO.read(jpegFile);  
        BufferedImage des = RotateImageUtils.Rotate(src, orientation);  
        ImageIO.write(des,"jpg", new File(path)); 
        }
    }
    
    
    /**
     * @Title: rotateOrientatione
     * @Description: 获取图片旋转角度
     * @param imageStream
     * @return
     * @throws ImageProcessingException
     * @throws IOException  int
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2016年12月9日 上午10:46:16 
     */
    public static int rotateOrientatione(InputStream imageStream) throws ImageProcessingException, IOException{
        Metadata metadata = ImageMetadataReader.readMetadata(imageStream);  
        Directory directory = (Directory) metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);  
        int orientation = 0;  
        InputStream rotateImgStream = imageStream;
        BufferedImage src1 = ImageIO.read(imageStream); 
        if(directory != null ){
        try {  
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);  
        } catch (MetadataException me) {  
           // logger.warn("Could not get orientation");  
        }  
        System.out.println("orientation: "+orientation);  
	       
        }
        return orientation;
    }
    
    /**
     * @Title: rotateImage
     * @Description: 旋转图片
     * @param image
     * @param orientation
     * @return
     * @throws ImageProcessingException
     * @throws IOException  Image
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2016年12月9日 上午10:45:55 
     */
    public static Image rotateImage(Image image,int orientation) throws ImageProcessingException, IOException{
        System.out.println("orientation: "+orientation);  
        BufferedImage newImage = (BufferedImage) image;
        if(image != null){
	        if(orientation != 0){
	        	 newImage = RotateImageUtils.Rotate(image, orientation);  
	        }
        }
        return newImage;
    }
    
    
    public static InputStream getImageStream(BufferedImage des){

        InputStream is = null;

        des.flush();

        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        ImageOutputStream imOut;
        try {
        imOut = ImageIO.createImageOutputStream(bs);

        ImageIO.write(des, "jpg",imOut);

        is= new ByteArrayInputStream(bs.toByteArray());

        } catch (IOException e) {
        e.printStackTrace();
        }
        return is;
        }
    
    public static void main(String[] args) throws IOException, Exception {
    	rotateImage("f:/x11.jpg");
    	
    	/*Image srcImage = null;
    	File file= new File("f:/x1.jpg");
    	InputStream bis =  new FileInputStream(file);
		srcImage = ImageIO.read(bis);
		System.out.println("srcImage:"+srcImage);
		//**********2016.12.09 zl 添加图片自动旋转功能 **************
		Image newImage = ImageIO.read(bis);
		System.out.println("newImage: "+newImage);
	    InputStream newIn = new FileInputStream(file);
	    Image newImage1 = ImageIO.read(newIn);
	    int rotateAngle = RotateImageUtils.rotateOrientatione(new FileInputStream(file));//获取图片旋转角度
    	if(rotateAngle > 0){
	    	 newImage = RotateImageUtils.rotateImage(ImageIO.read(bis),rotateAngle);
	    	 System.out.println("newImage: "+newImage);
	    	 newImage = RotateImageUtils.rotateImage(newImage1,rotateAngle);
	    	 System.out.println("newImage: "+newImage);
    	}
    	if(newImage != null){
    		srcImage = newImage;
    		System.out.println("srcImage: "+srcImage);
    	}
		//*****************************************************
		
		int srcWidth = srcImage.getWidth(null);// 原图片宽度
		int srcHeight = srcImage.getHeight(null);// 原图片高度
		int dstMaxSize = 120;// 目标缩略图的最大宽度/高度，宽度与高度将按比例缩写
		int dstWidth = srcWidth;// 缩略图宽度
		int dstHeight = srcHeight;// 缩略图高度
*/	}
    
    /*public static void main(String[] args) throws ImageProcessingException, IOException {  
    	//rotateImage("f:/xx.jpg");
    	File file= new File("f:/x1.jpg");
    	InputStream is =  new FileInputStream(file);
    	int rotateAngle= rotateOrientatione(is);
    	InputStream iss =  is;//new FileInputStream(file);
    	BufferedImage image= ImageIO.read(iss); 
    	
    	Image newImage = rotateImage(image,rotateAngle);
    	if(newImage != null){
    		InputStream newInputStream = getImageStream((BufferedImage) newImage);
    		System.out.println(newInputStream.toString());
    		BufferedImage testImage= ImageIO.read(newInputStream); 
    		ImageIO.write((RenderedImage) newImage,"jpg", new File("f:/newImage1.jpg")); 
    		ImageIO.write(testImage,"jpg", new File("f:/testImage2.jpg")); 
    	}
        //File jpegFile= new File("f:/y.jpg");  
  
        Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);  
        Directory directory = (Directory) metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);  

        int orientation = 0;  
        try {  
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);  
        } catch (MetadataException me) {  
           // logger.warn("Could not get orientation");  
        }  
  
        System.out.println("orientation: "+orientation);  
  
        BufferedImage src = ImageIO.read(jpegFile);  
        BufferedImage des = RotateImageUtils.Rotate(src, orientation);  
        ImageIO.write(des,"jpg", new File("f:/yy.JPG"));  
  

    }*/  
  
    
}