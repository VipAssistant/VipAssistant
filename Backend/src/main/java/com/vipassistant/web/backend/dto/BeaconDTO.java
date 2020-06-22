package com.vipassistant.web.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeaconDTO {
	private String uuid;
	private String macAddress;
	private String location;
}
