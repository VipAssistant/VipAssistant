package com.vipassistant.web.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/* Note: Lombok's @Builder conflicts with Mapstruct's AfterMapping */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Location")
@Table(name = "location")
@EntityListeners(AuditingEntityListener.class)
public class Location {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 255, nullable = false, unique = true)
	private String name;

	@Column(name = "type", length = 255, nullable = false)
	private String type;

	@Column(name = "location", length = 255, nullable = false)
	private String location;

	@Column(name = "locEpsLat", length = 255, nullable = false)
	private Double locEpsLat;

	@Column(name = "locEpsLong", length = 255, nullable = false)
	private Double locEpsLong;

	@Column(name = "floor", length = 255)
	private Integer floor; // May be null if location is in outdoor

	@Column(name = "indoorMapId", length = 255)
	private String indoorMapId; // May be null if location is in outdoor

	@Override
	public String toString() {
		return String.format("LocationEntityModel with id %d and name %s", id, name);
	}
}
