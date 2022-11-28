package com.flowlogix.website.security;

import com.flowlogix.shiro.ee.cdi.KeyGen.CipherKeySupplier;
import com.flowlogix.website.ui.Constants;
import java.nio.charset.StandardCharsets;
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
            acs.setKeySize(128);
            cipherKey = new String(acs.generateNewKey().getEncoded(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String get() {
        return cipherKey;
    }
}
