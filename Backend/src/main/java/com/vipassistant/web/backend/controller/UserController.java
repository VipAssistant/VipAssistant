package com.vipassistant.web.backend.controller;

import com.vipassistant.web.backend.constant.APIConstants;
import com.vipassistant.web.backend.dto.ResponseDTO;
import com.vipassistant.web.backend.dto.UserDTO;
import com.vipassistant.web.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.vipassistant.web.backend.constant.APIConstants.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/users", headers = "Accept=application/json")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<ResponseDTO> getAllUsers() {
		Pair<HttpStatus, ResponseDTO> response = userService.getAllUsers();
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@GetMapping("/{userId}")
	public ResponseEntity<ResponseDTO> getUserById(@PathVariable Long userId) {
		Pair<HttpStatus, ResponseDTO> response = userService.getUserById(userId);
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@PostMapping(value = "/login")
	public ResponseEntity<ResponseDTO> login(Authentication authRequest) {
		if (authRequest != null) {
			Pair<HttpStatus, ResponseDTO> response = userService.handleLogin(authRequest);
			return ResponseEntity.status(response.getFirst()).body(response.getSecond());
		} else {
			log.warn("BAD REQUEST on login - missing Authorization Header in the request.");
			return ResponseEntity.badRequest().body(new ResponseDTO(null,
					"Missing Authorization Header in the request.", RESPONSE_FAIL));
		}
	}

	@PostMapping(value = "/register")
	public ResponseEntity<ResponseDTO> registerUser(@RequestBody UserDTO newUserDTO) {
		Pair<HttpStatus, ResponseDTO> response = userService.registerUser(newUserDTO);
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<ResponseDTO> removeUserById(@PathVariable("userId") Long userId) {
		Pair<HttpStatus, ResponseDTO> response = userService.removeUserById(userId);
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@PutMapping("/{userId}")
	public ResponseEntity<ResponseDTO> updateUserById(@PathVariable("userId") Long userId,
														@RequestBody UserDTO newUserDTO) {
		Pair<HttpStatus, ResponseDTO> response = userService.updateUserById(userId, newUserDTO);
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@PutMapping("/addToSocialDistancingList/{userId}")
	public ResponseEntity<ResponseDTO> addSocialDistancingPeople(@PathVariable("userId") Long userId,
																 @RequestBody String macAddressOfPerson) {
		Pair<HttpStatus, ResponseDTO> response = userService.addSocialDistancingPeople(userId, macAddressOfPerson);
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}

	@GetMapping("/checkSocialDistancingList/{userId}")
	public ResponseEntity<ResponseDTO> checkUserSocialDistanceSetById(@PathVariable Long userId) {
		Pair<HttpStatus, ResponseDTO> response = userService.checkUserSocialDistanceSetById(userId);
		return ResponseEntity.status(response.getFirst()).body(response.getSecond());
	}
}

