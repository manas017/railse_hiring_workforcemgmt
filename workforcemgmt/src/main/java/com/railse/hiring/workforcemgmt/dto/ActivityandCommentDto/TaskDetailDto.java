package com.railse.hiring.workforcemgmt.dto.ActivityandCommentDto;



import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import lombok.Data;
import java.util.List;

@Data
public class TaskDetailDto {
    private TaskManagementDto task;
    private List<CommentDto> comments;
    private List<ActivityDto> activities;
}