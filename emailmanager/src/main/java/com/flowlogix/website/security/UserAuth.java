/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.security;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author lprimak
 */
public @EqualsAndHashCode class UserAuth implements Serializable
{
    public UserAuth()
    {
        // default constructor
    }
    
    
    public UserAuth(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
    }
    
    
    private @Getter @Setter String userName;
    private @Getter @Setter String password;

    private static final long serialVersionUID = 1L;
}
