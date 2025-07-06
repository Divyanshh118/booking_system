package dao;

import model.Event;
import java.sql.SQLException;
import java.util.List;

public interface EventDao {
    void setup() throws SQLException;
    List<Event> getAllEnabledEvents() throws SQLException;
    List<Event> getAllEventsAdmin() throws SQLException;
    Event getEventByDetails(String eventName, String venue, String day) throws SQLException;
    void addEvent(Event event) throws SQLException;
    void updateEvent(Event event) throws SQLException;
    void deleteEvent(int eventId) throws SQLException;
    boolean eventExists(String eventName, String venue, String day) throws SQLException;
    void updateEventDisabledStatus(int eventId, boolean disabled) throws SQLException;
    void addInitialEvents(List<Event> events) throws SQLException;
    Event getEventById(int id) throws SQLException;
    List<Event> getEventsByName(String eventName) throws SQLException;
}