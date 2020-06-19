package com.vipassistant.web.backend.entity;

import com.sun.istack.NotNull;
import com.vipassistant.web.backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/* Note: Lombok's @Builder conflicts with Mapstruct's AfterMapping */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "User")
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
public class User {
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

	@Column(name = "username", length = 255, nullable = false, unique = true)
	private String username;

	/* Password will be kept as hashed string into db */
	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "mac_address", length = 255, nullable = false, unique = true)
	private String macAddress;

	@Column(name = "tested_positive", nullable = false)
	private Boolean testedPositive;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private UserRole role;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<User> socialDistanceSet = new HashSet<>();

	@Override
	public String toString() {
		return String.format("UserEntityModel with id %d and username %s", id, username);
	}
}
