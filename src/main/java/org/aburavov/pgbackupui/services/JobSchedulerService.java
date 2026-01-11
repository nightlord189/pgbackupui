package org.aburavov.pgbackupui.services;

import jakarta.annotation.PostConstruct;
import org.aburavov.pgbackupui.models.Job;
import org.aburavov.pgbackupui.repositories.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class JobSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);

    private final TaskScheduler taskScheduler;
    private final JobRepository jobRepository;
    private final BackupService backupService;

    // Map of jobId -> ScheduledFuture for cancellation
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public JobSchedulerService(TaskScheduler taskScheduler,
                              JobRepository jobRepository,
                              BackupService backupService) {
        this.taskScheduler = taskScheduler;
        this.jobRepository = jobRepository;
        this.backupService = backupService;
    }

    /**
     * Initialize scheduler on application startup.
     * Loads all jobs with schedules from database and schedules them.
     */
    @PostConstruct
    public void initializeScheduledJobs() {
        logger.info("Initializing scheduled jobs...");
        List<Job> scheduledJobs = jobRepository.findByScheduleIsNotNull();

        int scheduledCount = 0;
        for (Job job : scheduledJobs) {
            try {
                scheduleJob(job);
                scheduledCount++;
            } catch (Exception e) {
                logger.error("Failed to schedule job {} on startup: {}",
                           job.getName(), e.getMessage());
            }
        }

        logger.info("Initialized {} scheduled jobs", scheduledCount);
    }

    /**
     * Schedule a job with its cron expression.
     * If job is already scheduled, it will be rescheduled.
     */
    public void scheduleJob(Job job) {
        if (job.getSchedule() == null || job.getSchedule().isBlank()) {
            logger.debug("Job {} has no schedule, skipping", job.getName());
            return;
        }

        cancelJob(job.getId());

        try {
            // Validate cron expression
            CronTrigger cronTrigger = new CronTrigger(job.getSchedule());

            // Schedule the task
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                () -> executeScheduledBackup(job),
                cronTrigger
            );

            scheduledTasks.put(job.getId(), scheduledTask);
            logger.info("Scheduled job '{}' with cron expression: {}",
                       job.getName(), job.getSchedule());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid cron expression for job '{}': {}",
                        job.getName(), job.getSchedule());
            throw new IllegalArgumentException(
                "Invalid cron expression: " + job.getSchedule() + ". " + e.getMessage()
            );
        }
    }

    public void cancelJob(String jobId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(jobId);
        if (scheduledTask != null) {
            boolean cancelled = scheduledTask.cancel(false); // Don't interrupt if running
            logger.info("Cancelled scheduled job {}: {}", jobId, cancelled);
        }
    }

    public void rescheduleJob(Job job) {
        logger.debug("Rescheduling job {}", job.getName());
        cancelJob(job.getId());

        if (job.getSchedule() != null && !job.getSchedule().isBlank()) {
            scheduleJob(job);
        }
    }

    /**
     * Execute a scheduled backup.
     * This runs asynchronously to avoid blocking the scheduler thread.
     */
    private void executeScheduledBackup(Job job) {
        logger.info("EXECUTING scheduled backup for job: {}", job.getName());

        try {
            Job currentJob = jobRepository.findById(job.getId())
                .orElseThrow(() -> new IllegalStateException(
                    "Job not found: " + job.getId() + ". It may have been deleted."
                ));

            backupService.executeScheduledBackup(currentJob);

            logger.info("Scheduled backup completed successfully for job: {}",
                       currentJob.getName());

        } catch (Exception e) {
            logger.error("Scheduled backup failed for job {}: {}",
                        job.getName(), e.getMessage(), e);
            // Error is already tracked in JobRun by BackupService
        }
    }

    /**
     * Validate a cron expression without scheduling.
     * Returns true if valid, throws IllegalArgumentException if invalid.
     */
    public boolean validateCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            return true; // null/blank is valid (means no schedule)
        }

        try {
            new CronTrigger(cronExpression);
            return true;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid cron expression: " + cronExpression + ". " + e.getMessage()
            );
        }
    }
}
