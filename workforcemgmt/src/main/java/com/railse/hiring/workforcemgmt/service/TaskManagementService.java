

package com.railse.hiring.workforcemgmt.service;


import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.dto.ActivityandCommentDto.TaskDetailDto;
import com.railse.hiring.workforcemgmt.model.enums.Priority;


import java.util.List;


public interface TaskManagementService {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);

    List<TaskManagementDto> updateTasksPriority(UpdatePriorityRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);

    List<TaskManagementDto> getTasksViaPriority(Priority priority);

    // Add a comment to a task
    void addComment(Long taskId, Long userId, String message);

    // Fetch task with comments and activity history
    TaskDetailDto getTaskDetails(Long taskId);
}




