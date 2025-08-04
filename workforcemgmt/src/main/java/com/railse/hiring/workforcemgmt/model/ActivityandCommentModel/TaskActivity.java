package com.railse.hiring.workforcemgmt.model.ActivityandCommentModel;




import com.railse.hiring.workforcemgmt.model.enums.TaskLogType;
import lombok.Data;

@Data
public class TaskActivity {
    private Long id;
    private Long taskId;
    private TaskLogType activityType;
    private String message;
    private Long createdByUserId;
    private Long createdAt;
}
