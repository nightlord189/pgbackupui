package org.aburavov.pgbackupui.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    @GetMapping("/")
    public ResponseEntity<Resource> index() {
        Resource resource = new ClassPathResource("static/index.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/connections")
    public ResponseEntity<Resource> listConnections() {
        Resource resource = new ClassPathResource("static/connections/list.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/connections/new")
    public ResponseEntity<Resource> newConnection() {
        Resource resource = new ClassPathResource("static/connections/new.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/connections/edit")
    public ResponseEntity<Resource> editConnection() {
        Resource resource = new ClassPathResource("static/connections/edit.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/connections/{id}")
    public ResponseEntity<Resource> viewConnection(@PathVariable("id") String id) {
        Resource resource = new ClassPathResource("static/connections/view.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/storages")
    public ResponseEntity<Resource> listStorages() {
        Resource resource = new ClassPathResource("static/storages/list.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/storages/new")
    public ResponseEntity<Resource> newStorage() {
        Resource resource = new ClassPathResource("static/storages/new.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/storages/edit")
    public ResponseEntity<Resource> editStorage() {
        Resource resource = new ClassPathResource("static/storages/edit.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/storages/{id}")
    public ResponseEntity<Resource> viewStorage(@PathVariable("id") String id) {
        Resource resource = new ClassPathResource("static/storages/view.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
