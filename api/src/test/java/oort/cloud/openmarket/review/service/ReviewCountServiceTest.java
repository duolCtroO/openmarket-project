package oort.cloud.openmarket.review.service;

import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.entity.Review;
import oort.cloud.openmarket.review.entity.ReviewCount;
import oort.cloud.openmarket.review.repository.ReviewCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewCountServiceTest {
    @Mock
    ReviewCountRepository reviewCountRepository;

    @InjectMocks
    ReviewCountService reviewCountService;

    Products product;
    CreateReviewRequest request;

    @BeforeEach
    void init(){
        product = Products.create("test", null, "test", 10000, 10000);
        ReflectionTestUtils.setField(product, "productId", 1L);
        request = new CreateReviewRequest(1L, "test", 5);
    }

    @Test
    void increase(){
        when(reviewCountRepository.findByProductId(product.getProductId()))
                .thenReturn(Optional.empty());

        reviewCountService.increaseCount(product, request);

        ArgumentCaptor<ReviewCount> captor = ArgumentCaptor.forClass(ReviewCount.class);
        verify(reviewCountRepository).save(captor.capture());

        ReviewCount saved = captor.getValue();
        assertThat(saved.getProductId()).isEqualTo(product.getProductId());
        assertThat(saved.getCount()).isEqualTo(1);
        assertThat(saved.getRatingSum()).isEqualTo(request.getRating());
    }

    @Test
    void exist_increase_CountAndRatingSum(){
        ReviewCount exist = ReviewCount.create(product.getProductId(), 2, 10L);

        when(reviewCountRepository.findByProductId(product.getProductId()))
                .thenReturn(Optional.of(exist));

        reviewCountService.increaseCount(product, request);

        verify(reviewCountRepository).save(exist);
        assertThat(exist.getProductId()).isEqualTo(product.getProductId());
        assertThat(exist.getCount()).isEqualTo(3);
        assertThat(exist.getRatingSum()).isEqualTo(15L);
    }

    @Test
    void decrease_countAndRating(){
        ReviewCount exist = ReviewCount.create(product.getProductId(), 2, 10L);

        when(reviewCountRepository.findByProductId(product.getProductId()))
                .thenReturn(Optional.of(exist));

        reviewCountService.decreaseCount(product, request);

        verify(reviewCountRepository).save(exist);
        assertThat(exist.getProductId()).isEqualTo(product.getProductId());
        assertThat(exist.getCount()).isEqualTo(1);
        assertThat(exist.getRatingSum()).isEqualTo(5L);
    }

    @Test
    void increase_rating(){
        Review review = mock(Review.class);
        when(review.getRating()).thenReturn(3);
        when(review.getProduct()).thenReturn(product);

        ReviewCount exist = ReviewCount.create(product.getProductId(), 2, 10L);
        when(reviewCountRepository.findByProductId(product.getProductId()))
                .thenReturn(Optional.of(exist));

        reviewCountService.updateRating(review, 5);

        assertThat(exist.getRatingSum()).isEqualTo(12);
    }

    @Test
    void decrease_rating(){
        Review review = mock(Review.class);
        when(review.getRating()).thenReturn(5);
        when(review.getProduct()).thenReturn(product);

        ReviewCount exist = ReviewCount.create(product.getProductId(), 2, 10L);
        when(reviewCountRepository.findByProductId(product.getProductId()))
                .thenReturn(Optional.of(exist));

        reviewCountService.updateRating(review, 2);

        assertThat(exist.getRatingSum()).isEqualTo(7);
    }
}