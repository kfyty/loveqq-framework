package com.kfyty.core.generic;

import com.kfyty.core.exception.ResolvableException;
import com.kfyty.core.reflect.GenericArrayTypeImpl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.kfyty.core.utils.ReflectUtil.getActualGenericType;

/**
 * 描述: 实际的泛型，用于推断父类泛型属性
 *
 * @author kfyty725
 * @date 2021/6/24 17:34
 * @email kfyty725@hotmail.com
 */
public class ActualGeneric extends SimpleGeneric {
    private Class<?> actualDeclaringClass;

    public ActualGeneric(Class<?> sourceType) {
        super(sourceType);
    }

    public ActualGeneric(Type generic) {
        super(generic);
    }

    public ActualGeneric(Class<?> sourceType, Type generic) {
        super(sourceType, generic);
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType) {
        return new ActualGeneric(sourceType);
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType, Type resolveType) {
        return new ActualGeneric(sourceType, resolveType);
    }

    protected void processActualGeneric() {
        if (this.resolveType instanceof TypeVariable) {
            Class<?> actualFieldType = getActualGenericType(this.getFirst().getTypeVariable(), this.actualDeclaringClass);
            this.sourceType = actualFieldType;
            this.resolveType = actualFieldType;
            this.genericInfo.clear();
            return;
        }
        Map<Generic, QualifierGeneric> genericMap = new LinkedHashMap<>(4);
        for (Generic generic : this.genericInfo.keySet()) {
            if (!generic.isTypeVariable()) {
                genericMap.put(generic, null);
            } else {
                Class<?> actualFieldType = getActualGenericType(generic.getTypeVariable(), this.actualDeclaringClass);
                genericMap.put(new Generic(actualFieldType, generic.isArray()), null);
            }
        }
        this.genericInfo.clear();
        this.genericInfo.putAll(genericMap);
        if (this.resolveType instanceof GenericArrayType) {
            this.sourceType = Array.newInstance(this.getFirst().get(), 0).getClass();
            this.resolveType = GenericArrayTypeImpl.make(this.getFirst().get());
        }
    }

    public static ActualGeneric from(Class<?> clazz) {
        return (ActualGeneric) new ActualGeneric(clazz).doResolve();
    }

    public static ActualGeneric from(Field field) {
        return (ActualGeneric) new ActualGeneric(field.getType(), field.getGenericType()).doResolve();
    }

    public static ActualGeneric from(Method method) {
        return (ActualGeneric) new ActualGeneric(method.getReturnType(), method.getGenericReturnType()).doResolve();
    }

    public static ActualGeneric from(Parameter parameter) {
        return (ActualGeneric) new ActualGeneric(parameter.getType(), parameter.getParameterizedType()).doResolve();
    }

    public static ActualGeneric from(Class<?> clazz, Field field) {
        return from(clazz, field.getType(), field.getGenericType());
    }

    public static ActualGeneric from(Class<?> clazz, Parameter parameter) {
        return from(clazz, parameter.getType(), parameter.getParameterizedType());
    }

    public static ActualGeneric from(Field sourceField, Field field) {
        Type genericType = sourceField.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            throw new ResolvableException("unable to get the source field generic type !");
        }
        ActualGeneric source = ActualGeneric.from(sourceField);
        String typeName = field.getGenericType().getTypeName();
        TypeVariable<?>[] typeParameters = ((Class<?>) ((ParameterizedType) genericType).getRawType()).getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if (typeName.equals(typeParameters[i].getName())) {
                QualifierGeneric generic = new ArrayList<>(source.getGenericInfo().values()).get(i);
                if (generic != null && generic.hasGeneric()) {
                    return (ActualGeneric) generic;
                }
            }
        }
        return from(sourceField.getGenericType(), field);
    }

    public static ActualGeneric from(Type sourceGenericType, Field field) {
        ActualGeneric actualGeneric = from(field);
        actualGeneric.resolveType = getActualGenericType(actualGeneric.getResolveType().getTypeName(), sourceGenericType);
        actualGeneric.genericInfo.clear();
        return actualGeneric;
    }

    public static ActualGeneric from(Class<?> clazz, Class<?> type, Type genericType) {
        ActualGeneric actualGeneric = new ActualGeneric(type, genericType);
        actualGeneric.actualDeclaringClass = clazz;
        actualGeneric.doResolve();
        if (actualGeneric.resolveType instanceof TypeVariable || actualGeneric.getGenericInfo().keySet().stream().anyMatch(Generic::isTypeVariable)) {
            actualGeneric.processActualGeneric();
        }
        return actualGeneric;
    }
}
