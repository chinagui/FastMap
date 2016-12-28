package com.navinfo.dataservice.engine.meta.svg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * 将svg转换为png格式的图片
 * 
 * 
 */
public class SvgPngConverter {

	/**
	 * 将svgCode转换成png文件，直接输出到流中
	 * 
	 * @param svgCode
	 *            svg代码
	 * @param outputStream
	 *            输出流
	 * @throws TranscoderException
	 *             异常
	 * @throws IOException
	 *             io异常
	 */
	public static void convertToPng(String svgCode, OutputStream outputStream) throws TranscoderException, IOException {
		byte[] bytes = svgCode.getBytes();
		PNGTranscoder t = new PNGTranscoder();
		TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(bytes));
		TranscoderOutput output = new TranscoderOutput(outputStream);
		t.transcode(input, output);
		outputStream.flush();
	}
}