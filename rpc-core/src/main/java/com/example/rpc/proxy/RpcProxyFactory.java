package com.example.rpc.proxy;

import com.example.rpc.core.RpcClient;
import com.example.rpc.example.HelloServiceImpl;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;

public class RpcProxyFactory {
    public static <T> T createProxy(Class<T> interfaceClass, RpcClient client) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(interfaceClass.getName() + "$Proxy");
        ctClass.addInterface(pool.get(interfaceClass.getName()));


        // 字段类型
        String fieldType = client.getClass().getName();
        // 创建新字段
        CtField ctField = new CtField(pool.get(fieldType), "client", ctClass);
        ctField.setModifiers(Modifier.PRIVATE);
        ctClass.addField(ctField);

        for (Method method : interfaceClass.getMethods()) {
            CtMethod ctMethod = CtNewMethod.make(
                    "public " + method.getReturnType().getName() + " " + method.getName() + "(" + getParamsTypes(method,client) + ")  throws Exception {" +
                            "   com.example.rpc.core.RpcRequest request = new com.example.rpc.core.RpcRequest();" +
                            "   request.setRequestId(\"" + UUID.randomUUID().toString() + "\");" +
                            "   request.setServiceName(\"" + interfaceClass.getName() + "\");" +
                            "   request.setMethodName(\"" + method.getName() + "\");" +
                            "   request.setParameterTypes(new java.lang.Class[]{" + getParamTypesArray(method) + "});" +
                            "   request.setParameters(new Object[]{" + getParamsValues(method) + "});" +
                            "   return (" + method.getReturnType().getName() + ") client.send(request);" +
                            "}", ctClass);
            ctClass.addMethod(ctMethod);
        }

        Class<?> newClass = ctClass.toClass();
        Object newObject = newClass.getDeclaredConstructor().newInstance();
        Field field = newClass.getDeclaredField("client");
        field.setAccessible(true);
        field.set(newObject, client);

        return (T) newObject;
    }

    private static String getParamsTypes(Method method, RpcClient client) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> paramType = method.getParameterTypes()[i];
            if (params.length() > 0) {
                params.append(", ");
            }
            params.append(paramType.getName()).append(" arg").append(i);
        }
        // if (params.length() > 0) {
        //     params.append(", ");
        // }
        // params.append(client.getClass().getName()).append(" client");
        return params.toString();
    }

    private static String getParamTypesArray(Method method) {
        StringBuilder types = new StringBuilder();
        for (Class<?> paramType : method.getParameterTypes()) {
            if (types.length() > 0) types.append(", ");
            types.append(paramType.getName()).append(".class");
        }
        return types.toString();
    }

    private static String getParamsValues(Method method) {
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            if (values.length() > 0) values.append(", ");
            values.append("arg").append(i);
        }
        return values.toString();
    }

    public static String sayHello(String world, RpcClient client) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        try {
            return helloService.sayHello(world,client);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}