/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClasspathScanner {

    public static List<Class> findClassesWithAnnotation(String basePackage, Class<? extends Annotation> annotationClass) {
        List<Class> classes = new ArrayList<Class>();
        try {
            String basePath = basePackage;
            if(basePath.equals(".")) {
                basePath = "";
            }
            basePath = basePath.replace('.', '/');

            ClassLoader ctx = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = ctx.getResources(basePath);

            List<File> dirs = new ArrayList<File>();

            while(resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }


            for(File dir : dirs) {
                if(dir.isDirectory()) {
                    classes.addAll( scanDirForClassesWithAnnotation(dir, basePackage.equals(".") ? "" : basePackage, annotationClass) );
                } else {
                    System.out.println("...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //TODO: Log
        }

        return classes;
    }

    public static List<Class> findClassesWithAnnotation(Class<? extends Annotation> annotationClass) {
        return findClassesWithAnnotation(".", annotationClass);
    }

    private static List<Class> scanDirForClassesWithAnnotation(File dir, String packageName, Class<? extends Annotation> annotationClass) {
        List<Class> classes = new ArrayList<Class>();

        File[] files = dir.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                StringBuilder nextPackageName = new StringBuilder(packageName);
                if(nextPackageName.length() > 0) {
                    nextPackageName.append(".");
                }
                nextPackageName.append(file.getName());
                classes.addAll( scanDirForClassesWithAnnotation(file, nextPackageName.toString(), annotationClass) );
            } else {
                if(file.getName().endsWith(".class")) {
                    try {
                        String className = file.getName();
                        className = className.substring(0, className.lastIndexOf(".class"));
                        Class clazz = Class.forName((!packageName.isEmpty() ?  packageName + "." : "") + className);

                        Annotation[] annotations = clazz.getDeclaredAnnotations();
                        if(annotations.length > 0) {
                            for(Annotation annotation : annotations) {
                                if(annotationClass.isInstance(annotation)) {
                                    classes.add(clazz);
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        //TODO: Log
                    }
                }
            }
        }

        return classes;
    }

}
