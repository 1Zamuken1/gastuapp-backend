package com.gastuapp.infrastructure.adapter.rest.controller;

import com.gastuapp.application.dto.request.ProyeccionRequestDTO;
import com.gastuapp.application.dto.response.ProyeccionResponseDTO;
import com.gastuapp.application.dto.response.TransaccionResponseDTO;
import com.gastuapp.application.service.ProyeccionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proyecciones")
@CrossOrigin(origins = "*")
public class ProyeccionController {

    private final ProyeccionService proyeccionService;

    public ProyeccionController(ProyeccionService proyeccionService) {
        this.proyeccionService = proyeccionService;
    }

    private Long getUsuarioId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        return Long.parseLong(userIdStr);
    }

    @PostMapping
    public ResponseEntity<ProyeccionResponseDTO> create(
            @Valid @RequestBody ProyeccionRequestDTO dto,
            HttpServletRequest request) {
        Long usuarioId = getUsuarioId(request);
        ProyeccionResponseDTO response = proyeccionService.crearProyeccion(dto, usuarioId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProyeccionResponseDTO>> getAll(HttpServletRequest request) {
        Long usuarioId = getUsuarioId(request);
        return ResponseEntity.ok(proyeccionService.listarProyeccionesActivas(usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProyeccionResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProyeccionRequestDTO dto,
            HttpServletRequest request) {
        Long usuarioId = getUsuarioId(request);
        ProyeccionResponseDTO response = proyeccionService.actualizarProyeccion(id, dto, usuarioId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long usuarioId = getUsuarioId(request);
        proyeccionService.eliminarProyeccion(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ejecutar")
    public ResponseEntity<TransaccionResponseDTO> execute(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long usuarioId = getUsuarioId(request);
        TransaccionResponseDTO transaccion = proyeccionService.ejecutarProyeccion(id, usuarioId);
        return ResponseEntity.ok(transaccion);
    }
}
