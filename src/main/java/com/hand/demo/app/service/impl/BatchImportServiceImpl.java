package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.repository.UserRepository;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;



@ImportService(templateCode = "DEMO-CLIENT-47361-BATCH")
public class BatchImportServiceImpl extends BatchImportHandler {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Boolean doImport(List<String> data) {
        List<Task> taskList = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return Boolean.FALSE;
        }

        AtomicBoolean flag = new AtomicBoolean(true);
        for(int i = 0; i < data.size(); i++) {
            try {
                Task currentTask = objectMapper.readValue(data.get(i), Task.class);
                String regex = "^[a-zA-Z]+$";

                User user = userRepository.selectByPrimaryKey(currentTask.getEmployeeId());

                boolean isMatch = currentTask.getTaskNumber().matches(regex);

                if(isMatch && user != null) {
                    taskList.add(currentTask);
                } else {
                    if (user == null) setMessage(i, "Employee ID not found");
                    else if (!isMatch) setMessage(i, "Task number must be English Letter");
                    flag.set(false);
                }

            } catch (JsonProcessingException e) {
                setMessage(i, e.getMessage());
                flag.set(false);
            }
        }
        taskRepository.batchInsertSelective(taskList);
        return flag.get();
    }

    private void setMessage(int index, String message) {
        getContextList().get(index).setBackInfo(message);
    }
}
