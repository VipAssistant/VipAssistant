package com.vipassistant.web.backend.mapper;


import com.vipassistant.web.backend.dto.BeaconDTO;
import com.vipassistant.web.backend.entity.Beacon;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface BeaconMapper {
    List<BeaconDTO> toBeaconDTOList(List<Beacon> beaconList);
    BeaconDTO toBeaconDTO(Beacon beacon);
}
