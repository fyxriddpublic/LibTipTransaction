package com.fyxridd.lib.tiptransaction.api;

import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.tiptransaction.TipTransactionPlugin;
import com.fyxridd.lib.tiptransaction.impl.TipTransactionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionApi {
    /**
     * 重新读取提示信息
     * 会读取'插件名/tips.yml'文件
     */
    public static void reloadTips(String plugin) {
        TipTransactionPlugin.instance.getTipTransactionManager().reloadTips(plugin);
    }

    /**
     * 注册提示的Recommends处理器
     * @param plugin 插件名
     * @param getName 获取名
     * @param tipRecommendsHandler 处理器
     */
    public static void registerRecommendsHandler(String plugin, String getName, TipRecommendsHandler tipRecommendsHandler) {
        TipTransactionPlugin.instance.getTipTransactionManager().registerRecommendsHandler(plugin, getName, tipRecommendsHandler);
    }

    /**
     * @see #newTipTransaction(boolean, String, long, int, String, java.util.List, java.util.Map, java.util.Map, String, boolean)
     */
    public static TipTransaction newTipTransaction(boolean instant, String name, long last, int tipInterval, String cmd,
                                                   List<FancyMessage> tip, Map<String, Object> map, String key){
        return new TipTransactionImpl(instant, name, last, tipInterval, cmd, tip, map, key);
    }

    /**
     * @see #newTipTransaction(boolean, String, long, int, String, java.util.List, java.util.Map, java.util.Map, String, boolean)
     */
    public static TipTransaction newTipTransaction(boolean instant, String name, long last, int tipInterval, String cmd,
                                                   List<FancyMessage> tip, Map<String, Object> map, String key, boolean convert){
        return new TipTransactionImpl(instant, name, last, tipInterval, cmd, tip, map, null, key, convert);
    }

    /**
     * @see #newTipTransaction(boolean, String, long, int, String, java.util.List, java.util.Map, java.util.Map, String, boolean)
     */
    public static TipTransaction newTipTransaction(boolean instant, String name, long last, int tipInterval, String cmd,
                                                   List<FancyMessage> tip, Map<String, Object> map,
                                                   Map<String, List<Object>> recommend, String key) {
        return new TipTransactionImpl(instant, name, last, tipInterval, cmd, tip, map, recommend, key, false);
    }

    /**
     * 创建新的提示事务<br>
     * 会更新值到tip显示里,但不会进行显示,需要调用updateShow()方法
     * @param instant 是否输入后自动确认提交
     * @param name 玩家名
     * @param last 持续时间
     * @param tipInterval 提示间隔
     * @param cmd 命令,可为null
     * @param tip 提示列表
     * @param map 名-值映射表,可为null,值可以为null或空,但名必须全
     * @param recommend 名-推荐值列表,可为null或空,名可以不全
     * @param key 正在修改的名,可为null
     * @param convert 是否在显示输入的内容时转换颜色字符
     */
    public static TipTransaction newTipTransaction(boolean instant, String name, long last, int tipInterval, String cmd,
                                                   List<FancyMessage> tip, Map<String, Object> map,
                                                   Map<String, List<Object>> recommend, String key, boolean convert) {
        return new TipTransactionImpl(instant, name, last, tipInterval, cmd, tip, map, recommend, key, convert);
    }

    /**
     * @see #tip(boolean, String, String, java.util.List, java.util.Map, java.util.Map, String, boolean)
     */
    public static void tip(boolean instant, String name, String cmd, FancyMessage tip, Map<String, Object> map, Map<String, List<Object>> recommend, String key) {
        List<FancyMessage> tips = new ArrayList<>();
        tips.add(tip);
        tip(instant, name, cmd, tips, map, recommend, key, false);
    }

    /**
     * 提示(简易&常用)
     * @param instant 是否输入后自动确认提交
     * @param name 玩家名
     * @param cmd 命令,可为null
     * @param tips 提示列表
     * @param map 名-值映射表,可为null,值可以为null或空,但名必须全
     * @param recommend 名-推荐值列表,可为null或空,名可以不全
     * @param key 正在修改的名,可为null
     * @param convert 是否在显示输入的内容时转换颜色字符
     */
    public static void tip(boolean instant, String name, String cmd, List<FancyMessage> tips, Map<String, Object> map, Map<String, List<Object>> recommend, String key, boolean convert) {
        TipTransactionPlugin.instance.getTipTransactionManager().tip(instant, name, cmd, tips, map, recommend, key, convert);
    }
}
