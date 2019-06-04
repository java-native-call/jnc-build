package jnc.foreign;

import java.io.Closeable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.spi.ForeignProvider;

@ParametersAreNonnullByDefault
public interface Foreign extends Closeable {

    @Nonnull
    static Foreign getDefault() {
        return ForeignProvider.getDefault().getForeign();
    }

    @Nonnull
    <T> T load(Class<T> interfaceClass, @Nullable String libname, LoadOptions loadOptions);

    /**
     * @throws UnsupportedOperationException if specified type is not supported
     * on current platform.
     */
    @Nonnull
    Type findType(TypeAlias alias) throws UnsupportedOperationException;

    @Nonnull
    Type findType(NativeType nativeType);

    // Don't use, internal support for enum type
    @Deprecated
    @Nonnull
    <E extends Enum<E>> FieldAccessor<E> getEnumFieldAccessor(Class<E> type);

    @Nonnull
    MemoryManager getMemoryManager();

    int getLastError();

}
