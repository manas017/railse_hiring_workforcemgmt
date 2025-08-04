package com.railse.hiring.workforcemgmt.dto.ActivityandCommentDto;


import lombok.Data;

@Data
public class CommentDto {
    private String message;
    private Long createdByUserId;
    private Long createdAt;
}
