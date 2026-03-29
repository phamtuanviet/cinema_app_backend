package com.example.my_movie_app.config;

import com.example.my_movie_app.entity.Post;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.enums.PostType;
import com.example.my_movie_app.repository.PostRepository;
import com.example.my_movie_app.repository.VoucherRepository;
import org.springframework.boot.CommandLineRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PostInitDataLoader {

    private final VoucherRepository voucherRepository;
    private final PostRepository postRepository;

    @Bean
    CommandLineRunner initPosts() {
        return args -> {

            List<Voucher> vouchers = voucherRepository.findAll();

            // Nếu đã có post rồi thì skip
            if (postRepository.count() > 0) return;

            // bộ title + content
            List<String> titles = List.of(
                    "Ưu đãi đặc biệt dành cho bạn",
                    "Cơ hội tiết kiệm không nên bỏ lỡ",
                    "Ưu đãi giới hạn – Nhanh tay trải nghiệm",
                    "Trải nghiệm tốt hơn với mức giá ưu đãi",
                    "Ưu đãi hấp dẫn đang chờ bạn"
            );

            List<String> contents = List.of(
                    "Khám phá những ưu đãi hấp dẫn đang chờ bạn ngay hôm nay. Đây là cơ hội tuyệt vời để tận hưởng dịch vụ với mức giá tốt hơn và trải nghiệm trọn vẹn hơn.",

                    "Một chương trình ưu đãi đặc biệt đang diễn ra trong thời gian giới hạn. Hãy nhanh tay nắm bắt để không bỏ lỡ những lợi ích hấp dẫn.",

                    "Đừng bỏ lỡ cơ hội trải nghiệm dịch vụ với nhiều lợi ích hơn. Ưu đãi lần này mang đến sự tiện lợi và giá trị tốt hơn cho người dùng.",

                    "Tận hưởng dịch vụ chất lượng với mức giá hợp lý hơn. Đây là lựa chọn phù hợp cho những ai muốn tối ưu chi phí mà vẫn đảm bảo trải nghiệm.",

                    "Cơ hội tuyệt vời để bạn trải nghiệm dịch vụ với nhiều ưu đãi hơn. Hãy khám phá ngay hôm nay để không bỏ lỡ những giá trị hấp dẫn."
            );

            int index = 0;

            for (Voucher voucher : vouchers) {

                Post post = new Post();

                post.setTitle(titles.get(index % titles.size()));
                post.setContent(contents.get(index % contents.size()));

                post.setThumbnailUrl("https://res.cloudinary.com/dinrpkau6/image/upload/v1774456193/images_rp6nko.jpg");
                post.setPublished(true);
                post.setType(PostType.VOUCHER);

                post.setStartDate(LocalDateTime.now());
                post.setEndDate(voucher.getExpiryDate());

                post.setVoucher(voucher);

                postRepository.save(post);

                index++;
            }

            System.out.println("Generated posts from vouchers!");
        };
    }
}