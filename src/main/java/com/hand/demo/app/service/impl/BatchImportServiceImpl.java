package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import jdk.nashorn.internal.runtime.Context;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.domain.entity.ImportData;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ImportService(templateCode = "DEMO-CLIENT-47358-2")
public class BatchImportServiceImpl extends BatchImportHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Boolean doImport(List<String> data) {
        boolean result = true;
        List<Task> tasks = new ArrayList<>();
        HashSet<String> userIds = new HashSet<>();
        String regex = "^[a-zA-Z0-9  _-]+$";
        Pattern pattern = Pattern.compile(regex);

//        for (String task: data){
//            try{
//                Task taskData = objectMapper.readValue(task, Task.class);
//                tasks.add(taskData);
//                userIds.add(String.valueOf(taskData.getEmployeeId()));
//
//                User user = userRepository.selectByPrimaryKey(taskData.getEmployeeId());
//                Matcher matcher = pattern.matcher(taskData.getTaskNumber());
//                if (!matcher.matches()) {
//                    result = false;
//                    getContextList().get(0).addErrorMsg("error: " + data);
//                }
//            } catch (Exception e) {
//                result = false;
//            }
//        }

        for (int index = 0; index < data.size(); index++) {
            String task = data.get(index);
            try {
                Task taskData = objectMapper.readValue(task, Task.class);
                tasks.add(taskData);
                userIds.add(String.valueOf(taskData.getEmployeeId()));

                Matcher matcher = pattern.matcher(taskData.getTaskNumber());
                if (!matcher.matches()) {
                    result = false;
                    List<ImportData> contextList = getContextList();
                    if (!contextList.isEmpty() && index < contextList.size()) { // Ensure index is within bounds
                        contextList.get(index).addErrorMsg("error at index " + index + ": " + task); // Add error for the specific index
                    }
                }
            } catch (Exception e) {
                result = false;
            }
        }


        userRepository.selectByIds(String.join(",",userIds));
        taskRepository.batchInsertSelective(tasks);

        return result;
    }
}
