package com.gotsaen.server.application.mapper;

import com.gotsaen.server.application.dto.ApplicationAndAdInfoDto;
import com.gotsaen.server.application.dto.ApplicationAndYoutuberInfoDto;
import com.gotsaen.server.application.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapper {

    ApplicationAndAdInfoDto applicationToApplicationAndAdInfoDto(Application application);

    ApplicationAndYoutuberInfoDto applicationToApplicationAndYoutuberInfo(Application application);
}
