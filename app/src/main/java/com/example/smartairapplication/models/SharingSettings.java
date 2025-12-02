package com.example.smartairapplication.models;

public class SharingSettings {
    private boolean rescueLogs = false;
    private boolean controllerAdherence = false;
    private boolean symptoms = false;
    private boolean triggers = false;
    private boolean PEF = false;
    private boolean triageIncidents = false;
    private boolean chartSummaries = false;

    public SharingSettings() {}

    public boolean isRescueLogs() { return rescueLogs; }
    public boolean isControllerAdherence() { return controllerAdherence; }
    public boolean isSymptoms() { return symptoms; }
    public boolean isTriggers() { return triggers; }
    public boolean isPEF() { return PEF; }
    public boolean isTriageIncidents() { return triageIncidents; }
    public boolean isChartSummaries() { return chartSummaries; }

    public void setRescueLogs(boolean rescueLogs) { this.rescueLogs = rescueLogs; }
    public void setControllerAdherence(boolean controllerAdherence) { this.controllerAdherence = controllerAdherence; }
    public void setSymptoms(boolean symptoms) { this.symptoms = symptoms; }
    public void setTriggers(boolean triggers) { this.triggers = triggers; }
    public void setPEF(boolean PEF) { this.PEF = PEF; }
    public void setTriageIncidents(boolean triageIncidents) { this.triageIncidents = triageIncidents; }

    public void setChartSummaries(boolean chartSummaries) { this.chartSummaries = chartSummaries; }

    public void setAll(boolean state) {
        setRescueLogs(state);
        setControllerAdherence(state);
        setSymptoms(state);
        setTriggers(state);
        setPEF(state);
        setTriageIncidents(state);
        setChartSummaries(state);
    }
}
