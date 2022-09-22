/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author lprimak
 */
@Entity
@Data
@NamedQueries(
{
    @NamedQuery(name = "Sample.findAll", query = "SELECT s FROM Sample s")
})
public class Sample implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;

    @Size(max = 300)
    @Column(name = "fullName")
    private String fullName;

    @Column(name = "DoB")
    @Temporal(TemporalType.DATE)
    private Date DoB;
}
