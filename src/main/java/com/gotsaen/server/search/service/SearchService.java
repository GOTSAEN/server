package com.gotsaen.server.search.service;

import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.search.dto.SearchAdvertisementDto;
import com.gotsaen.server.search.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final AdvertisementRepository advertisementRepository;
    private final SearchMapper searchMapper;

    @Transactional(readOnly = true)
    public MultiResponseDto<SearchAdvertisementDto> searchAdvertisement(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> searchPage = advertisementRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

        List<SearchAdvertisementDto> searchResults = searchPage.getContent().stream()
                .map(advertisement -> {
                    SearchAdvertisementDto searchAdvertisementDto = searchMapper.advertisementToSearchAdvertisementDto(advertisement);

                    // 이미지 URL 설정
                    if (!advertisement.getImageUrlList().isEmpty()) {
                        searchAdvertisementDto.setImageUrl(advertisement.getImageUrlList().get(0));
                    }

                    return searchAdvertisementDto;
                })
                .collect(Collectors.toList());

        return new MultiResponseDto<>(searchResults, searchPage);
    }
}
