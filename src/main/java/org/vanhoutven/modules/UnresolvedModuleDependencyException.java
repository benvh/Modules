/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;

public class UnresolvedModuleDependencyException extends Exception {

    public UnresolvedModuleDependencyException(Class<?> clazz, String name) {
        super( (new StringBuilder(clazz.getName()).append(" depends on ").append("`").append(name).append("` but no module is registered with that value")).toString() );
    }

    public UnresolvedModuleDependencyException(String s) {
        super(s);
    }
}
