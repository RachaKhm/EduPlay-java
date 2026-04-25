package dev.eduplay.services;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.interfaces.IGeneralService;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventRegistrationService implements IGeneralService<EventRegistration> {
    Connection cn;
    public EventRegistrationService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(EventRegistration registration) throws SQLException {
        String sql = "INSERT INTO event_registration(event_id, parent_id, status, registered_at, child_full_name, parent_phone, child_class_level, medical_notes, emergency_contact_name, emergency_contact_phone, notes, ticket_qr_code, qr_code_path, scanned_at, reminder_sent, reminder_sent_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setInt(1, registration.getEvent().getId());
        ps.setInt(2, registration.getParent().getId());
        ps.setString(3, registration.getStatus());
        ps.setTimestamp(4, Timestamp.valueOf(registration.getRegisteredAt()));
        ps.setString(5, registration.getChildFullName());
        ps.setString(6, registration.getParentPhone());
        ps.setString(7, registration.getChildClassLevel());
        ps.setString(8, registration.getMedicalNotes());
        ps.setString(9, registration.getEmergencyContactName());
        ps.setString(10, registration.getEmergencyContactPhone());
        ps.setString(11, registration.getNotes());
        ps.setString(12, registration.getTicketQrCode());
        ps.setString(13, registration.getQrCodePath());
        ps.setTimestamp(14, registration.getScannedAt() != null ? Timestamp.valueOf(registration.getScannedAt()) : null);
        ps.setBoolean(15, registration.getReminderSent());
        ps.setTimestamp(16, registration.getReminderSentAt() != null ? Timestamp.valueOf(registration.getReminderSentAt()) : null);


        ps.executeUpdate();


    }

    @Override
    public void supprimer(EventRegistration registration) throws SQLException {
        String sql = "DELETE FROM event_registration WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, registration.getId());
        pst.executeUpdate();

    }

    @Override
    public int chercher(EventRegistration registration) throws SQLException {
        String sql = "SELECT 1 FROM event_registration WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, registration.getId());
        ResultSet rs = pst.executeQuery();
        if (rs.next()){
            System.out.println("cette registration existe avec l'id "+registration.getId());
        }else{
            System.out.println("cette registration n'existe pas");
        }
        return registration.getId();
    }

    @Override
    public void modifier(EventRegistration registration) throws SQLException {
        if (chercher(registration) ==registration.getId()) {
            String sql = "UPDATE event_registration SET status = ?, child_full_name = ?, parent_phone = ?, child_class_level = ?, medical_notes = ?, emergency_contact_name = ?, emergency_contact_phone = ?, notes = ?, ticket_qr_code = ?, qr_code_path = ?, scanned_at = ?, reminder_sent = ?, reminder_sent_at = ? WHERE id = ?";

            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, registration.getStatus());
            pst.setString(2, registration.getChildFullName());
            pst.setString(3, registration.getParentPhone());
            pst.setString(4, registration.getChildClassLevel());
            pst.setString(5, registration.getMedicalNotes());
            pst.setString(6, registration.getEmergencyContactName());
            pst.setString(7, registration.getEmergencyContactPhone());
            pst.setString(8, registration.getNotes());
            pst.setString(9, registration.getTicketQrCode());
            pst.setString(10, registration.getQrCodePath());
            pst.setTimestamp(11, registration.getScannedAt() != null ? Timestamp.valueOf(registration.getScannedAt()) : null);
            pst.setBoolean(12, registration.getReminderSent());
            pst.setTimestamp(13, registration.getReminderSentAt() != null ? Timestamp.valueOf(registration.getReminderSentAt()) : null);
            pst.setInt(14, registration.getId());

            pst.executeUpdate();
            System.out.println("registration modifiée avec succès !");
        }
        else {
            System.out.println("cette registration n'existe pas");
        }

    }

    public void modifierBack(EventRegistration registration) throws SQLException {
        String sql = "UPDATE event_registration SET status = ?, notes = ? WHERE id = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, registration.getStatus());
        ps.setString(2, registration.getNotes());
        ps.setInt(3, registration.getId());

        int rowsAffected = ps.executeUpdate();
        System.out.println("Lignes mises à jour: " + rowsAffected);
    }

    @Override
    public List<EventRegistration> recuperer() throws SQLException {
        String sql = "SELECT er.*, se.title as event_title, se.start_date, se.end_date, se.location " +
                "FROM event_registration er " +
                "LEFT JOIN school_event se ON er.event_id = se.id " +
                "ORDER BY er.registered_at DESC";

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<EventRegistration> registrations = new ArrayList<>();

        while (rs.next()) {
            EventRegistration registration = new EventRegistration();
            registration.setId(rs.getInt("id"));
            registration.setStatus(rs.getString("status"));
            registration.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
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

            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("event_id"));
            event.setTitle(rs.getString("event_title"));
            event.setStartDate(rs.getTimestamp("start_date") != null ? rs.getTimestamp("start_date").toLocalDateTime() : null);
            event.setEndDate(rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date").toLocalDateTime() : null);  // ← Très important !
            event.setLocation(rs.getString("location"));
            registration.setEvent(event);

            registrations.add(registration);
        }
        return registrations;
    }

    /**
     * Récupère une inscription par son ID
     * @param id L'ID de l'inscription
     * @return L'inscription correspondante ou null
     */
    public EventRegistration recupererParId(int id) throws SQLException {
        String sql = "SELECT er.*, se.title as event_title FROM event_registration er " +
                "LEFT JOIN school_event se ON er.event_id = se.id " +
                "WHERE er.id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            EventRegistration registration = new EventRegistration();
            registration.setId(rs.getInt("id"));
            registration.setStatus(rs.getString("status"));
            registration.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
            registration.setChildFullName(rs.getString("child_full_name"));
            registration.setParentPhone(rs.getString("parent_phone"));
            registration.setNotes(rs.getString("notes"));

            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("event_id"));
            event.setTitle(rs.getString("event_title"));
            registration.setEvent(event);

            return registration;
        }
        return null;
    }

}
