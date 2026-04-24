package com.Service;

import com.DAO.Daos.AperturaCajaDAO;
import com.DAO.Daos.CierreCajaDAO;
import com.DAO.Daos.RetiroCajaDAO;
import com.Model.ModeloAperturaCaja;
import com.Model.ModeloCierreCaja;
import com.Model.ModeloRetiroCaja;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.Config.DatabasePool;

public class CajaService {

    private final AperturaCajaDAO aperturaDAO = new AperturaCajaDAO();
    private final CierreCajaDAO cierreDAO = new CierreCajaDAO();
    private final RetiroCajaDAO retiroDAO = new RetiroCajaDAO();

    /**
     * Abre la caja registrando el monto inicial y el usuario.
     */
    public ModeloAperturaCaja abrirCaja(double montoInicial, int idUsuario) {
        try {
            ModeloAperturaCaja aperturaHoy = aperturaDAO.findUltimaAperturaHoy();
            if (aperturaHoy != null) {
                System.err.println("Ya existe una apertura de caja para hoy.");
                return null;
            }

            ModeloAperturaCaja apertura = new ModeloAperturaCaja();
            apertura.setIdRelUsuario(idUsuario);
            apertura.setMontoInicial(montoInicial);
            apertura.setFechaApertura(LocalDateTime.now());
            return aperturaDAO.create(apertura);
        } catch (SQLException e) {
            System.err.println("Error al abrir caja: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene la apertura activa de hoy (sin cierre registrado).
     * Retorna null si no existe.
     */
    public ModeloAperturaCaja getAperturaActivaHoy() {
        try {
            return aperturaDAO.findAperturaActivaHoy();
        } catch (SQLException e) {
            System.err.println("Error al buscar apertura activa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene la ultima apertura registrada hoy, sin importar si ya fue cerrada.
     * Retorna null si no existe.
     */
    public ModeloAperturaCaja getUltimaAperturaHoy() {
        try {
            return aperturaDAO.findUltimaAperturaHoy();
        } catch (SQLException e) {
            System.err.println("Error al buscar ultima apertura de hoy: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene el cierre asociado a una apertura. Retorna null si no hay cierre aún.
     */
    public ModeloCierreCaja getCierrePorApertura(int idApertura) {
        try {
            return cierreDAO.findCierrePorApertura(idApertura);
        } catch (SQLException e) {
            System.err.println("Error al buscar cierre: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calcula el monto esperado en caja:
     * monto_inicial + ventas_efectivo - retiros_previos
     */
    public double calcularMontoEsperado(int idApertura) {
        try {
            ModeloAperturaCaja apertura = aperturaDAO.findAperturaActivaHoy();
            if (apertura == null) return 0.0;

            double montoInicial = apertura.getMontoInicial();
            double ventasEfectivo = getVentasEfectivoDesdeFecha(apertura.getFechaApertura());
            double retiros = retiroDAO.sumRetirosPorApertura(idApertura);
            double montoEsperado = montoInicial + ventasEfectivo - retiros;

            System.out.println("=== CÁLCULO MONTO ESPERADO EN CAJA ===");
            System.out.printf("  Monto inicial apertura : $%.2f%n", montoInicial);
            System.out.printf("  + Ventas en efectivo   : $%.2f%n", ventasEfectivo);
            System.out.printf("  - Retiros del día      : $%.2f%n", retiros);
            System.out.println("  -----------------------------------");
            System.out.printf("  Monto esperado         : $%.2f%n", montoEsperado);
            System.out.println("=======================================");

            return montoEsperado;
        } catch (SQLException e) {
            System.err.println("Error al calcular monto esperado: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Registra un retiro validando que haya saldo suficiente.
     * Si no hay saldo suficiente retorna false sin revelar el saldo.
     */
    public boolean registrarRetiro(int idApertura, double montoRetiro, String razon) {
        try {
            ModeloAperturaCaja apertura = aperturaDAO.findAperturaActivaHoy();
            if (apertura == null) return false;

            double ventasEfectivo = getVentasEfectivoDesdeFecha(apertura.getFechaApertura());
            double retirosPrevios = retiroDAO.sumRetirosPorApertura(idApertura);
            double saldoActual = apertura.getMontoInicial() + ventasEfectivo - retirosPrevios;

            if (montoRetiro > saldoActual) {
                return false;
            }

            ModeloRetiroCaja retiro = new ModeloRetiroCaja();
            retiro.setIdRelApertura(idApertura);
            retiro.setMontoRetirado(montoRetiro);
            retiro.setRazonRetiro(razon);
            retiro.setFechaRetiro(LocalDateTime.now());
            retiroDAO.create(retiro);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar retiro: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la lista de retiros registrados para una apertura.
     */
    public List<ModeloRetiroCaja> getRetirosPorApertura(int idApertura) {
        try {
            return retiroDAO.findRetirosPorApertura(idApertura);
        } catch (SQLException e) {
            System.err.println("Error al obtener retiros: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene los retiros de un mes y año específicos.
     */
    public List<ModeloRetiroCaja> getRetirosPorMesAnio(int mes, int anio) {
        try {
            return retiroDAO.findRetirosPorMesAnio(mes, anio);
        } catch (SQLException e) {
            System.err.println("Error al obtener retiros por mes: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene todos los retiros de un año.
     */
    public List<ModeloRetiroCaja> getRetirosPorAnio(int anio) {
        try {
            return retiroDAO.findRetirosPorAnio(anio);
        } catch (SQLException e) {
            System.err.println("Error al obtener retiros por año: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene la lista de años con retiros registrados.
     */
    public List<Integer> getAniosConRetiros() {
        try {
            return retiroDAO.getAniosDisponibles();
        } catch (SQLException e) {
            System.err.println("Error al obtener años disponibles: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Guarda el cierre de caja con todos los datos del arqueo.
     */
    public boolean cerrarCaja(ModeloCierreCaja cierre) {
        try {
            cierreDAO.create(cierre);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al cerrar caja: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Suma las ventas pagadas en efectivo desde una fecha de apertura hasta ahora.
     */
    private double getVentasEfectivoDesdeFecha(LocalDateTime desde) throws SQLException {
        String sql = """
            SELECT id_orden, precio_orden
            FROM orden
            WHERE idRel_tipo_pago = 1
              AND DATE(fecha_expedicion_orden) = CURDATE()
            """;

        double total = 0.0;
        System.out.println("  [DEBUG] Órdenes efectivo hoy:");
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idOrden = rs.getInt("id_orden");
                double precio = rs.getDouble("precio_orden");
                System.out.printf("    id_orden=%d  precio_orden=%.2f%n", idOrden, precio);
                total += precio;
            }
        }
        System.out.printf("  [DEBUG] Total efectivo calculado: $%.2f%n", total);
        return total;
    }
}
