package com.fyxridd.lib.tiptransaction.api;

import org.bukkit.entity.Player;

/**
 * 提示params获取器
 */
public interface TipParamsHandler {
    /**
     * 获取类
     * @param p 玩家
     * @param arg 变量(可为null或空,可包含空格)
     * @return 类,null表示异常(并已经提示玩家)
     */
    public Object get(Player p, String arg);
}
