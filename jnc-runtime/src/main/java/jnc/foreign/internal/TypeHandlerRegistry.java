package jnc.foreign.internal;

import java.lang.reflect.Array;
import javax.annotation.Nullable;
import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Struct;
import jnc.foreign.byref.ByReference;
import jnc.foreign.spi.ForeignProvider;

final class TypeHandlerRegistry implements TypeHandlerFactory {

    private static void putByReference(CallContext context, int index, ByReference obj) {
        if (obj == null) {
            context.putLong(index, 0);
        } else {
            Foreign foreign = ForeignProvider.getDefault().getForeign();
            Pointer memory = AllocatedMemory.allocate(obj.componentType(foreign).size());
            obj.toNative(foreign, memory);
            context.onFinish(() -> obj.fromNative(foreign, memory)).putLong(index, memory.address());
        }
    }

    private static void putBooleanArray(Pointer memory, int offset, boolean[] array, int off, int len) {
        for (int i = off; i < len; i++) {
            memory.putByte(offset + i, (byte) (array[i] ? 1 : 0));
        }
    }

    private static void getBooleanArray(Pointer memory, int offset, boolean[] array, int off, int len) {
        for (int i = off; i < len; i++) {
            array[i] = memory.getByte(offset + i) != 0;
        }
    }

    @SuppressWarnings("NestedAssignment")
    private static <T> ParameterHandler<T> toParameterHandler(
            ArrayParameterHandler<T> toNative, ArrayParameterHandler<T> fromNative, int typeBytes) {
        return (CallContext context, int index, T array) -> {
            int len;
            if (array == null) {
                context.putLong(index, 0);
            } else if ((len = Array.getLength(array)) == 0) {
                context.putLong(index, EmptyMemoryHolder.NOMEMORY.address());
            } else {
                int offset = 0;
                int off = 0;
                Pointer memory = AllocatedMemory.allocate(len, typeBytes);
                toNative.handle(memory, offset, array, off, len);
                context.onFinish(() -> fromNative.handle(memory, offset, array, off, len)).putLong(index, memory.address());
            }
        };
    }

    private final ConcurrentWeakIdentityHashMap<Class<?>, InvokerHandlerInfo> exactReturnTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, InvokerHandlerInfo> inheritedReturnTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, ParameterHandlerInfo<?>> exactParameterTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, ParameterHandlerInfo<?>> inheritedParameterTypeMap = new ConcurrentWeakIdentityHashMap<>(16);

    TypeHandlerRegistry(TypeFactory typeFactory) {
        InternalType pointerType = typeFactory.findByNativeType(NativeType.POINTER);

        addExactReturnTypeHandler(Pointer.class, PointerHandlerInfo.INSTANCE);
        addInheritedReturnTypeHandler(Enum.class, EnumHandlerInfo.INSTANCE);

        // parameter type should not be void, maybe user want to define a pointer type.
        addExactParameterTypeHandler(Void.class, pointerType, (context, index, __) -> context.putLong(index, 0));
        addPrimaryTypeHandler(void.class, NativeType.VOID, null, typeFactory);
        addPrimaryTypeHandler(boolean.class, NativeType.UINT8, CallContext::putBoolean, typeFactory);
        addPrimaryTypeHandler(byte.class, NativeType.SINT8, CallContext::putByte, typeFactory);
        addPrimaryTypeHandler(char.class, NativeType.UINT16, CallContext::putChar, typeFactory);
        addPrimaryTypeHandler(short.class, NativeType.SINT16, CallContext::putShort, typeFactory);
        addPrimaryTypeHandler(int.class, NativeType.SINT32, CallContext::putInt, typeFactory);
        addPrimaryTypeHandler(long.class, NativeType.SINT64, CallContext::putLong, typeFactory);
        addPrimaryTypeHandler(float.class, NativeType.FLOAT, CallContext::putFloat, typeFactory);
        addPrimaryTypeHandler(double.class, NativeType.DOUBLE, CallContext::putDouble, typeFactory);

        addInheritedParameterTypeHandler(Struct.class, ParameterHandlerInfo.always(pointerType, (context, index, obj) -> context.putLong(index, obj == null ? 0 : obj.getMemory().address())));
        addInheritedParameterTypeHandler(Pointer.class, ParameterHandlerInfo.always(pointerType, (context, index, obj) -> context.putLong(index, obj == null ? 0 : obj.address())));
        addInheritedParameterTypeHandler(ByReference.class, ParameterHandlerInfo.always(pointerType, TypeHandlerRegistry::putByReference));

        addPrimitiveArrayParameterTypeHandler(pointerType, byte[].class, Pointer::putBytes, Pointer::getBytes, Byte.BYTES);
        addPrimitiveArrayParameterTypeHandler(pointerType, char[].class, Pointer::putCharArray, Pointer::getCharArray, Character.BYTES);
        addPrimitiveArrayParameterTypeHandler(pointerType, short[].class, Pointer::putShortArray, Pointer::getShortArray, Short.BYTES);
        addPrimitiveArrayParameterTypeHandler(pointerType, int[].class, Pointer::putIntArray, Pointer::getIntArray, Integer.BYTES);
        addPrimitiveArrayParameterTypeHandler(pointerType, long[].class, Pointer::putLongArray, Pointer::getLongArray, Long.BYTES);
        addPrimitiveArrayParameterTypeHandler(pointerType, float[].class, Pointer::putFloatArray, Pointer::getFloatArray, Float.BYTES);
        addPrimitiveArrayParameterTypeHandler(pointerType, double[].class, Pointer::putDoubleArray, Pointer::getDoubleArray, Double.BYTES);
        addPrimitiveArrayParameterTypeHandler(pointerType, boolean[].class, TypeHandlerRegistry::putBooleanArray, TypeHandlerRegistry::getBooleanArray, Byte.BYTES);
    }

