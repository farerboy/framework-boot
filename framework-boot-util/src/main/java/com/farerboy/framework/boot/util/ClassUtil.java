package com.farerboy.framework.boot.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    private static final Map<String, Class<?>> primitiveClassMap;

    static {
        primitiveClassMap = new HashMap<>();
        primitiveClassMap.put("byte", int.class);
        primitiveClassMap.put("short", short.class);
        primitiveClassMap.put("int", int.class);
        primitiveClassMap.put("long", long.class);
        primitiveClassMap.put("char", char.class);
        primitiveClassMap.put("float", float.class);
        primitiveClassMap.put("double", double.class);
        primitiveClassMap.put("boolean", boolean.class);
        primitiveClassMap.put("void", void.class);
    }

    public static Class<?> forName(String className) {
        Class<?> clazz = primitiveClassMap.get(className);
        if (clazz != null) {
            return clazz;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class.forName(className) exception. className=" + className, e);
        }
    }

    public static <T> T newInstance(String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (Throwable e) {
            throw new RuntimeException("Class.forName(className) exception. className=" + className, e);
        }
        return (T)newInstance(clazz);
    }

    public static <T> T newInstance(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("clazz.newInstance() exception. clazz=" + clazz.getName(), e);
        }

        return instance;
    }

    public static Method getMethod(String destClass, String methodName, String[] argClassNames) {
        Class<?> clazz = ClassUtil.forName(destClass);

        Class<?>[] argClasses;
        if (argClassNames == null) {
            argClasses = null;
        } else if (argClassNames.length == 0) {
            argClasses = new Class<?>[] {};
        } else {
            argClasses = Arrays.stream(argClassNames).map(ClassUtil::forName).toArray(Class<?>[]::new);
        }

        Method method;
        try {
            method = clazz.getMethod(methodName, argClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("clazz.getMethod(methodName, argClasses) exception. clazz=" + destClass + ",methodName=" + methodName + ",argClasses=" + JSON.toJSONString(argClasses), e);
        }
        return method;
    }

//    public static String[] getArgNames(Method method) {
//        return getArgNames(method, false);
//    }
//
//    public static String[] getBeautifulArgNames(Method method) {
//        return getArgNames(method, true);
//    }

//    private static DefaultParameterNameDiscoverer defaultParameterNameDiscoverer;

    /**
     * @param defaultClassName 如果为true，当获取不到真实参数名时返回类型名（首字母小写），返回如：[ "arg0","arg1"]
     */
//    private static String[] getArgNames(Method method, boolean defaultClassName) {
//        if (defaultParameterNameDiscoverer == null) {
//            synchronized (ClassUtils.class) {
//                if (defaultParameterNameDiscoverer == null) {
//                    defaultParameterNameDiscoverer = new DefaultParameterNameDiscoverer();
//                }
//            }
//        }
//
//        String[] argNames = defaultParameterNameDiscoverer.getParameterNames(method);
//        if (argNames == null) {
//            if (defaultClassName) {
//                argNames = Arrays.stream(method.getParameterTypes())
//                        .map(Class::getSimpleName)
//                        .map(StringUtils::uncapitalize)
//                        .toArray(String[]::new);
//            } else {
//                argNames = Arrays.stream(method.getParameters())
//                        .map(Parameter::getName)
//                        .toArray(String[]::new);
//            }
//        }
//        return argNames;
//    }

    /**
     * 通过包名获取包内所有类
     */
    public static List<Class<?>> getAllClassByPackageName(Package pkg) {
        String packageName = pkg.getName();
        // 获取当前包下以及子包下所以的类
        List<Class<?>> returnClassList = getClasses(packageName);
        return returnClassList;
    }

    /**
     * 通过接口名取得某个接口下所有实现这个接口的类
     */
    public static List<Class<?>> getAllClassByInterface(Class<?> c) {
        List<Class<?>> returnClassList = null;

        if (c.isInterface()) {
            // 获取当前的包名
            String packageName = c.getPackage().getName();
            // 获取当前包下以及子包下所以的类
            List<Class<?>> allClass = getClasses(packageName);
            if (allClass != null) {
                returnClassList = new ArrayList<Class<?>>();
                for (Class<?> cls : allClass) {
                    // 判断是否是同一个接口
                    if (c.isAssignableFrom(cls)) {
                        // 本身不加入进去
                        if (!c.equals(cls)) {
                            returnClassList.add(cls);
                        }
                    }
                }
            }
        }

        return returnClassList;
    }

    /**
     * 取得某一类所在包的所有类名 不含迭代
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        // 将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            String[] allClassName = packeageDir.list();
            return allClassName;
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     */
    private static List<Class<?>> getClasses(String packageName) {

        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}