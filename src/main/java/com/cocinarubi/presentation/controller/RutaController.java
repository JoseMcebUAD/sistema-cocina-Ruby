package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.request.AsignarRutasOrdenDTO;
import com.cocinarubi.presentation.dto.request.RutaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.OrdenRutaResponseDTO;
import com.cocinarubi.presentation.dto.response.RutaResponseDTO;
import com.cocinarubi.presentation.dto.response.RutaSimpleResponseDTO;
import com.cocinarubi.domain.service.OrdenRutaService;
import com.cocinarubi.domain.service.RutaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


/**
 * Expone el CRUD de rutas de entrega y la gestión de grupos (OrdenRuta).
 * Capa: Controller — delega toda la lógica a RutaService y OrdenRutaService.
 */
@RestController
@RequestMapping("/ruta")
@Tag(name = "Rutas", description = "CRUD básico para gestionar zonas de reparto")
public class RutaController {

    private final RutaService rutaService;
    private final OrdenRutaService ordenRutaService;

    public RutaController(RutaService rutaService, OrdenRutaService ordenRutaService) {
        this.rutaService = rutaService;
        this.ordenRutaService = ordenRutaService;
    }

    /** Devuelve todas las rutas en vista simplificada (sin coordenadas), ordenadas por grupo e id. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RutaSimpleResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas obtenidas correctamente",
                rutaService.findAllSimple()));
    }

    /** Devuelve todas las rutas con sus coordenadas completas para renderizar polígonos en el mapa. */
    @GetMapping("/mapa")
    public ResponseEntity<ApiResponse<List<RutaResponseDTO>>> findAllParaMapa() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas con coordenadas obtenidas correctamente",
                rutaService.findAll()));
    }

    /** Devuelve una ruta por su ID con coordenadas y datos completos. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Ruta encontrada",
                rutaService.findById(id)));
    }

    /** Crea una nueva ruta con su área geográfica en formato WKT y su tarifa de envío. */
    @PostMapping
    public ResponseEntity<ApiResponse<RutaResponseDTO>> save(@Valid @RequestBody RutaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Ruta creada correctamente",
                        rutaService.save(dto)));
    }

    /** Reemplaza todos los datos de una ruta existente (nombre, boundary, tarifa, estado activo). */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaResponseDTO>> update(@PathVariable int id,
                                                               @Valid @RequestBody RutaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Ruta actualizada correctamente",
                rutaService.update(id, dto)));
    }

    /** Elimina una ruta permanentemente. Falla con 409 si tiene clientes o pedidos asociados. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        rutaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Devuelve las rutas asignadas a un grupo (OrdenRuta) específico. */
    @GetMapping("/orden/{id}")
    public ResponseEntity<ApiResponse<List<RutaSimpleResponseDTO>>> findByOrden(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas de la orden obtenidas correctamente",
                rutaService.findByOrden(id)));
    }

    /** Asigna una lista de rutas a un grupo (OrdenRuta). Rutas ya asignadas a otro grupo son reasignadas. */
    @PatchMapping("/orden")
    public ResponseEntity<ApiResponse<OrdenRutaResponseDTO>> asignarRutas(
            @Valid @RequestBody AsignarRutasOrdenDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas asignadas correctamente",
                rutaService.asignarRutas(dto)));
    }

    /** Actualiza el tiempo estimado de entrega (en minutos) de un grupo de rutas. */
    @PatchMapping("/orden/{id}")
    public ResponseEntity<ApiResponse<OrdenRutaResponseDTO>> actualizarTiempoEstimado(
            @PathVariable int id, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tiempo estimado actualizado",
                ordenRutaService.actualizarTiempoEstimado(id, body.get("tiempoEstimadoMin"))));
    }

    /**
     * Desasigna una ruta específica de su grupo, dejándola en estado "Sin Asignar" (idOrdenRuta = null).
     * Equivalente a PATCH /{idRuta} con { "idOrdenRuta": null }, pero semánticamente más explícito
     * para operaciones de drag-and-drop desde un grupo a la columna "Sin Asignar".
     */
    @DeleteMapping("/orden/{idOrdenRuta}/ruta/{idRuta}")
    public ResponseEntity<Void> desasignarRuta(@PathVariable int idOrdenRuta,
                                               @PathVariable int idRuta) {
        rutaService.desasignarRuta(idRuta);
        return ResponseEntity.noContent().build();
    }

    /**
     * Vacía un grupo completo: desasigna todas las rutas del grupo indicado,
     * dejándolas en estado "Sin Asignar". Útil cuando un grupo deja de operar
     * temporalmente sin necesidad de reasignar ruta por ruta.
     */
    @DeleteMapping("/orden/{idOrdenRuta}/rutas")
    public ResponseEntity<Void> vaciarGrupo(@PathVariable int idOrdenRuta) {
        rutaService.vaciarGrupo(idOrdenRuta);
        return ResponseEntity.noContent().build();
    }
}
