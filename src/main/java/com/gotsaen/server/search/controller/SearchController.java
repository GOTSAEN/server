package com.gotsaen.server.search.controller;

import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.dto.PageInfo;
import com.gotsaen.server.search.dto.SearchAdvertisementDto;
import com.gotsaen.server.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/advertisement")
    public ResponseEntity<MultiResponseDto<SearchAdvertisementDto>> searchAdvertisement(@RequestParam String keyword,
                                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                                      @RequestParam(name = "size", defaultValue = "10") int size) {
        MultiResponseDto<SearchAdvertisementDto> searchedAdvertisement = searchService.searchAdvertisement(keyword, page, size);

        PageInfo pageInfo = searchedAdvertisement.getPageInfo();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Info", pageInfo.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(searchedAdvertisement);
    }
}