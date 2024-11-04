package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ImportService(templateCode = "DEMO-CLIENT-47359")
public class DemoImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;
    private final TaskRepository taskRepository;
    private final UserService userService;

    @Autowired
    public DemoImportServiceImpl(ObjectMapper objectMapper, TaskRepository taskRepository, UserService userService) {
        this.objectMapper = objectMapper;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    private Boolean validateTask(Task task) {
        // task number only accept english letters
        Pattern patternTaskNumber = Pattern.compile("^[a-zA-Z]+$");
        Matcher matcherTaskNumber = patternTaskNumber.matcher(task.getTaskNumber());

        boolean taskNumberValidated = matcherTaskNumber.matches();
        boolean userExist = userService.detail(task.getTenantId(), task.getEmployeeId()) != null;

        return userExist && taskNumberValidated;
    }

    @Override
    public Boolean doImport(List<String> data) {
        if (data == null || data.isEmpty()) {
            return Boolean.FALSE;
        }

        List<Task> tasks = new ArrayList<>();

        for (String s: data) {
            try {
                Task task = objectMapper.readValue(s, Task.class);
                if (validateTask(task)) {
                    tasks.add(task);
                }
            } catch (JsonProcessingException e) {
                getContextList().get(0).addErrorMsg("Error Reading JSON Context");
                return Boolean.FALSE;
            }
        }

        taskRepository.batchInsert(tasks);
        return Boolean.TRUE;
    }
}
