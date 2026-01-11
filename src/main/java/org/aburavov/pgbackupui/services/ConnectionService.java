package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.repositories.ConnectionRepository;
import org.aburavov.pgbackupui.repositories.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    private final ConnectionRepository connectionRepository;
    private final JobRepository jobRepository;

    public ConnectionService(ConnectionRepository connectionRepository, JobRepository jobRepository) {
        this.connectionRepository = connectionRepository;
        this.jobRepository = jobRepository;
    }

    public List<Connection> findAll() {
        return connectionRepository.findAll();
    }

    public Optional<Connection> findById(String id) {
        return connectionRepository.findById(id);
    }

    public Optional<Connection> findByName(String name) {
        return connectionRepository.findByName(name);
    }

    public Connection create(Connection connection) {
        logger.info("Creating connection: {}", connection.getName());
        if (connectionRepository.existsByName(connection.getName())) {
            logger.error("Connection with name '{}' already exists", connection.getName());
            throw new IllegalArgumentException(
                "Connection with name '" + connection.getName() + "' already exists"
            );
        }
        Connection saved = connectionRepository.save(connection);
        logger.info("Connection created successfully: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    public Connection update(String id, Connection connection) {
        logger.info("Updating connection: {} (id={})", connection.getName(), id);
        Connection existing = connectionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));

        if (!existing.getName().equals(connection.getName())
            && connectionRepository.existsByName(connection.getName())) {
            logger.error("Connection with name '{}' already exists", connection.getName());
            throw new IllegalArgumentException(
                "Connection with name '" + connection.getName() + "' already exists"
            );
        }

        existing.setName(connection.getName());
        existing.setType(connection.getType());
        existing.setHost(connection.getHost());
        existing.setPort(connection.getPort());
        existing.setDatabase(connection.getDatabase());
        existing.setUsername(connection.getUsername());
        if (connection.getPassword() != null && !connection.getPassword().isBlank()) {
            existing.setPassword(connection.getPassword());
        }
        existing.updateTimestamp();

        Connection updated = connectionRepository.save(existing);
        logger.info("Connection updated successfully: {}", updated.getName());
        return updated;
    }

    public void delete(String id) {
        logger.info("Deleting connection: {}", id);
        if (!connectionRepository.existsById(id)) {
            logger.error("Connection not found: {}", id);
            throw new IllegalArgumentException("Connection not found: " + id);
        }
        if (jobRepository.existsByConnectionId(id)) {
            logger.error("Cannot delete connection {}: it is used by one or more jobs", id);
            throw new IllegalArgumentException("Cannot delete connection: it is used by one or more jobs");
        }
        connectionRepository.deleteById(id);
        logger.info("Connection deleted successfully: {}", id);
    }

    public boolean existsByName(String name) {
        return connectionRepository.existsByName(name);
    }
}
