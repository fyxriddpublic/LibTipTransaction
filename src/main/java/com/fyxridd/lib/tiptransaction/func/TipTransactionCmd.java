package com.fyxridd.lib.tiptransaction.func;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.core.config.ConfigManager.Setter;
import com.fyxridd.lib.func.api.func.Default;
import com.fyxridd.lib.func.api.func.Extend;
import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.params.api.Session;
import com.fyxridd.lib.tiptransaction.Info;
import com.fyxridd.lib.tiptransaction.MapValue;
import com.fyxridd.lib.tiptransaction.RecommendInfo;
import com.fyxridd.lib.tiptransaction.TipInfo;
import com.fyxridd.lib.tiptransaction.TipTransactionPlugin;
import com.fyxridd.lib.tiptransaction.api.TipRecommendsHandler;
import com.fyxridd.lib.tiptransaction.config.TipConfig;

@FuncType("cmd")
public class TipTransactionCmd {
    private TipConfig config;
    
    public TipTransactionCmd() {
        //添加配置监听
        ConfigApi.addListener(TipTransactionPlugin.instance.pn, TipConfig.class, new Setter<TipConfig>(){
            @Override
            public void set(TipConfig value) {
                config = value;
            }
        });
    }
    
    @Func("tip")
    public void tipTransaction(CommandSender sender, String plugin, String name, @Default("") @Extend String arg) {
        //提示事务只能玩家来请求
        if (!(sender instanceof Player)) return;
        Player p = (Player) sender;
        
        String[] args = arg.split(" ");
        
        try {
            if (args.length >= 2) {
                //未找到指定的提示配置
                Info info = TipTransactionPlugin.instance.getTipTransactionManager().getInfo(plugin, name);
                if (info == null) {
                    MessageApi.send(p, get(p.getName(), 10, plugin, name), true);
                    return;
                }
                //per检测
                if (!PerApi.checkHasPer(p.getName(), info.getPer())) return;
                //instant
                boolean instant = info.isInstant();
                //convert
                boolean convert = info.isConvert();
                //params
                Map<String, String> strDefaults = new HashMap<>();
                strDefaults.put("name", p.getName());
                Session paramsSession = info.getParamsFactory().openSession(null, strDefaults, args);
                //map
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, MapValue> entry:info.getMaps().entrySet()) map.put(entry.getKey(), convert(paramsSession, entry.getValue()));
                //recommend
                Map<String, List<Object>> recommend = new HashMap<>();
                for (Map.Entry<String, RecommendInfo> entry:info.getRecommends().entrySet()) {
                    RecommendInfo recommendInfo = entry.getValue();
                    switch (recommendInfo.getType()) {
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
                            Map<String, TipRecommendsHandler> hash2 = tipRecommends.get(recommendInfo.type2Plugin);
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
                String key = info.getKey();
                //tips
                List<FancyMessage> tips = new ArrayList<>();
                for (int langId:info.getTips()) {
                    FancyMessage msg = get(null, langId);
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
    
    private String convert(Session session, MapValue value) {
        String result = value.getValue();
        for (String param:value.getParams()) result = result.replace("{"+param+"}", session.getStr(param));
        return result;
    }

    private static String convert(Map<String, Object> params, String s) {
        for (Map.Entry<String, Object> entry:params.entrySet()) {
            s = s.replace("{"+entry.getKey()+"}", String.valueOf(entry.getValue()));
        }
        return s;
    }
    
    private FancyMessage get(String player, int id, Object... args) {
        return config.getLang().get(player, id, args);
    }
}
