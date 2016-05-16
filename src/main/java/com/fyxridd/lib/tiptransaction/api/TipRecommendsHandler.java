package com.fyxridd.lib.tiptransaction.api;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * 提示recommends获取器
 */
public interface TipRecommendsHandler {
    /**
     * 获取推荐值列表
     * @param p 玩家
     * @param arg 变量(可为null或空,可包含空格)
     * @return 值列表,null表示异常(并已经提示玩家)
     */
    public List<Object> get(Player p, String arg);
}
