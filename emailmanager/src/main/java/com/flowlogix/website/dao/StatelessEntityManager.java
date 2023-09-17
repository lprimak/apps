package com.flowlogix.website.dao;

import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface StatelessEntityManager {
}
