package com.ipower.cloud.channelunify.infra.repository;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**持久化条件构造工具*/
public class MongoORMUtil {

    /**强制更新所有值，null也更新进去*/
    public static Update createUpdateByNull(Class<?> clazz,Object target){
        Update update= new Update();
        ReflectionUtils.doWithFields(clazz,new ReflectionUtils.FieldCallback(){
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                //没有Transient注解
                if(!field.isAnnotationPresent(Transient.class)&&!field.getName().equals("id")){
                    if(!field.isAccessible()){
                        field.setAccessible(true);
                    }
                    Object fieldValue = field.get(target);
                    if(fieldValue!=null){
                        update.set(field.getName(),fieldValue);
                    }else{
                        update.unset(field.getName());
                    }
                }
            }
        });
        return update;

    }


    /**注意，target如果有集合字段不能有值*/
    public static Update createUpdateByNotNull(Class<?> clazz,Object target){
        Update update= new Update();
        ReflectionUtils.doWithFields(clazz,new ReflectionUtils.FieldCallback(){
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                //没有Transient注解
                if(!field.isAnnotationPresent(Transient.class)&&!field.getName().equals("id")){
                    if(!field.isAccessible()){
                        field.setAccessible(true);
                    }
                    Object fieldValue = field.get(target);
                    if(fieldValue!=null){
                        update.set(field.getName(),fieldValue);
                    }
                }
            }
        });
        return update;

    }

    public static Criteria createCriteriaByEqual(Class<?> clazz,Object target){

        Criteria criteria=new Criteria();
        /**注意，target如果有集合字段不能有值*/
        ReflectionUtils.doWithFields(clazz,new ReflectionUtils.FieldCallback(){
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                //没有Transient注解
                if(!field.isAnnotationPresent(Transient.class)&&!field.getName().equals("id")){
                    if(!field.isAccessible()){
                        field.setAccessible(true);
                    }
                    Object fieldValue = field.get(target);
                    if(fieldValue!=null){
                            criteria.and(field.getName()).is(fieldValue);
                    }
                }
            }
        });

       return criteria;

    }
}
