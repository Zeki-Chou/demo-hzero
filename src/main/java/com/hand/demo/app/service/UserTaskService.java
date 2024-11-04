package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.UserTaskInfoDTO;
import com.hand.demo.domain.entity.Task;

import java.util.List;

public interface UserTaskService {
    UserTaskInfoDTO findUserTaskInfo(Long id);
    List<UserTaskInfoDTO> findList(UserTaskInfoDTO dto);
}
