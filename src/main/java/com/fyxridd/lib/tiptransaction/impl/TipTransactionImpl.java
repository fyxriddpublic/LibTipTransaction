package com.fyxridd.lib.tiptransaction.impl;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.tiptransaction.api.TipTransaction;
import com.fyxridd.lib.transaction.api.TransactionApi;
import com.fyxridd.lib.transaction.api.TransactionUser;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示事务<br>
 * 本身并非实际的功能,而是作为玩家与功能的一个接口
 */
public class TipTransactionImpl extends TipTransaction {
    //是否输入后自动确认提交
    private boolean instant;
    //命令文本,包含{名称}这样的替换符
    private String cmd;
    //提示信息列表
    private List<FancyMessage> tip;
    //名-值映射表
    private HashMap<String, Object> map;
    //名-推荐值列表
    private HashMap<String, List<Object>> recommend;
    //玩家正在修改的名
    private String key;
    //是否在显示时转换颜色字符
    private boolean convert;

    //缓存
    private HashMap<String, Integer> recommendPosHash;

    /**
     * @see com.fyxridd.lib.core.api.TransactionApi#newTipTransaction(boolean, String, long, int, String, java.util.List, java.util.HashMap, java.util.HashMap, String)
     */
    public TipTransactionImpl(boolean instant, String name, long last, int tipInterval, String cmd,
                              List<FancyMessage> tip, HashMap<String, Object> map, String key) {
        this(instant, name, last, tipInterval, cmd, tip, map, null, key, false);
    }