    private <T> void addPrimitiveArrayParameterTypeHandler(
            InternalType pointerType, Class<T> primitiveArrayType,
            ArrayParameterHandler<T> toNative, ArrayParameterHandler<T> fromNative, int typeBytes) {
        ParameterHandler<T> parameterHandler = toParameterHandler(toNative, fromNative, typeBytes);
        addExactParameterTypeHandler(primitiveArrayType, ParameterHandlerInfo.always(pointerType, parameterHandler));
    }

    private <T> void addPrimaryTypeHandler(
            Class<T> primitiveType, NativeType nativeType,
            ParameterHandler<T> parameterHandler, TypeFactory typeFactory) {
        Class<T> wrapType = Primitives.wrap(primitiveType);
        InternalType defaultType = typeFactory.findByNativeType(nativeType);
        InvokerHandlerInfo prthi = PrimitiveReturnTypeHandlerInfo.of(primitiveType, defaultType);

        addExactReturnTypeHandler(primitiveType, prthi);
        addExactReturnTypeHandler(wrapType, prthi);
        if (parameterHandler != null) {
            addExactParameterTypeHandler(primitiveType, defaultType, parameterHandler);
            addExactParameterTypeHandler(wrapType, defaultType, parameterHandler);
        }
    }

    private <T> void addExactReturnTypeHandler(Class<T> returnType, InvokerHandlerInfo info) {
        exactReturnTypeMap.putIfAbsent(returnType, info);
    }

    @SuppressWarnings("SameParameterValue")
    private <T> void addInheritedReturnTypeHandler(Class<T> returnType, InvokerHandlerInfo info) {
        inheritedReturnTypeMap.putIfAbsent(returnType, info);
    }

    private <T> void addExactParameterTypeHandler(Class<T> parameterType, InternalType defaultType, ParameterHandler<T> parameterHandler) {
        addExactParameterTypeHandler(parameterType, ParameterHandlerInfo.typedefFirst(defaultType, parameterHandler));
    }

    private <T> void addExactParameterTypeHandler(Class<T> parameterType, ParameterHandlerInfo<T> info) {
        exactParameterTypeMap.putIfAbsent(parameterType, info);
    }

    private <T> void addInheritedParameterTypeHandler(Class<T> parameterType, ParameterHandlerInfo<T> info) {
        inheritedParameterTypeMap.putIfAbsent(parameterType, info);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> T extractFromMap(ConcurrentWeakIdentityHashMap<Class<?>, ?> exact,
            ConcurrentWeakIdentityHashMap<Class<?>, ?> inherited, Class<?> type) {
        {
            T result = (T) exact.get(type);
            if (result != null) {
                return result;
            }
        }
        // maybe type is an interface
        for (Class<?> klass = type; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            T result = (T) inherited.get(klass);
            if (result != null) {
                return result;
            }
        }
        for (Class<?> iface : type.getInterfaces()) {
            T result = (T) inherited.get(iface);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private UnsupportedOperationException unsupportedType(Class<?> type) {
        return new UnsupportedOperationException("no type handler for type '" + type.getName() + "'");
    }

    @Override
    public InvokerHandlerInfo findReturnTypeInfo(Class<?> returnType) {
        InvokerHandlerInfo typeHandlerInfo = extractFromMap(exactReturnTypeMap, inheritedReturnTypeMap, returnType);
        if (typeHandlerInfo != null) {
            return typeHandlerInfo;
        }
        throw unsupportedType(returnType);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> ParameterHandlerInfo<T> findParameterTypeInfo(Class<T> type) {
        ParameterHandlerInfo<T> parameterHandlerInfo = extractFromMap(exactParameterTypeMap, inheritedParameterTypeMap, type);
        if (parameterHandlerInfo != null) {
            return parameterHandlerInfo;
        } else if (type.isEnum()) {
            EnumTypeHandler typeHandler = EnumTypeHandler.getInstance((Class) type);
            ParameterHandler<T> parameterHandler = typeHandler.getParameterHandler();
            InternalType internalType = typeHandler.getDefaultType();
            ParameterHandlerInfo<T> phi = ParameterHandlerInfo.typedefFirst(internalType, parameterHandler);
            addExactParameterTypeHandler(type, phi);
            return phi;
        }
        throw unsupportedType(type);
    }

    private interface ArrayParameterHandler<T> {

        void handle(Pointer memory, int offset, T array, int off, int len);

    }

    private interface EmptyMemoryHolder {

        Pointer NOMEMORY = AllocatedMemory.allocate(0);

    }

}
