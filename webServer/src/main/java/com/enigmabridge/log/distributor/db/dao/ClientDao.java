package com.enigmabridge.log.distributor.db.dao;

import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.Domain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * A DAO for the entity User is simply created by extending the CrudRepository
 * interface provided by spring. The following methods are some of the ones
 * available from such interface: save, delete, deleteAll, findOne and findAll.
 * The magic is that such methods must not be implemented, and moreover it is
 * possible create new query methods working only by defining their signature!
 */
public interface ClientDao extends CrudRepository<Client, Integer> {
    /**
     * Return the user having the passed clientId or null if no user is found.
     *
     * @param clientId the client ID.
     */
    List<Client> findByClientId(String clientId);

    Client findByClientIdAndDomain(String clientId, Domain domain);

    List<Client> deleteByClientId(String clientId);

    List<Client> deleteByClientIdAndDomain(String clientId, Domain domain);
}
