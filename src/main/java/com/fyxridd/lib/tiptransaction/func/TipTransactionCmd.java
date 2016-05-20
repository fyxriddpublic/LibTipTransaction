package com.fyxridd.lib.tiptransaction.func;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.core.api.lang.LangApi;
import com.fyxridd.lib.core.api.lang.LangGetter;
import com.fyxridd.lib.core.config.ConfigManager.Setter;
import com.fyxridd.lib.func.api.func.Default;
import com.fyxridd.lib.func.api.func.Extend;
import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.params.api.Session;
import com.fyxridd.lib.tiptransaction.Info;
import com.fyxridd.lib.tiptransaction.RecommendInfo;
import com.fyxridd.lib.tiptransaction.TipTransactionPlugin;
import com.fyxridd.lib.tiptransaction.api.TipRecommendsHandler;
import com.fyxridd.lib.tiptransaction.config.TipConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        try {
            String[] args = arg.split(" ");
            //未找到指定的提示配置
            Info info = TipTransactionPlugin.instance.getTipTransactionManager().getInfo(plugin, name);
            if (info == null) {
                MessageApi.send(p, get(p.getName(), 10, plugin, name), true);
                return;
            }
            //per检测
            if (!PerApi.checkHasPer(p.getName(), info.getPer())) return;
            //开启会话
            Session paramsSession = info.getParamsFactory().openSession(null, null, args);
            //instant
            boolean instant = info.isInstant();
            //convert
            boolean convert = info.isConvert();
            //params
            Map<String, Object> params = new HashMap<>();
            params.put("name", p.getName());
            for (String strName:info.getParamsFactory().getStrNames()) params.put(strName, paramsSession.getStr(strName));
            //map
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, String> entry:info.getMaps().entrySet()) map.put(entry.getKey(), convert(params, entry.getValue()));
            //recommend
            Map<String, List<Object>> recommend = new HashMap<>();
            for (Map.Entry<String, RecommendInfo> entry:info.getRecommends().entrySet()) {
                RecommendInfo recommendInfo = entry.getValue();
                switch (recommendInfo.getType()) {
                    case 1:
                    {
                        List<Object> list = new ArrayList<>();
                        for (String s:recommendInfo.getType1List()) list.add(convert(params, s));
                        recommend.put(entry.getKey(), list);
                    }
                    break;
                    case 2:
                    {
                        //未注册指定的recommends处理器
                        TipRecommendsHandler recommendsHandler = TipTransactionPlugin.instance.getTipTransactionManager().getTipRecommendsHandler(recommendInfo.getType2Plugin(), recommendInfo.getType2GetName());
                        if (recommendsHandler == null) {
                            MessageApi.send(p, get(p.getName(), 20, recommendInfo.getType2Plugin(), recommendInfo.getType2GetName()), true);
                            return;
                        }
                        //异常
                        List<Object> list = recommendsHandler.get(p, convert(params, recommendInfo.getType2GetArg()));
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
            LangGetter langGetter = LangApi.getPluginLang(plugin);
            if (langGetter == null) throw new Exception("plugin '"+plugin+"' has no lang");
            List<FancyMessage> tips = new ArrayList<>();
            for (int langId:info.getTips()) {
                FancyMessage msg = langGetter.get(langId);
                MessageApi.convert(msg, params);
                tips.add(msg);
            }
            //cmd
            String cmd = convert(params, info.getCmd());
            //提示事务
            com.fyxridd.lib.tiptransaction.api.TransactionApi.tip(instant, p.getName(), cmd, tips, map, recommend, key, convert);
        } catch (Exception e) {
            e.printStackTrace();
            MessageApi.send(p, get(p.getName(), 5), true);
        }
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
