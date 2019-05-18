package jnc.foreign.internal;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class ParameterHandlers {

    private static final Map<Class<?>, ParameterHandler<?>> PRIMARY_HANDLERS;

    static {
        Map<Class<?>, ParameterHandler<?>> handlers = new HashMap<>(16);
        put(handlers, void.class, (context, index, obj) -> context.putLong(index, 0));
        put(handlers, boolean.class, CallContext::putBoolean);
        put(handlers, byte.class, CallContext::putByte);
        put(handlers, char.class, CallContext::putChar);
        put(handlers, short.class, CallContext::putShort);
        put(handlers, int.class, CallContext::putInt);
        put(handlers, long.class, CallContext::putLong);
        put(handlers, float.class, CallContext::putFloat);
        put(handlers, double.class, CallContext::putDouble);
        PRIMARY_HANDLERS = handlers;
    }

    @SuppressWarnings("unchecked")
    static <T> ParameterHandler<T> forHandler(Class<T> type) {
        // should be safe without check generic type
        return (ParameterHandler<T>) PRIMARY_HANDLERS.get(Primitives.unwrap(type));
    }

    private static <T> void put(Map<Class<?>, ParameterHandler<?>> map, Class<T> type, ParameterHandler<T> handler) {
        map.put(type, handler);
    }

}
