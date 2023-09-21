
<only the complete Code with the correction>

private static boolean isUserInContext(String username, String context) {
    String sql = "SELECT * FROM users WHERE username =? AND context =?";
    PreparedStatement pStatement = null;
    try {
        pStatement = connection().prepareStatement(sql);
        pStatement.setString(1, username);
        pStatement.setString(2, context);
        ResultSet resultSet = pStatement.executeQuery();
        return resultSet.next();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (pStatement!= null) {
                pStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return false;
}

public static boolean isAdmin(String username, String context) {
    return isUserInContext(username, context);
}

public static boolean isUser(String username, String password, String context) {
    String sql = "SELECT * FROM users WHERE username =? AND password =?";
    PreparedStatement pStatement = null;
    try {
        pStatement = connection().prepareStatement(sql);
        pStatement.setString(1, username);
        pStatement.setString(2, md5(password));
        ResultSet resultSet = pStatement.executeQuery();
        return resultSet.next();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (pStatement!= null) {
                pStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return false;
}

public static boolean insertUser(String username, String password, String context) {
    String sql = "INSERT INTO users (user_id, username, password, context) VALUES (?,?,?,?)";
    PreparedStatement pStatement = null;
    try {
        pStatement = connection().prepareStatement(sql);
        pStatement.setString(1, UUID.randomUUID().toString());
        pStatement.setString(2, username);
        pStatement.setString(3, md5(password));
        pStatement.setString(4, context);
        pStatement.executeUpdate();
        return isUser(username, password, context);
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (pStatement!= null) {
                pStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return false;
}

public static boolean insertComment(String username, String body, String context) {
    String sql = "INSERT INTO comments (id, username, body, context) VALUES (?,?,?,?)";
    PreparedStatement pStatement = null;
    try {
        pStatement = connection().prepareStatement(sql);
        pStatement.setString(1, UUID.randomUUID().toString());
        pStatement.setString(2, username);
        pStatement.setString(3, body);
        pStatement.setString(4, context);
        pStatement.executeUpdate();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (pStatement!= null) {
                pStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return false;
}
