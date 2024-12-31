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
package com.flowlogix.website.ui;

import com.flowlogix.jeedao.primefaces.JPALazyDataModel;
import com.flowlogix.website.dao.SampleDAOLocal;
import com.flowlogix.website.entities.Sample;
import com.flowlogix.website.entities.Sample_;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDate;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.primefaces.event.CellEditEvent;

/**
 *
 * @author lprimak
 */
@Named
@ViewScoped
public class Birthdays implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    EntityManager em;
    @Inject
    @Getter
    JPALazyDataModel<Sample> birthdayModel;
    @EJB
    SampleDAOLocal sampleDAO;

    private long firstRecordId;

    @PostConstruct
    void init() {
        birthdayModel.initialize(builder -> builder.sorter((sortData, cb, root) ->
                sortData.applicationSort(Sample_.id.getName(),
                var -> cb.asc(root.get(Sample_.id)))).build());
        firstRecordId = sampleDAO.findFirst().orElseThrow();
    }

    public LocalDate getFirstBirthday() {
        return em.find(birthdayModel.getEntityClass(), firstRecordId).getDateOfBirth();
    }

    @Transactional
    public void setFirstBirthday(LocalDate dob) {
        em.find(birthdayModel.getEntityClass(), firstRecordId).setDateOfBirth(dob);
    }

    @Transactional
    public void onCellEdited(CellEditEvent<?> event) {
        em.merge(birthdayModel.getRowData());
    }
}
