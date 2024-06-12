package com.ipower.cloud.channelunify.infra.repository;

/**分表工具类,目前只支持单键值分片*/
public abstract class CustomerTableSharding<T> {

    private ThreadLocal<T> shardKeyThreadLocal = new ThreadLocal<T>();

    //放入shardingkey
    public void setShardingKeyValue(T shardingKeyValue){
        shardKeyThreadLocal.set(shardingKeyValue);
    };

    //返回表名字，在domain对象的document注解上调用
    public String getCollectionName(){
        T shardingKeyValue = shardKeyThreadLocal.get();
        return getTableNameByShardingRule(shardingKeyValue);
    }


    public abstract String getTableNameByShardingRule(T shardingKeyValue);

}
