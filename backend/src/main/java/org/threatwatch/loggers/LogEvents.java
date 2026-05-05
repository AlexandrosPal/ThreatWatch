package org.threatwatch.loggers;

public class LogEvents {

    public static final String SETTINGS_UPDATE = "settings_update";
    public static final String BATCH_RUN = "batch_run";
    public static final String EMAIL_SENT = "email_sent";
    public static final String DISCORD_MESSAGE_SENT = "discord_message_sent";
    public static final String SLACK_MESSAGE_SENT = "slack_message_sent";
    public static final String SCHEDULER_RUN = "scheduler_run";
    public static final String NVD_REST_CLIENT = "nvd_rest_client";
    public static final String FILE_READ_ERROR = "file_read_error";
    public static final String EMAIL_CONNECTION = "email_connection";

    private LogEvents() {}

}
