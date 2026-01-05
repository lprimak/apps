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
package com.flowlogix.website.dao;

import com.flowlogix.jeedao.DaoHelper;
import com.flowlogix.website.entities.Sample;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
@Transactional
@ActivateRequestContext
public class Initializer {
    @Inject
    DaoHelper<Sample> sampleDaoHelper;

    @SuppressWarnings("checkstyle:MagicNumber")
    void init(@Observes Startup init) {
        if (sampleDaoHelper.count() == 0) {
            log.info("Empty Table - Adding sample data");
            Stream.of(
                            Sample.builder().fullName("Jill Primak").dateOfBirth(LocalDate.of(2010, 10, 20)).build(),
                            Sample.builder().fullName("Anya Primak").dateOfBirth(LocalDate.of(2015, 10, 20)).build())
                    .forEach(sampleDaoHelper.getEntityManager().get()::persist);
        }
    }
}
