package com.flowlogix.website.security.shiro.tmp;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.shiro.SecurityUtils;
import org.omnifaces.util.Lazy;
import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

/**
 * Remove when Shiro alpha-2 is released
 */
@Dependent
@Deprecated
public class PrincipalProducer {
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    public @interface Principal {
    }

    @Produces
    @Principal
    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> getPrincipal(InjectionPoint injectionPoint) {
        var parameterizedType = (ParameterizedType) injectionPoint.getType();
        var principalType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        Lazy.SerializableSupplier<T> supplier = () -> SecurityUtils.getSubject().getPrincipals().oneByType(principalType);
        return supplier;
    }
}
