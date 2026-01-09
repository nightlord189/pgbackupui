package org.aburavov.pgbackupui.controllers;

import org.aburavov.pgbackupui.dto.ErrorResponse;
import org.aburavov.pgbackupui.dto.StorageDto;
import org.aburavov.pgbackupui.models.Storage;
import org.aburavov.pgbackupui.services.StorageService;
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

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<StorageDto>> getAllStorages() {
        List<StorageDto> storages = storageService.findAll()
            .stream()
            .map(StorageDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(storages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageDto> getStorage(@PathVariable("id") String id) {
        return storageService.findById(id)
            .map(StorageDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StorageDto> createStorage(@Valid @RequestBody StorageDto dto) {
        Storage created = storageService.create(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(StorageDto.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StorageDto> updateStorage(
            @PathVariable("id") String id,
            @Valid @RequestBody StorageDto dto) {
        Storage updated = storageService.update(id, dto.toEntity());
        return ResponseEntity.ok(StorageDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorage(@PathVariable("id") String id) {
        storageService.delete(id);
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
