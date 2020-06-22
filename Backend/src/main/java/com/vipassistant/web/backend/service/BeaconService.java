package com.vipassistant.web.backend.service;

import com.vipassistant.web.backend.constant.APIConstants;
import com.vipassistant.web.backend.dto.BeaconDTO;
import com.vipassistant.web.backend.dto.ResponseDTO;
import com.vipassistant.web.backend.dto.UserDTO;
import com.vipassistant.web.backend.entity.Beacon;
import com.vipassistant.web.backend.entity.User;
import com.vipassistant.web.backend.mapper.BeaconMapper;
import com.vipassistant.web.backend.mapper.UserMapper;
import com.vipassistant.web.backend.repository.BeaconRepository;
import com.vipassistant.web.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.vipassistant.web.backend.constant.APIConstants.RESPONSE_FAIL;
import static com.vipassistant.web.backend.constant.APIConstants.RESPONSE_SUCCESS;

@Slf4j
@Service
public class BeaconService {

	private final BeaconRepository beaconRepository;
	private final BeaconMapper beaconMapper;

	public BeaconService(BeaconRepository beaconRepository,
						 BeaconMapper beaconMapper) {
		this.beaconRepository = beaconRepository;
		this.beaconMapper = beaconMapper;
	}

	/**
	 * Service that returns all beacons in the db
	 * with their periodically updated UUIDs
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> getBeaconTable() {
		List<BeaconDTO> beaconDTOList = beaconMapper.toBeaconDTOList(beaconRepository.findAll());
		return Pair.of(HttpStatus.OK, new ResponseDTO(beaconDTOList, null, RESPONSE_SUCCESS));
	}

	/**
	 * Service that returns the beacon specified by id
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> getBeaconById(Long bid) {
		Optional<Beacon> queryResult = beaconRepository.findById(bid);
		if (queryResult.isPresent()) {
			BeaconDTO beaconDTO = beaconMapper.toBeaconDTO(queryResult.get());
			return Pair.of(HttpStatus.OK, new ResponseDTO(beaconDTO, null, RESPONSE_SUCCESS));
		} else {
			log.warn("Beacon not found with id:{}", bid);
			return Pair.of(HttpStatus.NOT_FOUND, new ResponseDTO(null,
					String.format("Beacon not found with id:%s", bid), RESPONSE_FAIL));
		}
	}

	/**
	 * Generates a GUID using a seed.  This will generate the same GUID usign the same seed.
	 * @param seed The seed to use for generating the GUID
	 * @return A string representation of the GUID
	 */
	public UUID generateUuid(String seed) {
		try {
			return UUID.nameUUIDFromBytes(seed.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(String.format("UnsupportedEncodingException: %f", e.getMessage()));
		}
	}

	/**
	 * Service that returns current seed determined by the API
	 * for beacon shuffling UUID values
	 *
	 * @return Pair<HttpStatus, ResponseDTO>
	 */
	public Pair<HttpStatus, ResponseDTO> getUuidSeedValue() {
		return Pair.of(HttpStatus.OK, new ResponseDTO(APIConstants.UUID_SEED, null, RESPONSE_SUCCESS));
	}
}


