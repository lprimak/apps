package com.flowlogix.website.security.shiro.tmp;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.apache.shiro.cdi.annotations.Principal;
import java.lang.reflect.ParameterizedType;

/**
 * Remove when Shiro alpha-2 is released
 */
@Dependent
@Deprecated
public class PrincipalProducer {
    @Produces
    @Principal
    @SuppressWarnings("unchecked")
    public static <T> ShiroPrincipal<T> getPrincipal(InjectionPoint injectionPoint) {
        var parameterizedType = (ParameterizedType) injectionPoint.getType();
        return new ShiroPrincipal<>((Class<T>) parameterizedType.getActualTypeArguments()[0]);
    }
}
