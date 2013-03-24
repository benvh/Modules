/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <ben.vanhoutven@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Van Houtven Ben
 * ----------------------------------------------------------------------------
 */

package org.vanhoutven.modules;

import org.junit.Test;

public class ModuleBusTest {

    @Test
    public void testModuleBus() {
        ModuleBus bus = new AnnotationModuleBus();
        try {
            bus.scan();

            Foo f = (Foo)bus.find("org.vanhoutven.modules.test.Foo");
            f.doBar();

        } catch (UnresolvedModuleDependencyException e) {
            e.printStackTrace();
            System.out.println(":(");
        } catch (NoSuchModuleException e) {
            e.printStackTrace();
            System.out.println(";(");
        }
    }

}

@Module("org.vanhoutven.modules.test.Foo")
class Foo {

    @Find("org.vanhoutven.modules.test.Bar")
    Bar bar;

    public void doBar() {
        bar.doBar();
    }
}

@Module("org.vanhoutven.modules.test.Bar")
class Bar {
    public void doBar() {
        System.out.println("bar did bar");
    }
}

@Module("org.vanhoutven.modules.test.FooBar")
class FooBar implements ModuleListener {

    @Override
    public void moduleRegistered(ModuleContainer moduleContainer) {
        System.out.println("module registered! " + moduleContainer.getName());
    }
}
