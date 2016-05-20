package com.fyxridd.lib.tiptransaction.manager;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.config.ConfigManager.Setter;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.core.api.inter.*;
import com.fyxridd.lib.func.api.FuncApi;
import com.fyxridd.lib.tiptransaction.Info;
import com.fyxridd.lib.tiptransaction.MapValue;
import com.fyxridd.lib.tiptransaction.RecommendInfo;
import com.fyxridd.lib.tiptransaction.TipInfo;
import com.fyxridd.lib.tiptransaction.TipTransactionPlugin;
import com.fyxridd.lib.tiptransaction.api.TipRecommendsHandler;
import com.fyxridd.lib.tiptransaction.api.TipTransaction;
import com.fyxridd.lib.tiptransaction.config.TipConfig;
import com.fyxridd.lib.tiptransaction.func.TipTransactionCmd;
import com.fyxridd.lib.transaction.api.TransactionApi;
import com.fyxridd.lib.transaction.api.TransactionUser;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TipTransactionManager {
    //配置

    private TipConfig config;
    
    //提示前缀,如'提示: '
    private FancyMessage prefix;

    //插件名 配置名 配置信息
    private Map<String, Map<String, Info>> tips = new HashMap<>();

    //缓存

    //玩家 提示事务
    //每个玩家最多只能同时有一个提示事务
    private Map<Player, TipTransaction> playerTipTransactionHashMap = new HashMap<>();

    //插件名 获取名 获取器
    private Map<String, Map<String, TipRecommendsHandler>> tipRecommends = new HashMap<>();

	public TipTransactionManager() {
	    //添加配置监听
	    ConfigApi.addListener(TipTransactionPlugin.instance.pn, TipConfig.class, new Setter<TipConfig>(){
            @Override
            public void set(TipConfig value) {
                config = value;
            }
	    });
        //注册功能
        FuncApi.register(TipTransactionPlugin.instance.pn, new TipTransactionCmd());
        //注册事件
        {
            //玩家退出
            Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, TipTransactionPlugin.instance, EventPriority.LOWEST, new EventExecutor() {
                
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    PlayerQuitEvent event = (PlayerQuitEvent) e;
                    Player p = event.getPlayer();
                    TipTransaction t = playerTipTransactionHashMap.remove(p);
                    if (t != null) {
                        TransactionUser tu = TransactionApi.getTransactionUser(p.getName());
                        tu.delTransaction(t.getId());
                    }
                }
            }, TipTransactionPlugin.instance);
        }
	}

    public void reloadTips(String plugin) {
        reloadTips(plugin, UtilApi.loadConfigByUTF8(new File(CoreApi.pluginPath, plugin+"/tips.yml")));
    }

    public void reloadTips(String plugin, File file) {
        reloadTips(plugin, UtilApi.loadConfigByUTF8(file));
    }

    public void reloadTips(String plugin, YamlConfiguration config) {
        if (plugin == null || config == null) return;

        //重置
        tips.put(plugin, new HashMap<String, Info>());

        //读取
        for (String name:config.getValues(false).keySet()) {
            MemorySection ms = (MemorySection) config.get(name);

            //per
            String per = ms.getString("per");

            //instant
            boolean instant = ms.getBoolean("instant", false);

            //params
            Map<String, RecommendInfo.ParamInfo> params = new HashMap<>();
            if (ms.contains("params")) {
                MemorySection paramsMs = (MemorySection)ms.get("params");
                for (String s:paramsMs.getValues(false).keySet()) {
                    RecommendInfo.ParamInfo paramInfo = Info.ParamInfo.load(plugin, s, paramsMs.getString(s));
                    if (paramInfo != null) params.put(paramInfo.name, paramInfo);
                }
            }

            //maps
            Map<String, MapValue> maps = new HashMap<>();
            for (String s:ms.getStringList("maps")) {
                String[] args = s.split(" ", 2);
                maps.put(args[0], new MapValue(args.length == 1?"":args[1]));
            }

            //recommends
            Map<String, Info.RecommendInfo> recommends = new HashMap<>();
            for (String s:ms.getStringList("recommends")) {
                Info.RecommendInfo recommendInfo = Info.RecommendInfo.load(plugin, s);
                if (recommendInfo != null) recommends.put(recommendInfo.name, recommendInfo);
            }

            //key
            String key = ms.getString("key");

            //tips
            List<Integer> tips = ms.getIntegerList("tips");

            //cmd
            String cmd = ms.getString("cmd");

            //convert
            boolean convert = ms.getBoolean("convert");

            //添加缓存
            this.tips.get(plugin).put(name, new Info(per, instant, params, maps, recommends, key, tips, cmd, convert));
        }
    }

    /**
     * @see com.fyxridd.lib.core.api.TransactionApi#registerParamsHandler(String, String, com.fyxridd.lib.core.api.inter.TipParamsHandler)
     */
    public void registerParamsHandler(String plugin, String getName, TipParamsHandler tipParamsHandler) {
        Map<String, TipParamsHandler> getHash = tipParams.get(plugin);
        if (getHash == null) {
            getHash = new HashMap<>();
            tipParams.put(plugin, getHash);
        }
        getHash.put(getName, tipParamsHandler);
    }

    /**
     * @see com.fyxridd.lib.core.api.TransactionApi#registerRecommendsHandler(String, String, com.fyxridd.lib.core.api.inter.TipRecommendsHandler)
     */
    public void registerRecommendsHandler(String plugin, String getName, TipRecommendsHandler tipRecommendsHandler) {
        Map<String, TipRecommendsHandler> getHash = tipRecommends.get(plugin);
        if (getHash == null) {
            getHash = new HashMap<>();
            tipRecommends.put(plugin, getHash);
        }
        getHash.put(getName, tipRecommendsHandler);
    }

    /**
     * @see com.fyxridd.lib.core.api.TransactionApi#tip(boolean, String, String, java.util.List, java.util.Map, java.util.Map, String, boolean)
     */
    public void tip(boolean instant, String name, String cmd, List<FancyMessage> tips, Map<String, Object> map, Map<String, List<Object>> recommend, String key, boolean convert) {
        TipTransaction tipTransaction = TransactionApi.newTipTransaction(instant, name, -1, -1, cmd, tips, map, recommend, key, convert);
        TransactionUser tu = TransactionManager.getTransactionUser(name);
        tu.addTransaction(tipTransaction);
        tu.setRunning(tipTransaction.getId());
        tipTransaction.updateShow();
    }

    /**
     * 获取提示信息
     * @param plugin 插件
     * @param name 提示名
     * @return 不存在返回null
     */
    public Info getInfo(String plugin, String name) {
        Map<String, Info> map = tips.get(plugin);
        if (map != null) return map.get(name);
        return null;
    }
    
    public FancyMessage getPrefix() {
        return prefix;
    }

    public Map<Player, TipTransaction> getPlayerTipTransactionHashMap() {
        return playerTipTransactionHashMap;
    }

    private static String convert(Map<String, Object> params, String s) {
        for (Map.Entry<String, Object> entry:params.entrySet()) {
            s = s.replace("{"+entry.getKey()+"}", String.valueOf(entry.getValue()));
        }
        return s;
    }

	private void loadConfig() {
        //prefix
        prefix = get(1200);
        //tips
        reloadTips(CorePlugin.pn, new File(CorePlugin.dataPath, "tips.yml"));
    }

	private static FancyMessage get(int id, Object... args) {
		return FormatApi.get(CorePlugin.pn, id, args);
	}
}
