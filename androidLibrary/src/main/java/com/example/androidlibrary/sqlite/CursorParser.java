package com.example.androidlibrary.sqlite;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.example.androidlibrary.sqlite.annotations.Deserializer;
import com.example.androidlibrary.sqlite.annotations.FieldProperty;
import com.example.androidlibrary.sqlite.annotations.RequireConversion;
import com.example.androidlibrary.sqlite.annotations.Serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CursorParser {
    public static <T> T toObject(@NonNull Cursor cursor, int position, @NonNull Class<T> modelClass){
        cursor.moveToPosition(position);
        T model = createInstance(modelClass);
        setFields(model, modelClass, cursor);
        return model;
    }

    private static <T> T createInstance(@NonNull Class<T> modelClass){
        Constructor<T> constructor;
        try {
            constructor = modelClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.toString());
        }
        T model;
        try {
            model = constructor.newInstance();
        } catch (Exception e){
            throw new RuntimeException(e.toString());
        }

        return model;
    }

    private static <T> void setFields(@NonNull T model, @NonNull Class<T> modelClass, @NonNull Cursor cursor){
        for(Field field : modelClass.getDeclaredFields()){
            FieldProperty fieldProperty = field.getAnnotation(FieldProperty.class);
            if((fieldProperty != null && fieldProperty.ignore()) || Modifier.isFinal(field.getModifiers())) continue;

            String columnName = field.getName();
            columnName = fieldProperty != null ? fieldProperty.name() : columnName;

            Method setter = findSetter(field, modelClass);

            Object value;
            RequireConversion requireConversion = field.getAnnotation(RequireConversion.class);
            if(requireConversion != null){
                Method deserializer = findDeserializer(field, requireConversion.ConverterClass());
                value = getValueFromCursor(deserializer.getParameterTypes()[0], columnName, cursor);
                try {
                    value = deserializer.invoke(null, value);
                } catch (Exception e) {
                    throw new RuntimeException("Occurred while invoking deserializer for "+field.getName()+" field in "+deserializer.getDeclaringClass().getCanonicalName()+" class. Exception: "+e.toString());
                }
            } else{
                value = getValueFromCursor(field.getType(), columnName, cursor);
            }

            try {
                setter.invoke(model, field.getType().cast(value));
            } catch (Exception e) {
                throw new RuntimeException("Occurred while setting value to "+field.getName()+" field in "+field.getDeclaringClass().getCanonicalName()+" class. Exception: "+e.toString());
            }
        }
    }

    @NonNull
    private static Method findSetter(@NonNull Field field, @NonNull Class<?> modelClass){
        String setterName = "set"+Character.toString(field.getName().charAt(0)).toUpperCase()+field.getName().substring(1);
        try {
            return modelClass.getDeclaredMethod(setterName, field.getType());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No setter found for "+field.getName()+" field in "+field.getDeclaringClass().getCanonicalName()+" class.Exception: "+e.toString());
        }
    }

    @NonNull
    private static Method findSerializer(@NonNull Field field, @NonNull Class<?> converterClass){
        for(Method method : converterClass.getDeclaredMethods()){
            Serializer serializer = method.getAnnotation(Serializer.class);
            if(serializer != null && serializer.fieldName().equals(field.getName())){
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes.length == 1) {
                    if (parameterTypes[0].equals(field.getType())) {
                        return method;
                    } else {
                        throw new RuntimeException("Serializer method for "+field.getName()+" field in "+converterClass.getCanonicalName()+" class doesn't have a parameter of "+field.getType().getCanonicalName()+" type");
                    }
                } else {
                    throw new RuntimeException("Serializer method for "+field.getName()+" field in "+converterClass.getCanonicalName()+" class should have only 1 parameter");
                }
            }
        }
        throw new RuntimeException("No serializer found for "+field.getName()+" field in "+field.getDeclaringClass().getCanonicalName()+" class");
    }

    @NonNull
    private static Method findDeserializer(@NonNull Field field, @NonNull Class<?> converterClass){
        for(Method method : converterClass.getDeclaredMethods()){
            Deserializer deserializer = method.getAnnotation(Deserializer.class);
            if(deserializer != null && deserializer.fieldName().equals(field.getName())){
                Class<?> returnType = method.getReturnType();
                if(field.getType().equals(returnType)){
                    return method;
                } else{
                    throw new RuntimeException("Deserializer method for "+field.getName()+" field in "+converterClass.getCanonicalName()+" class doesn't have a "+field.getType().getCanonicalName()+" return type");
                }
            }
        }
        throw new RuntimeException("No deserializer found for "+field.getName()+" field in "+field.getDeclaringClass().getCanonicalName()+" class");
    }

    @NonNull
    private static Object getValueFromCursor(@NonNull Class<?> columnType, @NonNull String columnName, @NonNull Cursor cursor){
        if(cursor.getType(cursor.getColumnIndex(columnName)) == Cursor.FIELD_TYPE_INTEGER){
            if(columnType.equals(Long.class) || columnType.equals(long.class))
                return cursor.getLong(cursor.getColumnIndex(columnName));
            else if(columnType.equals(Integer.class) || columnType.equals(int.class))
                return cursor.getInt(cursor.getColumnIndex(columnName));
            else if(columnType.equals(Boolean.class) || columnType.equals(boolean.class))
                return cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
            else
                throw new RuntimeException("Couldn't get value from cursor for "+columnType+" column, column type doesn't match field/deserializer type");
        }
        else if(cursor.getType(cursor.getColumnIndex(columnName)) == Cursor.FIELD_TYPE_FLOAT){
            if(columnType.equals(Double.class) || columnType.equals(double.class))
                return cursor.getLong(cursor.getColumnIndex(columnName));
            else if(columnType.equals(Float.class) || columnType.equals(float.class))
                return cursor.getInt(cursor.getColumnIndex(columnName));
            else
                throw new RuntimeException("Couldn't get value from cursor for "+columnType+" column, column type doesn't match field/deserializer type");
        }
        else if(cursor.getType(cursor.getColumnIndex(columnName)) == Cursor.FIELD_TYPE_STRING){
            if(columnType.equals(String.class))
                return cursor.getString(cursor.getColumnIndex(columnName));
            else
                throw new RuntimeException("Couldn't get value from cursor for "+columnType+" column, column type doesn't match field/deserializer type");
        }else{
            throw new RuntimeException("BLOB and NULL field types are not allowed yet");
        }
    }


    /*
    NOTE: The Serializer and Deserializer methods should be static and should be annotated with Serializer or Deserializer annotation and field name
    should also be specified.
    */
}
