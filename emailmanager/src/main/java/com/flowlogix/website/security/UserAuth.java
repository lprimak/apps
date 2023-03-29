package com.flowlogix.website.security;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import org.apache.shiro.cdi.annotations.CipherKeySupplier;
import org.apache.shiro.crypto.cipher.AesCipherService;
import org.omnifaces.util.Beans;
import org.omnifaces.util.Lazy;

@EqualsAndHashCode
public class UserAuth implements Serializable {
    private final byte[] userName;
    private final byte[] password;

    private static final long serialVersionUID = 3L;
    private final Lazy<CipherKeySupplier> keySource
            = new Lazy<>(() -> Beans.getReference(CipherKeySupplier.class));
    private final Lazy<AesCipherService> cipherService = new Lazy<>(AesCipherService::new);

    public UserAuth(String userName, String password) {
        this.userName = cipherService.get().encrypt(userName.getBytes(StandardCharsets.UTF_8),
                keySource.get().get().getBytes(StandardCharsets.UTF_8)).getBytes();
        this.password = cipherService.get().encrypt(password.getBytes(StandardCharsets.UTF_8),
                keySource.get().get().getBytes(StandardCharsets.UTF_8)).getBytes();
    }

    public String getUserName() {
        return new String(cipherService.get().decrypt(userName, keySource.get().get()
                .getBytes(StandardCharsets.UTF_8)).getClonedBytes(), StandardCharsets.UTF_8);
    }

    public String getPassword() {
        return new String(cipherService.get().decrypt(password, keySource.get().get()
                .getBytes(StandardCharsets.UTF_8)).getClonedBytes(), StandardCharsets.UTF_8);
    }
}
