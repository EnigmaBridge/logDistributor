package com.enigmabridge.log.distributor.db.dao;

import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.Domain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 */
public interface DomainDao extends CrudRepository<Domain, Integer> {

    Domain findByDomain(String domain);

    List<Domain> deleteByDomain(String domain);
}
