package jnc.foreign.internal;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;

@ParametersAreNonnullByDefault
@NotFinal(NotFinal.Reason.EXTENSION_CLASS_PRESENT)
class SizedDirectMemory extends Memory {

    // not private
    // access by class Slice
    static void checkSliceRange(long total, int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > total || beginIndex > endIndex) {
            String msg = String.format("begin=%s,end=%s,capacity=%s", beginIndex, endIndex, total);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    /**
     * check if specified {@code size} can be put in the specified
     * {@code offset}
     *
     * @param total total size of the memory in bytes
     * @param offset the offset of the memory to put into
     * @param size the size of the object prepare to put
     */
    private static void checkSize(long total, int offset, int size) {
        if (offset < 0 || offset > total - size) {
            String format = "capacity of this pointer is %s, but trying to put an object with size=%s at position %s";
            String msg = String.format(format, total, size, offset);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    private static void checkArrayIndex(long total, int offset, int len, int unit, String typeMsg) {
        if (offset < 0 || offset > total - (long) len * unit) {
            String msg = String.format("access(offset=%s,size=%s)[%s*%s]", offset, total, typeMsg, len);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    private final long size;

    SizedDirectMemory(long address, long size) {
        super(new MemoryAccessor(address));
        this.size = size;
    }

    @Override
    public final long address() {
        return getAccessor().address();
    }

    public final long size() {
        return size;
    }

    /**
     * Do not rely on the String presentation, maybe changed in the future.
     */
    @Override
    public final String toString() {
        return "[" + getClass().getSimpleName() + "#" + Long.toHexString(address()) + ",size=" + size + "]";
    }

    @Override
    public final void putByte(int offset, byte value) {
        checkSize(size, offset, Byte.BYTES);
        getAccessor().putByte(offset, value);
    }

    @Override
    public final void putChar(int offset, char value) {
        checkSize(size, offset, Character.BYTES);
        getAccessor().putChar(offset, value);
    }

    @Override
    public final void putShort(int offset, short value) {
        checkSize(size, offset, Short.BYTES);
        getAccessor().putShort(offset, value);
    }

    @Override
    public final void putInt(int offset, int value) {
        checkSize(size, offset, Integer.BYTES);
        getAccessor().putInt(offset, value);
    }

    @Override
    public final void putLong(int offset, long value) {
        checkSize(size, offset, Long.BYTES);
        getAccessor().putLong(offset, value);
    }

    @Override
    public final void putFloat(int offset, float value) {
        checkSize(size, offset, Float.BYTES);
        getAccessor().putFloat(offset, value);
    }

    @Override
    public final void putDouble(int offset, double value) {
        checkSize(size, offset, Double.BYTES);
        getAccessor().putDouble(offset, value);
    }

    @Override
    public final byte getByte(int offset) {
        checkSize(size, offset, Byte.BYTES);
        return getAccessor().getByte(offset);
    }

    @Override
    public final short getShort(int offset) {
        checkSize(size, offset, Short.BYTES);
        return getAccessor().getShort(offset);
    }

    @Override
    public final char getChar(int offset) {
        checkSize(size, offset, Character.BYTES);
        return getAccessor().getChar(offset);
    }

    @Override
    public final int getInt(int offset) {
        checkSize(size, offset, Integer.BYTES);
        return getAccessor().getInt(offset);
    }

    @Override
    public final long getLong(int offset) {
        checkSize(size, offset, Long.BYTES);
        return getAccessor().getLong(offset);
    }

    @Override
    public final float getFloat(int offset) {
        checkSize(size, offset, Float.BYTES);
        return getAccessor().getFloat(offset);
    }

    @Override
    public final double getDouble(int offset) {
        checkSize(size, offset, Double.BYTES);
        return getAccessor().getDouble(offset);
    }

    @Override
    final void putDouble(int offset, InternalType internalType, double value) {
        checkSize(size, offset, internalType.size());
        getAccessor().putDouble(offset, internalType, value);
    }

    @Override
    final void putFloat(int offset, InternalType internalType, float value) {
        checkSize(size, offset, internalType.size());
        getAccessor().putFloat(offset, internalType, value);
    }

    @Override
    final void putBoolean(int offset, InternalType internalType, boolean value) {
        checkSize(size, offset, internalType.size());
        getAccessor().putBoolean(offset, internalType, value);
    }

    @Override
    final void putInt(int offset, InternalType internalType, int value) {
        checkSize(size, offset, internalType.size());
        getAccessor().putInt(offset, internalType, value);
    }

    @Override
    final void putLong(int offset, InternalType internalType, long value) {
        checkSize(size, offset, internalType.size());
        getAccessor().putLong(offset, internalType, value);
    }

    @Override
    final double getDouble(int offset, InternalType internalType) {
        checkSize(size, offset, internalType.size());
        return getAccessor().getDouble(offset, internalType);
    }

    @Override
    final float getFloat(int offset, InternalType internalType) {
        checkSize(size, offset, internalType.size());
        return getAccessor().getFloat(offset, internalType);
    }

    @Override
    final long getLong(int offset, InternalType internalType) {
        checkSize(size, offset, internalType.size());
        return getAccessor().getLong(offset, internalType);
    }

    @Override
    final int getInt(int offset, InternalType internalType) {
        checkSize(size, offset, internalType.size());
        return getAccessor().getInt(offset, internalType);
    }

    @Override
    final boolean getBoolean(int offset, InternalType internalType) {
        checkSize(size, offset, internalType.size());
        return getAccessor().getBoolean(offset, internalType);
    }

    @Override
    public final void putBytes(int offset, byte[] bytes, int off, int len) {
        checkArrayIndex(size, offset, len, Byte.BYTES, "byte");
        getAccessor().putBytes(offset, bytes, off, len);
    }

    @Override
    public final void getBytes(int offset, byte[] bytes, int off, int len) {
        checkArrayIndex(size, offset, len, Byte.BYTES, "byte");
        getAccessor().getBytes(offset, bytes, off, len);
    }

    @Override
    public final void getShortArray(int offset, short[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Short.BYTES, "short");
        getAccessor().getShortArray(offset, array, off, len);
    }

    @Override
    public final void putShortArray(int offset, short[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Short.BYTES, "short");
        getAccessor().putShortArray(offset, array, off, len);
    }

    @Override
    public final void putCharArray(int offset, char[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Character.BYTES, "char");
        getAccessor().putCharArray(offset, array, off, len);
    }

    @Override
    public final void getCharArray(int offset, char[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Character.BYTES, "char");
        getAccessor().getCharArray(offset, array, off, len);
    }

    @Override
    public final void putIntArray(int offset, int[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Integer.BYTES, "int");
        getAccessor().putIntArray(offset, array, off, len);
    }

    @Override
    public final void getIntArray(int offset, int[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Integer.BYTES, "int");
        getAccessor().getIntArray(offset, array, off, len);
    }

    @Override
    public final void putLongArray(int offset, long[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Long.BYTES, "long");
        getAccessor().putLongArray(offset, array, off, len);
    }

    @Override
    public final void getLongArray(int offset, long[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Long.BYTES, "long");
        getAccessor().getLongArray(offset, array, off, len);
    }

    @Override
    public final void putFloatArray(int offset, float[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Float.BYTES, "float");
        getAccessor().putFloatArray(offset, array, off, len);
    }

    @Override
    public final void getFloatArray(int offset, float[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Float.BYTES, "float");
        getAccessor().getFloatArray(offset, array, off, len);
    }

    @Override
    public final void putDoubleArray(int offset, double[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Double.BYTES, "double");
        getAccessor().putDoubleArray(offset, array, off, len);
    }

    @Override
    public final void getDoubleArray(int offset, double[] array, int off, int len) {
        checkArrayIndex(size, offset, len, Double.BYTES, "double");
        getAccessor().getDoubleArray(offset, array, off, len);
    }

    private void checkPutStringCapacity(long limit, int offset, int require) {
        if (offset < 0 || offset > limit - require) {
            String format = "capacity of this pointer is %s, but require %s to process the string at position %s";
            String msg = String.format(format, limit, require, offset);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    @Override
    public final void putStringUTF(int offset, @Nonnull String value) {
        // TODO assume time complexity of getStringUTFLength is O(1) ?? 
        //  maybe we should cache the length
        int utfLength = NativeLoader.getAccessor().getStringUTFLength(value);
        checkPutStringCapacity(size, offset, utfLength + 1);
        getAccessor().putStringUTF(offset, value);
    }

    @Nonnull
    @Override
    public final String getStringUTF(int offset) {
        return getAccessor().getStringUTF(offset, size);
    }

    @Override
    void putStringImpl(int offset, byte[] bytes, int terminatorLength) {
        checkPutStringCapacity(size, offset, bytes.length + terminatorLength);
        StringCoding.put(getAccessor(), offset, bytes, terminatorLength);
    }

    @Override
    String getStringImpl(int offset, Charset charset) {
        if (offset < 0 || offset > size) {
            throw new IndexOutOfBoundsException();
        }
        return StringCoding.get(getAccessor(), offset, charset, size - offset);
    }

    @Override
    final void putString16(int offset, @Nonnull String value) {
        checkPutStringCapacity(size, offset, (value.length() + 1) * Character.BYTES);
        getAccessor().putString16(offset, value);
    }

    @Nonnull
    @Override
    final String getString16(int offset) {
        if (offset < 0 || offset > size) {
            throw new IndexOutOfBoundsException();
        }
        return getAccessor().getString16(offset, size - offset);
    }

    @Nonnull
    @Override
    public Slice slice(int beginIndex, int endIndex) {
        checkSliceRange(size, beginIndex, endIndex);
        return new Slice(this, beginIndex, endIndex - beginIndex);
    }

    @Nullable
    @Override
    public final Pointer getPointer(int offset) {
        checkSize(size, offset, DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(NativeType.POINTER).size());
        return UnboundedDirectMemory.of(getAccessor().getAddress(offset));
    }

    @Override
    public final void putPointer(int offset, @Nullable Pointer pointer) {
        checkSize(size, offset, DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(NativeType.POINTER).size());
        getAccessor().putAddress(offset, pointer != null ? pointer.address() : 0);
    }

}
