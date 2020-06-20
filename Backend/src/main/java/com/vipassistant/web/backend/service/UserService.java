package com.vipassistant.web.backend.service;

import com.vipassistant.web.backend.constant.APIConstants;
import com.vipassistant.web.backend.dto.ResponseDTO;
import com.vipassistant.web.backend.dto.UserDTO;
import com.vipassistant.web.backend.entity.User;
import com.vipassistant.web.backend.mapper.UserMapper;
import com.vipassistant.web.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.vipassistant.web.backend.constant.APIConstants.*;

@Slf4j
@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository,
					   UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	/**
	 * Service that returns all users in the db
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> getAllUsers() {
		List<UserDTO> playerDTOList = userMapper.toUserDTOList(userRepository.findAll());
		return Pair.of(HttpStatus.OK, new ResponseDTO(playerDTOList, null, RESPONSE_SUCCESS));
	}

	/**
	 * Service that returns the user specified by id
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> getUserById(Long userId) {
		Optional<User> queryResult = userRepository.findById(userId);
		if (queryResult.isPresent()) {
			UserDTO userDTO = userMapper.toUserDTO(queryResult.get());
			return Pair.of(HttpStatus.OK, new ResponseDTO(userDTO, null, RESPONSE_SUCCESS));
		} else {
			log.warn("User not found with id:{}", userId);
			return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
					String.format("User not found with id:%s", userId), RESPONSE_FAIL));
		}
	}

	/**
	 * Service that handles the login process initiated by the authRequest.
	 * Returns the DTO of logged in player
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> handleLogin(Authentication authRequest) {
		org.springframework.security.core.userdetails.User principal =
				(org.springframework.security.core.userdetails.User) authRequest.getPrincipal();
		Optional<User> user = userRepository.findByUsername(principal.getUsername());
		if (user.isPresent()) {
			UserDTO playerDTO = userMapper.toUserDTO(user.get());
			return Pair.of(HttpStatus.OK, new ResponseDTO(playerDTO, null, RESPONSE_SUCCESS));
		}
		log.warn("User not found with username:{}", principal.getUsername());
		return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
				String.format("User not found with username:%s", principal.getUsername()), RESPONSE_FAIL));
	}

	/**
	 * Service that register the user by the given credentials.
	 * First validates the given credentials then creates a user and saves it. Returns the DTO of created user
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> registerUser(UserDTO userDTO) {
		String validationResult = validateRegister(userDTO);
		if (validationResult.equals("")) {
			if (!userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
				User user = userMapper.toUser(userDTO);
				userRepository.save(user);
				return Pair.of(HttpStatus.OK, new ResponseDTO(null,
						String.format("User is successfully created with username:%s", user.getUsername()), RESPONSE_SUCCESS));
			} else {
				log.warn("Username exists, could not complete user registration for username:{}", userDTO.getUsername());
				return Pair.of(HttpStatus.OK, new ResponseDTO(null,
						String.format("Username already exists.\nPlease choose another and try again",
								userDTO.getUsername()), RESPONSE_FAIL));
			}
		} else {
			return Pair.of(HttpStatus.OK, new ResponseDTO(null,
					String.format("Validation Error on registration.\nFollowing constraints must be met: %s", validationResult),
					RESPONSE_FAIL));
		}
	}

	/**
	 * Helper private method for credential validation of register service
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	private String validateRegister(UserDTO userDTO) {
		StringBuilder result = new StringBuilder();
		if (userDTO != null) {
			if (userDTO.getId() != null)
				result.append("& Can not set ID field of a user");
			if (userDTO.getUsername() == null)
				result.append("& Missing username field");
			if (userDTO.getPassword() == null)
				result.append("& Missing password field");
		} else {
			result.append("Missing user register credentials as a whole");
		}
		return result.toString();
	}

	/**
	 * Service that removes the user specified by id
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> removeUserById(Long userId) {
		Optional<User> queryResult = userRepository.findById(userId);
		if (queryResult.isPresent()) {
			userRepository.delete(queryResult.get());
			return Pair.of(HttpStatus.OK, new ResponseDTO(null,
					String.format("User with id:%s successfully removed", userId), RESPONSE_SUCCESS));
		}
		log.warn("User not found with id:{}", userId);
		return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
				String.format("User not found with id:%s", userId), RESPONSE_FAIL));
	}

	/**
	 * Service that updates the user specified by the id with the given credentials
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> updateUserById(Long userId, UserDTO userDTO) {
		Optional<User> queryResult = userRepository.findById(userId);
		if (queryResult.isEmpty()) {
			log.warn("User not found with id:{}", userId);
			return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
					String.format("User not found with id:%s", userId), RESPONSE_FAIL));
		}

		User user = queryResult.get();

		if (userDTO.getUsername() != null) {
			user.setUsername(userDTO.getUsername());
		}

		if (userDTO.getPassword() != null) {
			user.setPassword(userDTO.getPassword());
		}

		if (userDTO.getTestedPositive() != null) {
			user.setTestedPositive(userDTO.getTestedPositive());
		}

		userRepository.save(user);
		return Pair.of(HttpStatus.OK, new ResponseDTO(null,
				String.format("User with id:%s successfully updated", userId), RESPONSE_SUCCESS));
	}


	public Pair<HttpStatus, ResponseDTO> addSocialDistancingPeople(Long userId, String macAddressOfPerson) {
		Optional<User> queryResult = userRepository.findById(userId);
		if (queryResult.isEmpty()) {
			log.warn("User not found with id:{}", userId);
			return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
					String.format("User not found with id:%s", userId), RESPONSE_FAIL));
		}

		Optional<User> queryResult2 = userRepository.findByMacAddress(macAddressOfPerson);
		if (!queryResult2.isPresent()) {
			log.warn("User to add not found with macAddress:{}", macAddressOfPerson);
			return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
					String.format("User to add not found with macAddress:%s", macAddressOfPerson), RESPONSE_FAIL));
		}

		User user = queryResult.get();
		User userToAdd = queryResult2.get();
		user.getSocialDistanceSet().add(userToAdd);

		userRepository.save(user);
		return Pair.of(HttpStatus.OK, new ResponseDTO(null,
				String.format("User with id:%s's Social Distance People Set has been successfully updated" +
						" with new person with id:%s", userId, userToAdd.getId()), RESPONSE_SUCCESS));
	}

	public Pair<HttpStatus, ResponseDTO> checkUserSocialDistanceSetById(Long userId) {
		Optional<User> queryResult = userRepository.findById(userId);
		if (queryResult.isEmpty()) {
			log.warn("User not found with id:{}", userId);
			return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
					String.format("User not found with id:%s", userId), RESPONSE_FAIL));
		}

		User user = queryResult.get();
		Set<User> socialDistanceSet = user.getSocialDistanceSet();
		for (User hasBeenNearbyUser : socialDistanceSet) {
			if (hasBeenNearbyUser.getTestedPositive()) {
				hasBeenNearbyUser.setTestedPositive(false);
				userRepository.save(hasBeenNearbyUser);
				return Pair.of(HttpStatus.OK, new ResponseDTO(true,
						String.format("One of the users that was nearby User with id:%s has tested positive", userId), RESPONSE_SUCCESS));
			}
		}
		return Pair.of(HttpStatus.OK, new ResponseDTO(false,
				String.format("None of the users that was nearby User with id:%s has tested positive", userId), RESPONSE_SUCCESS));
	}
}


