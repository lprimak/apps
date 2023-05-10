package com.flowlogix.website.ui;

import com.flowlogix.website.dao.SampleDAOLocal;
import com.flowlogix.website.entities.Sample;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
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
    private @Getter List<Sample> birthdays;
    private @EJB SampleDAOLocal sampleDAO;
    private @Inject EntityManager em;

    @PostConstruct
    void init() {
        birthdays = sampleDAO.query("Sample.findAll");
    }

    public LocalDate getFirstBirthday() {
        return birthdays.get(0).getDoB();
    }

    @Transactional
    public void setFirstBirthday(LocalDate dob) {
        var birthday = em.find(Sample.class, birthdays.get(0).getId());
        birthday.setDoB(dob);
    }

    @Transactional
    public void onCellEdited(CellEditEvent<?> event) {
        var birthday = em.find(Sample.class, birthdays.get(event.getRowIndex()).getId());
        switch (event.getColumn().getHeaderText()) {
            case "Name":
                birthday.setFullName((String)event.getNewValue());
                break;
            case "Birthday":
                birthday.setDoB((LocalDate)event.getNewValue());
                break;
        }
    }
}
