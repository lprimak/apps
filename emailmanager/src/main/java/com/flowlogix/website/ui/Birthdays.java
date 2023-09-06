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
    JPALazyDataModel<Sample, Long> birthdayModel;
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
