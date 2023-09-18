package com.gotsaen.server.advertisement.mapper;

import com.gotsaen.server.advertisement.dto.AdvertisementDto;
import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdvertisementMapper {
    Advertisement advertisementPostToAdvertisement(AdvertisementDto.Post requestBody);
    AdvertisementResponseDto advertisementToAdvertisementResponse(Advertisement advertisement);
}
