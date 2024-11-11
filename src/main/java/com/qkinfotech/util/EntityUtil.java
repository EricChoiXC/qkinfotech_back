package com.qkinfotech.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity工具类
 * @author 蔡咏钦
 */
public class EntityUtil {

    /**
     * 获取一个类的所有基本属性（非静态、非 final 的字段）
     * @param clazz 要获取属性的类
     * @return List<Field> 包含所有基本属性的列表
     */
    public List<Field> getAllBaseFields(Class<?> clazz) {
        List<Field> basicFields = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            // 排除静态字段和 final 字段
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                basicFields.add(field);
            }
        }
        return basicFields;
    }
}
