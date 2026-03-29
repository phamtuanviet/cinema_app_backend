package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.*;
import com.example.my_movie_app.dto.mapper.BookingMapper;
import com.example.my_movie_app.dto.request.BookingRequest;
import com.example.my_movie_app.dto.response.BookingResponse;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.enums.DiscountType;
import com.example.my_movie_app.enums.LoyaltyTransactionType;
import com.example.my_movie_app.enums.UsageStatus;
import com.example.my_movie_app.repository.*;
import com.example.my_movie_app.util.QRCodeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatHoldSessionRepository sessionRepo;
    private final SeatReservationRepository seatReservationRepo;
    private final ComboRepository comboRepo;
    private final UserVoucherRepository userVoucherRepo;
    private final LoyaltyAccountRepository loyaltyAccountRepo;
    private final LoyaltyTransactionRepository loyaltyTransactionRepo;
    private final BookingRepository bookingRepo;
    private final BookingComboRepository  bookingComboRepo;
    private final VoucherUsageRepository voucherUsageRepo;
    private final CloudinaryService cloudinaryService;
    private final RatingRepository ratingRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest req, UUID userId) {
        System.out.println("Booking");

        Instant now = Instant.now();


        SeatHoldSession session = sessionRepo.findById(UUID.fromString(req.getSeatHoldSessionId()))
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getExpiresAt().isBefore(now)) {
            throw new RuntimeException("Session expired");
        }

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Invalid session");
        }


        int updated = sessionRepo.extendSession(
                session.getId(),
                5 // phút
        );

        if (updated == 0) {
            throw new RuntimeException("Session expired (cannot extend)");
        }

        Showtime showtime = session.getShowtime();

        // =========================
        // =========================
        List<SeatReservation> reservations =
                seatReservationRepo.findBySession_IdAndIsCancelFalse(session.getId());

        if (reservations.isEmpty()) {
            throw new RuntimeException("No seats selected");
        }

        // =========================
        // =========================
        BigDecimal seatAmount = reservations.stream()
                .map(r -> showtime.getBasePrice().add(r.getSeat().getPriceModifier()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // =========================
        // =========================
        BigDecimal comboAmount = BigDecimal.ZERO;

        if (req.getSelectedCombos() != null) {
            for (Map.Entry<String, Integer> entry : req.getSelectedCombos().entrySet()) {

                Combo combo = comboRepo.findById(UUID.fromString(entry.getKey()))
                        .orElseThrow(() -> new RuntimeException("Combo not found"));

                BigDecimal price = BigDecimal.valueOf(combo.getPrice())
                        .multiply(BigDecimal.valueOf(entry.getValue()));

                comboAmount = comboAmount.add(price);
            }
        }

        // =========================
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        UserVoucher userVoucher = null;

        if (req.getVoucherId() != null) {

            userVoucher = userVoucherRepo
                    .findByUser_IdAndVoucher_IdAndIsUsedFalse(
                            userId,
                            UUID.fromString(req.getVoucherId())
                    )
                    .orElseThrow(() -> new RuntimeException("Voucher not available"));

            Voucher voucher = userVoucher.getVoucher();

            if (!voucher.getActive()) {
                throw new RuntimeException("Voucher inactive");
            }

            if (voucher.getExpiryDate() != null &&
                    voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Voucher expired");
            }

            if (voucher.getUsageLimit() != null &&
                    voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new RuntimeException("Voucher usage limit reached");
            }

            BigDecimal orderAmount = seatAmount.add(comboAmount);

            if (voucher.getMinOrderValue() != null &&
                    orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new RuntimeException("Not enough amount to use voucher");
            }

            // =========================
            // =========================
            if (voucher.getDiscountType() == DiscountType.FIXED) {

                voucherDiscount = voucher.getDiscountValue();

            } else if (voucher.getDiscountType() == DiscountType.PERCENT) {

                voucherDiscount = orderAmount
                        .multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));

                if (voucher.getMaxDiscount() != null &&
                        voucherDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
                    voucherDiscount = voucher.getMaxDiscount();
                }
            }
        }

        BigDecimal pointDiscount = BigDecimal.ZERO;
        LoyaltyAccount account = null;

        if (req.getUsedPoints() != null && req.getUsedPoints() > 0) {

            account = loyaltyAccountRepo.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Loyalty account not found"));

            if (account.getAvailablePoints() < req.getUsedPoints()) {
                throw new RuntimeException("Not enough points");
            }

            pointDiscount = BigDecimal.valueOf(req.getUsedPoints());
        }

        // =========================
        BigDecimal totalAmount = seatAmount
                .add(comboAmount)
                .subtract(voucherDiscount)
                .subtract(pointDiscount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        String ticketCode = generateTicketCode();

        byte[] qrBytes;
        try {
            qrBytes = QRCodeUtil.generateQRCode(ticketCode);
        } catch (Exception e) {
            throw new RuntimeException("Generate QR failed", e);
        }

        String qrUrl = cloudinaryService.upload(qrBytes, ticketCode);



        Booking booking = Booking.builder()
                .user(session.getUser())
                .showtime(showtime)
                .ticketCode(ticketCode)
                .seatAmount(seatAmount)
                .comboAmount(comboAmount)
                .voucherDiscount(voucherDiscount)
                .pointDiscount(pointDiscount)
                .totalAmount(totalAmount)
                .qrCodeUrl(qrUrl)
                .status(BookingStatus.PENDING)
                .session(session)
                .build();
        bookingRepo.save(booking);

        if (req.getSelectedCombos() != null) {

            for (Map.Entry<String, Integer> entry : req.getSelectedCombos().entrySet()) {

                UUID comboId = UUID.fromString(entry.getKey());
                Integer quantity = entry.getValue();

                Combo combo = comboRepo.findById(comboId)
                        .orElseThrow(() -> new RuntimeException("Combo not found"));

                BookingCombo bookingCombo = new BookingCombo();

                // 🔥 set ID
                BookingComboId id = new BookingComboId();
                id.setBookingId(booking.getId());
                id.setComboId(comboId);

                bookingCombo.setId(id);

                bookingCombo.setBooking(booking);
                bookingCombo.setCombo(combo);

                bookingCombo.setQuantity(quantity);

                bookingCombo.setPrice(BigDecimal.valueOf(combo.getPrice()));

                bookingComboRepo.save(bookingCombo);
            }
        }


        if (userVoucher != null) {

            userVoucher.setIsUsed(true);
            userVoucher.setUsedAt(LocalDateTime.now());
            userVoucherRepo.save(userVoucher);

            VoucherUsage usage = new VoucherUsage();
            usage.setId(UUID.randomUUID());
            usage.setVoucher(userVoucher.getVoucher());
            usage.setUser(session.getUser());
            usage.setBooking(booking);
            usage.setUserVoucher(userVoucher);
            usage.setDiscountAmount(voucherDiscount);
            usage.setStatus(UsageStatus.USED);
            usage.setUsedAt(LocalDateTime.now());

            voucherUsageRepo.save(usage);
        }


        if (account != null && req.getUsedPoints() != null && req.getUsedPoints() > 0) {

            account.setAvailablePoints(account.getAvailablePoints() - req.getUsedPoints());
            loyaltyAccountRepo.save(account);

            LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                    .account(account)
                    .points(-req.getUsedPoints())
                    .type(LoyaltyTransactionType.REDEEM)
                    .description("Use points for booking " + booking.getTicketCode())
                    .booking(booking)
                    .build();

            loyaltyTransactionRepo.save(transaction);
        }


        return BookingMapper.toResponse(booking);
    }


    private String generateTicketCode() {
        int rand = new Random().nextInt(10000);
        return "TICKET-" + System.currentTimeMillis() + "-" + rand;
    }

    private boolean filterByType(Booking b, LocalDateTime now, String type) {
        LocalDateTime start = b.getShowtime().getStartTime();
        LocalDateTime end = b.getShowtime().getEndTime();

        return switch (type) {
            case "UPCOMING" -> start.isAfter(now);
            case "ONGOING" -> start.isBefore(now) && end.isAfter(now);
            case "COMPLETED" -> end.isBefore(now);
            default -> true;
        };
    }

    private BookingMyBookingDto mapToDto(
            Booking b,
            Map<UUID, Integer> userRatingMap,
            Map<UUID, Double> avgRatingMap,
            Map<UUID, List<SeatReservation>> seatMap
    ) {

        BookingMyBookingDto dto = new BookingMyBookingDto();

        dto.setId(b.getId());
        dto.setTicketCode(b.getTicketCode());
        dto.setQrCodeUrl(b.getQrCodeUrl());
        dto.setStatus(b.getStatus().name());

        dto.setSeatAmount(b.getSeatAmount());
        dto.setComboAmount(b.getComboAmount());
        dto.setVoucherDiscount(b.getVoucherDiscount());
        dto.setPointDiscount(b.getPointDiscount());
        dto.setTotalAmount(b.getTotalAmount());

        Showtime s = b.getShowtime();
        UUID movieId = s.getMovie().getId();

        dto.setShowtimeStart(s.getStartTime());
        dto.setShowtimeEnd(s.getEndTime());

        // ===== MOVIE =====
        MovieMyBookingDto movieDto = new MovieMyBookingDto();
        movieDto.setId(s.getMovie().getId());
        movieDto.setTitle(s.getMovie().getTitle());
        movieDto.setPosterUrl(s.getMovie().getPosterUrl());
        dto.setMovie(movieDto);

        // ===== CINEMA =====
        CinemaMyBookingDto cinemaDto = new CinemaMyBookingDto();
        cinemaDto.setName(s.getRoom().getCinema().getName());
        cinemaDto.setAddress(s.getRoom().getCinema().getAddress());
        dto.setCinema(cinemaDto);

        // ===== ROOM =====
        RoomMyBookingDto roomDto = new RoomMyBookingDto();
        roomDto.setName(s.getRoom().getName());
        dto.setRoom(roomDto);

        // ===== SEATS (từ map) =====
        List<SeatMyBookingDto> seats = Optional.ofNullable(b.getSession())
                .map(session -> seatMap.get(session.getId()))
                .orElse(List.of())
                .stream()
                .map(sr -> {
                    SeatMyBookingDto seat = new SeatMyBookingDto();
                    seat.setSeatRow(sr.getSeat().getSeatRow());
                    seat.setSeatNumber(sr.getSeat().getSeatNumber());
                    return seat;
                })
                .toList();

        dto.setSeats(seats);

        // ===== COMBOS =====
        List<BookingComboMyBookingDto> combos = Optional.ofNullable(b.getBookingCombos())
                .orElse(List.of())
                .stream()
                .map(bc -> {
                    BookingComboMyBookingDto c = new BookingComboMyBookingDto();
                    c.setComboName(bc.getCombo().getName());
                    c.setQuantity(bc.getQuantity());
                    c.setPrice(bc.getPrice());
                    return c;
                }).toList();

        dto.setCombos(combos);

        // ===== RATING =====
        dto.setUserRating(userRatingMap.get(movieId));
        dto.setAverageRating(avgRatingMap.get(movieId));

        return dto;
    }

    public List<BookingMyBookingDto> getMyBookings(UUID userId, String type) {

        List<Booking> bookings = bookingRepo.findAllByUserId(userId);
        if (bookings.isEmpty()) return List.of();

        LocalDateTime now = LocalDateTime.now();

        // ===== FILTER TRƯỚC (giảm data xử lý) =====
        List<Booking> filtered = bookings.stream()
                .filter(b -> filterByType(b, now, type))
                .toList();

        if (filtered.isEmpty()) return List.of();

        // ===== LẤY MOVIE IDS =====
        Set<UUID> movieIds = filtered.stream()
                .map(b -> b.getShowtime().getMovie().getId())
                .collect(Collectors.toSet());

        // ===== USER RATING =====
        Map<UUID, Integer> userRatingMap = ratingRepository
                .findByUserIdAndMovieIds(userId, movieIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getMovie().getId(),
                        Rating::getScore
                ));

        // ===== AVG RATING =====
        Map<UUID, Double> avgRatingMap = ratingRepository
                .getAverageRatingsByMovieIds(movieIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (UUID) r[0],
                        r -> (Double) r[1]
                ));

        // ===== LẤY SESSION IDS =====
        Set<UUID> sessionIds = filtered.stream()
                .map(Booking::getSession)
                .filter(Objects::nonNull)
                .map(SeatHoldSession::getId)
                .collect(Collectors.toSet());

        // ===== LOAD SEATS 1 LẦN =====
        Map<UUID, List<SeatReservation>> seatMap = sessionIds.isEmpty()
                ? Map.of()
                : seatReservationRepo.findAllBySessionIds(sessionIds)
                .stream()
                .collect(Collectors.groupingBy(sr -> sr.getSession().getId()));

        // ===== BUILD DTO =====
        return filtered.stream()
                .map(b -> mapToDto(b, userRatingMap, avgRatingMap, seatMap))
                .toList();
    }
}