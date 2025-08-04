package com.railse.hiring.workforcemgmt.repository.ActivityandCommentRepository;

import com.railse.hiring.workforcemgmt.model.ActivityandCommentModel.TaskActivity;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TaskActivityRepository {
    private final Map<Long, TaskActivity> store = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    public TaskActivity save(TaskActivity activity) {
        activity.setId(idGen.incrementAndGet());
        store.put(activity.getId(), activity);
        return activity;
    }

    public List<TaskActivity> findByTaskId(Long taskId) {
        return store.values().stream()
                .filter(a -> Objects.equals(a.getTaskId(), taskId))
                .collect(Collectors.toList());
    }
}

