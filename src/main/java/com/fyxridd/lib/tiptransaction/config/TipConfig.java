package com.fyxridd.lib.tiptransaction.config;

import com.fyxridd.lib.core.api.config.basic.Path;
import com.fyxridd.lib.core.api.config.convert.ConfigConvert;
import com.fyxridd.lib.core.api.lang.LangConverter;
import com.fyxridd.lib.core.api.lang.LangGetter;

public class TipConfig {
    @Path("lang")
    @ConfigConvert(LangConverter.class)
    private LangGetter lang;

    public LangGetter getLang() {
        return lang;
    }
}
