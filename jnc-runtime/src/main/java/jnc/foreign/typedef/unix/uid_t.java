package jnc.foreign.typedef.unix;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jnc.foreign.annotation.Typedef;
import jnc.foreign.enums.TypeAlias;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Typedef(TypeAlias.uid_t)
public @interface uid_t {
}
