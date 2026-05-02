package dev.eduplay.services;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.entities.User;
import dev.eduplay.interfaces.IGeneralService;
import dev.eduplay.tools.MyDataBase;
import dev.eduplay.utils.QRCodeGenerator;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventRegistrationService implements IGeneralService<EventRegistration> {

    Connection cn;

    public EventRegistrationService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(EventRegistration registration) throws SQLException {
        // Vérifier si l'événement a encore des places
        String checkCapacitySql = "SELECT max_capacity, current_registrations FROM school_event WHERE id = ?";
        PreparedStatement checkSt = cn.prepareStatement(checkCapacitySql);
        checkSt.setInt(1, registration.getEvent().getId());
        ResultSet rs = checkSt.executeQuery();

        if (rs.next()) {
            int maxCapacity = rs.getInt("max_capacity");
            int currentRegs = rs.getInt("current_registrations");

            if (currentRegs >= maxCapacity) {
                throw new SQLException("❌ Désolé, cet événement a atteint sa capacité maximale de " + maxCapacity + " participants");
            }
        }

        // Vérifier si le parent a déjà inscrit le même enfant
        String checkDuplicateSql = "SELECT COUNT(*) FROM event_registration WHERE event_id = ? AND parent_id = ? AND child_full_name = ?";
        PreparedStatement checkDuplicateSt = cn.prepareStatement(checkDuplicateSql);
        checkDuplicateSt.setInt(1, registration.getEvent().getId());
        checkDuplicateSt.setInt(2, registration.getParent().getId());
        checkDuplicateSt.setString(3, registration.getChildFullName());
        ResultSet rsDuplicate = checkDuplicateSt.executeQuery();
        rsDuplicate.next();

        if (rsDuplicate.getInt(1) > 0) {
            throw new SQLException("❌ Ce parent a déjà inscrit l'enfant '" + registration.getChildFullName() + "' à cet événement");
        }

        // ========== 1. D'abord, insérer sans QR code ==========
        String sql = "INSERT INTO event_registration(event_id, parent_id, registered_at, child_full_name, parent_phone, child_class_level, medical_notes, emergency_contact_name, emergency_contact_phone, notes, scanned_at, reminder_sent, reminder_sent_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, registration.getEvent().getId());
        ps.setInt(2, registration.getParent().getId());
        ps.setTimestamp(3, Timestamp.valueOf(registration.getRegisteredAt()));
        ps.setString(4, registration.getChildFullName());
        ps.setString(5, registration.getParentPhone());
        ps.setString(6, registration.getChildClassLevel());
        ps.setString(7, registration.getMedicalNotes());
        ps.setString(8, registration.getEmergencyContactName());
        ps.setString(9, registration.getEmergencyContactPhone());
        ps.setString(10, registration.getNotes());
        ps.setTimestamp(11, registration.getScannedAt() != null ? Timestamp.valueOf(registration.getScannedAt()) : null);
        ps.setBoolean(12, registration.isReminderSent());
        ps.setTimestamp(13, registration.getReminderSentAt() != null ? Timestamp.valueOf(registration.getReminderSentAt()) : null);

        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet generatedKeys = ps.getGeneratedKeys();
        int registrationId = -1;
        if (generatedKeys.next()) {
            registrationId = generatedKeys.getInt(1);
            registration.setId(registrationId);
            System.out.println("✅ Inscription ajoutée avec ID: " + registrationId);
        }

        // ========== 2. Générer le QR code avec l'ID réel ==========
        String qrCodeValue = "REG_ID:" + registrationId;
        String qrCodePath = QRCodeGenerator.generateQRCode(qrCodeValue, "ticket_" + registrationId);

        registration.setTicketQrCode(qrCodeValue);
        registration.setQrCodePath(qrCodePath);

        System.out.println("✅ QR Code généré automatiquement avec l'ID: " + registrationId);
        System.out.println("   Valeur QR: " + qrCodeValue);
        System.out.println("   Chemin: " + qrCodePath);

        // ========== 3. Mettre à jour l'inscription avec le QR code ==========
        String updateQrSql = "UPDATE event_registration SET ticket_qr_code = ?, qr_code_path = ? WHERE id = ?";
        PreparedStatement updatePs = cn.prepareStatement(updateQrSql);
        updatePs.setString(1, qrCodeValue);
        updatePs.setString(2, qrCodePath);
        updatePs.setInt(3, registrationId);
        updatePs.executeUpdate();

        // Incrémenter le compteur d'inscriptions
        String updateCapacitySql = "UPDATE school_event SET current_registrations = current_registrations + 1 WHERE id = ?";
        PreparedStatement updateSt = cn.prepareStatement(updateCapacitySql);
        updateSt.setInt(1, registration.getEvent().getId());
        updateSt.executeUpdate();

        // ========== 4. ENVOI DE L'EMAIL DE CONFIRMATION ==========
        if (registrationId > 0 && registration.getParent() != null && registration.getParent().getEmail() != null) {
            try {
                EmailServiceEvent emailService = new EmailServiceEvent();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                String eventDate = registration.getEvent().getStartDate() != null ?
                        registration.getEvent().getStartDate().format(formatter) : "Date non spécifiée";
                String eventLocation = registration.getEvent().getLocation() != null ?
                        registration.getEvent().getLocation() : "Lieu non spécifié";

                emailService.sendRegistrationConfirmation(
                        registration.getParent().getEmail(),
                        registration.getParent().getFirstName() + " " + registration.getParent().getLastName(),
                        registration.getChildFullName(),
                        registration.getEvent().getTitle(),
                        eventDate,
                        eventLocation,
                        qrCodePath,
                        registrationId
                );
                System.out.println("📧 Email de confirmation envoyé à: " + registration.getParent().getEmail());
            } catch (Exception e) {
                System.err.println("⚠️ Erreur envoi email confirmation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void supprimer(EventRegistration registration) throws SQLException {
        // Récupérer l'event_id avant suppression
        String getEventIdSql = "SELECT event_id FROM event_registration WHERE id = ?";
        PreparedStatement getEventSt = cn.prepareStatement(getEventIdSql);
        getEventSt.setInt(1, registration.getId());
        ResultSet rs = getEventSt.executeQuery();

        int eventId = -1;
        if (rs.next()) {
            eventId = rs.getInt("event_id");
        }

        // Supprimer l'inscription
        String sql = "DELETE FROM event_registration WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, registration.getId());
        pst.executeUpdate();

        // Décrémenter le compteur d'inscriptions
        if (eventId != -1) {
            String updateCapacitySql = "UPDATE school_event SET current_registrations = current_registrations - 1 WHERE id = ?";
            PreparedStatement updateSt = cn.prepareStatement(updateCapacitySql);
            updateSt.setInt(1, eventId);
            updateSt.executeUpdate();
        }
    }

    @Override
    public int chercher(EventRegistration registration) throws SQLException {
        String sql = "SELECT 1 FROM event_registration WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, registration.getId());
        ResultSet rs = pst.executeQuery();
        return rs.next() ? registration.getId() : -1;
    }

    @Override
    public void modifier(EventRegistration registration) throws SQLException {
        String sql = "UPDATE event_registration SET child_full_name = ?, parent_phone = ?, child_class_level = ?, medical_notes = ?, emergency_contact_name = ?, emergency_contact_phone = ?, notes = ?, ticket_qr_code = ?, qr_code_path = ?, scanned_at = ?, reminder_sent = ?, reminder_sent_at = ? WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, registration.getChildFullName());
        pst.setString(2, registration.getParentPhone());
        pst.setString(3, registration.getChildClassLevel());
        pst.setString(4, registration.getMedicalNotes());
        pst.setString(5, registration.getEmergencyContactName());
        pst.setString(6, registration.getEmergencyContactPhone());
        pst.setString(7, registration.getNotes());
        pst.setString(8, registration.getTicketQrCode());
        pst.setString(9, registration.getQrCodePath());
        pst.setTimestamp(10, registration.getScannedAt() != null ? Timestamp.valueOf(registration.getScannedAt()) : null);
        pst.setBoolean(11, registration.isReminderSent());
        pst.setTimestamp(12, registration.getReminderSentAt() != null ? Timestamp.valueOf(registration.getReminderSentAt()) : null);
        pst.setInt(13, registration.getId());
        pst.executeUpdate();
    }

    @Override
    public List<EventRegistration> recuperer() throws SQLException {
        String sql = "SELECT er.*, se.title as event_title, se.start_date, se.end_date, se.location, " +
                "u.id as parent_user_id, u.first_name as parent_first_name, u.last_name as parent_last_name, u.email as parent_email " +
                "FROM event_registration er " +
                "LEFT JOIN school_event se ON er.event_id = se.id " +
                "LEFT JOIN user u ON er.parent_id = u.id " +
                "ORDER BY er.registered_at DESC";

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<EventRegistration> registrations = new ArrayList<>();
        while (rs.next()) {
            registrations.add(extractRegistration(rs));
        }
        return registrations;
    }

    public List<EventRegistration> recupererParEventId(int eventId) throws SQLException {
        String sql = "SELECT er.*, se.title as event_title, se.start_date, se.end_date, se.location, " +
                "u.id as parent_user_id, u.first_name as parent_first_name, u.last_name as parent_last_name, u.email as parent_email " +
                "FROM event_registration er " +
                "LEFT JOIN school_event se ON er.event_id = se.id " +
                "LEFT JOIN user u ON er.parent_id = u.id " +
                "WHERE er.event_id = ? " +
                "ORDER BY er.registered_at DESC";

        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, eventId);
        ResultSet rs = pst.executeQuery();
        List<EventRegistration> registrations = new ArrayList<>();
        while (rs.next()) {
            registrations.add(extractRegistration(rs));
        }
        return registrations;
    }

    public List<EventRegistration> recupererParParentId(int parentId) throws SQLException {
        String sql = "SELECT er.*, se.title as event_title, se.start_date, se.end_date, se.location, " +
                "u.id as parent_user_id, u.first_name as parent_first_name, u.last_name as parent_last_name, u.email as parent_email " +
                "FROM event_registration er " +
                "LEFT JOIN school_event se ON er.event_id = se.id " +
                "LEFT JOIN user u ON er.parent_id = u.id " +
                "WHERE er.parent_id = ? " +
                "ORDER BY er.registered_at DESC";

        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, parentId);
        ResultSet rs = pst.executeQuery();
        List<EventRegistration> registrations = new ArrayList<>();
        while (rs.next()) {
            registrations.add(extractRegistration(rs));
        }
        return registrations;
    }

    public EventRegistration recupererParId(int id) throws SQLException {
        String sql = "SELECT er.*, se.title as event_title, se.start_date, se.end_date, se.location, " +
                "u.id as parent_user_id, u.first_name as parent_first_name, u.last_name as parent_last_name, u.email as parent_email " +
                "FROM event_registration er " +
                "LEFT JOIN school_event se ON er.event_id = se.id " +
                "LEFT JOIN user u ON er.parent_id = u.id " +
                "WHERE er.id = ?";

        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return extractRegistration(rs);
        }
        return null;
    }

    public void regenerateQRCode(int registrationId) throws SQLException {
        EventRegistration registration = recupererParId(registrationId);
        if (registration != null) {
            String qrCodeValue = "REG_ID:" + registrationId;
            String qrCodePath = QRCodeGenerator.generateQRCode(qrCodeValue, "ticket_" + registrationId);

            String sql = "UPDATE event_registration SET ticket_qr_code = ?, qr_code_path = ? WHERE id = ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, qrCodeValue);
            ps.setString(2, qrCodePath);
            ps.setInt(3, registrationId);
            ps.executeUpdate();

            System.out.println("✅ QR Code régénéré pour l'inscription ID: " + registrationId);
        }
    }

    public void updateQRCode(int registrationId, String qrCodePath, String qrCodeValue) throws SQLException {
        String sql = "UPDATE event_registration SET ticket_qr_code = ?, qr_code_path = ? WHERE id = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, qrCodeValue);
        ps.setString(2, qrCodePath);
        ps.setInt(3, registrationId);
        ps.executeUpdate();
        System.out.println("✅ QR Code enregistré pour l'inscription ID: " + registrationId);
    }

    private EventRegistration extractRegistration(ResultSet rs) throws SQLException {
        EventRegistration registration = new EventRegistration();
        registration.setId(rs.getInt("id"));
        registration.setRegisteredAt(rs.getTimestamp("registered_at") != null ? rs.getTimestamp("registered_at").toLocalDateTime() : null);
        registration.setChildFullName(rs.getString("child_full_name"));
        registration.setParentPhone(rs.getString("parent_phone"));
        registration.setChildClassLevel(rs.getString("child_class_level"));
        registration.setMedicalNotes(rs.getString("medical_notes"));
        registration.setEmergencyContactName(rs.getString("emergency_contact_name"));
        registration.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        registration.setNotes(rs.getString("notes"));
        registration.setTicketQrCode(rs.getString("ticket_qr_code"));
        registration.setScannedAt(rs.getTimestamp("scanned_at") != null ? rs.getTimestamp("scanned_at").toLocalDateTime() : null);
        registration.setQrCodePath(rs.getString("qr_code_path"));
        registration.setReminderSent(rs.getBoolean("reminder_sent"));
        registration.setReminderSentAt(rs.getTimestamp("reminder_sent_at") != null ? rs.getTimestamp("reminder_sent_at").toLocalDateTime() : null);

        // Charger l'événement
        SchoolEvent event = new SchoolEvent();
        event.setId(rs.getInt("event_id"));
        event.setTitle(rs.getString("event_title") != null ? rs.getString("event_title") : "Événement");

        if (rs.getTimestamp("start_date") != null) {
            event.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        }
        if (rs.getTimestamp("end_date") != null) {
            event.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
        }
        event.setLocation(rs.getString("location") != null ? rs.getString("location") : "");

        try {
            event.setMaxCapacity(rs.getInt("max_capacity"));
        } catch (SQLException e) {}
        try {
            event.setCurrentRegistrations(rs.getInt("current_registrations"));
        } catch (SQLException e) {}

        registration.setEvent(event);

        // ✅ CHARGER LE PARENT (version corrigée pour User)
        int parentId = rs.getInt("parent_user_id");
        if (parentId > 0) {
            User parent = new User();
            parent.setId(parentId);
            parent.setFirstName(rs.getString("parent_first_name"));
            parent.setLastName(rs.getString("parent_last_name"));
            parent.setEmail(rs.getString("parent_email"));
            registration.setParent(parent);
        }

        return registration;
    }
}