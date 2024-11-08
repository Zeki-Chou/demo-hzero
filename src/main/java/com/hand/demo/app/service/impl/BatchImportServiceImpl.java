package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@ImportService(templateCode = "DEMO-CLIENT-47357")
public class BatchImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private static final String TASK_NUMBER_REGEX = "^[A-Za-z]+$";

    public BatchImportServiceImpl(ObjectMapper objectMapper, TaskRepository taskRepository, UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }


    @Override
    public Boolean doImport(List<String> data) {
        List<Task> taskList = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return Boolean.FALSE;
        }

        AtomicBoolean flag = new AtomicBoolean(true);
        Pattern pattern = Pattern.compile(TASK_NUMBER_REGEX);

        data.forEach(task -> {
            try {
                Task taskObj = objectMapper.readValue(task, Task.class);

                if (userRepository.selectByPrimaryKey(taskObj.getEmployeeId()) != null) {

                    if (pattern.matcher(taskObj.getTaskNumber()).matches()) {
                        taskList.add(taskObj);
                    } else {
                        flag.set(false);
                    }
                } else {
                    flag.set(false);
                }
            } catch (JsonProcessingException e) {
                flag.set(false);
            }
        });

        if (flag.get()) {
            taskRepository.batchInsertSelective(taskList);
        }

        return flag.get();
    }
}


