package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.SeatDto;
import com.example.my_movie_app.dto.SeatMapDto;
import com.example.my_movie_app.dto.SeatRowDto;
import com.example.my_movie_app.dto.response.CancelSeatCoupleResponse;
import com.example.my_movie_app.dto.response.CancelSeatResponse;
import com.example.my_movie_app.dto.response.HoldSeatCoupleResponse;
import com.example.my_movie_app.dto.response.HoldSeatResponse;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.enums.SeatStatus;
import com.example.my_movie_app.enums.SeatType;
import com.example.my_movie_app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final BookingRepository bookingRepository;
    private final SeatHoldSessionRepository seatHoldSessionRepository;
    private final UserRepository userRepository;

    public List<Seat> getSeatsByRoom(UUID roomId) {
        return seatRepository.findByRoomId(roomId);
    }

    private SeatStatus resolveSeatStatus(SeatReservation sr, UUID currentUserId) {

        if (sr == null) {
            return SeatStatus.AVAILABLE;
        }

        SeatHoldSession session = sr.getSession();

        if (session == null) {
            return SeatStatus.AVAILABLE;
        }

        boolean expired = session.getExpiresAt().isBefore(Instant.now());

        // 🔥 CHECK BOOKING FIRST
        Booking booking = bookingRepository.findBySession(session).orElse(null);

        if (booking != null && booking.getStatus() == BookingStatus.PAID) {
            return SeatStatus.BOOKED;
        }

        // 🔥 SESSION EXPIRED
        if (expired) {
            return SeatStatus.AVAILABLE;
        }

        // 🔥 HOLD LOGIC (KHÔNG phụ thuộc booking)
        if (session.getUser().getId().equals(currentUserId)) {
            return SeatStatus.HOLD_BY_ME;
        } else {
            return SeatStatus.HOLD_BY_OTHER;
        }
    }



    public SeatMapDto getSeatMap(UUID showtimeId, UUID currentUserId) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        List<Seat> seats = seatRepository.findByRoomId(showtime.getRoom().getId());

        List<SeatReservation> reservations =
                seatReservationRepository.findAllByShowtimeId(showtimeId);

        // Map seatId -> reservation
        Map<UUID, SeatReservation> reservationMap = reservations.stream()
                .collect(Collectors.toMap(
                        sr -> sr.getSeat().getId(),
                        sr -> sr,
                        (a, b) -> a
                ));

        List<SeatRowDto> rows = seats.stream()
                .collect(Collectors.groupingBy(Seat::getSeatRow))
                .entrySet()
                .stream()
                .map(entry -> {

                    String rowName = entry.getKey();

                    List<SeatDto> seatDtos = entry.getValue().stream()
                            .map(seat -> {

                                SeatReservation sr = reservationMap.get(seat.getId());

                                SeatStatus status = resolveSeatStatus(sr, currentUserId);

                                double finalPrice = showtime.getBasePrice()
                                        .add(seat.getPriceModifier())
                                        .doubleValue();

                                return new SeatDto(
                                        seat.getId().toString(),
                                        seat.getSeatRow(),
                                        seat.getSeatNumber(),
                                        seat.getSeatType(),
                                        finalPrice,
                                        status
                                );
                            })
                            .sorted(Comparator.comparing(SeatDto::getSeatNumber))
                            .toList();

                    return new SeatRowDto(rowName, seatDtos);
                })
                .sorted(Comparator.comparing(SeatRowDto::getRow))
                .toList();

        return new SeatMapDto(showtimeId.toString(), rows);
    }

    @Transactional
    public HoldSeatResponse holdSeat(UUID showtimeId, UUID seatId, UUID userId) {

        Instant now = Instant.now();

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow();

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow();

        // 🔥 1. Check existing reservation
        SeatReservation existing = seatReservationRepository
                .findActiveBySeatAndShowtime(seatId, showtimeId)
                .orElse(null);

        if (existing != null) {

            SeatHoldSession session = existing.getSession();

            // 🔥 check booking trước
            Booking booking = bookingRepository
                    .findBySession_Id(session.getId())
                    .orElse(null);

            if (booking != null && booking.getStatus() == BookingStatus.PAID) {
                throw new RuntimeException("Seat already booked");
            }

            // 🔥 check expire
            if (session.getExpiresAt().isBefore(now)) {
                // session hết hạn → release ghế
                existing.setCancel(true);
                seatReservationRepository.save(existing);
            } else {
                // chưa hết hạn → đang bị giữ
                throw new RuntimeException("Seat is being held");
            }
        }

        // 🔥 2. Lấy hoặc tạo session
        SeatHoldSession session = seatHoldSessionRepository
                .findActiveSession(userId, showtimeId, now)
                .orElseGet(() -> {
                    SeatHoldSession newSession = new SeatHoldSession();
                    newSession.setId(UUID.randomUUID());
                    newSession.setUser(userRepository.getReferenceById(userId));
                    newSession.setShowtime(showtime);
                    newSession.setCreatedAt(now);
                    newSession.setExpiresAt(now.plusSeconds(600)); // 10 phút

                    return seatHoldSessionRepository.save(newSession);
                });

        // 🔥 3. Tạo reservation
        SeatReservation reservation = new SeatReservation();
        reservation.setSeat(seat);
        reservation.setShowtime(showtime);
        reservation.setSession(session);
        reservation.setCancel(false);

        seatReservationRepository.save(reservation);

        double finalPrice = showtime.getBasePrice()
                .add(seat.getPriceModifier())
                .doubleValue();

        return new HoldSeatResponse(
                seat.getId().toString(),
                seat.getSeatRow(),
                seat.getSeatNumber(),
                session.getId().toString(),
                true,
                session.getExpiresAt().toString(),
                finalPrice
        );
    }

    @Transactional
    public CancelSeatResponse cancelSeat(UUID showtimeId, UUID seatId, UUID userId) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        Instant now = Instant.now();

        SeatReservation reservation = seatReservationRepository
                .findBySeatAndShowtimeAndUser(seatId, showtimeId, userId)
                .orElseThrow(() -> new RuntimeException("Seat not found or not yours"));

        SeatHoldSession session = reservation.getSession();

        // 🔥 check booking
        Booking booking = bookingRepository
                .findBySession_Id(session.getId())
                .orElse(null);

        if (booking != null && booking.getStatus() == BookingStatus.PAID) {
            throw new RuntimeException("Cannot cancel booked seat");
        }

        // 🔥 nếu session đã hết hạn thì cũng coi như cancel luôn
        if (session.getExpiresAt().isBefore(now)) {
            reservation.setCancel(true);
        } else {
            // vẫn còn hạn → cancel bình thường
            reservation.setCancel(true);
        }

        seatReservationRepository.save(reservation);

        Seat seat = reservation.getSeat();

        double finalPrice = showtime.getBasePrice()
                .add(seat.getPriceModifier())
                .doubleValue();

        return new CancelSeatResponse(
                seat.getId().toString(),
                seat.getSeatRow(),
                seat.getSeatNumber(),
                session.getId().toString(),
                true,
                session.getExpiresAt().toString(),
                finalPrice
        );
    }

    private SeatReservation holdSingleSeatInternal(
            Seat seat,
            Showtime showtime,
            SeatHoldSession session,
            Instant now
    ) {

        SeatReservation existing = seatReservationRepository
                .findActiveBySeatAndShowtime(seat.getId(), showtime.getId())
                .orElse(null);

        if (existing != null) {

            SeatHoldSession oldSession = existing.getSession();

            Booking booking = bookingRepository
                    .findBySession_Id(oldSession.getId())
                    .orElse(null);

            if (booking != null && booking.getStatus() == BookingStatus.PAID) {
                throw new RuntimeException("Seat already booked");
            }

            if (oldSession.getExpiresAt().isBefore(now)) {
                existing.setCancel(true);
                seatReservationRepository.save(existing);
            } else {
                throw new RuntimeException("Seat is being held");
            }
        }

        SeatReservation reservation = new SeatReservation();
        reservation.setSeat(seat);
        reservation.setShowtime(showtime);
        reservation.setSession(session);
        reservation.setCancel(false);

        return seatReservationRepository.save(reservation);
    }

    @Transactional
    public HoldSeatCoupleResponse holdSeatCouple(
            UUID showtimeId,
            UUID firstSeatId,
            UUID secondSeatId,
            UUID userId
    ) {

        Instant now = Instant.now();

        Seat firstSeat = seatRepository.findById(firstSeatId).orElseThrow();
        Seat secondSeat = seatRepository.findById(secondSeatId).orElseThrow();

        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();

        // 🔥 1. Validate phải là COUPLE
        if (firstSeat.getSeatType() != SeatType.COUPLE ||
                secondSeat.getSeatType() != SeatType.COUPLE) {
            throw new RuntimeException("Invalid couple seats");
        }

        // 🔥 (OPTIONAL nhưng NÊN có)
        // validate cùng group (nếu bạn thêm coupleGroupId)
        // if (!firstSeat.getCoupleGroupId().equals(secondSeat.getCoupleGroupId())) throw...

        // 🔥 2. Lấy hoặc tạo session
        SeatHoldSession session = seatHoldSessionRepository
                .findActiveSession(userId, showtimeId, now)
                .orElseGet(() -> {
                    SeatHoldSession newSession = new SeatHoldSession();
                    newSession.setId(UUID.randomUUID());
                    newSession.setUser(userRepository.getReferenceById(userId));
                    newSession.setShowtime(showtime);
                    newSession.setCreatedAt(now);
                    newSession.setExpiresAt(now.plusSeconds(600));

                    return seatHoldSessionRepository.save(newSession);
                });

        // 🔥 3. HOLD 2 ghế (atomic)
        SeatReservation r1 = holdSingleSeatInternal(firstSeat, showtime, session, now);
        SeatReservation r2 = holdSingleSeatInternal(secondSeat, showtime, session, now);

        // 🔥 4. Price
        double price1 = showtime.getBasePrice().add(firstSeat.getPriceModifier()).doubleValue();
        double price2 = showtime.getBasePrice().add(secondSeat.getPriceModifier()).doubleValue();

        return new HoldSeatCoupleResponse(
                session.getId().toString(),
                session.getExpiresAt().toString(),
                price1 + price2
        );
    }

    @Transactional
    public CancelSeatCoupleResponse cancelSeatCouple(
            UUID showtimeId,
            UUID firstSeatId,
            UUID secondSeatId,
            UUID userId
    ) {

        Instant now = Instant.now();

        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();


        SeatReservation r1 = seatReservationRepository
                .findBySeatAndShowtimeAndUser(firstSeatId, showtimeId, userId)
                .orElseThrow(() -> new RuntimeException("Seat 1 not found"));

        SeatReservation r2 = seatReservationRepository
                .findBySeatAndShowtimeAndUser(secondSeatId, showtimeId, userId)
                .orElseThrow(() -> new RuntimeException("Seat 2 not found"));

        SeatHoldSession session = r1.getSession();

        Booking booking = bookingRepository
                .findBySession_Id(session.getId())
                .orElse(null);

        if (booking != null && booking.getStatus() == BookingStatus.PAID) {
            throw new RuntimeException("Cannot cancel booked seats");
        }

        r1.setCancel(true);
        r2.setCancel(true);

        seatReservationRepository.save(r1);
        seatReservationRepository.save(r2);

        Seat firstSeat = r1.getSeat();
        Seat secondSeat = r2.getSeat();

        double price1 = showtime.getBasePrice().add(firstSeat.getPriceModifier()).doubleValue();
        double price2 = showtime.getBasePrice().add(secondSeat.getPriceModifier()).doubleValue();

        double sum = price1 + price2;

        return new CancelSeatCoupleResponse(
                session.getId().toString(),
                session.getExpiresAt().toString(),
                sum
        );
    }


}