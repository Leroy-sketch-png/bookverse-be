package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.CuratedCollection.CuratedCollectionDetailResponse;
import com.example.bookverseserver.dto.response.CuratedCollection.CuratedCollectionSummaryResponse;
import com.example.bookverseserver.entity.Product.CuratedCollection;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.CuratedCollectionMapper;
import com.example.bookverseserver.repository.CuratedCollectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuratedCollectionService {

    private final CuratedCollectionRepository curatedCollectionRepository;
    private final CuratedCollectionMapper curatedCollectionMapper;

    @Transactional(readOnly = true)
    public ApiResponse<List<CuratedCollectionSummaryResponse>> getAllCollections() {
        log.info("Fetching all curated collections");

        List<CuratedCollection> collections = curatedCollectionRepository.findAllByOrderByCreatedAtDesc();
        List<CuratedCollectionSummaryResponse> responses = curatedCollectionMapper.toSummaryResponseList(collections);

        log.info("Found {} curated collections", collections.size());

        return ApiResponse.<List<CuratedCollectionSummaryResponse>>builder()
                .message("ok")
                .result(responses)
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<CuratedCollectionDetailResponse> getCollectionBySlug(String slug) {
        log.info("Fetching curated collection with slug: {}", slug);

        CuratedCollection collection = curatedCollectionRepository.findBySlugWithBooks(slug)
                .orElseThrow(() -> {
                    log.warn("Curated collection not found with slug: {}", slug);
                    return new AppException(ErrorCode.COLLECTION_NOT_FOUND);
                });

        CuratedCollectionDetailResponse response = curatedCollectionMapper.toDetailResponse(collection);

        log.info("Found curated collection '{}' with {} books", collection.getName(), collection.getTotalBooks());

        return ApiResponse.<CuratedCollectionDetailResponse>builder()
                .message("ok")
                .result(response)
                .build();
    }
}
