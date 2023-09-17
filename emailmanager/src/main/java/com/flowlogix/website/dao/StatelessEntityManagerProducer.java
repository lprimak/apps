package com.flowlogix.website.dao;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import static jakarta.ejb.TransactionAttributeType.SUPPORTS;

@Stateless
@TransactionAttribute(SUPPORTS)
public class StatelessEntityManagerProducer {
    @Getter(onMethod = @__({@Produces, @StatelessEntityManager}))
    @PersistenceContext
    EntityManager entityManager;
}
