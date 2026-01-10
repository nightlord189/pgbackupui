package org.aburavov.pgbackupui.controllers;

import org.aburavov.pgbackupui.models.JobRun;
import org.aburavov.pgbackupui.repositories.JobRunRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/runs")
public class JobRunController {

    private final JobRunRepository jobRunRepository;

    public JobRunController(JobRunRepository jobRunRepository) {
        this.jobRunRepository = jobRunRepository;
    }

    @GetMapping
    public ResponseEntity<List<JobRun>> getJobRuns(@PathVariable("jobId") String jobId) {
        Pageable pageable = PageRequest.of(0, 10);
        List<JobRun> runs = jobRunRepository.findByJobIdOrderByStartedAtDesc(jobId, pageable);
        return ResponseEntity.ok(runs);
    }
}
