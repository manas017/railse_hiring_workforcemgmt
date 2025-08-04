package com.railse.hiring.workforcemgmt.dto.ActivityandCommentDto;

import com.railse.hiring.workforcemgmt.model.enums.TaskLogType;
import lombok.Data;

@Data
public class ActivityDto {
    private TaskLogType activityType;
    private String message;
    private Long createdByUserId;
    private Long createdAt;
}