package org.aburavov.pgbackupui.controllers;

import org.aburavov.pgbackupui.models.JobRun;
import org.aburavov.pgbackupui.repositories.JobRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/runs")
public class JobRunController {

    private static final Logger logger = LoggerFactory.getLogger(JobRunController.class);

    private final JobRunRepository jobRunRepository;

    public JobRunController(JobRunRepository jobRunRepository) {
        this.jobRunRepository = jobRunRepository;
    }

    @GetMapping
    public ResponseEntity<List<JobRun>> getJobRuns(@PathVariable("jobId") String jobId) {
        logger.debug("GET /api/jobs/{}/runs - Fetching job runs", jobId);
        Pageable pageable = PageRequest.of(0, 10);
        List<JobRun> runs = jobRunRepository.findByJobIdOrderByStartedAtDesc(jobId, pageable);
        logger.debug("Returning {} job runs for job {}", runs.size(), jobId);
        return ResponseEntity.ok(runs);
    }
}
