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

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@ApplicationScoped
public class JNACleaner {
    private record JNAThreadLocals(ThreadLocal<?> busy, ThreadLocal<?> reads) {
        private void remove() {
            busy.remove();
            reads.remove();
        }
    }
    private AtomicReference<JNAThreadLocals> jnaThreadLocals = new AtomicReference<>();

    void begin() {
        jnaThreadLocals.updateAndGet(current -> current == null ? createPAMThreadLocals() : current);
    }

    @PreDestroy
    void destroy() {
        try {
            destroyCleanerThread();
        } catch (ReflectiveOperationException | InterruptedException e) {
            log.warn("Unable to interrupt JNA cleaner thread", e);
        }
    }

    void removeThreadLocals() {
        begin();
        Optional.ofNullable(jnaThreadLocals.get()).ifPresent(JNAThreadLocals::remove);
    }

    private static JNAThreadLocals createPAMThreadLocals() {
        try {
            Class<?> structureClass = Class.forName("com.sun.jna.Structure");
            Field busyField = structureClass.getDeclaredField("busy");
            busyField.setAccessible(true);
            Field readsField = structureClass.getDeclaredField("reads");
            readsField.setAccessible(true);
            return new JNAThreadLocals((ThreadLocal<?>) busyField.get(null),
                    (ThreadLocal<?>) readsField.get(null));
        } catch (ReflectiveOperationException e) {
            log.warn("Unable to initialize JNA", e);
            return null;
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static void destroyCleanerThread() throws ReflectiveOperationException, InterruptedException {
        Class<?> cleanerClass = Class.forName("com.sun.jna.internal.Cleaner");
        Method getCleanerMethod = cleanerClass.getMethod("getCleaner");
        Object cleaner = getCleanerMethod.invoke(null);
        Field cleanerThreadField = cleanerClass.getDeclaredField("cleanerThread");
        cleanerThreadField.setAccessible(true);
        Thread cleanerThread = (Thread) cleanerThreadField.get(cleaner);
        if (cleanerThread != null) {
            cleanerThread.interrupt();
            cleanerThread.join(Duration.ofMillis(100));
        }
    }
}
