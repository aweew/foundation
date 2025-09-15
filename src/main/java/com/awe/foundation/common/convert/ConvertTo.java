package com.awe.foundation.common.convert;

/**
 * 实现此接口的convert方法，将Req对象转为实体类
 * T为实体类持久化对象(PO)，和数据库一一对应
 *
 * @author Awe
 * @date 2023/4/7 15:13
 */
public interface ConvertTo<T> {

    /**
     * 将Req对象转为实体类
     *
     * @return 实体对象
     */
    T convert();

}
