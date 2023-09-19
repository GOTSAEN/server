package com.gotsaen.server.advertisement.mapper;

import com.gotsaen.server.advertisement.dto.AdvertisementDto;
import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.dto.PageInfo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdvertisementMapper {
    Advertisement advertisementPostToAdvertisement(AdvertisementDto.Post requestBody);
    AdvertisementResponseDto advertisementToAdvertisementResponse(Advertisement advertisement);
    AdvertisementSummaryDto advertisementToAdvertisementSummaryDto(Advertisement advertisement);
}
