package com.gotsaen.server.advertisement.controller;

import com.gotsaen.server.advertisement.dto.AdvertisementDto;
import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.dto.AdvertisementUpdateDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.mapper.AdvertisementMapper;
import com.gotsaen.server.advertisement.service.AdvertisementService;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.dto.PageInfo;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/advertisement")
@Validated
@Slf4j
public class AdvertisementController {
    private final static String ADVERTISEMENT_DEFAULT_URL = "/advertisement";
    private final AdvertisementService advertisementService;
    private final AdvertisementMapper advertisementMapper;

    public AdvertisementController(AdvertisementService advertisementService, AdvertisementMapper advertisementMapper) {
        this.advertisementService = advertisementService;
        this.advertisementMapper = advertisementMapper;
    }

    @PostMapping
    public ResponseEntity<Advertisement> postAdvertisement(@AuthenticationPrincipal String memberEmail, @Valid @RequestBody AdvertisementDto.Post requestBody) {
        Advertisement advertisement = advertisementMapper.advertisementPostToAdvertisement(requestBody);

        Advertisement createdAdvertisement = advertisementService.createAdvertisement(memberEmail, advertisement);
        URI location = UriCreator.createUri(ADVERTISEMENT_DEFAULT_URL, createdAdvertisement.getAdvertisementId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping
    public ResponseEntity<?> updateAdvertisement(@AuthenticationPrincipal String memberEmail, @RequestBody AdvertisementUpdateDto updateDto) {
        try {
            Advertisement updatedAdvertisement = advertisementService.updateAdvertisement(memberEmail, updateDto);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAdvertisement(@AuthenticationPrincipal String memberId) {
        try {
            AdvertisementResponseDto getAdvertisement = advertisementService.getAdvertisement(memberId);
            return ResponseEntity.ok(getAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found"); // 또는 적절한 응답을 반환
        }
    }

    @GetMapping("/allAd")
    public ResponseEntity<MultiResponseDto<AdvertisementSummaryDto>> getAllAdvertisementSummaries(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        MultiResponseDto<AdvertisementSummaryDto> advertisementResponse = advertisementService.getAllAdvertisementSummaries(page, size);

        // 페이지 정보를 헤더에 추가
        PageInfo pageInfo = advertisementResponse.getPageInfo();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Info", pageInfo.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(advertisementResponse);
    }

    @GetMapping("/byCategory")
    public ResponseEntity<MultiResponseDto<AdvertisementSummaryDto>> getAdvertisementsByCategory(
            @RequestParam(name = "category") String category,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        MultiResponseDto<AdvertisementSummaryDto> advertisementResponse = advertisementService.getAdvertisementsByCategory(category, page, size);

        PageInfo pageInfo = advertisementResponse.getPageInfo();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Info", pageInfo.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(advertisementResponse);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAdvertisement(@AuthenticationPrincipal String memberId) {
        try {
            advertisementService.deleteAdvertisement(memberId);
            return ResponseEntity.noContent().build();
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("광고를 찾을 수 없습니다.");
        }
    }
}