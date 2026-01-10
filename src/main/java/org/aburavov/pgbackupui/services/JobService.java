package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.models.Job;
import org.aburavov.pgbackupui.repositories.ConnectionRepository;
import org.aburavov.pgbackupui.repositories.JobRepository;
import org.aburavov.pgbackupui.repositories.JobRunRepository;
import org.aburavov.pgbackupui.repositories.StorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ConnectionRepository connectionRepository;
    private final StorageRepository storageRepository;
    private final JobRunRepository jobRunRepository;

    public JobService(JobRepository jobRepository,
                      ConnectionRepository connectionRepository,
                      StorageRepository storageRepository,
                      JobRunRepository jobRunRepository) {
        this.jobRepository = jobRepository;
        this.connectionRepository = connectionRepository;
        this.storageRepository = storageRepository;
        this.jobRunRepository = jobRunRepository;
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public Optional<Job> findById(String id) {
        return jobRepository.findById(id);
    }

    public Optional<Job> findByName(String name) {
        return jobRepository.findByName(name);
    }

    public Job create(Job job) {
        if (jobRepository.existsByName(job.getName())) {
            throw new IllegalArgumentException(
                "Job with name '" + job.getName() + "' already exists"
            );
        }

        if (!connectionRepository.existsById(job.getConnectionId())) {
            throw new IllegalArgumentException(
                "Connection not found: " + job.getConnectionId()
            );
        }

        if (!storageRepository.existsById(job.getStorageId())) {
            throw new IllegalArgumentException(
                "Storage not found: " + job.getStorageId()
            );
        }

        return jobRepository.save(job);
    }

    public Job update(String id, Job job) {
        Job existing = jobRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        if (!existing.getName().equals(job.getName())
            && jobRepository.existsByName(job.getName())) {
            throw new IllegalArgumentException(
                "Job with name '" + job.getName() + "' already exists"
            );
        }

        if (!connectionRepository.existsById(job.getConnectionId())) {
            throw new IllegalArgumentException(
                "Connection not found: " + job.getConnectionId()
            );
        }

        if (!storageRepository.existsById(job.getStorageId())) {
            throw new IllegalArgumentException(
                "Storage not found: " + job.getStorageId()
            );
        }

        existing.setName(job.getName());
        existing.setConnectionId(job.getConnectionId());
        existing.setStorageId(job.getStorageId());
        existing.setRetentionCount(job.getRetentionCount());
        existing.setSchedule(job.getSchedule());
        existing.setTables(job.getTables());
        existing.updateTimestamp();

        return jobRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        if (!jobRepository.existsById(id)) {
            throw new IllegalArgumentException("Job not found: " + id);
        }
        // Delete all associated job runs first
        jobRunRepository.deleteByJobId(id);
        // Then delete the job itself
        jobRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return jobRepository.existsByName(name);
    }
}
