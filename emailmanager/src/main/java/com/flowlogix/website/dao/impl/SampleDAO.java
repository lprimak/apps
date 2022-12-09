/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.dao.impl;

import com.flowlogix.website.dao.SampleDAOLocal;
import com.flowlogix.website.entities.Sample;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author lprimak
 */
@Stateless
public class SampleDAO implements SampleDAOLocal {
    @PersistenceContext
    EntityManager em;

    @Override
    public List<Sample> query(String queryName) {
        return em.createNamedQuery(queryName, Sample.class).getResultList();
    }
}
