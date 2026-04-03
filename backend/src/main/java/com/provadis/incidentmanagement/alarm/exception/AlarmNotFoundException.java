package com.provadis.incidentmanagement.alarm.exception;

public class AlarmNotFoundException extends RuntimeException {

    public AlarmNotFoundException(Long alarmId) {
        super("Alarm with id " + alarmId + " was not found.");
    }
}