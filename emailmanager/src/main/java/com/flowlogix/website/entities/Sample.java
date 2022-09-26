/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.entities;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author lprimak
 */
@Entity
@Data
@NamedQuery(name = "Sample.findAll", query = "SELECT s FROM Sample s order by s.id")
public class Sample implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;

    @Size(max = 300)
    @Column(name = "fullName")
    private String fullName;

    @Column(name = "DoB")
    private LocalDate DoB;
}
