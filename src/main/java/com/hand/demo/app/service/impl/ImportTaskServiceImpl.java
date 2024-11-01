package com.hand.demo.app.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

@ImportService(templateCode = "DEMO-CLIENT-47360")
public class ImportTaskServiceImpl extends BatchImportHandler {
    private final ObjectMapper objectMapper;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public ImportTaskServiceImpl (ObjectMapper objectMapper, TaskRepository taskRepository,UserRepository userRepository){
        this.objectMapper=objectMapper;
        this.taskRepository= taskRepository;
        this.userRepository=userRepository;
    }

    @Override
    public Boolean doImport(List<String> data) {

        String regex = "^[A-Za-z0-9]+$";
        Pattern pattern = Pattern.compile(regex);

        List<Task> tasks = new ArrayList<>();
        HashSet<String> userIds = new HashSet<>();
        for (String singleData:data) {
            try {
                Task task = objectMapper.readValue(singleData, Task.class);
                if (!pattern.matcher(task.getTaskNumber()).matches()) {
                    return Boolean.FALSE;
                }
                tasks.add(task);
                userIds.add(task.getEmployeeId().toString());
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        }

        List<User> users = userRepository.selectByIds(String.join(",",userIds));
        if(users.size() != userIds.size()){
            return Boolean.FALSE;
        }

        taskRepository.batchInsert(tasks);
        return true;
    }
}
