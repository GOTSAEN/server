package com.gotsaen.server.application.dto;

import com.gotsaen.server.application.entity.Application;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ApplicationUpdateDto {
    private Application.Status status;
    private String youtubeUrl;
}
