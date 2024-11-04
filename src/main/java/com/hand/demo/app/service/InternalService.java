package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.InternalUserDTO;

import java.util.List;

public interface InternalService {
    List<InternalUserDTO> saveUser(List<InternalUserDTO> users);
}
