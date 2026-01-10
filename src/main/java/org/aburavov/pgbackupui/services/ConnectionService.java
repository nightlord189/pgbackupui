package org.aburavov.pgbackupui.services;

import org.aburavov.pgbackupui.models.Connection;
import org.aburavov.pgbackupui.repositories.ConnectionRepository;
import org.aburavov.pgbackupui.repositories.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

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
        if (connectionRepository.existsByName(connection.getName())) {
            throw new IllegalArgumentException(
                "Connection with name '" + connection.getName() + "' already exists"
            );
        }
        return connectionRepository.save(connection);
    }

    public Connection update(String id, Connection connection) {
        Connection existing = connectionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));

        if (!existing.getName().equals(connection.getName())
            && connectionRepository.existsByName(connection.getName())) {
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

        return connectionRepository.save(existing);
    }

    public void delete(String id) {
        if (!connectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Connection not found: " + id);
        }
        if (jobRepository.existsByConnectionId(id)) {
            throw new IllegalArgumentException("Cannot delete connection: it is used by one or more jobs");
        }
        connectionRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return connectionRepository.existsByName(name);
    }
}
