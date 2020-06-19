/*
    Appenders
 */
def ROOT_DIR = "."
def log_pattern = "%d [%thread] %-5level %logger %class - %msg%n"
appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = log_pattern
    }
}

appender("ROLLING", RollingFileAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = log_pattern
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${ROOT_DIR}/log/app-%d{yyyy-MM-dd_HH}.log"
    }
}
/*
    Root Logger
 */
root(INFO, ["CONSOLE", "ROLLING"])
