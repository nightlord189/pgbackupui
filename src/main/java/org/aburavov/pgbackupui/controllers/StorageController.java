package org.aburavov.pgbackupui.controllers;

import org.aburavov.pgbackupui.dto.ErrorResponse;
import org.aburavov.pgbackupui.dto.StorageDto;
import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/storages")
public class StorageController {

    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<StorageDto>> getAllStorages() {
        logger.debug("GET /api/storages - Fetching all storages");
        List<StorageDto> storages = storageService.findAll()
            .stream()
            .map(StorageDto::from)
            .collect(Collectors.toList());
        logger.debug("Returning {} storages", storages.size());
        return ResponseEntity.ok(storages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageDto> getStorage(@PathVariable("id") String id) {
        logger.debug("GET /api/storages/{} - Fetching storage", id);
        return storageService.findById(id)
            .map(StorageDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StorageDto> createStorage(@Valid @RequestBody StorageDto dto) {
        logger.info("POST /api/storages - Creating storage: {} (type={})", dto.getName(), dto.getType());
        Storage created = storageService.create(dto.toEntity());
        logger.info("Storage created successfully: {} (id={})", created.getName(), created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(StorageDto.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StorageDto> updateStorage(
            @PathVariable("id") String id,
            @Valid @RequestBody StorageDto dto) {
        logger.info("PUT /api/storages/{} - Updating storage: {}", id, dto.getName());
        Storage updated = storageService.update(id, dto.toEntity());
        logger.info("Storage updated successfully: {}", updated.getName());
        return ResponseEntity.ok(StorageDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorage(@PathVariable("id") String id) {
        logger.info("DELETE /api/storages/{} - Deleting storage", id);
        storageService.delete(id);
        logger.info("Storage deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError("Validation Failed");
        errorResponse.setMessage("Invalid input data");
        errorResponse.setValidationErrors(errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError("Bad Request");
        errorResponse.setMessage(ex.getMessage());

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
