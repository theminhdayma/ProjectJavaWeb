package com.data.entity.enums;

public enum Progress {
    PENDING("Đang xử lý"),
    INTERVIEWING("Quá trình phỏng vấn"),
    WAITING_FOR_INTERVIEW_CONFIRM("Chờ xác nhận lịch phỏng vấn"),
    REQUEST_RESCHEDULE("Yêu cầu thay đổi lịch phỏng vấn"),
    INTERVIEW_SCHEDULED("Đã xác nhận lịch phỏng vấn"),
    HANDING("Chờ phản hồi"),
    DESTROYED("Đã hủy đơn"),
    REJECTED("Đã bị từ chối"),
    DONE("Hoàn thành");

    private final String displayName;

    Progress(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
