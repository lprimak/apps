/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.entities;

import java.io.Serializable;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author lprimak
 */
@Entity
@Data
@NamedQuery(name = "Sample.findAll", query = "SELECT s FROM Sample s order by s.id")
@NamedQuery(name = "Sample.findAllIDs", query = "SELECT s.id FROM Sample s order by s.id")
public class Sample implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;

    @Size(max = 300)
    @Column(name = "fullName")
    private String fullName;

    @Column(name = "DoB")
    private LocalDate dateOfBirth;
}
