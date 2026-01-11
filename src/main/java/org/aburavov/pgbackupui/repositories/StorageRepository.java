package org.aburavov.pgbackupui.repositories;

import org.aburavov.pgbackupui.models.Storage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageRepository extends MongoRepository<Storage, String> {
    Optional<Storage> findByName(String name);
    boolean existsByName(String name);
}
