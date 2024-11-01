package com.hand.demo.domain.repository;

import com.hand.demo.domain.dto.UserTaskDTO;
import com.hand.demo.infra.mapper.UserMapper;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.User;

import java.util.List;

/**
 * 用户表资源库
 *
 * @author azhar.naufal@hand-global.com 2024-10-17 13:48:26
 */
public interface UserRepository extends BaseRepository<User> {

    List<UserTaskDTO> selectUserTaskByEmpOrTaskNumber(String employeeNumber, String taskNumber);
}
