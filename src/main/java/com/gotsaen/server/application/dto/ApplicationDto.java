package com.gotsaen.server.application.dto;

import lombok.Getter;

import javax.persistence.Column;

@Getter
public class ApplicationDto {
    private Long memberId;
    private Long advertisementId;
}
