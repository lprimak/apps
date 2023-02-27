package com.flowlogix.website.security;

import com.flowlogix.website.ui.Constants;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Base64;
import static org.apache.commons.lang3.StringUtils.isBlank;
import org.apache.shiro.cdi.annotations.CipherKeySupplier;
import org.apache.shiro.crypto.cipher.AesCipherService;

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
