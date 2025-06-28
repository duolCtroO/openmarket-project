package oort.cloud.openmarket.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.entity.Review;
import oort.cloud.openmarket.review.entity.ReviewCount;
import oort.cloud.openmarket.review.repository.ReviewCountRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewCountService {
    private final ReviewCountRepository reviewCountRepository;

    @Transactional
    public void increaseCount(Products product, CreateReviewRequest request){
        ReviewCount reviewCount = reviewCountRepository.findByProductId(product.getProductId())
                .orElse(ReviewCount.create(
                        product.getProductId(),
                        0,
                        0L
                ));

        reviewCount.increaseCount();
        reviewCount.increaseRatingSum(request.getRating());

        reviewCountRepository.save(reviewCount);
    }

    @Transactional
    public void decreaseCount(Products product, CreateReviewRequest request){
        ReviewCount reviewCount = findByProductId(product);

        reviewCount.decreaseCount();
        reviewCount.decreaseRatingSum(request.getRating());

        reviewCountRepository.save(reviewCount);
    }

    private ReviewCount findByProductId(Products product) {
        return reviewCountRepository.findByProductId(product.getProductId())
                .orElseThrow(() -> new NotFoundResourceException("저장된 리뷰가 없습니다."));
    }

    @Transactional
    public void updateRating(Review review, int updateRating) {
        ReviewCount reviewCount = findByProductId(review.getProduct());

        if(review.getRating() < updateRating){
            reviewCount.increaseRatingSum(updateRating - review.getRating() );
        } else if (review.getRating() > updateRating) {
            reviewCount.decreaseRatingSum(review.getRating() - updateRating);
        }
    }
}
