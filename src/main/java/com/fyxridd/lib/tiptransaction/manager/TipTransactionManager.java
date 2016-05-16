package com.fyxridd.lib.tiptransaction.manager;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TipTransactionManager implements Listener, FunctionInterface {
    private static final String FUNC_NAME = "TipTransaction";

    //Tip配置信息
    private static class Info {
        //Param信息
        private static class ParamInfo {
            //提示变量名
            String name;
            //1,2,3
            int type;
            //type为1
            String type1Str;//可为空
            //type为2
            String type2Plugin;//插件名
            String type2GetName;//获取名
            String type2GetData;//属性/方法
            String type2GetArg;//变量,可为空不为null
            //type为3
            String type3Str;

            private ParamInfo(String name, int type, String type1Str, String type2Plugin, String type2GetName, String type2GetData, String type2GetArg, String type3Str) {
                this.name = name;
                this.type = type;
                this.type1Str = type1Str;
                this.type2Plugin = type2Plugin;
                this.type2GetName = type2GetName;
                this.type2GetData = type2GetData;
                this.type2GetArg = type2GetArg;
                this.type3Str = type3Str;
            }

            public static ParamInfo getType1(String name, String str) {
                return new ParamInfo(name, 1, str, null, null, null, null, null);
            }

            public static ParamInfo getType2(String name, String plugin, String getName, String getData, String getArg) {
                return new ParamInfo(name, 2, null, plugin, getName, getData, getArg, null);
            }

            public static ParamInfo getType3(String name, String str) {
                return new ParamInfo(name, 3, null, null, null, null, null, str);
            }

            /**
             * 读取param信息
             * @param plugin 插件名
             * @param name 提示变量名
             * @param data map项数据
             * @return 异常返回null
             */
            public static ParamInfo load(String plugin, String name, String data) {
                try {
                    String[] args = data.split(" ");
                    switch (Integer.parseInt(args[0])) {
                        case 1:
                            return getType1(name, args.length == 1?"":CoreApi.combine(args, " ", 1, args.length));
                        case 2:
                            String[] args2 = args[1].split(":");
                            String pluginName, getName, getData, getArg;
                            if (args2.length == 2) {
                                pluginName = plugin;
                                getName = args2[0];
                                getData = args2[1];
                            }else {
                                pluginName = args2[0];
                                getName = args2[1];
                                getData = args2[2];
                            }
                            getArg = args.length > 2?CoreApi.combine(args, " ", 2, args.length):"";
                            return getType2(name, pluginName, getName, getData, getArg);
                        case 3:
                            return getType3(name, args[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        //Recommends信息
        private static class RecommendInfo {
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
                            getArg = args.length > 3?CoreApi.combine(args, " ", 3, args.length):"";
                            return getType2(args[0], pluginName, getName, getArg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        //Tip信息
        private static class TipInfo {
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

        String per;
        boolean instant;
        HashMap<String, ParamInfo> params;//不为null
        HashMap<String, String> maps;//不为null
        HashMap<String, RecommendInfo> recommends;//不为null
        String key;
        List<TipInfo> tips;
        String cmd;
        boolean convert;

        public Info(String per, boolean instant, HashMap<String, ParamInfo> params, HashMap<String, String> maps, HashMap<String, RecommendInfo> recommends, String key, List<TipInfo> tips, String cmd, boolean convert) {
            this.per = per;
            this.instant = instant;
            this.params = params;
            this.maps = maps;
            this.recommends = recommends;
            this.key = key;
            this.tips = tips;
            this.cmd = cmd;
            this.convert = convert;
        }
    }

    //配置

    //提示前缀,如'提示: '
    private FancyMessage prefix;

    //插件名 配置名 配置信息
    private HashMap<String, HashMap<String, Info>> tips = new HashMap<>();

    //缓存

    //玩家 提示事务
    //每个玩家最多只能同时有一个提示事务
    private HashMap<Player, TipTransaction> playerTipTransactionHashMap = new HashMap<>();

    //提示Maps获取器列表
    //插件名 获取名 获取器
    private HashMap<String, HashMap<String, TipParamsHandler>> tipParams = new HashMap<>();

    //插件名 获取名 获取器
    private HashMap<String, HashMap<String, TipRecommendsHandler>> tipRecommends = new HashMap<>();

	public TipTransactionManager() {
		//读取配置文件
		loadConfig();
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //注册功能
        FuncApi.register(this);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
	}

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        TipTransaction t = playerTipTransactionHashMap.remove(p);
        if (t != null) {
            TransactionUser tu = TransactionApi.getTransactionUser(p.getName());
            tu.delTransaction(t.getId());
        }
    }

    @Override
    public String getName() {
        return FUNC_NAME;
    }

    @Override
    public boolean isOn(String name, String data) {
        return true;
    }

    /**
     * '<插件名> <配置名> [配置变量]' 提示
     */
    @Override
    public void onOperate(Player p, String... args) {
        try {
            if (args.length >= 2) {
                //解析
                String plugin = args[0];
                String name = args[1];
                String[] configParams = (args.length > 2?CoreApi.combine(args, " ", 2, args.length):"").split(" ");
                //指定的插件未注册任何提示配置
                HashMap<String, Info> hash = tips.get(plugin);
                if (hash == null) {
                    ShowApi.tip(p, get(1215, plugin), true);
                    return;
                }
                //指定插件未注册指定的配置名
                Info info = hash.get(name);
                if (info == null) {
                    ShowApi.tip(p, get(1220, plugin, name), true);
                    return;
                }
                //per检测
                if (!PerApi.checkPer(p, info.per)) return;
                //instant
                boolean instant = info.instant;
                //convert
                boolean convert = info.convert;
                //params
                HashMap<String, Object> params = new HashMap<>();
                params.put("name", p.getName());//默认变量
                for (Map.Entry<String, Info.ParamInfo> entry:info.params.entrySet()) {
                    Info.ParamInfo paramInfo = entry.getValue();
                    switch (paramInfo.type) {
                        case 1:
                            params.put(entry.getKey(), paramInfo.type1Str);
                            break;
                        case 2:
                            //未注册任何提示params处理器
                            HashMap<String, TipParamsHandler> hash2 = tipParams.get(paramInfo.type2Plugin);
                            if (hash2 == null) {
                                ShowApi.tip(p, get(1235, paramInfo.type2Plugin), true);
                                return;
                            }
                            //未注册指定的params处理器
                            TipParamsHandler tipParamsHandler = hash2.get(paramInfo.type2GetName);
                            if (tipParamsHandler == null) {
                                ShowApi.tip(p, get(1240, paramInfo.type2Plugin, paramInfo.type2GetName), true);
                                return;
                            }
                            //异常
                            int index = 0;
                            String[] ss = paramInfo.type2GetArg.split(" ");
                            String[] result = new String[ss.length];
                            for (String s:ss) result[index++] = CoreApi.convertArg(configParams, s);
                            Object c = tipParamsHandler.get(p, CoreApi.combine(result, " ", 0, result.length));
                            if (c == null) return;
                            //获取值
                            String value = null;
                            try {
                                String data = paramInfo.type2GetData;
                                if (data.charAt(data.length() - 1) == ')') {//调用方法
                                    if (data.charAt(data.length()-2) == '(') {
                                        String methodName = data.substring(0, data.length()-2);
                                        Method method = c.getClass().getDeclaredMethod(methodName);
                                        boolean accessible = method.isAccessible();
                                        method.setAccessible(true);
                                        value = String.valueOf(method.invoke(c));
                                        method.setAccessible(accessible);
                                    }else {
                                        String methodName = data.substring(0, data.length()-3);
                                        Method method = c.getClass().getDeclaredMethod(methodName, Player.class);
                                        boolean accessible = method.isAccessible();
                                        method.setAccessible(true);
                                        value = String.valueOf(method.invoke(c, p));
                                        method.setAccessible(accessible);
                                    }
                                }else {//调用属性
                                    Field field = c.getClass().getDeclaredField(data);
                                    boolean accessible = field.isAccessible();
                                    field.setAccessible(true);
                                    value = String.valueOf(field.get(c));
                                    field.setAccessible(accessible);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //属性/方法异常
                            if (value == null) {
                                ShowApi.tip(p, get(1245, paramInfo.type2Plugin, paramInfo.type2GetName), true);
                                return;
                            }
                            //设置
                            params.put(entry.getKey(), value);
                            break;
                        case 3:
                            params.put(entry.getKey(), CoreApi.convertArg(configParams, paramInfo.type3Str));
                            break;
                    }
                }
                //map
                HashMap<String, Object> map = new HashMap<>();
                for (Map.Entry<String, String> entry:info.maps.entrySet()) map.put(entry.getKey(), convert(params, entry.getValue()));
                //recommend
                HashMap<String, List<Object>> recommend = new HashMap<>();
                for (Map.Entry<String, Info.RecommendInfo> entry:info.recommends.entrySet()) {
                    Info.RecommendInfo recommendInfo = entry.getValue();
                    switch (recommendInfo.type) {
                        case 1:
                        {
                            List<Object> list = new ArrayList<>();
                            for (String s:recommendInfo.type1List) list.add(convert(params, s));
                            recommend.put(entry.getKey(), list);
                        }
                        break;
                        case 2:
                        {
                            //未注册任何提示recommends处理器
                            HashMap<String, TipRecommendsHandler> hash2 = tipRecommends.get(recommendInfo.type2Plugin);
                            if (hash2 == null) {
                                ShowApi.tip(p, get(1225, recommendInfo.type2Plugin), true);
                                return;
                            }
                            //未注册指定的recommends处理器
                            TipRecommendsHandler recommendsHandler = hash2.get(recommendInfo.type2GetName);
                            if (recommendsHandler == null) {
                                ShowApi.tip(p, get(1230, recommendInfo.type2Plugin, recommendInfo.type2GetName), true);
                                return;
                            }
                            //异常
                            List<Object> list = recommendsHandler.get(p, convert(params, recommendInfo.type2GetArg));
                            if (list == null) return;
                            //添加
                            recommend.put(entry.getKey(), list);
                        }
                        break;
                    }
                }
                //key
                String key = info.key;
                //tips
                List<FancyMessage> tips = new ArrayList<>();
                for (Info.TipInfo tipInfo:info.tips) {
                    FancyMessage msg = FormatApi.get(tipInfo.plugin, tipInfo.langId);
                    MessageApi.convert(msg, params);
                    tips.add(msg);
                }
                //cmd
                String cmd = convert(params, info.cmd);
                //提示事务
                tip(instant, p.getName(), cmd, tips, map, recommend, key, convert);
                return;
            }
        } catch (Exception e) {
            //操作异常
            e.printStackTrace();
            ShowApi.tip(p, get(100), true);
            return;
        }
        //输入格式错误
        ShowApi.tip(p, get(5), true);
    }

    public void reloadTips(String plugin) {
        reloadTips(plugin, CoreApi.loadConfigByUTF8(new File(CoreApi.pluginPath, plugin+"/tips.yml")));
    }

    public void reloadTips(String plugin, File file) {
        reloadTips(plugin, CoreApi.loadConfigByUTF8(file));
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
            HashMap<String, Info.ParamInfo> params = new HashMap<>();
            if (ms.contains("params")) {
                MemorySection paramsMs = (MemorySection)ms.get("params");
                for (String s:paramsMs.getValues(false).keySet()) {
                    Info.ParamInfo paramInfo = Info.ParamInfo.load(plugin, s, paramsMs.getString(s));
                    if (paramInfo != null) params.put(paramInfo.name, paramInfo);
                }
            }

            //maps
            HashMap<String, String> maps = new HashMap<>();
            for (String s:ms.getStringList("maps")) {
                String[] args = s.split(" ");
                maps.put(args[0], args.length == 1?"":CoreApi.combine(args, " ", 1, args.length));
            }

            //recommends
            HashMap<String, Info.RecommendInfo> recommends = new HashMap<>();
            for (String s:ms.getStringList("recommends")) {
                Info.RecommendInfo recommendInfo = Info.RecommendInfo.load(plugin, s);
                if (recommendInfo != null) recommends.put(recommendInfo.name, recommendInfo);
            }

            //key
            String key = ms.getString("key");

            //tips
            List<Info.TipInfo> tips = new ArrayList<>();
            for (String s:ms.getStringList("tips")) tips.add(Info.TipInfo.load(plugin, s));

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
        HashMap<String, TipParamsHandler> getHash = tipParams.get(plugin);
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
        HashMap<String, TipRecommendsHandler> getHash = tipRecommends.get(plugin);
        if (getHash == null) {
            getHash = new HashMap<>();
            tipRecommends.put(plugin, getHash);
        }
        getHash.put(getName, tipRecommendsHandler);
    }

    /**
     * @see com.fyxridd.lib.core.api.TransactionApi#tip(boolean, String, String, java.util.List, java.util.HashMap, java.util.HashMap, String, boolean)
     */
    public void tip(boolean instant, String name, String cmd, List<FancyMessage> tips, HashMap<String, Object> map, HashMap<String, List<Object>> recommend, String key, boolean convert) {
        TipTransaction tipTransaction = TransactionApi.newTipTransaction(instant, name, -1, -1, cmd, tips, map, recommend, key, convert);
        TransactionUser tu = TransactionManager.getTransactionUser(name);
        tu.addTransaction(tipTransaction);
        tu.setRunning(tipTransaction.getId());
        tipTransaction.updateShow();
    }

    public FancyMessage getPrefix() {
        return prefix;
    }

    public HashMap<Player, TipTransaction> getPlayerTipTransactionHashMap() {
        return playerTipTransactionHashMap;
    }

    private static String convert(HashMap<String, Object> params, String s) {
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
