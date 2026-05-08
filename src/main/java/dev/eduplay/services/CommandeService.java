package dev.eduplay.services;

import dev.eduplay.entities.Commande;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.sql.Types;

public class CommandeService {

    Connection cnx = MyDataBase.getInstance().getCnx();
    // cache table -> set of column names to avoid repeated slow metadata calls
    private final java.util.Map<String, java.util.Set<String>> columnsCache = new java.util.HashMap<>();
    
    public CommandeService() {
        // Ensure the commande table has the expected parent_id column
        ensureParentIdColumn();
    }

    public int ajouter(Commande c) {
        // Clear cached columns for 'commande' to ensure fresh metadata (avoid stale cache causing false positives)
        synchronized (columnsCache) { columnsCache.remove("commande"); }
        // Detect available columns once (support multiple name variants)
        String productCol = findExistingColumn("commande", "product_id", "id_product_id", "id_product");
        String parentCol = findExistingColumn("commande", "parent_id", "user_id", "id_user_id");
        String totalCol = findExistingColumn("commande", "total_price", "total_amount", "montant_total", "total", "amount");
        boolean hasStatus = hasColumn("commande", "status");
        boolean hasDateCommande = hasColumn("commande", "date_commande") || hasColumn("commande", "created_at");

        try {
            System.out.println("[CommandeService] detected parentCol=" + parentCol + " totalCol=" + totalCol);
            if (parentCol != null && totalCol != null) {
                // Newer schema
                List<String> colsList = new ArrayList<>();
                List<Object> values = new ArrayList<>();
                // use the detected product column name (fall back to product_id)
                colsList.add(productCol != null ? productCol : "product_id"); values.add(c.getProductId());
                colsList.add(parentCol != null ? parentCol : "parent_id"); values.add(c.getParentId());
                colsList.add("quantity"); values.add(c.getQuantity());
                colsList.add(totalCol); values.add(c.getTotalPrice());
                if (hasStatus) { colsList.add("status"); values.add(c.getStatus()); }

                // Only add a small, curated set of defaults to avoid pulling unexpected legacy columns
                // Add payment flag default if present
                if (hasColumn("commande", "is_paid") && colsList.stream().noneMatch(s -> s.equalsIgnoreCase("is_paid"))) {
                    colsList.add("is_paid"); values.add(false);
                }
                if (hasColumn("commande", "paid") && colsList.stream().noneMatch(s -> s.equalsIgnoreCase("paid"))) {
                    colsList.add("paid"); values.add(false);
                }
                // Add date column if present
                if (hasColumn("commande", "date_commande") && colsList.stream().noneMatch(s -> s.equalsIgnoreCase("date_commande"))) {
                    colsList.add("date_commande"); values.add(new java.sql.Timestamp(System.currentTimeMillis()));
                } else if (hasColumn("commande", "created_at") && colsList.stream().noneMatch(s -> s.equalsIgnoreCase("created_at"))) {
                    colsList.add("created_at"); values.add(new java.sql.Timestamp(System.currentTimeMillis()));
                }

                // Force removal of unwanted legacy french column name to avoid duplicate/unknown column errors.
                // Keep canonical 'total_amount' when present.
                for (int i = colsList.size() - 1; i >= 0; i--) {
                    String col = colsList.get(i);
                    if (col != null && col.equalsIgnoreCase("montant_total")) {
                        // remove the column and its value
                        colsList.remove(i);
                        values.remove(i);
                        System.out.println("[CommandeService] Removed legacy column montant_total from insert list");
                    }
                }

                // Filter columns: keep only those that actually exist in the table, avoid duplicates
                List<String> finalCols = new ArrayList<>();
                List<Object> finalValues = new ArrayList<>();
                for (int i = 0; i < colsList.size(); i++) {
                    String col = colsList.get(i);
                    Object val = values.get(i);
                    if (!hasColumn("commande", col)) {
                        System.out.println("[CommandeService] Skipping non-existing column: " + col);
                        continue;
                    }
                    boolean already = finalCols.stream().anyMatch(s -> s.equalsIgnoreCase(col));
                    if (already) {
                        System.out.println("[CommandeService] Skipping duplicate column: " + col);
                        continue;
                    }
                    finalCols.add(col);
                    finalValues.add(val);
                }
                // Ensure required (NOT NULL without default) columns are present in the INSERT.
                // If a required column is missing, provide a reasonable value (prefer the parent id for user-like columns).
                Map<String, Integer> required = getRequiredColumns("commande");
                for (Map.Entry<String, Integer> req : required.entrySet()) {
                    String reqCol = req.getKey();
                    if (finalCols.stream().anyMatch(s -> s.equalsIgnoreCase(reqCol))) continue;
                    // Prefer to supply the parent/user id if this looks like the user column
                    if (reqCol.toLowerCase().contains("user") || reqCol.toLowerCase().contains("parent")) {
                        Object parentVal = c.getParentId();
                        if (parentVal == null) parentVal = defaultForColumn(reqCol, req.getValue());
                        finalCols.add(reqCol);
                        finalValues.add(parentVal);
                        System.out.println("[CommandeService] Added required user-like column to INSERT: " + reqCol + " value=" + parentVal);
                    } else {
                        Object def = defaultForColumn(reqCol, req.getValue());
                        finalCols.add(reqCol);
                        finalValues.add(def);
                        System.out.println("[CommandeService] Added required column to INSERT: " + reqCol + " default=" + def);
                    }
                }
                String colsJoined = String.join(", ", finalCols);
                String placeholders = String.join(", ", Collections.nCopies(finalCols.size(), "?"));
                String sql = "INSERT INTO commande (" + colsJoined + ") VALUES (" + placeholders + ")";
                System.out.println("[CommandeService] SQL (final): " + sql + " values=" + finalValues);
                // try executing with fallback in case some column name is wrong
                try {
                    tryExecuteInsert(sql, finalCols, finalValues);
                    try (PreparedStatement ps2 = cnx.prepareStatement("SELECT LAST_INSERT_ID()")) {
                        try (ResultSet rs = ps2.executeQuery()) { if (rs.next()) return rs.getInt(1); }
                    }
                } catch (SQLException ex) {
                    throw ex;
                }
                return 0;
            } else {
                // Fallback: try a minimal insert (product_id, quantity)
                String sql = "INSERT INTO commande (product_id, quantity) VALUES (?,?)";
                try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    try { ps.setQueryTimeout(10); } catch (Throwable ignore) {}
                    ps.setInt(1, c.getProductId());
                    ps.setInt(2, c.getQuantity());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
                }
                return 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // If schema problem, try to ensure parent_id exists and retry once
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown column")) {
                ensureParentIdColumn();
                // retry once
                try { return ajouter(c); } catch (RuntimeException ex) { throw ex; }
            }
            throw new RuntimeException("Erreur insertion commande: " + e.getMessage());
        }
    }

    /**
     * Retourne une map des colonnes qui sont NON NULLS et n'ont pas de valeur par défaut.
     * Key = column name, Value = java.sql.Types int
     */
    private Map<String, Integer> getRequiredColumns(String tableName) {
        Map<String, Integer> required = new HashMap<>();
        try {
            DatabaseMetaData meta = cnx.getMetaData();
            try (ResultSet cols = meta.getColumns(null, null, tableName, null)) {
                while (cols.next()) {
                    String name = cols.getString("COLUMN_NAME");
                    if (name == null) continue;
                    if ("id".equalsIgnoreCase(name)) continue; // skip PK id
                    String colDef = cols.getString("COLUMN_DEF");
                    int nullable = cols.getInt("NULLABLE");
                    String isAuto = null;
                    try { isAuto = cols.getString("IS_AUTOINCREMENT"); } catch (Exception ex) { }
                    if (isAuto != null && isAuto.equalsIgnoreCase("YES")) continue;
                    if (nullable == DatabaseMetaData.columnNoNulls && (colDef == null || colDef.trim().isEmpty())) {
                        int dataType = cols.getInt("DATA_TYPE");
                        required.put(name, dataType);
                    }
                }
            }
        } catch (SQLException e) {
            // ignore
        }
        return required;
    }

    private Object defaultForSqlType(int sqlType) {
        switch (sqlType) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return 0;
            case Types.BIT:
            case Types.BOOLEAN:
                return false;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new java.sql.Timestamp(System.currentTimeMillis());
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return "";
            default:
                return "";
        }
    }

    private Object defaultForColumn(String columnName, int sqlType) {
        if (columnName == null) return defaultForSqlType(sqlType);
        String n = columnName.trim().toLowerCase();
        // Special-case common paid flag
        if (n.equals("is_paid") || n.equals("paid") || n.equals("ispaid") || n.equals("paid_flag")) {
            return false; // non payée par défaut
        }
        return defaultForSqlType(sqlType);
    }

    private void setPreparedStatementValue(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) { ps.setObject(index, null); return; }
        if (value instanceof Integer) ps.setInt(index, (Integer) value);
        else if (value instanceof Long) ps.setLong(index, (Long) value);
        else if (value instanceof Double) ps.setDouble(index, (Double) value);
        else if (value instanceof Float) ps.setFloat(index, (Float) value);
        else if (value instanceof Boolean) ps.setBoolean(index, (Boolean) value);
        else if (value instanceof java.sql.Timestamp) ps.setTimestamp(index, (java.sql.Timestamp) value);
        else ps.setString(index, String.valueOf(value));
    }

    /**
     * Vérifie si la colonne parent_id existe dans la table commande, et la crée si nécessaire.
     */
    private void ensureParentIdColumn() {
        try {
            DatabaseMetaData md = cnx.getMetaData();
            try (ResultSet cols = md.getColumns(null, null, "commande", "parent_id")) {
                if (cols.next()) {
                    return; // colonne existe
                }
            }

            // Column does not exist — attempt to add it. Use a simple INT allowing NULL to be safe.
            String alter = "ALTER TABLE commande ADD COLUMN parent_id INT";
            try (Statement st = cnx.createStatement()) {
                st.executeUpdate(alter);
                System.out.println("Schéma mis à jour: colonne 'parent_id' ajoutée à la table 'commande'.");
            }
        } catch (SQLException e) {
            // Log but don't crash here; higher-level code will handle insertion failure.
            System.out.println("Impossible de vérifier/ajouter la colonne parent_id: " + e.getMessage());
        }
    }

    public List<Commande> afficherParParent(int parentId) {
        List<Commande> list = new ArrayList<>();
        try {
            boolean hasParentId = hasColumn("commande", "parent_id");
            boolean hasUserId = hasColumn("commande", "user_id");
            boolean hasCreatedAt = hasColumn("commande", "created_at");
            boolean hasDateCommande = hasColumn("commande", "date_commande");
            PreparedStatement ps;
            if (hasParentId) {
                String order = hasCreatedAt ? "created_at DESC" : (hasDateCommande ? "date_commande DESC" : "id DESC");
                ps = cnx.prepareStatement("SELECT * FROM commande WHERE parent_id=? ORDER BY " + order);
                ps.setInt(1, parentId);
            } else if (hasUserId) {
                String order = hasDateCommande ? "date_commande DESC" : (hasCreatedAt ? "created_at DESC" : "id DESC");
                ps = cnx.prepareStatement("SELECT * FROM commande WHERE user_id=? ORDER BY " + order);
                ps.setInt(1, parentId);
            } else {
                // No user column available: return all and filter in memory by id match if possible
                ps = cnx.prepareStatement("SELECT * FROM commande ORDER BY " + (hasCreatedAt ? "created_at DESC" : (hasDateCommande ? "date_commande DESC" : "id DESC")));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande cmd = mapRow(rs);
                    // if neither parent_id nor user_id existed, try to match by id in a possible user_id column stored elsewhere
                    if (!hasParentId && !hasUserId) {
                        // no reliable way to filter, just add
                        list.add(cmd);
                    } else {
                        list.add(cmd);
                    }
                }
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    /** Supprime une commande par son id. */
    public void supprimer(int id) {
        try {
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM commande WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur suppression commande: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Mark a commande as paid and set status text. This method is resilient to different schema names
     * (is_paid / paid / status) and will only update columns that exist.
     */
    public void updatePaymentStatus(int commandeId, boolean paid, String status) throws SQLException {
        // Build SET clauses depending on existing columns
        java.util.List<String> sets = new java.util.ArrayList<>();
        java.util.List<Object> values = new java.util.ArrayList<>();
        if (hasColumn("commande", "is_paid")) {
            sets.add("is_paid = ?"); values.add(paid ? 1 : 0);
        } else if (hasColumn("commande", "paid")) {
            sets.add("paid = ?"); values.add(paid ? 1 : 0);
        }
        if (status != null && !status.isBlank() && hasColumn("commande", "status")) {
            sets.add("status = ?"); values.add(status);
        }

        if (sets.isEmpty()) {
            // Nothing to update in DB schema
            return;
        }

        String sql = "UPDATE commande SET " + String.join(", ", sets) + " WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                setPreparedStatementValue(ps, i + 1, values.get(i));
            }
            ps.setInt(values.size() + 1, commandeId);
            ps.executeUpdate();
        }
    }

    /**
     * Modifie une commande existante (quantité / total_price / status si présent).
     */
    public void modifier(Commande c) {
        if (c == null || c.getId() <= 0) throw new IllegalArgumentException("Commande invalide pour modification");
        String totalCol = findExistingColumn("commande", "total_price", "total_amount", "montant_total", "total", "amount");
        java.util.List<String> sets = new java.util.ArrayList<>();
        java.util.List<Object> values = new java.util.ArrayList<>();
        // quantity
        if (resultHasColumnNameInTable("commande", "quantity")) {
            sets.add("quantity = ?"); values.add(c.getQuantity());
        }
        if (totalCol != null) {
            sets.add(totalCol + " = ?"); values.add(c.getTotalPrice());
        }
        if (c.getStatus() != null && hasColumn("commande", "status")) {
            sets.add("status = ?"); values.add(c.getStatus());
        }
        if (sets.isEmpty()) return; // nothing to do
        String sql = "UPDATE commande SET " + String.join(", ", sets) + " WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) setPreparedStatementValue(ps, i + 1, values.get(i));
            ps.setInt(values.size() + 1, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification commande: " + e.getMessage(), e);
        }
    }

    // helper: quick check for column existence using cached mechanism
    private boolean resultHasColumnNameInTable(String tableName, String columnName) {
        return hasColumn(tableName, columnName);
    }

    private Commande mapRow(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setId(rs.getInt("id"));
        // product id support (product_id or id_product_id)
        if (resultHasColumn(rs, "product_id")) {
            c.setProductId(rs.getInt("product_id"));
        } else if (resultHasColumn(rs, "id_product_id")) {
            c.setProductId(rs.getInt("id_product_id"));
        } else if (resultHasColumn(rs, "id_product")) {
            c.setProductId(rs.getInt("id_product"));
        }
        // Support multiple parent/user column names
        if (resultHasColumn(rs, "parent_id")) {
            c.setParentId(rs.getInt("parent_id"));
        } else if (resultHasColumn(rs, "user_id")) {
            c.setParentId(rs.getInt("user_id"));
        } else if (resultHasColumn(rs, "id_user_id")) {
            c.setParentId(rs.getInt("id_user_id"));
        }
        if (resultHasColumn(rs, "quantity")) c.setQuantity(rs.getInt("quantity"));
        // total price support
        if (resultHasColumn(rs, "total_price")) {
            c.setTotalPrice(rs.getDouble("total_price"));
        } else if (resultHasColumn(rs, "total_amount")) {
            c.setTotalPrice(rs.getDouble("total_amount"));
        } else if (resultHasColumn(rs, "montant_total")) {
            c.setTotalPrice(rs.getDouble("montant_total"));
        } else if (resultHasColumn(rs, "total")) {
            c.setTotalPrice(rs.getDouble("total"));
        } else if (resultHasColumn(rs, "amount")) {
            c.setTotalPrice(rs.getDouble("amount"));
        }
        if (resultHasColumn(rs, "status")) c.setStatus(rs.getString("status"));
        // created_at / date_commande handled by DB; parse if present
        try {
            if (resultHasColumn(rs, "date_commande")) {
                java.sql.Timestamp ts = rs.getTimestamp("date_commande");
                if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
            } else if (resultHasColumn(rs, "created_at")) {
                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
            }
        } catch (Exception ignore) {}
        return c;
    }

    private boolean hasColumn(String tableName, String columnName) {
        if (tableName == null || columnName == null) return false;
        synchronized (columnsCache) {
            java.util.Set<String> cols = columnsCache.get(tableName);
            if (cols != null) return cols.contains(columnName.toLowerCase());
        }
        try {
            DatabaseMetaData meta = cnx.getMetaData();
            java.util.Set<String> found = new java.util.HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, tableName, null)) {
                while (cols.next()) {
                    String n = cols.getString("COLUMN_NAME");
                    if (n != null) found.add(n.toLowerCase());
                }
            }
            if (found.isEmpty()) {
                try (ResultSet cols2 = meta.getColumns(null, null, tableName.toUpperCase(), null)) {
                    while (cols2.next()) {
                        String n = cols2.getString("COLUMN_NAME");
                        if (n != null) found.add(n.toLowerCase());
                    }
                }
            }
            synchronized (columnsCache) { columnsCache.put(tableName, found); }
            return found.contains(columnName.toLowerCase());
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean resultHasColumn(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String label = md.getColumnLabel(i);
                if (label == null) label = md.getColumnName(i);
                if (columnName.equalsIgnoreCase(label)) return true;
            }
        } catch (SQLException e) {
            // ignore
        }
        return false;
    }

    /**
     * Return the first existing column name from candidates (case-insensitive), or null.
     */
    private String findExistingColumn(String tableName, String... candidates) {
        if (candidates == null || candidates.length == 0) return null;
        for (String c : candidates) {
            if (c == null) continue;
            if (hasColumn(tableName, c)) return c;
        }
        // Fallback: try to detect a reasonable column name containing 'user' or 'parent'
        try {
            DatabaseMetaData meta = cnx.getMetaData();
            try (ResultSet cols = meta.getColumns(null, null, tableName, null)) {
                while (cols.next()) {
                    String n = cols.getString("COLUMN_NAME");
                    if (n == null) continue;
                    String lower = n.toLowerCase();
                    if (lower.contains("user") || lower.contains("parent")) {
                        return n; // return first matching column (e.g. user_id, parent_id, id_user_id)
                    }
                }
            }
        } catch (SQLException e) {
            // ignore and return null
        }
        return null;
    }

    /**
     * Try executing an INSERT built from colsList and values. If a SQLException occurs
     * complaining about Unknown column X, try to detect alternative column names
     * (for total or parent) and retry once.
     */
    private void tryExecuteInsert(String sql, List<String> colsList, List<Object> values) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try { ps.setQueryTimeout(10); } catch (Throwable ignore) {}
            for (int i = 0; i < values.size(); i++) setPreparedStatementValue(ps, i + 1, values.get(i));
            ps.executeUpdate();
            return;
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            // parse unknown column
            String unknown = null;
            try {
                int p1 = msg.indexOf('\'');
                int p2 = msg.indexOf('\'', p1 + 1);
                if (p1 >= 0 && p2 > p1) unknown = msg.substring(p1 + 1, p2);
            } catch (Exception ex) { }
            if (unknown != null) {
                System.out.println("[CommandeService] Unknown column reported: " + unknown + "; attempting fallback");
                // try to replace with an existing candidate for total or parent
                String[] totalCandidates = new String[]{"total_price","total_amount","montant_total","total","amount"};
                for (String cand : totalCandidates) {
                    if (cand.equalsIgnoreCase(unknown)) continue;
                    if (hasColumn("commande", cand)) {
                        List<String> newCols = new ArrayList<>(colsList);
                        boolean candExists = newCols.stream().anyMatch(s -> s.equalsIgnoreCase(cand));
                        // Build adjusted columns and values: if cand already exists, drop the unknown entry; else replace it
                        List<String> adjCols = new ArrayList<>();
                        List<Object> adjValues = new ArrayList<>();
                        for (int i = 0; i < newCols.size(); i++) {
                            String coln = newCols.get(i);
                            Object val = values.get(i);
                            if (coln.equalsIgnoreCase(unknown)) {
                                if (candExists) {
                                    // skip this pair to avoid duplicate column
                                    continue;
                                } else {
                                    adjCols.add(cand);
                                    adjValues.add(val);
                                }
                            } else {
                                adjCols.add(coln);
                                adjValues.add(val);
                            }
                        }
                        String colsJoined = String.join(", ", adjCols);
                        String placeholders = String.join(", ", Collections.nCopies(adjCols.size(), "?"));
                        String newSql = "INSERT INTO commande (" + colsJoined + ") VALUES (" + placeholders + ")";
                        System.out.println("[CommandeService] Retrying with column " + cand + ": " + newSql + " values=" + adjValues);
                        try (PreparedStatement ps2 = cnx.prepareStatement(newSql, Statement.RETURN_GENERATED_KEYS)) {
                            try { ps2.setQueryTimeout(10); } catch (Throwable ignore) {}
                            for (int i = 0; i < adjValues.size(); i++) setPreparedStatementValue(ps2, i + 1, adjValues.get(i));
                            ps2.executeUpdate();
                            return;
                        } catch (SQLException e2) {
                            // continue to next candidate
                        }
                    }
                }
            }
            throw e;
        }
    }
}

