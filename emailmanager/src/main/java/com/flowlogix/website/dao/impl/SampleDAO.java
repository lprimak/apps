/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
