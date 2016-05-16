package com.fyxridd.lib.tiptransaction;

import com.fyxridd.lib.config.api.ConfigApi;
import com.fyxridd.lib.core.api.plugin.SimplePlugin;
import com.fyxridd.lib.tiptransaction.manager.TipTransactionManager;
import com.fyxridd.lib.transaction.config.TransactionConfig;
import com.fyxridd.lib.transaction.manager.TransactionManager;

public class TipTransactionPlugin extends SimplePlugin{
    public static TipTransactionPlugin instance;

    private TipTransactionManager tipTransactionManager;

    @Override
    public void onEnable() {
        instance = this;

        //注册配置
        ConfigApi.register(pn, TransactionConfig.class);

        tipTransactionManager = new TipTransactionManager();

        super.onEnable();
    }

    public TipTransactionManager getTipTransactionManager() {
        return tipTransactionManager;
    }

}