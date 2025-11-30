package com.BiteBooking.backend.service;

import com.BiteBooking.backend.dto.AvailabilityResponse;
import com.BiteBooking.backend.dto.AvailabilityResponse.TimeSlotDTO;
import com.BiteBooking.backend.model.*;
import com.BiteBooking.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AvailabilityService {

    private final RestaurantScheduleRepository scheduleRepository;
    private final ClosedDateRepository closedDateRepository;
    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Obtener disponibilidad completa de un restaurante para una fecha
     */
    public AvailabilityResponse getAvailability(Long restaurantId, LocalDate date, int numPeople) {
        // Verificar si el restaurante existe
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        AvailabilityResponse.AvailabilityResponseBuilder responseBuilder = AvailabilityResponse.builder()
                .restaurantId(restaurantId)
                .date(date);

        // 1. Verificar si está cerrado ese día
        Optional<ClosedDate> closedDate = closedDateRepository.findByRestaurantIdAndClosedDate(restaurantId, date);
        if (closedDate.isPresent()) {
            return responseBuilder
                    .isOpen(false)
                    .closedReason(closedDate.get().getReason())
                    .availableSlots(new ArrayList<>())
                    .build();
        }

        // 2. Obtener horario del día de la semana
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<RestaurantSchedule> scheduleOpt = scheduleRepository.findByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek);

        // Si no hay configuración, usar horarios del restaurante
        if (scheduleOpt.isEmpty()) {
            return getDefaultAvailability(restaurant, date, numPeople);
        }

        RestaurantSchedule schedule = scheduleOpt.get();

        if (!schedule.getIsOpen()) {
            return responseBuilder
                    .isOpen(false)
                    .closedReason("Cerrado los " + getDayName(dayOfWeek))
                    .availableSlots(new ArrayList<>())
                    .build();
        }

        // 3. Generar slots de tiempo disponibles
        List<TimeSlotDTO> slots = generateTimeSlots(restaurant, schedule, date, numPeople);

        return responseBuilder
                .isOpen(true)
                .availableSlots(slots)
                .build();
    }

    /**
     * Genera slots de tiempo con capacidad disponible
     */
    private List<TimeSlotDTO> generateTimeSlots(Restaurant restaurant, RestaurantSchedule schedule, 
                                                 LocalDate date, int numPeople) {
        List<TimeSlotDTO> slots = new ArrayList<>();
        
        int interval = schedule.getSlotIntervalMinutes() != null ? schedule.getSlotIntervalMinutes() : 30;
        int maxPerSlot = schedule.getMaxCapacityPerSlot() != null ? schedule.getMaxCapacityPerSlot() : 20;

        // Generar slots para almuerzo
        if (schedule.getLunchStart() != null && schedule.getLunchEnd() != null) {
            LocalTime current = schedule.getLunchStart();
            while (current.isBefore(schedule.getLunchEnd())) {
                int booked = getBookedPeopleForSlot(restaurant.getId(), date, current);
                int available = maxPerSlot - booked;
                
                slots.add(TimeSlotDTO.builder()
                        .time(current)
                        .maxCapacity(maxPerSlot)
                        .availableCapacity(Math.max(0, available))
                        .isAvailable(available >= numPeople)
                        .period("LUNCH")
                        .build());
                
                current = current.plusMinutes(interval);
            }
        }

        // Generar slots para cena
        if (schedule.getDinnerStart() != null && schedule.getDinnerEnd() != null) {
            LocalTime current = schedule.getDinnerStart();
            while (current.isBefore(schedule.getDinnerEnd())) {
                int booked = getBookedPeopleForSlot(restaurant.getId(), date, current);
                int available = maxPerSlot - booked;
                
                slots.add(TimeSlotDTO.builder()
                        .time(current)
                        .maxCapacity(maxPerSlot)
                        .availableCapacity(Math.max(0, available))
                        .isAvailable(available >= numPeople)
                        .period("DINNER")
                        .build());
                
                current = current.plusMinutes(interval);
            }
        }

        // Si no hay almuerzo/cena definidos, usar horario general
        if (slots.isEmpty() && schedule.getOpenTime() != null && schedule.getCloseTime() != null) {
            LocalTime current = schedule.getOpenTime();
            while (current.isBefore(schedule.getCloseTime())) {
                int booked = getBookedPeopleForSlot(restaurant.getId(), date, current);
                int available = maxPerSlot - booked;
                
                slots.add(TimeSlotDTO.builder()
                        .time(current)
                        .maxCapacity(maxPerSlot)
                        .availableCapacity(Math.max(0, available))
                        .isAvailable(available >= numPeople)
                        .period("GENERAL")
                        .build());
                
                current = current.plusMinutes(interval);
            }
        }

        return slots;
    }

    /**
     * Obtiene personas reservadas para un slot específico
     */
    private int getBookedPeopleForSlot(Long restaurantId, LocalDate date, LocalTime time) {
        Integer count = bookingRepository.countBookedPeopleForSlot(restaurantId, date, time);
        return count != null ? count : 0;
    }

    /**
     * Disponibilidad por defecto usando horarios del restaurante
     */
    private AvailabilityResponse getDefaultAvailability(Restaurant restaurant, LocalDate date, int numPeople) {
        List<TimeSlotDTO> slots = new ArrayList<>();
        
        if (restaurant.getOpeningTime() != null && restaurant.getClosingTime() != null) {
            LocalTime current = restaurant.getOpeningTime();
            int interval = 30; // Default 30 min
            int maxPerSlot = 20; // Default

            while (current.isBefore(restaurant.getClosingTime())) {
                int booked = getBookedPeopleForSlot(restaurant.getId(), date, current);
                int available = maxPerSlot - booked;
                
                slots.add(TimeSlotDTO.builder()
                        .time(current)
                        .maxCapacity(maxPerSlot)
                        .availableCapacity(Math.max(0, available))
                        .isAvailable(available >= numPeople)
                        .period("GENERAL")
                        .build());
                
                current = current.plusMinutes(interval);
            }
        }

        return AvailabilityResponse.builder()
                .restaurantId(restaurant.getId())
                .date(date)
                .isOpen(true)
                .availableSlots(slots)
                .build();
    }

    /**
     * Validar si se puede hacer una reserva
     */
    public boolean canBook(Long restaurantId, LocalDate date, LocalTime time, int numPeople) {
        // 1. Verificar fecha cerrada
        if (closedDateRepository.existsByRestaurantIdAndClosedDate(restaurantId, date)) {
            return false;
        }

        // 2. Verificar día de la semana
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<RestaurantSchedule> scheduleOpt = scheduleRepository.findByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek);
        
        if (scheduleOpt.isPresent() && !scheduleOpt.get().getIsOpen()) {
            return false;
        }

        // 3. Verificar horario
        if (scheduleOpt.isPresent()) {
            RestaurantSchedule schedule = scheduleOpt.get();
            if (!schedule.isTimeWithinOpenHours(time)) {
                return false;
            }
        }

        // 4. Verificar capacidad
        int maxPerSlot = scheduleOpt.map(s -> s.getMaxCapacityPerSlot() != null ? s.getMaxCapacityPerSlot() : 20).orElse(20);
        int booked = getBookedPeopleForSlot(restaurantId, date, time);
        
        return (maxPerSlot - booked) >= numPeople;
    }

    /**
     * Obtener mensaje de error si no se puede reservar
     */
    public String getBookingErrorMessage(Long restaurantId, LocalDate date, LocalTime time, int numPeople) {
        // Verificar fecha cerrada
        Optional<ClosedDate> closedDate = closedDateRepository.findByRestaurantIdAndClosedDate(restaurantId, date);
        if (closedDate.isPresent()) {
            return "El restaurante está cerrado: " + closedDate.get().getReason();
        }

        // Verificar día de la semana
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<RestaurantSchedule> scheduleOpt = scheduleRepository.findByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek);
        
        if (scheduleOpt.isPresent() && !scheduleOpt.get().getIsOpen()) {
            return "El restaurante no abre los " + getDayName(dayOfWeek);
        }

        // Verificar horario
        if (scheduleOpt.isPresent()) {
            RestaurantSchedule schedule = scheduleOpt.get();
            if (!schedule.isTimeWithinOpenHours(time)) {
                return "La hora " + time + " está fuera del horario de apertura";
            }
        }

        // Verificar capacidad
        int maxPerSlot = scheduleOpt.map(s -> s.getMaxCapacityPerSlot() != null ? s.getMaxCapacityPerSlot() : 20).orElse(20);
        int booked = getBookedPeopleForSlot(restaurantId, date, time);
        int available = maxPerSlot - booked;
        
        if (available < numPeople) {
            return "No hay suficiente capacidad para " + numPeople + " personas a las " + time + 
                   ". Disponible: " + available;
        }

        return null; // No hay error
    }

    private String getDayName(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "lunes";
            case TUESDAY -> "martes";
            case WEDNESDAY -> "miércoles";
            case THURSDAY -> "jueves";
            case FRIDAY -> "viernes";
            case SATURDAY -> "sábados";
            case SUNDAY -> "domingos";
        };
    }
}
