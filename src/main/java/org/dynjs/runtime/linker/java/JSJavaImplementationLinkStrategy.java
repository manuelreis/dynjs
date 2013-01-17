package org.dynjs.runtime.linker.java;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Proxy;

import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.JSObject;
import org.projectodd.linkfusion.StrategicLink;
import org.projectodd.linkfusion.StrategyChain;
import org.projectodd.linkfusion.mop.ContextualLinkStrategy;
import org.projectodd.linkfusion.mop.java.Resolver;
import org.projectodd.linkfusion.mop.java.ResolverManager;

import com.headius.invokebinder.Binder;

public class JSJavaImplementationLinkStrategy extends ContextualLinkStrategy<ExecutionContext> {

    private static JSJavaImplementationManager manager = new JSJavaImplementationManager();

    public JSJavaImplementationLinkStrategy() {
        super(ExecutionContext.class);
    }

    @Override
    public StrategicLink linkConstruct(StrategyChain chain, Object receiver, Object[] args, Binder binder, Binder guardBinder) throws NoSuchMethodException,
            IllegalAccessException {

        if (!(receiver instanceof Class<?>)) {
            return chain.nextStrategy();
        }

        if (args.length == 1 && args[0] instanceof JSObject) {

            binder = binder.spread(JSObject.class)
                    .convert(Object.class, Class.class, ExecutionContext.class, JSObject.class);

            MethodHandle guard = getConstructGuard((Class<?>) receiver, guardBinder);

            return new StrategicLink(makeImplementation(binder), guard);
        }

        return chain.nextStrategy();

    }

    private MethodHandle makeImplementation(Binder binder) throws NoSuchMethodException, IllegalAccessException {
        return binder.invokeStatic(lookup(), JSJavaImplementationLinkStrategy.class, "makeImplementation");
    }

    public static Object makeImplementation(Class<?> targetClass, ExecutionContext context, JSObject implementation) throws Exception {
        return manager.getImplementationWrapper(targetClass, context, implementation);
    }

    private MethodHandle getConstructGuard(Class<?> targetClass, Binder binder) throws NoSuchMethodException, IllegalAccessException {
        return binder
                .drop(0)
                .insert(2, targetClass)
                .invokeStatic(lookup(), JSJavaImplementationLinkStrategy.class, "constructGuard");
    }

    public static boolean constructGuard(Object targetClass, Object[] args, Class<?> expectedTargetClass) {
        if (targetClass != expectedTargetClass) {
            return false;
        }

        if (args.length != 1) {
            return false;
        }

        if (!(args[0] instanceof JSObject)) {
            return false;
        }
        return true;
    }

}