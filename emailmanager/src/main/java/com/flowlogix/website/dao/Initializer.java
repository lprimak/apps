package com.flowlogix.website.dao;

import com.flowlogix.jeedao.DaoHelper;
import com.flowlogix.website.entities.Sample;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.stream.Stream;
import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

@Slf4j
@ApplicationScoped
@Transactional
@ActivateRequestContext
public class Initializer {
    @Inject
    DaoHelper<Sample> sampleDaoHelper;

    void init(@Observes @Priority(APPLICATION + 100) Startup init) {
        if (sampleDaoHelper.count() == 0) {
            log.info("Empty Table - Adding sample data");
            Stream.of(
                            Sample.builder().fullName("Jill Primak").dateOfBirth(LocalDate.of(2010, 10, 20)).build(),
                            Sample.builder().fullName("Anya Primak").dateOfBirth(LocalDate.of(2015, 10, 20)).build())
                    .forEach(sampleDaoHelper.getEntityManager().get()::persist);
        }
    }
}
