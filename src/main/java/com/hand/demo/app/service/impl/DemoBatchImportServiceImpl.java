package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.job.UserJob;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@ImportService(templateCode = "DEMO-47355-CLIENT")
public class DemoBatchImportServiceImpl extends BatchImportHandler {
    private static Logger LOG = LoggerFactory.getLogger(DemoBatchImportServiceImpl.class);

    private ObjectMapper objectMapper;
    private TaskRepository taskRepository;
    private UserRepository userRepository;

    @Override
    public Boolean doImport(List<String> data) {
        LOG.info("Hello brohhh: " + data);

        List<Task> taskList = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return Boolean.FALSE;
        }

        AtomicBoolean flag = new AtomicBoolean(true);
        for(int i = 0; i < data.size(); i++) {
            try {
                Task currTask = objectMapper.readValue(data.get(i), Task.class);
                String regex = "^[a-zA-Z]+$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(currTask.getTaskNumber());

                User user = userRepository.selectByPrimaryKey(currTask.getEmployeeId());

                boolean isMatch = matcher.matches();

                if(isMatch && user != null) {
                    taskList.add(currTask);
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
