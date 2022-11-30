package com.flowlogix.website.security;

import com.flowlogix.shiro.ee.cdi.KeyGen.CipherKeySupplier;
import com.flowlogix.website.ui.Constants;
import java.util.Base64;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import static org.apache.commons.lang3.StringUtils.isBlank;
import org.apache.shiro.crypto.AesCipherService;

/**
 *
 * @author lprimak
 */
@ApplicationScoped
public class CipherKeySource implements CipherKeySupplier {
    private String cipherKey;

    @Inject
    Constants constants;

    @PostConstruct
    void init() {
        cipherKey = constants.getCipherKey();
        if (isBlank(cipherKey)) {
            var acs = new AesCipherService();
            cipherKey = Base64.getEncoder().encodeToString(acs.generateNewKey().getEncoded());
        }
    }

    @Override
    public String get() {
        return cipherKey;
    }
}
