package com.BiteBooking.backend.config;

import com.BiteBooking.backend.model.BookingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter para manejar valores antiguos de status en la BD
 * Convierte valores numéricos/booleanos a BookingStatus
 */
@Converter
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus status) {
        if (status == null) {
            return BookingStatus.PENDING.name();
        }
        return status.name();
    }

    @Override
    public BookingStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isEmpty()) {
            return BookingStatus.PENDING;
        }
        
        // Manejar valores antiguos
        switch (dbValue.toLowerCase().trim()) {
            case "0":
            case "false":
                return BookingStatus.PENDING;
            case "1":
            case "true":
                return BookingStatus.CONFIRMED;
            default:
                // Intentar parsear como enum
                try {
                    return BookingStatus.valueOf(dbValue.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Si no se puede parsear, devolver PENDING
                    return BookingStatus.PENDING;
                }
        }
    }
}
