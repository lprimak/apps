package com.flowlogix.website.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class EntityManagerProducer {
    @Produces
    @PersistenceContext
    EntityManager em;
}
