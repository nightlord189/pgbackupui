package org.aburavov.pgbackupui.repositories;

import org.aburavov.pgbackupui.models.JobRun;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRunRepository extends MongoRepository<JobRun, String> {

    List<JobRun> findByJobIdOrderByStartedAtDesc(String jobId, Pageable pageable);

    void deleteByJobId(String jobId);
}
