package org.aburavov.pgbackupui.controllers;

import jakarta.validation.Valid;
import org.aburavov.pgbackupui.dto.ErrorResponse;
import org.aburavov.pgbackupui.dto.JobDto;
import org.aburavov.pgbackupui.models.Job;
import org.aburavov.pgbackupui.services.BackupService;
import org.aburavov.pgbackupui.services.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final BackupService backupService;

    public JobController(JobService jobService, BackupService backupService) {
        this.jobService = jobService;
        this.backupService = backupService;
    }

    @GetMapping
    public ResponseEntity<List<JobDto>> getAllJobs() {
        List<JobDto> jobs = jobService.findAll().stream()
                .map(JobDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJob(@PathVariable("id") String id) {
        return jobService.findById(id)
                .map(job -> ResponseEntity.ok(JobDto.from(job)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JobDto> createJob(@Valid @RequestBody JobDto jobDto) {
        Job job = jobDto.toEntity();
        Job created = jobService.create(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(JobDto.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDto> updateJob(
            @PathVariable("id") String id,
            @Valid @RequestBody JobDto jobDto) {
        Job job = jobDto.toEntity();
        Job updated = jobService.update(id, job);
        return ResponseEntity.ok(JobDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable("id") String id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<?> runJob(@PathVariable("id") String id) {
        try {
            Job job = jobService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

            String backupPath = backupService.executeBackup(job);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Backup completed successfully");
            response.put("path", backupPath);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Backup failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError("Validation Failed");
        errorResponse.setMessage("Invalid input data");
        errorResponse.setValidationErrors(validationErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}
