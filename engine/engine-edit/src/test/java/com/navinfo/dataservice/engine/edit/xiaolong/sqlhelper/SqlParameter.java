/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.sqlhelper;

/** 
* @ClassName: SqlParameter 
* @author Zhang Xiaolong
* @date 2016年7月15日 上午10:57:03 
* @Description: TODO
*/
/**
 * 存储过程参数类型
 * @author Administrator
 *
 */
public class SqlParameter {
    /**
     * 参数名称
     */
    public String Name;
    /**
     * 参数值
     */
    public Object Value;
    /**
     * true表示参数为输出类型
     */
    public boolean OutPut;
    /**
     * 参数类型
     */
    public int Type;
    /**
     * 输入类型参数的构造函数
     * @param name 存储过程 输入类型 参数名称
     * @param value 存储过程 输入类型 参数值
     */
    public SqlParameter(String name,Object value){
        this.Name = name;
        this.Value= value;
    }
    /**
     * 输出类型参数的构造函数
     * @param type 存储过程 输出类型 参数类型
     * @param name 存储过程 输出类型 参数名称
     */
    public SqlParameter(int type,String name){
        this.Name = name;
        this.OutPut = true;
        this.Type = type;
    }
    /**
     * 返回类型参数的构造函数
     * @param type 存储过程 返回类型
     */
    public SqlParameter(int type){
        this.Name = "";
        this.OutPut = true;
        this.Type = type;
    }
}
