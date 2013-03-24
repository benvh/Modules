/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;

import java.util.List;


public class ModuleContainer {

    private String name;
    private Class<?> clazz;
    private List<Class<?>> ifaces;
    private Object module;

    public ModuleContainer(String name, Class<?> clazz, List<Class<?>> ifaces, Object module) {
        this.name = name;
        this.clazz = clazz;
        this.ifaces = ifaces;
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<Class<?>> getIfaces() {
        return ifaces;
    }

    public Object getModule() {
        return module;
    }

    public boolean doesImplement(Class<?> iface) {
        return ifaces.contains(iface);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleContainer that = (ModuleContainer) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
