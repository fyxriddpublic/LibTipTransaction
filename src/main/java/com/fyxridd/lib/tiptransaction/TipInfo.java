package com.fyxridd.lib.tiptransaction;

public class TipInfo {
    //插件名
    String plugin;
    //语言ID
    int langId;

    public TipInfo(String plugin, int langId) {
        this.plugin = plugin;
        this.langId = langId;
    }

    /**
     * @return 异常返回null
     */
    public static TipInfo load(String plugin, String s) {
        try {
            if (s.contains(" ")) return new TipInfo(s.split(" ")[0], Integer.parseInt(s.split(" ")[1]));
            else return new TipInfo(plugin, Integer.parseInt(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
