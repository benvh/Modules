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
import java.util.*;


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
        Map<Class<?>, List<ModuleDependency>> unresolvedModules = new IdentityHashMap<Class<?>, List<ModuleDependency>>();

        for (Class<?> clazz : annotatedClasses) {
            List<Field> fields = ClassScanner.getFieldsAnnotatedWith(clazz, Find.class);
            boolean hasUnresolvedDependencies = false;

            for (Field field : fields) {
                Find findAnnotation = field.getAnnotation(Find.class);

                if (!registeredModules.containsKey(findAnnotation.value())) {
                    hasUnresolvedDependencies = true;

                    List<ModuleDependency> dependencies = new ArrayList<ModuleDependency>();
                    for (Field fieldInception : fields) {
                        dependencies.add(new ModuleDependency(fieldInception));
                    }
                    unresolvedModules.put(clazz, dependencies);
                    break;
                }
            }

            if (!hasUnresolvedDependencies) {
                createAndRegisterModuleContainer(clazz, fields);
            }
        }

        while (!unresolvedModules.isEmpty()) {
            for (Map.Entry<Class<?>, List<ModuleDependency>> entry : unresolvedModules.entrySet()) {
                Class<?> clazz = entry.getKey();
                List<ModuleDependency> dependencies = entry.getValue();

                //might be registered by now...
                if (!registeredModules.containsKey(clazz.getAnnotation(Module.class).value())) {

                    boolean stillHasUnresolvedDependencies = false;

                    for (ModuleDependency dependency : dependencies) {
                        if (!registeredModules.containsKey(dependency.getRequiredModuleName())) {
                            if (!unresolvedModules.containsKey(dependency.getField().getType())) {
                                throw new UnresolvedModuleDependencyException(clazz, dependency.getRequiredModuleName());
                            }
                            stillHasUnresolvedDependencies = true;
                        }
                    }

                    if (!stillHasUnresolvedDependencies) {
                        List<Field> fields = new ArrayList<Field>();
                        //Todo: Probably should remove the useless ModuleDependency class...
                        for (ModuleDependency moduleDependency : dependencies) {
                            fields.add(moduleDependency.getField());
                        }
                        createAndRegisterModuleContainer(clazz, fields);
                        unresolvedModules.remove(clazz);
                    }
                }
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

    private void createAndRegisterModuleContainer(Class<?> clazz, List<Field> fields) {
        try {
            Object module = clazz.newInstance();
            Module moduleAnnotation = clazz.getAnnotation(Module.class);
            ModuleContainer container = createModuleContainer(moduleAnnotation.value(), module);

            for (Field field : fields) {
                field.setAccessible(true);
                field.set(module, registeredModules.get(field.getAnnotation(Find.class).value()).getModule());
                field.setAccessible(false);
            }

            register(container);

            if (container.doesImplement(ModuleListener.class)) {
                registerModuleListener((ModuleListener) container.getModule());
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void notifyModuleListeners(ModuleContainer moduleContainer) {
        for(ModuleListener moduleListener : moduleListeners) {
            moduleListener.moduleRegistered(moduleContainer);
        }
    }

}
