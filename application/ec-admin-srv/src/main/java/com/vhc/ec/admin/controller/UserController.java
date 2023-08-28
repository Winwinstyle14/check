package com.vhc.ec.admin.controller;

import com.vhc.ec.admin.dto.*;
import com.vhc.ec.admin.service.AuthService;
import com.vhc.ec.admin.service.UserService;
import com.vhc.ec.api.versioning.ApiVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@ApiVersion("1")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private final AuthService authService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserViewDto signUp(@Valid @RequestBody SaveUserDto regUserDto) {
        return userService.create(regUserDto);
    }

    @PutMapping("/{id}")
    public UserViewDto edit(@PathVariable long id, @Valid @RequestBody SaveUserDto adUserDto) {
        return userService.updateUser(id, adUserDto);
    }

    @PostMapping("/auth")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.auth(loginRequest);
    }

    @GetMapping({"/internal/{id}", "/{id}"})
    public UserViewDto getDetail(@PathVariable long id) {
        return userService.getUserDetail(id);
    }

    @GetMapping("/search")
    public PageDto<UserViewDto> search(@RequestParam(required = false) String name,
                                       @RequestParam(required = false) String email,
                                       @RequestParam(required = false) String phone,
                                       Pageable page) {

        return userService.search(name, email, phone, page);
    }
}
