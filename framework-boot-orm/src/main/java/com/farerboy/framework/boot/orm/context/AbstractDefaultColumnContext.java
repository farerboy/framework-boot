package com.farerboy.framework.boot.orm.context;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.farerboy.framework.boot.common.exception.BaseException;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author linjianbin
 */
public abstract class AbstractDefaultColumnContext {

    private static Map<String,Object> defaultColumn = new HashMap<>();

    public void initDefaultColumn(){
        setDefaultColumn(defaultColumn);
    }

    public abstract void setDefaultColumn(Map<String, Object> defaultColumn);

    public static boolean contains(String column){
        if(MapUtils.isEmpty(defaultColumn)){
            return false;
        }
        return defaultColumn.containsKey(column);
    }

    public static Object getDefaultColumn(String column){
        if(MapUtils.isEmpty(defaultColumn)){
            return null;
        }
        return defaultColumn.get(column);
    }

    public static Map<String, Object> getBaseColumn(Class<?> cls) {
        Map<String,Object> column = Maps.newHashMap();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String columnStr = null;
            TableField tableField = f.getAnnotation(TableField.class);
            if(tableField != null && StringUtils.isNotBlank(tableField.value())){
                columnStr = tableField.value();
            }
            if(columnStr == null){
                columnStr = f.getName();
            }
            if(contains(columnStr)){
                column.put(columnStr,getDefaultColumn(columnStr));
            }
        }
        return column;
    }

    public static QueryWrapper getIdQueryWrapper(Class<?> cls, Serializable id){
        QueryWrapper wrapper = new QueryWrapper();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String columnStr = null;
            // 先判断是否是主键
            if("id".equals(f.getName())){
                columnStr = "id";
            }
            if(columnStr == null){
                TableId tableId = f.getAnnotation(TableId.class);
                if(tableId != null && StringUtils.isNotBlank(tableId.value())){
                    columnStr = tableId.value();
                }
            }
            // 是主键
            if(StringUtils.isNotBlank(columnStr)){
                wrapper.eq(columnStr,id);
                continue;
            }
            TableField tableField = f.getAnnotation(TableField.class);
            if(tableField != null && StringUtils.isNotBlank(tableField.value())){
                columnStr = tableField.value();
            }
            if(columnStr == null){
                columnStr = f.getName();
            }
            if(contains(columnStr)){
                wrapper.eq(columnStr,getDefaultColumn(columnStr));
            }
        }
        return wrapper;
    }

    public static QueryWrapper getBaseQueryWrapper(Class<?> cls) {
        QueryWrapper wrapper = new QueryWrapper();
        Map<String,Object> map = getBaseColumn(cls);
        Set<String> keySet = map.keySet();
        if(CollectionUtils.isEmpty(keySet)){
            return wrapper;
        }
        for(String key : keySet){
            wrapper.eq(key,map.get(key));
        }
        return wrapper;
    }

    public static UpdateWrapper getBaseUpdateWrapper(Class<?> cls){
        UpdateWrapper wrapper = new UpdateWrapper();
        Map<String,Object> map = getBaseColumn(cls);
        Set<String> keySet = map.keySet();
        if(CollectionUtils.isEmpty(keySet)){
            return wrapper;
        }
        for(String key : keySet){
            wrapper.eq(key,map.get(key));
        }
        return wrapper;
    }

    public static QueryWrapper getQueryWrapper(Object o) {
        Class cls = o.getClass();
        Map<String,Object> map = getBaseColumn(cls);
        QueryWrapper wrapper = new QueryWrapper();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            Object fieldObject = null;
            try {
                fieldObject = f.get(o);
            } catch (IllegalAccessException e) {
                throw new BaseException("IllegalAccessException",e);
            }
            if (fieldObject == null) {
                continue;
            }
            if(fieldObject instanceof String && StringUtils.isBlank(fieldObject.toString())){
                continue;
            }
            String column = null;
            TableField tableField = f.getAnnotation(TableField.class);
            if(tableField != null && StringUtils.isNotBlank(tableField.value())){
                column = tableField.value();
            }
            if(column == null){
                column = f.getName();
            }
            wrapper.eq(column,fieldObject);
            map.remove(column);
        }
        Set<String> keySet = map.keySet();
        if(CollectionUtils.isEmpty(keySet)){
            return wrapper;
        }
        for(String key : keySet){
            wrapper.eq(key,map.get(key));
        }
        return wrapper;
    }

    public static  <T> T newInstance(Class<T> cls) {
        T t = null;
        try {
            t = cls.newInstance();
        }catch (Exception e){
            throw new BaseException("ClassNotFound",e.getMessage(),e);
        }
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String columnStr = null;
            TableField tableField = f.getAnnotation(TableField.class);
            if(tableField != null && StringUtils.isNotBlank(tableField.value())){
                columnStr = tableField.value();
            }
            if(columnStr == null){
                columnStr = f.getName();
            }
            if(contains(columnStr)){
                try {
                    f.set(t,getDefaultColumn(columnStr));
                }catch (Exception e){
                    throw new BaseException("SET_DEFAULT_COLUMN_ERROR",e.getMessage(),e);
                }
            }
        }
        return t;
    }
}
