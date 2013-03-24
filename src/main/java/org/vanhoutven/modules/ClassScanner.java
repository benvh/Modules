/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {

    public static <T extends Annotation> List<Field> getFieldsAnnotatedWith(Class<?> clazz, Class<T> annotationClazz ) {
        List<Field> annotatedFields = new ArrayList<Field>();

        if(clazz != null && annotationClazz != null) {
            Field[] fields = clazz.getDeclaredFields();

            for(Field field : fields) {
                if(field.getAnnotation(annotationClazz) != null) {
                    annotatedFields.add(field);
                }
            }
        }

        return annotatedFields;
    }

}
