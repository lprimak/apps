/*
 * Copyright (C) 2011-2025 Flow Logix, Inc. All Rights Reserved.
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

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cdi.annotations.CipherKeySupplier;
import org.apache.shiro.crypto.CryptoException;
import org.apache.shiro.crypto.cipher.AesCipherService;
import org.omnifaces.util.Beans;
import org.omnifaces.util.Lazy;
import static lombok.EqualsAndHashCode.CacheStrategy.LAZY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@EqualsAndHashCode(doNotUseGetters = true, cacheStrategy = LAZY,
        of = {"userName", "password"})
public class UserAuth implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    private final byte[] userName;
    private final byte[] password;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final String cipherKey = initializeCipherKey();
    private final Lazy<AesCipherService> cipherService = new Lazy<>(AesCipherService::new);

    public UserAuth(String userName, String password) {
        this.userName = cipherService.get().encrypt(userName.getBytes(StandardCharsets.UTF_8),
                getCipherKey().getBytes(StandardCharsets.UTF_8)).getBytes();
        this.password = cipherService.get().encrypt(password.getBytes(StandardCharsets.UTF_8),
                getCipherKey().getBytes(StandardCharsets.UTF_8)).getBytes();
    }

    public Optional<String> getUserName() {
        try {
            return Optional.of(new String(cipherService.get().decrypt(userName,
                    getCipherKey().getBytes(StandardCharsets.UTF_8)).getClonedBytes(), StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            log.warn("Can't decrypt user name", e);
            return Optional.empty();
        }
    }

    public Optional<String> getPassword() {
        try {
            return Optional.of(new String(cipherService.get().decrypt(password,
                    getCipherKey().getBytes(StandardCharsets.UTF_8)).getClonedBytes(), StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            log.warn("Can't decrypt user password", e);
            return Optional.empty();
        }
    }

    private String initializeCipherKey() {
        String configuredKey = Beans.getReference(CipherKeySupplier.class).get();
        if (isBlank(configuredKey)) {
            var acs = new AesCipherService();
            return Base64.getEncoder().encodeToString(acs.generateNewKey().getEncoded());
        }
        return configuredKey;
    }
}
