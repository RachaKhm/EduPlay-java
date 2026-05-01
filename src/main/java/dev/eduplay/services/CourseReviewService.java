package dev.eduplay.services;

import dev.eduplay.entities.CourseReview;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseReviewService {

    private Connection cnx;

    public CourseReviewService() throws SQLException {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void ajouter(CourseReview review) throws SQLException {
        String sql = "INSERT INTO course_reviews (course_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, review.getCourseId());
            st.setInt(2, review.getUserId());
            st.setInt(3, review.getRating());
            st.setString(4, review.getComment());
            st.executeUpdate();
            
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    review.setId(rs.getInt(1));
                }
            }
        }
    }

    public void modifier(CourseReview review) throws SQLException {
        String sql = "UPDATE course_reviews SET rating=?, comment=? WHERE id=?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, review.getRating());
            st.setString(2, review.getComment());
            st.setInt(3, review.getId());
            st.executeUpdate();
        }
    }

    public CourseReview getReviewByUserAndCourse(int userId, int courseId) throws SQLException {
        String sql = "SELECT * FROM course_reviews WHERE user_id=? AND course_id=?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, courseId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new CourseReview(
                            rs.getInt("id"),
                            rs.getInt("course_id"),
                            rs.getInt("user_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    );
                }
            }
        }
        return null;
    }

    public double getAverageRating(int courseId) throws SQLException {
        String sql = "SELECT AVG(rating) FROM course_reviews WHERE course_id=?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, courseId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public List<CourseReview> getAllReviewsForCourse(int courseId) throws SQLException {
        List<CourseReview> list = new ArrayList<>();
        String sql = "SELECT * FROM course_reviews WHERE course_id=? ORDER BY created_at DESC";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, courseId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(new CourseReview(
                            rs.getInt("id"),
                            rs.getInt("course_id"),
                            rs.getInt("user_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    ));
                }
            }
        }
        return list;
    }
}
