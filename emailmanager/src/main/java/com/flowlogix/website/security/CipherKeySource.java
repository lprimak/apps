/*
 * Copyright (C) 2011-2024 Flow Logix, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    @Inject
    Constants constants;

    private String cipherKey;

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
