package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import org.hzero.boot.imported.app.service.ImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@ImportService(templateCode = "DEMO-CLIENT-47361")
public class ImportServiceImpl extends ImportHandler {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Boolean doImport(String data) {
        try {
            Task task = objectMapper.readValue(data, Task.class);
            List<Long> userIdS = userRepository.selectAll()
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            if (!userIdS.contains(task.getEmployeeId())) {
                getContext().addErrorMsg("Employee ID not found!");
                return Boolean.FALSE;
            }

            if (!task.getTaskNumber().matches("^[a-zA-Z]+$")) {
                getContext().addErrorMsg("Task_Number must be English letters only!");
                return Boolean.FALSE;
            }

            taskRepository.insertSelective(task);
            getContext().addBackInfo(String.valueOf(task.getId()));
            return Boolean.TRUE;

        } catch (IOException e) {
            getContext().addErrorMsg("Data Import Error, Check the File!");
            return Boolean.FALSE;
        }
    }

    //    public Boolean doImport(String data) {
//        Task task;
//        List<User> users = userRepository.selectAll();
//        List<Long> userIdS = new LinkedList<>();
//        for (User user : users) {
//            userIdS.add(user.getId());
//        }
//
//        try {
//            task = objectMapper.readValue(data, Task.class);
//
//            boolean employeeIdMatched = false; // Menyimpan status kecocokan employeeId
//
//            for (long id : userIdS) {
//                if (task.getEmployeeId() == id) {
//                    employeeIdMatched = true; // Jika cocok, set ke true
//
//                    if (task.getTaskNumber().matches("^[a-zA-Z]+$")) {
//                        taskRepository.insertSelective(task);
//                        getContext().addBackInfo(String.valueOf(task.getId()));
//                        return Boolean.TRUE;
//                    } else {
//                        getContext().addErrorMsg("Task_Number is English Letter!");
//                        return Boolean.FALSE;
//                    }
//                }
//            }
//
//            // Jika tidak ada yang cocok dengan employeeId
//            if (!employeeIdMatched) {
//                getContext().addErrorMsg("Employee ID not found!");
//                return Boolean.FALSE;
//            }
//
//        } catch (IOException e) {
//            getContext().addErrorMsg("Data Import Error, Check the File!");
//            return Boolean.FALSE;
//        }
//
//        return Boolean.FALSE; // Kembali jika tidak ada kondisi yang terpenuhi
//    }
}
