package com.BiteBooking.backend.model;

/**
 * Estados posibles de una reserva
 */
public enum BookingStatus {
    PENDING,      // Pendiente de confirmación por el restaurante
    CONFIRMED,    // Confirmada por el restaurante
    CANCELLED,    // Cancelada por el usuario
    REJECTED,     // Rechazada por el restaurante
    COMPLETED,    // Reserva completada (el cliente asistió)
    NO_SHOW       // El cliente no se presentó
}
