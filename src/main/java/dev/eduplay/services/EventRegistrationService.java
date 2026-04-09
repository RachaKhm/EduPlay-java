package dev.eduplay.services;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventRegistrationService implements IGeneralService<EventRegistration>{
    Connection cn;
    public EventRegistrationService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(EventRegistration registration) throws SQLException {
        String sql = "INSERT INTO event_registration(status, registered_at, child_full_name, parent_phone, child_class_level, medical_notes, emergency_contact_name, emergency_contact_phone, notes, ticket_qr_code, qr_code_path, scanned_at, reminder_sent, reminder_sent_at, event_id, parent_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setString(1, registration.getStatus());
        ps.setTimestamp(2, Timestamp.valueOf(registration.getRegisteredAt()));
        ps.setString(3, registration.getChildFullName());
        ps.setString(4, registration.getParentPhone());
        ps.setString(5, registration.getChildClassLevel());
        ps.setString(6, registration.getMedicalNotes());
        ps.setString(7, registration.getEmergencyContactName());
        ps.setString(8, registration.getEmergencyContactPhone());
        ps.setString(9, registration.getNotes());
        ps.setString(10, registration.getTicketQrCode());
        ps.setString(11, registration.getQrCodePath());
        ps.setTimestamp(12, registration.getScannedAt() != null ? Timestamp.valueOf(registration.getScannedAt()) : null);
        ps.setBoolean(13, registration.getReminderSent());
        ps.setTimestamp(14, registration.getReminderSentAt() != null ? Timestamp.valueOf(registration.getReminderSentAt()) : null);
        ps.setInt(15, registration.getEvent().getId());
        ps.setInt(16, registration.getParent().getId());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            registration.setId(rs.getInt(1));
        }

        ps.close();
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

    @Override
    public List<EventRegistration> recuperer() throws SQLException {
        String sql = "select * from event_registration";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<EventRegistration> registrations = new ArrayList<>();
        while(rs.next()){
            EventRegistration er = new EventRegistration(
                    rs.getInt("id"),
                    rs.getString("status"),
                    rs.getTimestamp("registered_at").toLocalDateTime(),
                    rs.getString("child_full_name"),
                    rs.getString("parent_phone"),
                    rs.getString("child_class_level"),
                    rs.getString("medical_notes"),
                    rs.getString("emergency_contact_name"),
                    rs.getString("emergency_contact_phone"),
                    rs.getString("notes"),
                    rs.getString("ticket_qr_code"),
                    rs.getString("qr_code_path"),
                    rs.getTimestamp("scanned_at") != null ? rs.getTimestamp("scanned_at").toLocalDateTime() : null,
                    rs.getBoolean("reminder_sent"),
                    rs.getTimestamp("reminder_sent_at") != null ? rs.getTimestamp("reminder_sent_at").toLocalDateTime() : null);
            registrations.add(er);
        }

        return registrations;    }
}
