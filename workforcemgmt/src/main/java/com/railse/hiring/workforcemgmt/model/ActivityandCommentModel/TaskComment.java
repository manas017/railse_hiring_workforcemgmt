package com.railse.hiring.workforcemgmt.model.ActivityandCommentModel;



import lombok.Data;

@Data
public class TaskComment {
    private Long id;
    private Long userId;
    private Long taskId;
    private String message;
    private Long createdByUserId;
    private Long createdAt;
}
