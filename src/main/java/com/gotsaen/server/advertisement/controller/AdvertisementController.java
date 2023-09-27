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
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Advertisement> postAdvertisement(Authentication authentication, @Valid @RequestBody AdvertisementDto.Post requestBody) {
        Advertisement advertisement = advertisementMapper.advertisementPostToAdvertisement(requestBody);

        Advertisement createdAdvertisement = advertisementService.createAdvertisement(authentication.getPrincipal().toString(), advertisement);
        URI location = UriCreator.createUri(ADVERTISEMENT_DEFAULT_URL, createdAdvertisement.getAdvertisementId());

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{advertisementId}")
    public ResponseEntity<?> getAdvertisement(Authentication authentication, @PathVariable Long advertisementId) {
        try {
            AdvertisementResponseDto advertisementResponseDto = advertisementService.getAdvertisement(authentication.getPrincipal().toString(), advertisementId);
            return ResponseEntity.ok(advertisementResponseDto);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PatchMapping("/{advertisementId}")
    public ResponseEntity<?> updateAdvertisement(
            Authentication authentication,
            @PathVariable Long advertisementId,
            @RequestBody AdvertisementUpdateDto updateDto) {
        try {
            Advertisement updatedAdvertisement = advertisementService.updateAdvertisement(authentication.getPrincipal().toString(), advertisementId, updateDto);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());// 적절한 응답 상태 및 내용으로 변경
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

    @DeleteMapping("/{advertisementId}")
    public ResponseEntity<?> deleteAdvertisement(Authentication authentication, @PathVariable Long advertisementId) {
        try {
            advertisementService.deleteAdvertisement(authentication.getPrincipal().toString(), advertisementId);
            return ResponseEntity.noContent().build();
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{advertisementId}/complete-recruitment")
    public ResponseEntity<?> completeRecruitment(Authentication authentication, @PathVariable Long advertisementId) {
        try {
            Advertisement updatedAdvertisement = advertisementService.updateAdvertisementStatus(authentication.getPrincipal().toString(), advertisementId);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

//    @GetMapping("/update")
//    public ResponseEntity<?> updateStatus() {
//        Advertisement updatedAdvertisement = advertisementService.updateAdvertisementStatus();
//        return ResponseEntity.ok(updatedAdvertisement);
//    }
}