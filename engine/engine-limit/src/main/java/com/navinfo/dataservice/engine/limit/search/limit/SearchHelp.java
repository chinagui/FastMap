package com.navinfo.dataservice.engine.limit.search.limit;

import java.util.HashMap;

public class SearchHelp {

   private static HashMap<String, String> adminMapping = new HashMap<String, String>() {{

        put("110099", "110100");

        put("120099", "120100");

        put("310099", "310100");

        put("419098", "419001");

        put("429095", "429004");

        put("429094", "429005");

        put("429093", "429006");

        put("429078", "429021");

        put("441999", "441900");

        put("442099", "442000");

        put("460399", "460300");

        put("460499", "460400");

        put("469098", "469001");

        put("469097", "469002");

        put("469094", "469005");

        put("469093", "469006");

        put("469092", "469007");

        put("469078", "469021");

        put("469077", "469022");

        put("469076", "469023");

        put("469075", "469024");

        put("469074", "469025");

        put("469073", "469026");

        put("469072", "469027");

        put("469071", "469028");

        put("469070", "469029");

        put("469069", "469030");

        put("500099", "500100");

        put("659098", "659001");

        put("659097", "659002");

        put("659096", "659003");

        put("659095", "659004");

        put("659094", "659005");

        put("659093", "659006");

        put("659092", "659007");

        put("659091", "659008");

        put("659090", "659009");
    }};


    public static String updateInfoAdminCode(String mkadminCode) {
        String infoAdminCode = mkadminCode;

        if (adminMapping.containsKey(mkadminCode)) {
            infoAdminCode = adminMapping.get(mkadminCode);
        }

        return infoAdminCode;
    }
}
