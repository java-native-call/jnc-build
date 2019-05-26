package jnc.foreign.internal;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.byref.ByReference;

enum ByReferenceTypeHandler implements InternalTypeHandler<ByReference> {
    INSTANCE;

    @SuppressWarnings("unchecked")
    public static <T> InternalTypeHandler<T> getInstance() {
        return (InternalTypeHandler<T>) INSTANCE;
    }

    @Override
    public InternalType getInternalType() {
        return TypeHelper.TYPE_POINTER;
    }

    @Override
    public Invoker<ByReference> getInvoker() {
        throw new IllegalStateException("ByReference should not be a return type");
    }

    @Override
    public ParameterHandler<ByReference> getParameterHandler() {
        return (context, index, obj) -> {
            if (obj == null) {
                context.putLong(index, 0);
            } else {
                Foreign foreign = DefaultForeignProvider.getInstance().getForeign();
                Pointer memory = AllocatedMemory.allocate(obj.componentType(foreign).size());
                obj.toNative(foreign, memory);
                context.onFinish(() -> obj.fromNative(foreign, memory)).putLong(index, memory.address());
            }
        };
    }

    @Override
    public NativeType nativeType() {
        return NativeType.ADDRESS;
    }

}
