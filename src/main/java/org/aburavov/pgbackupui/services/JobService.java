package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.models.Job;
import org.aburavov.pgbackupui.repositories.ConnectionRepository;
import org.aburavov.pgbackupui.repositories.JobRepository;
import org.aburavov.pgbackupui.repositories.JobRunRepository;
import org.aburavov.pgbackupui.repositories.StorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final ConnectionRepository connectionRepository;
    private final StorageRepository storageRepository;
    private final JobRunRepository jobRunRepository;
    private final JobSchedulerService jobSchedulerService;

    public JobService(JobRepository jobRepository,
                      ConnectionRepository connectionRepository,
                      StorageRepository storageRepository,
                      JobRunRepository jobRunRepository,
                      JobSchedulerService jobSchedulerService) {
        this.jobRepository = jobRepository;
        this.connectionRepository = connectionRepository;
        this.storageRepository = storageRepository;
        this.jobRunRepository = jobRunRepository;
        this.jobSchedulerService = jobSchedulerService;
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
        logger.info("Creating job: {}", job.getName());
        if (jobRepository.existsByName(job.getName())) {
            logger.error("Create job: job with name '{}' already exists", job.getName());
            throw new IllegalArgumentException(
                "Job with name '" + job.getName() + "' already exists"
            );
        }

        if (!connectionRepository.existsById(job.getConnectionId())) {
            logger.error("Create job: connection not found: {}", job.getConnectionId());
            throw new IllegalArgumentException(
                "Connection not found: " + job.getConnectionId()
            );
        }

        if (!storageRepository.existsById(job.getStorageId())) {
            logger.error("Create job: storage not found: {}", job.getStorageId());
            throw new IllegalArgumentException(
                "Storage not found: " + job.getStorageId()
            );
        }

        if (job.getSchedule() != null && !job.getSchedule().isBlank()) {
            logger.debug("Create job: validating cron expression: {}", job.getSchedule());
            jobSchedulerService.validateCronExpression(job.getSchedule());
        }

        Job savedJob = jobRepository.save(job);
        logger.info("Job created successfully: {} (id={})", savedJob.getName(), savedJob.getId());

        if (savedJob.getSchedule() != null && !savedJob.getSchedule().isBlank()) {
            jobSchedulerService.scheduleJob(savedJob);
        }

        return savedJob;
    }

    public Job update(String id, Job job) {
        logger.info("Updating job: {} (id={})", job.getName(), id);
        Job existing = jobRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        if (!existing.getName().equals(job.getName())
            && jobRepository.existsByName(job.getName())) {
            logger.error("Update job: job with name '{}' already exists", job.getName());
            throw new IllegalArgumentException(
                "Job with name '" + job.getName() + "' already exists"
            );
        }

        if (!connectionRepository.existsById(job.getConnectionId())) {
            logger.error("Update job: connection not found: {}", job.getConnectionId());
            throw new IllegalArgumentException(
                "Connection not found: " + job.getConnectionId()
            );
        }

        if (!storageRepository.existsById(job.getStorageId())) {
            logger.error("Update job: storage not found: {}", job.getStorageId());
            throw new IllegalArgumentException(
                "Storage not found: " + job.getStorageId()
            );
        }

        if (job.getSchedule() != null && !job.getSchedule().isBlank()) {
            logger.debug("Update job: validating cron expression: {}", job.getSchedule());
            jobSchedulerService.validateCronExpression(job.getSchedule());
        }

        existing.setName(job.getName());
        existing.setConnectionId(job.getConnectionId());
        existing.setStorageId(job.getStorageId());
        existing.setRetentionCount(job.getRetentionCount());
        existing.setSchedule(job.getSchedule());
        existing.setTables(job.getTables());
        existing.updateTimestamp();

        Job updatedJob = jobRepository.save(existing);
        logger.info("Job updated successfully: {}", updatedJob.getName());

        jobSchedulerService.rescheduleJob(updatedJob);

        return updatedJob;
    }

    @Transactional
    public void delete(String id) {
        logger.info("Deleting job: {}", id);
        if (!jobRepository.existsById(id)) {
            logger.error("Job not found: {}", id);
            throw new IllegalArgumentException("Job not found: " + id);
        }

        jobSchedulerService.cancelJob(id);

        jobRunRepository.deleteByJobId(id);

        jobRepository.deleteById(id);
        logger.info("Job deleted successfully: {}", id);
    }

    public boolean existsByName(String name) {
        return jobRepository.existsByName(name);
    }
}
