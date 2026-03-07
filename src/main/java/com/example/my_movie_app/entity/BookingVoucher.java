package com.example.my_movie_app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingVoucher {

    @EmbeddedId
    private BookingVoucherId id;

    @ManyToOne
    @MapsId("bookingId")
    private Booking booking;

    @ManyToOne
    @MapsId("voucherId")
    private Voucher voucher;
}