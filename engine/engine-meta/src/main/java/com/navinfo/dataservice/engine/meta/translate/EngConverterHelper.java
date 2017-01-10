package com.navinfo.dataservice.engine.meta.translate;

import com.navinfo.dataservice.commons.util.StringUtils;

import java.io.IOException;

/**
 * Created by chaixin on 2016/12/3 0003.
 */
public class EngConverterHelper {
    private FenciConsole console;

    private EngConverter converter;

    public EngConverterHelper() {
        console = new FenciConsole();
        converter = new EngConverter();
    }

    public String chiToEng(String text) throws IOException {
        if (StringUtils.isEmpty(text)) return "";

        String[] result = console.mainSplit(text);
        String res = result[0];
        String eng = result[1];
        return converter.convertEng(text, "", res, eng);
    }
}
