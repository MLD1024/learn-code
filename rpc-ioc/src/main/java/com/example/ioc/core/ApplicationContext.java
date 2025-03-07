package com.example.ioc.core;

import com.example.ioc.annotation.Autowired;
import com.example.ioc.annotation.Component;
import com.example.ioc.annotation.Qualifier;
import com.example.ioc.annotation.Service;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 要实现类似Spring的IoC框架，需要重点实现以下核心机制：
 * 1. 基于注解或XML的Bean定义解析
 * 2. Bean容器管理(单例/原型作用域)
 * 3. 依赖注入(构造器注入/字段注入)
 * 4. Bean生命周期管理。建议先定义BeanDefinition元数据结构，使用ConcurrentHashMap实现Bean容器，
 * 通过反射完成依赖注入，最后添加配置解析器支持注解和XML两种配置方式。
 *
 * 首先创建核心注解类型，为后续的Bean定义和依赖注入提供元数据支持。这将包含@Component、@Autowired等基础注解的定义。
 *
 * rpc-ioc\src\main\java\com\example\ioc\annotation\Component.java
 *
 * 接下来创建依赖注入相关注解，包括@Autowired和@Qualifier注解，这是实现自动装配的关键元数据。
 * rpc-ioc\src\main\java\com\example\ioc\annotation\Autowired.java
 *
 *
 * 现在需要创建@Qualifier注解来实现按名称注入的功能，这是处理多个同类型Bean注入的关键机制。
 *
 * rpc-ioc\src\main\java\com\example\ioc\annotation\Qualifier.java
 *
 *
 * 现在需要实现IoC容器的核心逻辑，创建ApplicationContext类来处理Bean的创建、依赖注入和生命周期管理。
 * 这将包含包扫描、Bean实例化、依赖注入等核心功能。
 *
 * rpc-ioc\src\main\java\com\example\ioc\core\ApplicationContext.java
 *
 * 完善BeanDefinition类的getter/setter方法，这是正确管理Bean元数据的关键。
 * 同时需要补充BeanScope枚举的完整实现，确保作用域机制能够正常运作。
 *
 * rpc-ioc\src\main\java\com\example\ioc\core\ApplicationContext.java
 *
 * 1. 注解体系
 * - 创建@Component、@Autowired、@Qualifier注解
 * - 支持类级别组件声明和依赖注入标识
 * 2. 容器实现
 * - ApplicationContext管理Bean生命周期
 * - 支持单例模式Bean缓存(singletonObjects)
 * - 实现包扫描机制自动注册Bean定义(beanDefinitions)
 * 3. 依赖注入
 * - 完成字段注入功能
 * - 支持按类型和限定符名称注入
 * - 处理required依赖校验
 * 4. 基础架构
 * - 定义BeanDefinition元数据结构
 * - 实现Bean作用域枚举(BeanScope)
 * - 包含类加载和反射实例化机制
 * 当前实现已具备基础IoC容器功能，后续可扩展支持构造器注入、原型作用域、AOP等高级特性。
 */
public class ApplicationContext {
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private final String basePackage;

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
        initialize();
    }

    private void initialize() {
        scanPackage(basePackage);
        instantiateBeans();
        injectDependencies();
    }

    private void scanPackage(String packageName) {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    File directory = new File(resource.getFile());
                    processDirectory(directory, packageName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                registerBeanDefinition(className);
            }
        }
    }

    private void registerBeanDefinition(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Component component = clazz.getAnnotation(Component.class);
            if (component != null) {
                BeanDefinition definition = new BeanDefinition();
                definition.setBeanClass(clazz);
                definition.setScope(BeanScope.SINGLETON); // 默认单例
                String beanName = component.value().isEmpty() ? clazz.getSimpleName() : component.value();
                beanDefinitions.put(beanName, definition);
            }
            Service dubboService = clazz.getAnnotation(Service.class);
            if (Objects.nonNull(dubboService)){
                BeanDefinition definition = new BeanDefinition();
                definition.setBeanClass(clazz);
                definition.setScope(BeanScope.SINGLETON); // 默认单例
                String beanName = component.value().isEmpty() ? clazz.getSimpleName() : component.value();
                beanDefinitions.put(beanName, definition);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void instantiateBeans() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            if (entry.getValue().getScope() == BeanScope.SINGLETON) {
                Object bean = createBean(entry.getKey(), entry.getValue());
                singletonObjects.put(entry.getKey(), bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        try {
            Class<?> clazz = beanDefinition.getBeanClass();
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Bean creation failed for " + beanName, e);
        }
    }

    private void injectDependencies() {
        for (Object bean : singletonObjects.values()) {
            doInjectDependencies(bean);
        }
    }

    private void doInjectDependencies(Object bean) {
        Class<?> clazz = bean.getClass();
        // 字段注入
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                String qualifier = field.isAnnotationPresent(Qualifier.class) 
                    ? field.getAnnotation(Qualifier.class).value() 
                    : "";
                Object dependency = getBean(field.getType(), qualifier);
                if (dependency == null && autowired.required()) {
                    throw new RuntimeException("Unsatisfied dependency: " + field.getType().getName());
                }
                try {
                    field.setAccessible(true);
                    field.set(bean, dependency);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object getBean(Class<?> requiredType, String qualifier) {
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            if (requiredType.isAssignableFrom(entry.getValue().getClass())) {
                if (qualifier.isEmpty() || entry.getKey().equals(qualifier)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public Object getBean(String name) {
        return singletonObjects.get(name);
    }

    enum BeanScope {
        SINGLETON, PROTOTYPE
    }

    static class BeanDefinition {
        private Class<?> beanClass;
        private BeanScope scope;

        public Class<?> getBeanClass() {
            return beanClass;
        }

        public void setBeanClass(Class<?> beanClass) {
            this.beanClass = beanClass;
        }

        public BeanScope getScope() {
            return scope;
        }

        public void setScope(BeanScope scope) {
            this.scope = scope;
        }
    }
}