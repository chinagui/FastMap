package com.navinfo.dataservice.commons.photo;

import java.awt.Dimension;
import java.awt.Graphics2D;  
import java.awt.Image;  
import java.awt.Rectangle;  
import java.awt.image.BufferedImage;  
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

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
     * @Title: rotateImage
     * @Description: 传入图片流,按角度旋转后,输出 图片流
     * @param imageStream
     * @return
     * @throws ImageProcessingException
     * @throws IOException  InputStream
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2016年12月7日 下午7:51:59 
     */
    public static InputStream rotateImage(InputStream imageStream) throws ImageProcessingException, IOException{
    	  
        Metadata metadata = ImageMetadataReader.readMetadata(imageStream);  
        Directory directory = (Directory) metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);  

        int orientation = 0;  
        InputStream rotateImgStream = imageStream;
        if(directory != null ){
        try {  
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);  
        } catch (MetadataException me) {  
           // logger.warn("Could not get orientation");  
        }  
        System.out.println("orientation: "+orientation);  
	        if(orientation != 0){
	        	BufferedImage src = ImageIO.read(imageStream);  
	        	BufferedImage des = RotateImageUtils.Rotate(src, orientation);  
	        	// ImageIO.write(des,"jpg", new File(path)); 
	        	rotateImgStream=getImageStream(des);
	        }
        }
        return rotateImgStream;
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
    
    
    public static void main(String[] args) throws ImageProcessingException, IOException {  
    	rotateImage("f:/x.jpg");
    	
    	
    	
        //File jpegFile= new File("f:/y.jpg");  
  
       /* Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);  
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
        ImageIO.write(des,"jpg", new File("f:/yy.JPG"));  */
  

    }  
  
    
}