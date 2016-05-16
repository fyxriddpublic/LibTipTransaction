package com.fyxridd.lib.tiptransaction.api;

/**
 * 提示事务<br>
 * 本身并非实际的功能,而是作为玩家与功能的一个接口
 */
public abstract class TipTransaction extends Transaction {
    /**
     * 会更新值到tip显示里,但不会进行显示,需要调用updateShow()方法
     * @param name 玩家名
     * @param last 持续时间
     * @param tipInterval 提示间隔
     */
    public TipTransaction(String name, long last, int tipInterval) {
        super(name, last, tipInterval);
    }

    @Override
    public void onTip() {
    }

    /**
     * 'a' 确认提交<br>
     * 'b 名' 指定正在修改的key(会同时注册输入)
     * 'c' 取消
     * @param args 操作内容
     */
    @Override
    public void onOperate(String... args) {
    }

    @Override
    public void onTimeOut() {
    }

    @Override
    public void onCancel() {
    }

    /**
     * 指定玩家正在输入的key(同时会注册输入)
     * @param key 名
     * @param change 如果有推荐值,是否改变
     */
    public void setKey(final String key, boolean change) {
    }

    /**
     * 玩家更新显示信息<br>
     *     会将map里的值代入<br>
     *     此外如果设置了key,则会进行相应的处理显示
     */
    public void updateShow() {
    }
}
