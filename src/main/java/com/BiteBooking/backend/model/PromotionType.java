package com.BiteBooking.backend.model;

/**
 * Tipos de promoción disponibles
 */
public enum PromotionType {
    PERCENTAGE_DISCOUNT,  // Descuento en porcentaje (ej: 20% off)
    FIXED_DISCOUNT,       // Descuento fijo (ej: 5€ off)
    HAPPY_HOUR,           // Descuento en horario específico
    TWO_FOR_ONE,          // 2x1 en platos seleccionados
    FREE_ITEM,            // Item gratis (ej: postre gratis)
    FIRST_BOOKING,        // Descuento primera reserva
    LOYALTY,              // Programa de fidelidad
    SPECIAL_MENU          // Menú especial a precio fijo
}
