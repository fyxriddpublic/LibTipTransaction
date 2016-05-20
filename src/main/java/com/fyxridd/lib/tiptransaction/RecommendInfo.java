package com.fyxridd.lib.tiptransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fyxridd.lib.core.api.UtilApi;

public class RecommendInfo {
    //Map映射名
    String name;
    //1,2
    int type;
    //type为1
    List<String> type1List;
    //type为2
    String type2Plugin;//插件名
    String type2GetName;//获取名
    String type2GetArg;//变量

    public RecommendInfo(String name, int type, List<String> type1List, String type2Plugin, String type2GetName, String type2GetArg) {
        this.name = name;
        this.type = type;
        this.type1List = type1List;
        this.type2Plugin = type2Plugin;
        this.type2GetName = type2GetName;
        this.type2GetArg = type2GetArg;
    }

    public static RecommendInfo getType1(String name, List<String> list) {
        return new RecommendInfo(name, 1, list, null, null, null);
    }

    public static RecommendInfo getType2(String name, String plugin, String getName, String getArg) {
        return new RecommendInfo(name, 2, null, plugin, getName, getArg);
    }

    /**
     * 读取recommend信息
     * @param data 数据
     * @return 异常返回null
     */
    public static RecommendInfo load(String plugin, String data) {
        try {
            String[] args = data.split(" ");
            switch (Integer.parseInt(args[1])) {
                case 1:
                    List<String> list = new ArrayList<>();
                    Collections.addAll(list, args[2].split(","));
                    return getType1(args[0], list);
                case 2:
                    String pluginName;
                    String getName;
                    String getArg;
                    if (args[2].contains(":")) {
                        pluginName = args[2].split(":")[0];
                        getName = args[2].split(":")[1];
                    }else {
                        pluginName = plugin;
                        getName = args[2];
                    }
                    getArg = args.length > 3?UtilApi.combine(args, " ", 3, args.length):"";
                    return getType2(args[0], pluginName, getName, getArg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getType() {
        return type;
    }
}
