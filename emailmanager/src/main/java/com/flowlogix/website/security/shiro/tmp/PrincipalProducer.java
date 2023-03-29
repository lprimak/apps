package com.flowlogix.website.security.shiro.tmp;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cdi.annotations.Principal;
import org.omnifaces.util.Lazy;
import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

/**
 * Remove when Shiro alpha-2 is released
 */
@Dependent
@Deprecated
public class PrincipalProducer {
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
