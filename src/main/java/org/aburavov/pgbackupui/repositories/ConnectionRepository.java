package org.aburavov.pgbackupui.repositories;

import org.aburavov.pgbackupui.models.Connection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectionRepository extends MongoRepository<Connection, String> {

    Optional<Connection> findByName(String name);

    boolean existsByName(String name);
}
