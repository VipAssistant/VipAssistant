package com.vipassistant.web.backend.controller;

import com.vipassistant.web.backend.dto.ResponseDTO;
import com.vipassistant.web.backend.dto.UserDTO;
import com.vipassistant.web.backend.service.BeaconService;
import com.vipassistant.web.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.vipassistant.web.backend.constant.APIConstants.RESPONSE_FAIL;

@Slf4j
@RestController
@RequestMapping(value = "/api/beacons", headers = "Accept=application/json")
public class BeaconController {

	private final BeaconService beaconService;

	public BeaconController(BeaconService beaconService) {
		this.beaconService = beaconService;
	}

	@GetMapping
	public ResponseEntity<ResponseDTO> getBeaconTable() {
		Pair<HttpStatus, ResponseDTO> response = beaconService.getBeaconTable();
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@GetMapping("/{bid}")
	public ResponseEntity<ResponseDTO> getBeaconById(@PathVariable Long bid) {
		Pair<HttpStatus, ResponseDTO> response = beaconService.getBeaconById(bid);
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@GetMapping("/seed")
	public ResponseEntity<ResponseDTO> getUuidSeed() {
		Pair<HttpStatus, ResponseDTO> response = beaconService.getUuidSeedValue();
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}
}

