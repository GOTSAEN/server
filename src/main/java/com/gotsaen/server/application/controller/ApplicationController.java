package com.gotsaen.server.application.controller;


import com.gotsaen.server.application.dto.ApplicationDto;
import com.gotsaen.server.application.dto.ApplicationUpdateDto;
import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.application.service.ApplicationService;
import com.gotsaen.server.auth.service.OAuth2MemberService;
import com.gotsaen.server.dto.MultiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/applications")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final OAuth2MemberService oAuth2MemberService;

    @PostMapping
    public ResponseEntity<Boolean> postApplication(@Valid @RequestBody ApplicationDto requestBody, Authentication authentication){
        oAuth2MemberService.checkYoutuber(authentication);
        boolean check = applicationService.createOrDeleteApplication(requestBody, authentication.getPrincipal().toString());
        return ResponseEntity.ok(check);
    }

    @GetMapping
    public ResponseEntity<MultiResponseDto> getApplicationByStatus(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "1000") int size,
            @RequestParam Application.Status status){
        MultiResponseDto applications = applicationService.findByStatus(status, page, size);

        return new ResponseEntity<>(applications,HttpStatus.OK);
    }

    @PatchMapping("/{applicationId}")
    public ResponseEntity updateApplicationStatus(Authentication authentication,
                                                  @PathVariable Long applicationId,
                                                  @RequestBody ApplicationUpdateDto updateDto){
        Application updatedApplication = applicationService.updateApplication(authentication.getPrincipal().toString(), applicationId, updateDto);
        return new ResponseEntity<>(updatedApplication,HttpStatus.OK);
    }
    @GetMapping("/{advertisementId}")
    public ResponseEntity<Boolean> checkAdvertisementApplication(@PathVariable Long advertisementId, Authentication authentication){
        boolean check = applicationService.checkAdvertisementApplication(advertisementId, authentication.getPrincipal().toString());
        return ResponseEntity.ok(check);
    }
}
