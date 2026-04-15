package dev.eduplay.entities;

import java.time.LocalDateTime;

public class EventRegistration {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int id;
    private String status;
    private LocalDateTime registeredAt;
    private String childFullName;
    private String parentPhone;
    private String childClassLevel;
    private String medicalNotes;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String notes;
    private String ticketQrCode;
    private String qrCodePath;
    private LocalDateTime scannedAt;
    private Boolean reminderSent;
    private LocalDateTime reminderSentAt;
    private SchoolEvent event;
    private User parent;

    public EventRegistration(){}

    public EventRegistration(int id, String status, LocalDateTime registeredAt, String childFullName, String parentPhone, String childClassLevel, String medicalNotes, String emergencyContactName, String emergencyContactPhone, String notes, String ticketQrCode, String qrCodePath, LocalDateTime scannedAt, Boolean reminderSent, LocalDateTime reminderSentAt, SchoolEvent event, User parent) {
        this.id = id;
        this.status = status;
        this.registeredAt = registeredAt;
        this.childFullName = childFullName;
        this.parentPhone = parentPhone;
        this.childClassLevel = childClassLevel;
        this.medicalNotes = medicalNotes;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.notes = notes;
        this.ticketQrCode = ticketQrCode;
        this.qrCodePath = qrCodePath;
        this.scannedAt = scannedAt;
        this.reminderSent = reminderSent;
        this.reminderSentAt = reminderSentAt;
        this.event = event;
        this.parent = parent;
    }

    public EventRegistration(int id, String status, LocalDateTime registeredAt, String childFullName,
                             String parentPhone, String childClassLevel, String medicalNotes,
                             String emergencyContactName, String emergencyContactPhone, String notes,
                             String ticketQrCode, String qrCodePath, LocalDateTime scannedAt,
                             Boolean reminderSent, LocalDateTime reminderSentAt) {
        this.id = id;
        this.status = status;
        this.registeredAt = registeredAt;
        this.childFullName = childFullName;
        this.parentPhone = parentPhone;
        this.childClassLevel = childClassLevel;
        this.medicalNotes = medicalNotes;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.notes = notes;
        this.ticketQrCode = ticketQrCode;
        this.qrCodePath = qrCodePath;
        this.scannedAt = scannedAt;
        this.reminderSent = reminderSent;
        this.reminderSentAt = reminderSentAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getChildFullName() {
        return childFullName;
    }

    public void setChildFullName(String childFullName) {
        this.childFullName = childFullName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getChildClassLevel() {
        return childClassLevel;
    }

    public void setChildClassLevel(String childClassLevel) {
        this.childClassLevel = childClassLevel;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTicketQrCode() {
        return ticketQrCode;
    }

    public void setTicketQrCode(String ticketQrCode) {
        this.ticketQrCode = ticketQrCode;
    }

    public String getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public Boolean getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public LocalDateTime getReminderSentAt() {
        return reminderSentAt;
    }

    public void setReminderSentAt(LocalDateTime reminderSentAt) {
        this.reminderSentAt = reminderSentAt;
    }

    public SchoolEvent getEvent() {
        return event;
    }

    public void setEvent(SchoolEvent event) {
        this.event = event;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "EventRegistration{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", registeredAt=" + registeredAt +
                ", childFullName='" + childFullName + '\'' +
                ", parentPhone='" + parentPhone + '\'' +
                ", childClassLevel='" + childClassLevel + '\'' +
                ", medicalNotes='" + medicalNotes + '\'' +
                ", emergencyContactName='" + emergencyContactName + '\'' +
                ", emergencyContactPhone='" + emergencyContactPhone + '\'' +
                ", notes='" + notes + '\'' +
                ", ticketQrCode='" + ticketQrCode + '\'' +
                ", qrCodePath='" + qrCodePath + '\'' +
                ", scannedAt=" + scannedAt +
                ", reminderSent=" + reminderSent +
                ", reminderSentAt=" + reminderSentAt +
                ", event=" + event +
                ", parent=" + parent +
                '}';
    }
}
