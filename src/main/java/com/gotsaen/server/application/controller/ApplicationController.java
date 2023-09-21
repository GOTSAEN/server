package com.gotsaen.server.application.controller;


import com.gotsaen.server.application.dto.ApplicationDto;
import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.application.service.ApplicationService;
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

    @PostMapping
    public ResponseEntity postApplication(@Valid @RequestBody ApplicationDto requestBody, Authentication authentication){
        String email = authentication.getPrincipal().toString();
        applicationService.createOrDeleteBookmark(requestBody, email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<MultiResponseDto> getApplicationByStatus(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "1000") int size,
            @RequestParam Application.Status status){
        MultiResponseDto applications = applicationService.findByStatus(status, page, size);

        return new ResponseEntity<>(applications,HttpStatus.OK);
    }

}
