package dev.eduplay.entities;

import java.time.LocalDateTime;

public class EventRegistration {

    private int id;
    private SchoolEvent event;
    private User parent;
    private LocalDateTime registeredAt;  // ← Garder registered_at
    private String childFullName;
    private String parentPhone;
    private String childClassLevel;
    private String medicalNotes;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String notes;
    private String ticketQrCode;
    private LocalDateTime scannedAt;
    private String qrCodePath;
    private boolean reminderSent;
    private LocalDateTime reminderSentAt;

    // Constructeurs
    public EventRegistration() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public SchoolEvent getEvent() { return event; }
    public void setEvent(SchoolEvent event) { this.event = event; }

    public User getParent() { return parent; }
    public void setParent(User parent) { this.parent = parent; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public String getChildFullName() { return childFullName; }
    public void setChildFullName(String childFullName) { this.childFullName = childFullName; }

    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public String getChildClassLevel() { return childClassLevel; }
    public void setChildClassLevel(String childClassLevel) { this.childClassLevel = childClassLevel; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTicketQrCode() { return ticketQrCode; }
    public void setTicketQrCode(String ticketQrCode) { this.ticketQrCode = ticketQrCode; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public LocalDateTime getReminderSentAt() { return reminderSentAt; }
    public void setReminderSentAt(LocalDateTime reminderSentAt) { this.reminderSentAt = reminderSentAt; }
}