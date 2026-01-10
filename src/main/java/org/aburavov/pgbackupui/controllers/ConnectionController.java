package org.aburavov.pgbackupui.controllers;

import org.aburavov.pgbackupui.dto.ConnectionDto;
import org.aburavov.pgbackupui.dto.ErrorResponse;
import org.aburavov.pgbackupui.dto.TableSchema;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.services.ConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDto>> getAllConnections() {
        List<ConnectionDto> connections = connectionService.findAll()
            .stream()
            .map(ConnectionDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(connections);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectionDto> getConnection(@PathVariable("id") String id) {
        return connectionService.findById(id)
            .map(ConnectionDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConnectionDto> createConnection(@Valid @RequestBody ConnectionDto dto) {
        Connection created = connectionService.create(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ConnectionDto.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConnectionDto> updateConnection(
            @PathVariable("id") String id,
            @Valid @RequestBody ConnectionDto dto) {
        Connection updated = connectionService.update(id, dto.toEntity());
        return ResponseEntity.ok(ConnectionDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(@PathVariable("id") String id) {
        connectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/schema")
    public ResponseEntity<?> getDatabaseSchema(@PathVariable("id") String id) {
        try {
            Connection connection = connectionService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));

            List<TableSchema> schema = connectionService.getDatabaseSchema(connection);
            return ResponseEntity.ok(schema);
        } catch (SQLException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to connect to database: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
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
