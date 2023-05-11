package com.flowlogix.website.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RequestScoped
public class EntityManagerProducer {
    @Produces
    @PersistenceContext
    EntityManager em;
}
