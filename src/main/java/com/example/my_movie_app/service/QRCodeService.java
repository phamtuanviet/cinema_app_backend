package com.example.my_movie_app.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class QRCodeService {

    public byte[] generateQRCode(String text) {

        try {
            int width = 300;
            int height = 300;

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ImageIO.write(
                    MatrixToImageWriter.toBufferedImage(matrix),
                    "PNG",
                    outputStream
            );

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Generate QR failed");
        }
    }
}