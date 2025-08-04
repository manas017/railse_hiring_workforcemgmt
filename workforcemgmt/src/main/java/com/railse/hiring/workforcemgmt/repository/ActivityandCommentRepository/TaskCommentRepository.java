package com.railse.hiring.workforcemgmt.repository.ActivityandCommentRepository;

import com.railse.hiring.workforcemgmt.model.ActivityandCommentModel.TaskComment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TaskCommentRepository {
    private final Map<Long, TaskComment> store = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    public TaskComment save(TaskComment comment) {
        comment.setId(idGen.incrementAndGet());
        store.put(comment.getId(), comment);
        return comment;
    }

    public List<TaskComment> findByTaskId(Long taskId) {
        return store.values().stream()
                .filter(c -> Objects.equals(c.getTaskId(), taskId))
                .collect(Collectors.toList());
    }
}

