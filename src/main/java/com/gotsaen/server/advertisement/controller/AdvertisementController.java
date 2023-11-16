package com.gotsaen.server.advertisement.controller;

import com.gotsaen.server.advertisement.dto.AdvertisementDto;
import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.dto.AdvertisementUpdateDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.mapper.AdvertisementMapper;
import com.gotsaen.server.advertisement.service.AdvertisementService;
import com.gotsaen.server.advertisement.service.AwsS3UploadService;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.dto.PageInfo;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.service.MemberService;
import com.gotsaen.server.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/advertisement")
@Validated
@Slf4j
@RequiredArgsConstructor
public class AdvertisementController {
    private final static String ADVERTISEMENT_DEFAULT_URL = "/advertisement";
    private final AdvertisementService advertisementService;
    private final AdvertisementMapper advertisementMapper;
    private final AwsS3UploadService awsS3UploadService;

    @PostMapping
    public ResponseEntity<Long> postAdvertisement(Authentication authentication, @Valid @RequestBody AdvertisementDto.Post requestBody) {
        Advertisement advertisement = advertisementMapper.advertisementPostToAdvertisement(requestBody);

        Advertisement createdAdvertisement = advertisementService.createAdvertisement(authentication.getPrincipal().toString(), advertisement);
        URI location = UriCreator.createUri(ADVERTISEMENT_DEFAULT_URL, createdAdvertisement.getAdvertisementId());

        // Advertisement ID만 반환
        Long advertisementId = createdAdvertisement.getAdvertisementId();

        return ResponseEntity.created(location).body(advertisementId);
    }


    @PostMapping(value="/upload/{advertisementId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadMultipleImages(@RequestParam("file") List<MultipartFile> files, @PathVariable Long advertisementId) {
        try {
            // 다중 이미지를 업로드하고 이미지 URL 목록을 받아옴.
            List<String> imageUrls = awsS3UploadService.uploadImages(files);
            for (String imageUrl : imageUrls) {
                advertisementService.saveImageUrl(advertisementId, imageUrl);
            }
            return ResponseEntity.ok(imageUrls);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    @GetMapping("/{advertisementId}")
    public ResponseEntity<?> getAdvertisement(@PathVariable Long advertisementId) {
        try {
            AdvertisementResponseDto advertisementResponseDto = advertisementService.getAdvertisement(advertisementId);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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

    @GetMapping("/byStatus")
    public ResponseEntity<MultiResponseDto<AdvertisementSummaryDto>> getAdvertisementsByStatus(
            @RequestParam(name = "status") Advertisement.Status status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        MultiResponseDto<AdvertisementSummaryDto> advertisementResponse = advertisementService.getAdvertisementsByStatus(status, page, size);

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

    @PatchMapping("/{advertisementId}/progressAd")
    public ResponseEntity<?> waitingToProgress(Authentication authentication, @PathVariable Long advertisementId) {
        try {
            Advertisement updatedAdvertisement = advertisementService.waitingToProgress(authentication.getPrincipal().toString(), advertisementId);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{advertisementId}/finishAd")
    public ResponseEntity<?> progressToFinished(Authentication authentication, @PathVariable Long advertisementId) {
        try {
            Advertisement updatedAdvertisement = advertisementService.progressToFinished(authentication.getPrincipal().toString(), advertisementId);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}