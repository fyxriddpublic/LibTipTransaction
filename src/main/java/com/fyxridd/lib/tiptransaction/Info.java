package com.fyxridd.lib.tiptransaction;

import java.util.List;
import java.util.Map;

import com.fyxridd.lib.params.api.ParamsFactory;

/**
 * Tip配置信息
 */
public class Info {
    private ParamsFactory paramsFactory;
    private String per;
    private boolean instant;
    private Map<String, String> maps;//不为null
    private Map<String, RecommendInfo> recommends;//不为null
    private String key;
    private List<Integer> tips;
    private String cmd;
    private boolean convert;

    public Info(ParamsFactory paramsFactory, String per, boolean instant, Map<String, String> maps, Map<String, RecommendInfo> recommends, String key, List<Integer> tips, String cmd, boolean convert) {
        this.paramsFactory = paramsFactory;
        this.per = per;
        this.instant = instant;
        this.maps = maps;
        this.recommends = recommends;
        this.key = key;
        this.tips = tips;
        this.cmd = cmd;
        this.convert = convert;
    }

    public ParamsFactory getParamsFactory() {
        return paramsFactory;
    }

    public String getPer() {
        return per;
    }

    public boolean isInstant() {
        return instant;
    }

    public Map<String, String> getMaps() {
        return maps;
    }

    public Map<String, RecommendInfo> getRecommends() {
        return recommends;
    }

    public String getKey() {
        return key;
    }

    public List<Integer> getTips() {
        return tips;
    }

    public String getCmd() {
        return cmd;
    }

    public boolean isConvert() {
        return convert;
    }
}
