package com.review.ms.service.imp;

import com.review.ms.dto.ReviewDTO;
import com.review.ms.exception.ReviewException;
import com.review.ms.external.service.IProductServiceFeign;
import com.review.ms.external.service.IUserServiceFeign;
import com.review.ms.model.ReviewEntity;
import com.review.ms.repository.IReviewRepository;
import com.review.ms.service.IReviewService;
import com.review.ms.utils.MessageUtils;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final IReviewRepository iReviewRepository;

    private final IUserServiceFeign iUserClient;

    private final IProductServiceFeign iProductClient;

    @Override
    public ResponseEntity<ReviewEntity> createReview(ReviewDTO reviewDTO) {

        try {
            iUserClient.getUserById(reviewDTO.getUserId());
            iProductClient.getProductById(reviewDTO.getProductId());

            ReviewEntity reviewEntity = toEntity(reviewDTO);

            iReviewRepository.save(reviewEntity);
            return ResponseEntity.ok(reviewEntity);
        } catch (FeignException.NotFound e) {
            throw new ReviewException(MessageUtils.USER_OR_PRODUCT_NOT_FOUND
                    + reviewDTO.getUserId() + " or " + reviewDTO.getProductId());
        }
    }

    @Override
    public ResponseEntity<List<ReviewEntity>> getAllReviews() {
        List<ReviewEntity> reviews = iReviewRepository.findAll();
        return ResponseEntity.ok(reviews);
    }

    @Override
    public ResponseEntity<ReviewEntity> getReviewById(String id) {
        ReviewEntity review = iReviewRepository.findById(id).
                orElseThrow(() -> new ReviewException(MessageUtils.REVIEW_NOT_FOUND + id));
        return ResponseEntity.ok(review);
    }

    @Override
    public ResponseEntity<ReviewEntity> updateReview(String id, ReviewDTO reviewDTO) {
        ReviewEntity review = iReviewRepository.findById(id)
                .orElseThrow(() -> new ReviewException(MessageUtils.REVIEW_NOT_FOUND + id));

        review.setReview(reviewDTO.getReview());
        review.setRating(reviewDTO.getRating());

        iReviewRepository.save(review);
        return ResponseEntity.ok(review);
    }

    @Override
    public ResponseEntity<String> deleteReview(String id) {
        if (!iReviewRepository.existsById(id)) {
            throw new ReviewException(MessageUtils.REVIEW_NOT_FOUND + id);
        }
        iReviewRepository.deleteById(id);
        return ResponseEntity.ok(MessageUtils.REVIEW_DELETED + id);
    }

    private ReviewEntity toEntity(ReviewDTO reviewDTO) {
        return ReviewEntity.builder()
                .id(UUID.randomUUID().toString())
                .productId(reviewDTO.getProductId())
                .userId(reviewDTO.getUserId())
                .review(reviewDTO.getReview())
                .rating(reviewDTO.getRating())
                .build();
    }
}
