package dao;

import model.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EventDaoImpl implements EventDao {

    private final String TABLE_NAME = "events";

    @Override
    public void setup() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event TEXT NOT NULL," +
                    "venue TEXT NOT NULL," +
                    "day TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "sold INTEGER NOT NULL," +
                    "total INTEGER NOT NULL," +
                    "disabled BOOLEAN DEFAULT FALSE)";
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public List<Event> getAllEnabledEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT id, event, venue, day, price, sold, total, disabled FROM " + TABLE_NAME + " WHERE disabled = FALSE";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("id"),
                        rs.getString("event"),
                        rs.getString("venue"),
                        rs.getString("day"),
                        rs.getDouble("price"),
                        rs.getInt("sold"),
                        rs.getInt("total"),
                        rs.getBoolean("disabled")
                ));
            }
        }
        return events;
    }

    @Override
    public List<Event> getAllEventsAdmin() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT id, event, venue, day, price, sold, total, disabled FROM " + TABLE_NAME;
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("id"),
                        rs.getString("event"),
                        rs.getString("venue"),
                        rs.getString("day"),
                        rs.getDouble("price"),
                        rs.getInt("sold"),
                        rs.getInt("total"),
                        rs.getBoolean("disabled")
                ));
            }
        }
        return events;
    }

    @Override
    public Event getEventByDetails(String eventName, String venue, String day) throws SQLException {
        String sql = "SELECT id, event, venue, day, price, sold, total, disabled FROM " + TABLE_NAME + " WHERE event = ? AND venue = ? AND day = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eventName);
            pstmt.setString(2, venue);
            pstmt.setString(3, day);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Event(
                            rs.getInt("id"),
                            rs.getString("event"),
                            rs.getString("venue"),
                            rs.getString("day"),
                            rs.getDouble("price"),
                            rs.getInt("sold"),
                            rs.getInt("total"),
                            rs.getBoolean("disabled")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public void updateEvent(Event event) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET event = ?, venue = ?, day = ?, price = ?, sold = ?, total = ?, disabled = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, event.getEvent());
            pstmt.setString(2, event.getVenue());
            pstmt.setString(3, event.getDay());
            pstmt.setDouble(4, event.getPrice());
            pstmt.setInt(5, event.getSold());
            pstmt.setInt(6, event.getTotal());
            pstmt.setBoolean(7, event.isDisabled());
            pstmt.setInt(8, event.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void updateEventDisabledStatus(int eventId, boolean disabled) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET disabled = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, disabled);
            pstmt.setInt(2, eventId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void addEvent(Event event) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + "(event, venue, day, price, sold, total, disabled) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, event.getEvent());
            pstmt.setString(2, event.getVenue());
            pstmt.setString(3, event.getDay());
            pstmt.setDouble(4, event.getPrice());
            pstmt.setInt(5, event.getSold());
            pstmt.setInt(6, event.getTotal());
            pstmt.setBoolean(7, event.isDisabled());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteEvent(int eventId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean eventExists(String eventName, String venue, String day) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE event = ? AND venue = ? AND day = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eventName);
            pstmt.setString(2, venue);
            pstmt.setString(3, day);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public void addInitialEvents(List<Event> currentHardcodedEvents) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            
            // If table is empty, add initial events
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("No events found in database. Adding initial events...");
                
                List<Event> eventsToAdd = new ArrayList<>();
                
                // First try to read from events.dat
                try (BufferedReader br = new BufferedReader(new FileReader("events.dat"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split(";");
                        if (parts.length >= 4) {
                            try {
                                String eventName = parts[0].trim();
                                String venue = parts[1].trim();
                                String day = parts[2].trim();
                                double price = Double.parseDouble(parts[3].trim());
                                // Default values for sold and total if not provided
                                int sold = parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 0;
                                int total = parts.length > 5 ? Integer.parseInt(parts[5].trim()) : 100; // Default to 100 seats if not provided
                                Event newEvent = new Event(eventName, venue, day, price, sold, total);
                                eventsToAdd.add(newEvent);
                                System.out.println("Added event from file: " + eventName + " at " + venue + " on " + day);
                            } catch (NumberFormatException e) {
                                System.err.println("Skipping invalid event data line: " + line + " - " + e.getMessage());
                            }
                        } else if (!line.trim().isEmpty()) {
                            System.err.println("Skipping malformed line (incorrect number of parts): " + line);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("events.dat not found or error reading file. Using hardcoded events.");
                }
                
                // If no events from file, use hardcoded events
                if (eventsToAdd.isEmpty() && currentHardcodedEvents != null) {
                    System.out.println("Using hardcoded events");
                    eventsToAdd.addAll(currentHardcodedEvents);
                }
                
                // Add events to database
                if (!eventsToAdd.isEmpty()) {
                    String insertSql = "INSERT INTO " + TABLE_NAME + 
                                     "(event, venue, day, price, sold, total, disabled) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                        for (Event event : eventsToAdd) {
                            pstmt.setString(1, event.getEvent());
                            pstmt.setString(2, event.getVenue());
                            pstmt.setString(3, event.getDay());
                            pstmt.setDouble(4, event.getPrice());
                            pstmt.setInt(5, event.getSold());
                            pstmt.setInt(6, event.getTotal());
                            pstmt.setBoolean(7, false); // Not disabled by default
                            pstmt.addBatch();
                        }
                        int[] results = pstmt.executeBatch();
                        System.out.println("Successfully added " + results.length + " events to database");
                    } catch (SQLException e) {
                        System.err.println("Error adding initial events to database: " + e.getMessage());
                        throw e;
                    }
                } else {
                    System.err.println("No events to add to database");
                }
            } else {
                System.out.println("Database already contains events. Skipping initial event insertion.");
            }
        }
    }

    @Override
    public Event getEventById(int id) throws SQLException {
        String sql = "SELECT id, event, venue, day, price, sold, total, disabled FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Event(
                            rs.getInt("id"),
                            rs.getString("event"),
                            rs.getString("venue"),
                            rs.getString("day"),
                            rs.getDouble("price"),
                            rs.getInt("sold"),
                            rs.getInt("total"),
                            rs.getBoolean("disabled")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Event> getEventsByName(String eventName) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT id, event, venue, day, price, sold, total, disabled FROM " + TABLE_NAME + " WHERE event = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eventName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(new Event(
                            rs.getInt("id"),
                            rs.getString("event"),
                            rs.getString("venue"),
                            rs.getString("day"),
                            rs.getDouble("price"),
                            rs.getInt("sold"),
                            rs.getInt("total"),
                            rs.getBoolean("disabled")
                    ));
                }
            }
        }
        return events;
    }
}