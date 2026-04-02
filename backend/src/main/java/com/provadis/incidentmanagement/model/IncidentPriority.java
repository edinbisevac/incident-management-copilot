package com.provadis.incidentmanagement.model;

public enum IncidentPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int rank;

    IncidentPriority(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public static IncidentPriority fromSeverity(AlarmSeverity severity) {
        if (severity == null) {
            return MEDIUM;
        }

        return switch (severity) {
            case CRITICAL -> HIGH;
            case MAJOR -> MEDIUM;
            case MINOR -> LOW;
            default -> MEDIUM;
        };
    }

    public static IncidentPriority higherOf(IncidentPriority current, IncidentPriority candidate) {
        IncidentPriority safeCurrent = current == null ? MEDIUM : current;
        IncidentPriority safeCandidate = candidate == null ? MEDIUM : candidate;
        return safeCandidate.rank > safeCurrent.rank ? safeCandidate : safeCurrent;
    }
}
