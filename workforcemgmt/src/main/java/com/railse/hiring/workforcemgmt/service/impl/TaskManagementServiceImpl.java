package com.railse.hiring.workforcemgmt.service.impl;


import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.dto.ActivityandCommentDto.ActivityDto;
import com.railse.hiring.workforcemgmt.dto.ActivityandCommentDto.CommentDto;
import com.railse.hiring.workforcemgmt.dto.ActivityandCommentDto.TaskDetailDto;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.ActivityandCommentModel.TaskActivity;
import com.railse.hiring.workforcemgmt.model.ActivityandCommentModel.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskLogType;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.ActivityandCommentRepository.TaskActivityRepository;
import com.railse.hiring.workforcemgmt.repository.ActivityandCommentRepository.TaskCommentRepository;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TaskManagementServiceImpl implements TaskManagementService {


    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;

    private final TaskCommentRepository commentRepository;
    private final TaskActivityRepository activityRepository;




    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }


    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");

            TaskManagement savedTask = taskRepository.save(newTask);
            createdTasks.add(savedTask);

            logActivity(savedTask.getId(), "Task created with priority " + savedTask.getPriority(), savedTask.getAssigneeId(),TaskLogType.TASK_CREATED);


        }
        return taskMapper.modelListToDtoList(createdTasks);
    }


    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));


            if (item.getTaskStatus() != null) {
              task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }
            updatedTasks.add(taskRepository.save(task));
            logActivity(task.getId(), "Task updated: status=" + task.getStatus() + ", description=" + task.getDescription(), task.getAssigneeId(),TaskLogType.STATUS_UPDATED);

        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }

    @Override
    public List<TaskManagementDto> updateTasksPriority(UpdatePriorityRequest updateRequest) {
        List<TaskManagement> updatedTasksPriority = new ArrayList<>();

        for (UpdatePriorityRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Task not found with id: " + item.getTaskId()));

            if (item.getPriority() != null ){
                task.setPriority(item.getPriority());
            }

            updatedTasksPriority.add(taskRepository.save(task));
            logActivity(task.getId(), "Task priority changed to " + task.getPriority(), task.getAssigneeId(),TaskLogType.PRIORITY_UPDATED);

        }

        return taskMapper.modelListToDtoList(updatedTasksPriority);
        }


    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());


        for (Task taskType : applicableTasks) {
            List<TaskManagement> tasksOfType = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
                    .collect(Collectors.toList());


            // BUG #1 is here. It should assign one and cancel the rest.
            // Instead, it reassigns ALL of them.
            if (!tasksOfType.isEmpty()) {


                //earlier
//                for (TaskManagement taskToUpdate : tasksOfType) {
//                    taskToUpdate.setAssigneeId(request.getAssigneeId());
//                    taskRepository.save(taskToUpdate);
//                }

                //Removed bug 1 code
                TaskManagement toAssign = tasksOfType.get(0);
                toAssign.setAssigneeId(request.getAssigneeId());
                taskRepository.save(toAssign);


                for (int i = 1; i < tasksOfType.size(); i++) {
                    TaskManagement duplicate = tasksOfType.get(i);
                    duplicate.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(duplicate);
                }


            } else {
                // Create a new task if none exist
                TaskManagement newTask = new TaskManagement();
                newTask.setReferenceId(request.getReferenceId());
                newTask.setReferenceType(request.getReferenceType());
                newTask.setTask(taskType);
                newTask.setAssigneeId(request.getAssigneeId());
                newTask.setStatus(TaskStatus.ASSIGNED);
                taskRepository.save(newTask);
            }
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }


    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        long start = request.getStartDate();
        long end = request.getEndDate();

        // BUG #2 is here. It should filter out CANCELLED tasks but doesn't.
        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> {
                    // This logic is incomplete for the assignment.
                    // It should check against startDate and endDate.
                    // For now, it just returns all tasks for the assignees.
                    return true;
                })

                //Revised code to improve bug 2
//////
//                .filter(task ->
//                        task.getStatus() != TaskStatus.CANCELLED && //  not cancelled
//                        task.getTaskDeadlineTime() >= start &&
//                        task.getTaskDeadlineTime() <=end
//                )

//                  Code for feature 1 ie Task View
                .filter(task ->{
                    TaskStatus status = task.getStatus();
                    if (task.getStatus() == TaskStatus.CANCELLED || task.getStatus() == TaskStatus.COMPLETED) {
                        return false;
                    }

                    long deadline = task.getTaskDeadlineTime();

                    //Include if it's within the date range
                    boolean isWithinRange = deadline >= start && deadline <= end;

                    //Or if it's before start date but still active
                    boolean isBeforeRangeButActive = deadline < start;

                    return isWithinRange || isBeforeRangeButActive;
                })

                .collect(Collectors.toList());


        return taskMapper.modelListToDtoList(filteredTasks);
    }
    //Code for feature 2
    @Override
    public List<TaskManagementDto> getTasksViaPriority(Priority priority) {
        List<TaskManagement> tasks = taskRepository.findByPriority(priority);
        return taskMapper.modelListToDtoList(tasks);
    }

    //Code for feature 3
    @Override
    public void addComment(Long taskId, Long userId, String message) {
        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setMessage(message);
        comment.setCreatedByUserId(userId);
        comment.setCreatedAt(System.currentTimeMillis());
        commentRepository.save(comment);
    }

   @Override
    public TaskDetailDto getTaskDetails(Long taskId) {
        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        List<TaskComment> comments = commentRepository.findByTaskId(taskId);
        List<TaskActivity> activities = activityRepository.findByTaskId(taskId);

        TaskDetailDto dto = new TaskDetailDto();
        dto.setTask(taskMapper.modelToDto(task));

        dto.setComments(comments.stream().map(c -> {
            CommentDto cd = new CommentDto();
            cd.setMessage(c.getMessage());
            cd.setCreatedByUserId(c.getCreatedByUserId());
            cd.setCreatedAt(c.getCreatedAt());
            return cd;
        }).collect(Collectors.toList()));

        dto.setActivities(activities.stream().map(a -> {
            ActivityDto ad = new ActivityDto();
            ad.setActivityType(a.getActivityType());
            ad.setMessage(a.getMessage());
            ad.setCreatedByUserId(a.getCreatedByUserId());
            ad.setCreatedAt(a.getCreatedAt());
            return ad;
        }).collect(Collectors.toList()));

        return dto;
    }

    private void logActivity(Long taskId, String message, Long userId,TaskLogType type) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setActivityType(type);
        activity.setMessage(message);
        activity.setCreatedByUserId(userId != null ? userId : 1L); // default if null
        activity.setCreatedAt(System.currentTimeMillis());
        activityRepository.save(activity);
    }

}
