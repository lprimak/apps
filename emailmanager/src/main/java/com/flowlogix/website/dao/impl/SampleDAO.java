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
package com.flowlogix.website.dao.impl;

import com.flowlogix.website.dao.SampleDAOLocal;
import com.flowlogix.website.entities.Sample;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

/**
 *
 * @author lprimak
 */
@Stateless
public class SampleDAO implements SampleDAOLocal {
    @Inject
    EntityManager em;

    @Override
    public List<Sample> query(String queryName) {
        return em.createNamedQuery(queryName, Sample.class).getResultList();
    }

    @Override
    public Optional<Long> findFirst() {
        return em.createNamedQuery("Sample.findAllIDs", Long.class).setMaxResults(1).getResultStream().findFirst();
    }
}
