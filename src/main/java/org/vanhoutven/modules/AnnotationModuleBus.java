/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;


/**
 * Default implementation for {@link ModuleBus}.
 *
 * When scan is called the <b>AnnotationModuleBus</b> will scan the specified part of the classpath for classes annotated with the @Module annotation.
 * All fields annotated with the <b>@Find</b> annotation within these classes will be injected with the correct modules (if they are found!)
 */
public class AnnotationModuleBus implements ModuleBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationModuleBus.class);

    Map<String, ModuleContainer> registeredModules = new HashMap<String, ModuleContainer>();
    List<ModuleListener> moduleListeners = new ArrayList<ModuleListener>();

    @Override
    public void scan() throws UnresolvedModuleDependencyException {
        scan("");
    }

    @Override
    public void scan(String basePackage) throws UnresolvedModuleDependencyException {
        List<Class> annotatedClasses = ClasspathScanner.findClassesWithAnnotation(basePackage, Module.class);

        Map<ModuleContainer, List<ModuleDependency>> unresolvedModuleDependencies = new HashMap<ModuleContainer, List<ModuleDependency>>();

        for (Class<?> clazz : annotatedClasses) {
            try {
                Module moduleAnnotation = clazz.getAnnotation(Module.class);
                if(moduleAnnotation != null) {
                    Object module = clazz.newInstance();
                    ModuleContainer moduleContainer = createModuleContainer(moduleAnnotation.value(), module);

                    boolean hasUnresolvedDependencies = false;

                    /*
                     * Find all module dependencies and try to resolve them. If this fails we'll try again later when all modules are loaded
                     */
                    List<Field> fields = ClassScanner.getFieldsAnnotatedWith(module.getClass(), Find.class);
                    for(Field field : fields) {
                        Find findAnnotation = field.getAnnotation(Find.class);
                        try {
                            if(registeredModules.containsKey(findAnnotation.value())) {
                                field.setAccessible(true);
                                field.set( module, registeredModules.get(findAnnotation.value()).getModule() );
                                field.setAccessible(false);
                            } else {
                                if(!unresolvedModuleDependencies.containsKey(moduleContainer)) {
                                    unresolvedModuleDependencies.put( moduleContainer, new ArrayList<ModuleDependency>() );
                                }
                                unresolvedModuleDependencies.get(moduleContainer).add( new ModuleDependency(field) );
                                hasUnresolvedDependencies = true;
                            }
                        } catch (IllegalAccessException e) {
                            LOGGER.error("Failed to resolve dependency!", e);
                        }
                    }



                    if(!hasUnresolvedDependencies) {
                        if(moduleContainer.doesImplement( ModuleListener.class ) ) {
                            LOGGER.debug("Registered new ModuleListener `" + moduleAnnotation.value() + "`");
                            registerModuleListener( (ModuleListener)module ); //ModuleListeners aren't really registered as actual modules *snicker
                        } else {
                            register(moduleContainer);
                        }
                    }
                }
            } catch (InstantiationException e) {
                LOGGER.error("Failed to resolve dependency!", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Failed to resolve dependency!", e);
            }
        }

        /*
         * Now that all modules are loaded all the outstanding module dependencies  resolved...
         */
        for(Map.Entry<ModuleContainer, List<ModuleDependency>> entry : unresolvedModuleDependencies.entrySet()) {

            ModuleContainer container = entry.getKey();
            List<ModuleDependency> dependencies = entry.getValue();

            Iterator<ModuleDependency> it = dependencies.iterator();
            LOGGER.debug("Resolving dependencies for module `" + container.getName() + "`");
            while(it.hasNext()) {
                ModuleDependency dependency = it.next();
                if(!registeredModules.containsKey(dependency.getRequiredModuleName())) {
                    throw new UnresolvedModuleDependencyException(container.getModule().getClass(), dependency.getRequiredModuleName());
                } else {
                    try {
                        dependency.getField().set(container.getModule(), registeredModules.get( dependency.getRequiredModuleName() ).getModule() );
                        LOGGER.debug("Dependency `" + dependency.getField().getName() + "` for module `" + container.getName() + "` resolved");
                        it.remove();
                    } catch (IllegalAccessException e) {
                        LOGGER.error("Failed to resolve dependency!", e);
                    }
                }
            }

            if(!dependencies.isEmpty()) {
                //wot? we should have thrown an UnresolvedDependencyException by now...
                throw new UnresolvedModuleDependencyException("Tried to divide by 0... :(");
            } else {
                register(container);
            }
        }
    }

    @Override
    public void register(String name, Object module) {
        if(module != null) {
            if(registeredModules.containsKey(name)) {
                LOGGER.warn("Overwriting module `" + name + "`");
            }
            LOGGER.debug("Registered new module `" + name + "`");
            ModuleContainer moduleContainer = createModuleContainer(name, module);
            registeredModules.put(name, moduleContainer);
            notifyModuleListeners(moduleContainer);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object find(String name) throws NoSuchModuleException{
        if(!registeredModules.containsKey(name)) {
            throw new NoSuchModuleException(name);
        }
        return registeredModules.get(name).getModule();
    }




    /*
     * Some extra private utility methods...
     */
    private void register(ModuleContainer moduleContainer) {
        if(registeredModules.containsKey( moduleContainer.getName())) {
           LOGGER.warn("Overwriting module `" + moduleContainer.getName() + "`");
        }
        LOGGER.debug("Registered new module `" + moduleContainer.getName() + "`");
        registeredModules.put(moduleContainer.getName(), moduleContainer);
        notifyModuleListeners(moduleContainer);
    }

    private void registerModuleListener(ModuleListener listener) {
        moduleListeners.add(listener);
        for(ModuleContainer moduleContainer : registeredModules.values()) {
            listener.moduleRegistered(moduleContainer);
        }
    }

    private ModuleContainer createModuleContainer(String name, Object module) {
        Class<?> clazz = module.getClass();
        return new ModuleContainer(name, clazz, Arrays.asList(clazz.getInterfaces()), module);
    }

    private void notifyModuleListeners(ModuleContainer moduleContainer) {
        for(ModuleListener moduleListener : moduleListeners) {
            moduleListener.moduleRegistered(moduleContainer);
        }
    }

}
