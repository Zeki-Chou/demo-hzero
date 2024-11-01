package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import org.hzero.boot.imported.app.service.ImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@ImportService(templateCode = "DEMO-CLIENT-47358")
public class ImportServiceImpl extends ImportHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;


    @Override
    public Boolean doImport(String data) {
        Task task;
        try {
            task = objectMapper.readValue(data, Task.class);
        } catch (IOException e) {
            return Boolean.FALSE;
        }
        taskRepository.insertSelective(task);
        getContext().addBackInfo(String.valueOf(task.getId()));
        return Boolean.TRUE;
    }
}
