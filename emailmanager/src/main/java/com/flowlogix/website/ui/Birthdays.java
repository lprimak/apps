package com.flowlogix.website.ui;

import com.flowlogix.website.dao.SampleDAOLocal;
import com.flowlogix.website.entities.Sample;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import lombok.Getter;

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

    @PostConstruct
    void init() {
        birthdays = sampleDAO.query("Sample.findAll");
    }

    public LocalDate getFirstBirthday() {
        return birthdays.get(0).getDoB();
    }
}
