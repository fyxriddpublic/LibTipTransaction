package com.fyxridd.lib.tiptransaction;

import java.util.Set;

import com.fyxridd.lib.core.api.UtilApi;

public class MapValue {
    private String value;
    //不为null可为空列表
    private Set<String> params;
    public MapValue(String value) {
        super();
        this.value = value;
        this.params = UtilApi.getParams(value);
    }
    public String getValue() {
        return value;
    }
    public Set<String> getParams() {
        return params;
    }
}
