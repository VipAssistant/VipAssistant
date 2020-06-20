package com.vipassistant.web.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/* Note: Lombok's @Builder conflicts with Mapstruct's AfterMapping */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Beacon")
@Table(name = "beacon")
@EntityListeners(AuditingEntityListener.class)
public class Beacon {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Temporal(TemporalType.DATE)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Date createdAt;

	@LastModifiedDate
	@Temporal(TemporalType.DATE)
	@Column(name = "last_modified_at", nullable = false)
	private Date lastModifiedAt;

	@Column(name = "mac_address", length = 255, nullable = false, unique = true)
	private String macAddress;

	@Column(name = "uuid", length = 255, nullable = false, unique = true)
	private UUID UUID;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "location_id", referencedColumnName = "id")
	private Location location;

	@Override
	public String toString() {
		return String.format("BeaconEntityModel with id %d and macAddress %s", id, macAddress);
	}
}
