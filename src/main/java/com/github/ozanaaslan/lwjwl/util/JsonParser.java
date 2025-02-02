package com.github.ozanaaslan.lwjwl.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.beans.Transient;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class JsonParser {

    /**
     * Converts an object to its JSON string representation, wrapped with the class name as the parent.
     *
     * @param object the object to convert
     * @return a JSON string
     */
    public static String toJson(Object object) {
        // Serialize the object to JSON
        Object jsonValue = serializeValue(object);

        // Create the root object with the class name as the key
        JSONObject root = new JSONObject();
        String className = object.getClass().getName(); // Use simple class name

        // Place the serialized object inside the root object under the class name key
        root.put(className, jsonValue);

        return root.toString();
    }

    /**
     * Recursively serializes an object into a representation that can be
     * converted to JSON using org.json.
     *
     * @param value the object to serialize
     * @return a JSONObject, JSONArray, or primitive wrapper/String/null
     */
    private static Object serializeValue(Object value) {
        if (value == null) {
            return JSONObject.NULL;
        }
        // If it's a primitive, a String, Boolean, or Number, return it directly.
        if (value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean) {
            return value;
        }
        // Handle arrays
        if (value.getClass().isArray()) {
            JSONArray jsonArray = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                jsonArray.put(serializeValue(element));
            }
            return jsonArray;
        }
        // Handle collections (e.g. List, Set)
        if (value instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            for (Object element : (Collection<?>) value) {
                jsonArray.put(serializeValue(element));
            }
            return jsonArray;
        }
        // For other objects, build a JSONObject using reflection.
        JSONObject jsonObject = new JSONObject();
        Field[] fields = value.getClass().getDeclaredFields();
        for (Field field : fields) {
            // Skip fields annotated with @Transient
            if (field.isAnnotationPresent(Transient.class)) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(value);
                jsonObject.put(field.getName(), serializeValue(fieldValue));
            } catch (IllegalAccessException e) {
                // In production, you may wish to log this error instead.
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * Helper method to extract the first key from the JSONObject, which represents the fully qualified class name.
     * It ensures we get a valid class name.
     *
     * @param jsonObject the JSON object to extract the class name from
     * @return the fully qualified class name
     */
    private static String getFirstKey(JSONObject jsonObject) {
        String firstKey = jsonObject.keys().next();

        // You can perform additional validation if necessary, e.g. ensuring the key is a valid class name
        if (firstKey == null || firstKey.isEmpty()) {
            throw new IllegalArgumentException("Invalid JSON: No top-level class key found.");
        }

        // Return the first key, which is expected to be a fully qualified class name
        return firstKey;
    }

    /**
     * Converts a JSON string into an instance of the class based on the parent key in the JSON.
     * This assumes the top-level key in the JSON is the fully qualified class name of the target class.
     * It automatically determines the class and converts the values inside.
     *
     * @param json the JSON string
     * @return an instance of the object corresponding to the class found in the JSON's top-level key
     */
    public static Object toObject(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            // Get the parent key, which is the fully qualified class name
            String parentClassName = getFirstKey(jsonObject);

            // Dynamically load the class using its name
            Class<?> clazz = Class.forName(parentClassName);
            // Create an instance of the class
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // Extract the nested object inside the class name key
            JSONObject classFields = jsonObject.getJSONObject(parentClassName);

            // Populate fields of the instance
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // Skip fields annotated with @Transient
                if (field.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                field.setAccessible(true);
                if (!classFields.has(field.getName())) {
                    continue;
                }
                Object jsonValue = classFields.get(field.getName());
                // Convert jsonValue to the appropriate type and set the field value
                Object value = convertJsonValue(jsonValue, field.getType());
                field.set(instance, value);
            }
            return instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            // Handle errors as necessary
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a JSON value (from org.json) into a value of the desired type.
     * This method supports basic types: String, int/Integer, boolean/Boolean, double/Double.
     * You can expand it for other types as needed.
     *
     * @param jsonValue the value from a JSONObject
     * @param targetType the type to convert to
     * @return the converted value
     */
    private static Object convertJsonValue(Object jsonValue, Class<?> targetType) {
        if (JSONObject.NULL.equals(jsonValue)) {
            return null;
        }
        if (targetType == String.class) {
            return jsonValue.toString();
        }
        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) jsonValue).intValue();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return jsonValue instanceof Boolean ? jsonValue : Boolean.parseBoolean(jsonValue.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) jsonValue).doubleValue();
        }
        // For arrays, collections, and nested objects you would need to recursively convert.
        // For simplicity, if not a recognized type, return the string representation.
        return jsonValue.toString();
    }
}
