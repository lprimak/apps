package com.flowlogix.website.security;

import com.flowlogix.shiro.ee.cdi.KeyGen.CipherKeySupplier;
import com.flowlogix.website.ui.Constants;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author lprimak
 */
@ApplicationScoped
public class CipherKeySource implements CipherKeySupplier {
    @Inject
    Constants constants;

    @Override
    public String get() {
        return constants.getCipherKey();
    }
}
