/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.security;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author lprimak
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class UserAuth implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String userName;
    private final String password;
}
