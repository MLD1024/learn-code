package com.example.mq.serialize;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.Feature;

public class SerializationUtil {
    public static byte[] serialize(Object object) {
        try {
            return JSON.toJSONBytes(object);
        } catch (JSONException e) {
            throw new SerializationException("Serialization failed", e);
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            return JSON.parseObject(bytes, clazz, Feature.SupportClassForName);
        } catch (JSONException e) {
            throw new SerializationException("Deserialization failed", e);
        }
    }
}