    /**
     * @see com.fyxridd.lib.core.api.TransactionApi#newTipTransaction(boolean, String, long, int, String, java.util.List, java.util.HashMap, java.util.HashMap, String)
     */
    public TipTransactionImpl(boolean instant, String name, long last, int tipInterval, String cmd,
                              List<FancyMessage> tip, HashMap<String, Object> map, HashMap<String, List<Object>> recommend, String key, boolean convert) {
        super(name, last, tipInterval);
        this.convert = convert;
        this.instant = instant;
        this.cmd = cmd;
        this.tip = tip;
        if (!this.tip.isEmpty()) this.tip.get(0).combine(CoreMain.tipTransactionManager.getPrefix(), true);
        this.map = map;
        this.recommend = recommend;
        if (recommend != null && !recommend.isEmpty()) {
            this.recommendPosHash = new HashMap<>();
            for (String s:recommend.keySet()) this.recommendPosHash.put(s, 0);

            //将recommend里对应的第一项值更新到map里
            for (Map.Entry<String, List<Object>> entry:recommend.entrySet()) {
                if (map.containsKey(entry.getKey()) && !entry.getValue().isEmpty()) map.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        //
        setKey(key, false);
        //每个玩家最多只能同时有一个提示事务检测
        Player p = Bukkit.getPlayerExact(name);
        if (p != null) {
            //删除旧的
            TipTransaction pre = CoreMain.tipTransactionManager.getPlayerTipTransactionHashMap().remove(p);
            if (pre != null) {
                TransactionUser tu = TransactionApi.getTransactionUser(name);
                tu.delTransaction(pre.getId());
            }
            //设置新的
            CoreMain.tipTransactionManager.getPlayerTipTransactionHashMap().put(p, this);
        }
    }

    @Override
    public void onTip() {
        super.onTip();
    }

    /**
     * 'a' 确认提交<br>
     * 'b 名' 指定正在修改的key(会同时注册输入)
     * 'c' 取消
     */
    @Override
    public void onOperate(String... args) {
        if (args.length == 0) return;
        Player p = Bukkit.getPlayerExact(getName());
        if (p == null) {//玩家不存在或不在线
            TransactionUser tu = TransactionApi.getTransactionUser(getName());
            if (tu != null) tu.delTransaction(getId());
            return;
        }
        switch (args.length) {
            case 1:
                if (args[0].equalsIgnoreCase("a")) {//确认提交
                    //删除输入注册
                    InputManager.del(p, false);
                    //将map值更新到命令
                    if (cmd != null && map != null) {
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            String show;
                            if (value == null) show = "";
                            else show = String.valueOf(value);
                            cmd = cmd.replace("{" + key + "}", show);
                        }
                    }
                    //结束事务
                    TransactionUser tu = TransactionApi.getTransactionUser(getName());
                    if (tu != null) tu.delTransaction(getId());
                    //发出命令
                    if (cmd != null) p.chat(cmd);
                } else if (args[0].equalsIgnoreCase("c")) {//取消
                    //删除输入注册
                    InputManager.del(p, false);
                    //删除事务
                    TipTransaction t = CoreMain.tipTransactionManager.getPlayerTipTransactionHashMap().get(p);
                    if (t != null && t.getId() == getId()) {
                        CoreMain.tipTransactionManager.getPlayerTipTransactionHashMap().remove(p);
                        //清空提示
                        ShowApi.tip(p, "", true);
                    }
                    TransactionUser tu = TransactionApi.getTransactionUser(getName());
                    if (tu != null) tu.delTransaction(getId());
                }
                break;
            case 2:
                if (args[0].equalsIgnoreCase("b")) {//指定正在修改的key
                    //设置
                    setKey(args[1], true);
                    //重新显示
                    updateShow();
                }
                break;
        }
    }

    @Override
    public void onTimeOut() {
        super.onTimeOut();
    }

    @Override
    public void onCancel() {
        super.onCancel();
    }

    /**
     * 指定玩家正在输入的key(同时会注册输入)
     * @param key 名
     * @param change 如果有推荐值,是否改变
     */
    public void setKey(final String key, boolean change) {
        if (key != null && map != null && map.containsKey(key)) {
            Player p = Bukkit.getPlayerExact(getName());
            if (p != null) {
                //注册输入
                if (CoreApi.registerInput(p, new InputHandler() {
                    @Override
                    public boolean onInput(String s) {
                        if (instant) {
                            if (map.containsKey(key)) map.put(key, s);
                            onOperate("a");
                            return true;
                        } else {
                            if (map.containsKey(key)) {
                                map.put(key, s);
                                //重新显示
                                updateShow();
                            }
                            return false;
                        }
                    }
                }, true, false)) {
                    //注册输入成功
                    this.key = key;
                    //推荐值
                    if (change && recommend != null) {
                        List<Object> recommendValues = recommend.get(key);
                        if (recommendValues != null) {
                            //pos
                            int pos;
                            if (recommendPosHash.containsKey(key)) pos = recommendPosHash.get(key);
                            else pos = 0;
                            pos ++;
                            if (pos >= recommendValues.size()) pos = 0;
                            recommendPosHash.put(key, pos);
                            //
                            Object recommendValue = recommendValues.get(pos);
                            map.put(key, recommendValue);
                        }
                    }
                }
            }
        }
    }

    /**
     * 玩家更新显示信息<br>
     *     会将map里的值代入<br>
     *     此外如果设置了key,则会进行相应的处理显示
     */
    public void updateShow() {
        Player p = Bukkit.getPlayerExact(getName());
        if (p != null) {
            HashMap<String, Object> copy = null;
            //代入值
            if (key != null && map != null && map.containsKey(key)) {
                copy = new HashMap<>();
                for (String key:map.keySet()) copy.put(key, convert?convert(map.get(key)):map.get(key));
                copy.put(key, "\u00A7d\u00A7n>"+(convert?convert(map.get(key)):map.get(key))+"\u00A7d\u00A7n<");
            }
            //转换替换符
            List<FancyMessage> result = new ArrayList<>();
            for (FancyMessage fmi : this.tip) {
                FancyMessage fmi2 = fmi.clone();
                MessageApi.convert(fmi2, copy);
                result.add(fmi2);
            }
            //显示
            ShowApi.tip(p, result, true);
        }
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

    public void setMap(HashMap<String, Object> map) {
        this.map = map;
    }

    public List<FancyMessage> getTip() {
        return tip;
    }

    public void setTip(List<FancyMessage> tip) {
        this.tip = tip;
    }

    private String convert(Object obj) {
        if (obj == null) return null;
        return CoreApi.convert(obj.toString());
    }
}
