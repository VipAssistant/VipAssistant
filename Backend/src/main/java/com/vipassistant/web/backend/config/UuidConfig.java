package com.vipassistant.web.backend.config;

import com.vipassistant.web.backend.constant.APIConstants;
import com.vipassistant.web.backend.entity.Beacon;
import com.vipassistant.web.backend.repository.BeaconRepository;
import com.vipassistant.web.backend.service.BeaconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@EnableScheduling
@Configuration
public class UuidConfig {

	@Autowired
	private BeaconService beaconService;

	@Autowired
	private BeaconRepository beaconRepository;

	/**
	 * Method that is responsible for periodic (per 10 seconds)
	 * refresh of beacon UUIDs generated with the constant random seed UUID_SEED
	 */
	@Transactional
	@Scheduled(fixedRate = 10000)
	public void refreshUUIDs() {
		List<Beacon> beacons = beaconRepository.findAll();
		for (Beacon beacon : beacons) {
			UUID newUUID = beaconService.generateUuid(APIConstants.UUID_SEED);
			beacon.setUUID(newUUID);
			beaconRepository.save(beacon);
		}
	}
}
