/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;


public interface ModuleBus {

    /**
     * Scan the complete classpath for <b><Module</b> candidates. Any dependencies on other modules will also be resolved.
     * If any of the dependencies cannot be resolved an {@link UnresolvedModuleDependencyException} will be thrown.
     *
     * @throws UnresolvedModuleDependencyException thrown when a dependency failed to be resolved
     */
    void scan() throws UnresolvedModuleDependencyException;

    /**
     * @see ModuleBus#scan()
     *
     * @param basePackage a specified part of the classpath that needs to be scanned
     * @throws UnresolvedModuleDependencyException
     */
    void scan(String basePackage) throws UnresolvedModuleDependencyException;

    /**
     * Manually register a <b>Module</b>. It's not recommended to register modules manually though!
     *
     * @param name module name (or 'identifier')
     * @param module a module object. Any object should work
     */
    void register(String name, Object module);

    /**
     * Find a specific <b>Module</b> inside the <b>ModuleBus</b>. If the <b>Module</b> can't be found a NoSuchModuleException will be thrown.
     *
     * @param name a module name (or 'identifier')
     * @return the module (if found!)
     * @throws NoSuchModuleException thrown when the specified <b>Module</b> isn't found in the <b>ModuleBus</b>
     */
    Object find(String name) throws NoSuchModuleException;

}
