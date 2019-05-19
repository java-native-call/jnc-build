package jnc.foreign.internal;

import jnc.foreign.NativeType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinTypeTest {

    private static final Logger log = LoggerFactory.getLogger(BuiltinTypeTest.class);

    @Test
    public void testValues() {
        log.info("values");
        for (BuiltinType value : BuiltinType.values()) {
            String name = value.name();
            String address = String.format("%#x", value.address());
            int type = value.type();
            int size = value.size();
            int alignment = value.alignment();
            char signed = value.isSigned() ? 's' : 'u';
            log.info("{},addr={},type={},size={},align={},{}", name, address, type, size, alignment, signed);
        }
    }

    /**
     * Test of findByNativeType method, of class BuiltinType.
     */
    @Test
    public void testFindByNativeType() {
        log.info("findByNativeType");
        NativeType nativeType = NativeType.ADDRESS;
        BuiltinType expResult = BuiltinType.POINTER;
        BuiltinType result = BuiltinTypeHelper.findByNativeType(nativeType);
        assertEquals(expResult, result);
    }

    /**
     * Test of findAlias method, of class BuiltinType.
     */
    @Test
    public void testFindAlias() {
        log.info("findAlias");
        String name = "";
        try {
            BuiltinTypeHelper.findAlias(name);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        assertEquals(BuiltinType.UINT8, BuiltinTypeHelper.findAlias("uint8_t"));
        assertEquals(BuiltinType.SINT8, BuiltinTypeHelper.findAlias("int8_t"));
        assertEquals(BuiltinType.UINT16, BuiltinTypeHelper.findAlias("uint16_t"));
        assertEquals(BuiltinType.SINT16, BuiltinTypeHelper.findAlias("int16_t"));
        assertEquals(BuiltinType.UINT32, BuiltinTypeHelper.findAlias("uint32_t"));
        assertEquals(BuiltinType.SINT32, BuiltinTypeHelper.findAlias("int32_t"));
        assertEquals(BuiltinType.SINT64, BuiltinTypeHelper.findAlias("int64_t"));
        assertEquals(BuiltinType.UINT64, BuiltinTypeHelper.findAlias("uint64_t"));
        BuiltinType intptr_t = BuiltinTypeHelper.findAlias("intptr_t");
        BuiltinType uintptr_t = BuiltinTypeHelper.findAlias("uintptr_t");
        assertEquals(BuiltinType.POINTER.size(), intptr_t.size());
        assertEquals(BuiltinType.POINTER.size(), uintptr_t.size());
        assertTrue(intptr_t.isSigned());
        assertFalse(uintptr_t.isSigned());
        assertEquals(BuiltinType.POINTER, BuiltinTypeHelper.findAlias("pointer"));
    }

}
