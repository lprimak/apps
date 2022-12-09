/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.security;

import com.flowlogix.shiro.ee.cdi.KeyGen.CipherKeySupplier;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import org.apache.shiro.crypto.cipher.AesCipherService;
import org.omnifaces.util.Beans;
import org.omnifaces.util.Lazy;

/**
 *
 * @author lprimak
 */
@EqualsAndHashCode
public class UserAuth implements Serializable {
    private final byte[] userName;
    private final byte[] password;

    private static final long serialVersionUID = 2L;
    private transient final Lazy<CipherKeySupplier> keySource
            = new Lazy<>(() -> Beans.getReference(CipherKeySupplier.class));
    private transient final Lazy<AesCipherService> cipherService = new Lazy<>(AesCipherService::new);

    public UserAuth(String userName, String password) {
        this.userName = cipherService.get().encrypt(userName.getBytes(StandardCharsets.UTF_8),
                keySource.get().get().getBytes(StandardCharsets.UTF_8)).getBytes();
        this.password = cipherService.get().encrypt(password.getBytes(StandardCharsets.UTF_8),
                keySource.get().get().getBytes(StandardCharsets.UTF_8)).getBytes();
    }

    private UserAuth(byte[] userName, byte[] password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return new String(cipherService.get().decrypt(userName, keySource.get().get()
                .getBytes(StandardCharsets.UTF_8)).getClonedBytes(), StandardCharsets.UTF_8);
    }

    public String getPassword() {
        return new String(cipherService.get().decrypt(password, keySource.get().get()
                .getBytes(StandardCharsets.UTF_8)).getClonedBytes(), StandardCharsets.UTF_8);
    }

    protected Object readResolve() {
        return new UserAuth(userName, password);
    }
}
