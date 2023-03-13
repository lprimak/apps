package com.flowlogix.website.impl;

import com.flowlogix.website.security.UserAuth;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.apache.shiro.cdi.annotations.Principal;

import java.util.Optional;

@RequestScoped
public class ShiroPrincipal {
    @Inject
    @Principal
    @Getter
    Optional<UserAuth> principal;
}
