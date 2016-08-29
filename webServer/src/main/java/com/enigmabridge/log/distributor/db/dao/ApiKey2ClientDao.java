package com.enigmabridge.log.distributor.db.dao;

import com.enigmabridge.log.distributor.db.model.ApiKey2Client;
import org.springframework.data.repository.CrudRepository;

/**
 * DAO for loading ApiKeys bindings.
 *
 * Created by dusanklinec on 29.08.16.
 */
public interface ApiKey2ClientDao extends CrudRepository<ApiKey2Client, Integer> {

}
