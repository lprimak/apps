/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.dao;

import com.flowlogix.website.entities.Sample;
import java.util.List;
import java.util.Optional;
import jakarta.ejb.Local;

/**
 *
 * @author lprimak
 */
@Local
public interface SampleDAOLocal {
    List<Sample> query(String queryName);
    Optional<Long> findFirst();
}
