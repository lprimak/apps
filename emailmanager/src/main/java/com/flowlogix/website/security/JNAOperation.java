/*
 * Copyright (C) 2011-2026 Flow Logix, Inc. All Rights Reserved.
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

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Beans;

@Slf4j
@RequestScoped
public class JNAOperation {
    @Inject
    JNACleaner jnaCleaner;

    public static void begin()  {
        try {
            Beans.getInstance(JNAOperation.class).doBegin();
        } catch (ContextNotActiveException e) {
            Beans.getInstance(JNACleaner.class).removeThreadLocals();
        }
    }

    @PreDestroy
    void destroy() {
        jnaCleaner.removeThreadLocals();
    }

    private void doBegin() {
        jnaCleaner.begin();
    }
}
