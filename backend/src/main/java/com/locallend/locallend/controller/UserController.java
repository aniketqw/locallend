package com.locallend.locallend.controller;

import com.locallend.locallend.dto.request.UserUpdateDto;
import com.locallend.locallend.dto.response.UserPublicDto;
import com.locallend.locallend.dto.response.UserResponseDto;
import com.locallend.locallend.exception.BusinessException;
import com.locallend.locallend.model.User;
import com.locallend.locallend.service.UserQueryService;
import com.locallend.locallend.service.UserService;
import com.locallend.locallend.util.SecurityUtils;
import com.locallend.locallend.util.UserMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 
* REST controller to manage authenticated user operations and public user queries.
* Handles profile management, public info lookup, and user search.
**/

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserQueryService userQueryService;

	@Autowired
	private UserMapper userMapper;

	/**
	 * Get current user's profile.
	 * Extracts user ID from JWT token in SecurityContext.
	 * 
	 * @return Full user profile
	 */
	@GetMapping("/profile")
	public ResponseEntity<UserResponseDto> getProfile() {
		String userId = SecurityUtils.getCurrentUserId();
		User user = userQueryService.findById(userId)
				.orElseThrow(() -> new BusinessException("User not found"));
		return ResponseEntity.ok(userMapper.toUserResponseDto(user));
	}

	/**
	 * Update current user's profile.
	 * Extracts user ID from JWT token in SecurityContext.
	 * 
	 * @param updateDto Profile update data
	 * @return Updated user profile
	 */
	@PutMapping("/profile")
	public ResponseEntity<UserResponseDto> updateProfile(@Valid @RequestBody UserUpdateDto updateDto) {
		String userId = SecurityUtils.getCurrentUserId();
		User updated = userService.updateProfile(userId,
				updateDto.getName(),
				updateDto.getPhoneNumber(),
				updateDto.getProfileImageUrl());
		return ResponseEntity.ok(userMapper.toUserResponseDto(updated));
	}

	/**
	 * Get public information about any user.
	 * Does not expose sensitive information like email or phone.
	 * 
	 * @param id User ID to lookup
	 * @return Public user information
	 */
	@GetMapping("/{id}/public")
	public ResponseEntity<UserPublicDto> getPublicInfo(@PathVariable String id) {
		User user = userQueryService.findById(id)
				.orElseThrow(() -> new BusinessException("User not found"));
		return ResponseEntity.ok(userMapper.toUserPublicDto(user));
	}

	/**
	 * Search active users by name or username.
	 * Returns paginated results with only public information.
	 * 
	 * @param term Search term (case-insensitive)
	 * @param page Page number (default: 0)
	 * @param size Page size (default: 10)
	 * @return Page of matching users (public info only)
	 */
	@GetMapping("/search")
	public ResponseEntity<Page<UserPublicDto>> search(
			@RequestParam String term,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Page<User> users = userQueryService.searchActiveUsers(term, page, size);
		Page<UserPublicDto> result = users.map(userMapper::toUserPublicDto);
		return ResponseEntity.ok(result);
	}
}

