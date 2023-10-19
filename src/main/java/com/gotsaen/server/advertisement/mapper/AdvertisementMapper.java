package com.gotsaen.server.advertisement.mapper;

import com.gotsaen.server.advertisement.dto.*;
import com.gotsaen.server.advertisement.entity.Advertisement;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdvertisementMapper {
    Advertisement advertisementPostToAdvertisement(AdvertisementDto.Post requestBody);
    AdvertisementResponseDto advertisementToAdvertisementResponse(Advertisement advertisement);
    AdvertisementSummaryDto advertisementToAdvertisementSummaryDto(Advertisement advertisement);
    Advertisement advertisementUpdateToAdvertisement(AdvertisementUpdateDto requestBody);
    AdvertisementByStatusDto advertisementToAdvertisementByStatusDto(Advertisement advertisement);
}
