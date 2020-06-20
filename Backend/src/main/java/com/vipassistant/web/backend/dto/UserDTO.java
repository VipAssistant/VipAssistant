package com.vipassistant.web.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
	private Long id;
	private String username;
	private String password;
	private String macAddress;
	private Boolean testedPositive;
}
