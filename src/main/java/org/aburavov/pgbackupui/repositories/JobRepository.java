package org.aburavov.pgbackupui.repositories;

import org.aburavov.pgbackupui.models.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {

    Optional<Job> findByName(String name);

    boolean existsByName(String name);

    boolean existsByConnectionId(String connectionId);

    boolean existsByStorageId(String storageId);

    List<Job> findByScheduleIsNotNull();
}
