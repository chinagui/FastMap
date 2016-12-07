package com.navinfo.dataservice.engine.audio;


/**
 * 获取音频类
 */
public class AudioGetter {

	/**
	 * 通过uuid获取音频，返回原图或缩略图
	 * 
	 * @param rowkey
	 * @return JSONObject
	 * @throws Exception
	 */
	public byte[] getAudioByRowkey(String rowkey)
			throws Exception {

		AudioController control = new AudioController();

		byte[] audio = control.getAudioByRowkey(rowkey);

	    return audio;
	}

}
