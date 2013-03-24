/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;

import java.lang.reflect.Field;

public class ModuleDependency {

    private Field field;
    private String requiredModuleName;

    public ModuleDependency(Field field) {
        this.field = field;
    }

    public String getRequiredModuleName() {
        if(requiredModuleName == null) {
            Find findAnnotation = field.getAnnotation(Find.class);
            if(findAnnotation != null) {
                requiredModuleName = findAnnotation.value();
            }
        }
        return requiredModuleName;
    }

    public Field getField() {
        return field;
    }
}
