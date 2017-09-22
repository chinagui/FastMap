package com.navinfo.dataservice.engine.limit.glm.iface;

/**
 * Created by ly on 2017/9/20.
 */
public interface IProcess {
    /**
     * @return 操作参数
     */
    public ICommand getCommand();

    /**
     * @return 操作结果
     */
    public Result getResult();

    /**
     * 从数据库获取数据，并加锁
     *
     * @return True 成功
     * @throws Exception
     */
    public boolean prepareData() throws Exception;

    /**
     * 前检查
     *
     * @return 检查结果
     * @throws Exception
     */
    public String preCheck() throws Exception;

    /**
     * 执行操作
     *
     * @return 操作后的对象
     * @throws Exception
     */
    public String run() throws Exception;

    /**
     * 后检查
     *
     * @throws Exception
     */
    public void postCheck() throws Exception;

    /**
     * @return 检查结果
     * @throws Exception
     */
    public String getPostCheck() throws Exception;

    /**
     * 操作结果写入数据库
     *
     * @return True 成功
     * @throws Exception
     */
    public boolean recordData() throws Exception;

}