package com.gotsaen.server.advertisement.controller;

import com.gotsaen.server.advertisement.dto.AdvertisementDto;
import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.dto.AdvertisementUpdateDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.mapper.AdvertisementMapper;
import com.gotsaen.server.advertisement.service.AdvertisementService;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.member.dto.MemberResponseDto;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Advertisement> postAdvertisement(@Valid @RequestBody AdvertisementDto.Post requestBody){
        Advertisement advertisement = advertisementMapper.advertisementPostToAdvertisement(requestBody);

        Advertisement createdAdvertisement = advertisementService.createAdvertisement(advertisement);
        URI location = UriCreator.createUri(ADVERTISEMENT_DEFAULT_URL, createdAdvertisement.getAdvertisementId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{advertisementId}")
    public ResponseEntity<?> updateAdvertisement(@PathVariable Long advertisementId, @RequestBody AdvertisementUpdateDto updateDto) {
        try {
            Advertisement updatedAdvertisement = advertisementService.updateAdvertisement(advertisementId, updateDto);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found");
        }
    }

    @GetMapping("/{advertisementId}")
    public ResponseEntity<?> getAdvertisement(@PathVariable Long advertisementId) {
        try {
            AdvertisementResponseDto getAdvertisement = advertisementService.getAdvertisement(advertisementId);
            return ResponseEntity.ok(getAdvertisement);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found"); // 또는 적절한 응답을 반환
        }
    }
}
