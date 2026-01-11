package org.aburavov.pgbackupui.controllers;

import org.aburavov.pgbackupui.dto.ConnectionDto;
import org.aburavov.pgbackupui.dto.ErrorResponse;
import org.aburavov.pgbackupui.dto.TableSchema;
import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.services.ConnectionService;
import org.aburavov.pgbackupui.services.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ConnectionController.class);

    private final ConnectionService connectionService;
    private final DbService dbService;

    public ConnectionController(ConnectionService connectionService, DbService dbService) {
        this.connectionService = connectionService;
        this.dbService = dbService;
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDto>> getAllConnections() {
        logger.debug("GET /api/connections - Fetching all connections");
        List<ConnectionDto> connections = connectionService.findAll()
            .stream()
            .map(ConnectionDto::from)
            .collect(Collectors.toList());
        logger.debug("Returning {} connections", connections.size());
        return ResponseEntity.ok(connections);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectionDto> getConnection(@PathVariable("id") String id) {
        logger.debug("GET /api/connections/{} - Fetching connection", id);
        return connectionService.findById(id)
            .map(ConnectionDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConnectionDto> createConnection(@Valid @RequestBody ConnectionDto dto) {
        logger.info("POST /api/connections - Creating connection: {}", dto.getName());
        Connection created = connectionService.create(dto.toEntity());
        logger.info("Connection created successfully: {} (id={})", created.getName(), created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ConnectionDto.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConnectionDto> updateConnection(
            @PathVariable("id") String id,
            @Valid @RequestBody ConnectionDto dto) {
        logger.info("PUT /api/connections/{} - Updating connection: {}", id, dto.getName());
        Connection updated = connectionService.update(id, dto.toEntity());
        logger.info("Connection updated successfully: {}", updated.getName());
        return ResponseEntity.ok(ConnectionDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(@PathVariable("id") String id) {
        logger.info("DELETE /api/connections/{} - Deleting connection", id);
        connectionService.delete(id);
        logger.info("Connection deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/schema")
    public ResponseEntity<?> getDatabaseSchema(@PathVariable("id") String id) {
        logger.debug("GET /api/connections/{}/schema - Fetching database schema", id);
        try {
            Connection connection = connectionService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));

            List<TableSchema> schema = dbService.getDatabaseSchema(connection);
            logger.debug("Database schema retrieved successfully: {} tables", schema.size());
            return ResponseEntity.ok(schema);
        } catch (SQLException e) {
            logger.error("Failed to connect to database for connection {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to connect to database: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalArgumentException e) {
            logger.error("Connection not found: {}", id);
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
