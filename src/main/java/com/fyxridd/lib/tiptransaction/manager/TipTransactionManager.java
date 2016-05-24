package com.fyxridd.lib.tiptransaction.manager;

import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.config.Setter;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.func.api.FuncApi;
import com.fyxridd.lib.params.api.ParamsConverter;
import com.fyxridd.lib.params.api.ParamsFactory;
import com.fyxridd.lib.tiptransaction.Info;
import com.fyxridd.lib.tiptransaction.RecommendInfo;
import com.fyxridd.lib.tiptransaction.TipTransactionPlugin;
import com.fyxridd.lib.tiptransaction.api.TipRecommendsHandler;
import com.fyxridd.lib.tiptransaction.api.TipTransaction;
import com.fyxridd.lib.tiptransaction.config.TipConfig;
import com.fyxridd.lib.tiptransaction.func.TipTransactionCmd;
import com.fyxridd.lib.transaction.api.TransactionApi;
import com.fyxridd.lib.transaction.api.TransactionUser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TipTransactionManager {
    //配置

    private TipConfig config;
    
    //插件名 配置名 配置信息
    private Map<String, Map<String, Info>> tips = new HashMap<>();

    //插件名 获取名 获取器
    private Map<String, Map<String, TipRecommendsHandler>> tipRecommends = new HashMap<>();

    //玩家 提示事务
    //每个玩家最多只能同时有一个提示事务
    private Map<Player, TipTransaction> playerTipTransactionHashMap = new HashMap<>();

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
                    if (t != null) TransactionApi.getTransactionUser(p.getName()).delTransaction(t.getId());
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
        try {
            if (plugin == null || config == null) throw new Exception("plugin or config is null");

            //重置
            tips.put(plugin, new HashMap<String, Info>());

            //读取
            for (String name:config.getValues(false).keySet()) {
                ConfigurationSection ms = config.getConfigurationSection(name);

                //per
                String per = ms.getString("per");

                //instant
                boolean instant = ms.getBoolean("instant", false);

                //paramsFactory
                ParamsFactory paramsFactory = new ParamsConverter().convert(plugin, ms.getConfigurationSection("params"));

                //maps
                Map<String, String> maps = new HashMap<>();
                for (String s:ms.getStringList("maps")) {
                    String[] args = s.split(" ", 2);
                    maps.put(args[0], args.length == 1?"":args[1]);
                }

                //recommends
                Map<String, RecommendInfo> recommends = new HashMap<>();
                for (String s:ms.getStringList("recommends")) {
                    RecommendInfo recommendInfo = RecommendInfo.load(plugin, s);
                    if (recommendInfo != null) recommends.put(recommendInfo.getName(), recommendInfo);
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
                this.tips.get(plugin).put(name, new Info(paramsFactory, per, instant, maps, recommends, key, tips, cmd, convert));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see com.fyxridd.lib.tiptransaction.api.TransactionApi#registerRecommendsHandler(String, String, TipRecommendsHandler)
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
     * @see com.fyxridd.lib.tiptransaction.api.TransactionApi#tip(boolean, String, String, java.util.List, java.util.Map, java.util.Map, String, boolean)
     */
    public void tip(boolean instant, String name, String cmd, List<FancyMessage> tips, Map<String, Object> map, Map<String, List<Object>> recommend, String key, boolean convert) {
        TipTransaction tipTransaction = com.fyxridd.lib.tiptransaction.api.TransactionApi.newTipTransaction(instant, name, -1, -1, cmd, tips, map, recommend, key, convert);
        TransactionUser tu = TransactionApi.getTransactionUser(name);
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
    
    /**
     * @return 不存在返回null
     */
    public TipRecommendsHandler getTipRecommendsHandler(String plugin, String getName) {
        Map<String, TipRecommendsHandler> map = tipRecommends.get(plugin);
        if (map != null) return map.get(getName);
        return null;
    }

    /**
     * 获取玩家的提示事务
     * @return 不存在返回null
     */
    public TipTransaction get(Player p) {
        return playerTipTransactionHashMap.get(p);
    }

    /**
     * 添加提示事务
     */
    public void add(Player p, TipTransaction tipTransaction) {
        playerTipTransactionHashMap.put(p, tipTransaction);
    }

    /**
     * 删除玩家的提示事务
     * @return 删除的提示事务,不存在返回null
     */
    public TipTransaction del(Player p) {
        return playerTipTransactionHashMap.remove(p);
    }

    public TipConfig getConfig() {
        return config;
    }
}
