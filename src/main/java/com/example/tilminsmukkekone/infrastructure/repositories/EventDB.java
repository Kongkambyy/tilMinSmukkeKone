package com.example.tilminsmukkekone.infrastructure.repositories;

import com.example.tilminsmukkekone.domain.classes.Event;
import com.example.tilminsmukkekone.domain.classes.Location;
import com.example.tilminsmukkekone.domain.classes.Memory;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.EventType;
import com.example.tilminsmukkekone.infrastructure.util.DatabaseException;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class EventDB {

    private final DatabaseOperations dbOps;
    private final UserDB userDB;
    private final MemoryDB memoryDB;
    private final LocationDB locationDB;

    public EventDB(DatabaseOperations dbOps, UserDB userDB, MemoryDB memoryDB, LocationDB locationDB) {
        this.dbOps = dbOps;
        this.userDB = userDB;
        this.memoryDB = memoryDB;
        this.locationDB = locationDB;
    }

    private Event mapResultSetToEvent(ResultSet rs) {
        try {
            Event event = new Event();
            event.setId(rs.getLong("id"));
            event.setTitle(rs.getString("title"));
            event.setDescription(rs.getString("description"));
            event.setEventDate(rs.getTimestamp("event_date").toLocalDateTime());
            event.setType(EventType.valueOf(rs.getString("event_type")));

            Long locationId = rs.getLong("location_id");
            if (!rs.wasNull()) {
                Optional<Location> location = locationDB.findById(locationId);
                location.ifPresent(event::setLocation);
            }

            return event;
        } catch (SQLException e) {
            throw new DatabaseException("Error mapping database row to Event object",
                    "mapping", "event", e);
        }
    }

    private List<User> findParticipantsByEventId(Long eventId) {
        try {
            String sql = "SELECT user_id FROM event_participants WHERE event_id = ?";
            List<Long> userIds = dbOps.executeQuery(sql, new Object[]{eventId}, rs -> {
                try {
                    return rs.getLong("user_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            List<User> participants = new ArrayList<>();
            for (Long userId : userIds) {
                userDB.findById(userId).ifPresent(participants::add);
            }

            return participants;
        } catch (SQLException e) {
            throw DatabaseException.readError("event_participants", eventId, e);
        }
    }

    private List<Memory> findRelatedMemoriesByEventId(Long eventId) {
        try {
            String sql = "SELECT memory_id FROM event_memories WHERE event_id = ?";
            List<Long> memoryIds = dbOps.executeQuery(sql, new Object[]{eventId}, rs -> {
                try {
                    return rs.getLong("memory_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            List<Memory> memories = new ArrayList<>();
            for (Long memoryId : memoryIds) {
                memoryDB.findById(memoryId).ifPresent(memories::add);
            }

            return memories;
        } catch (SQLException e) {
            throw DatabaseException.readError("event_memories", eventId, e);
        }
    }

    private void saveParticipants(Long eventId, List<User> participants) {
        try {
            String sql = "INSERT INTO event_participants (event_id, user_id) VALUES (?, ?)";
            List<Object[]> batchParams = new ArrayList<>();

            for (User participant : participants) {
                batchParams.add(new Object[]{eventId, participant.getId()});
            }

            dbOps.executeBatch(sql, batchParams);
        } catch (SQLException e) {
            throw DatabaseException.writeError("event_participants", e);
        }
    }

    private void saveRelatedMemories(Long eventId, List<Memory> memories) {
        try {
            String sql = "INSERT INTO event_memories (event_id, memory_id) VALUES (?, ?)";
            List<Object[]> batchParams = new ArrayList<>();

            for (Memory memory : memories) {
                batchParams.add(new Object[]{eventId, memory.getId()});
            }

            dbOps.executeBatch(sql, batchParams);
        } catch (SQLException e) {
            throw DatabaseException.writeError("event_memories", e);
        }
    }


    public Optional<Event> findById(Long id) {
        try {
            String sql = "SELECT * FROM events WHERE id = ?";
            Event event = dbOps.executeQueryForSingleResult(sql, new Object[]{id}, this::mapResultSetToEvent);

            if (event != null) {
                event.setParticipants(findParticipantsByEventId(id));
                event.setRelatedMemories(findRelatedMemoriesByEventId(id));
            }

            return Optional.ofNullable(event);
        } catch (SQLException e) {
            throw DatabaseException.readError("event", id, e);
        }
    }

    public List<Event> findAll() {
        try {
            String sql = "SELECT * FROM events ORDER BY event_date DESC";
            List<Event> events = dbOps.executeQuery(sql, null, this::mapResultSetToEvent);

            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            return events;
        } catch (SQLException e) {
            throw DatabaseException.createError("read_all", "event", e);
        }
    }

    public List<Event> findByEventType(EventType eventType) {
        try {
            String sql = "SELECT * FROM events WHERE event_type = ? ORDER BY event_date DESC";
            List<Event> events = dbOps.executeQuery(sql, new Object[]{eventType.name()}, this::mapResultSetToEvent);

            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            return events;
        } catch (SQLException e) {
            throw DatabaseException.readError("events_by_type", eventType.name(), e);
        }
    }

    public List<Event> findByParticipantId(Long userId) {
        try {
            String sql = "SELECT e.* FROM events e " +
                    "INNER JOIN event_participants ep ON e.id = ep.event_id " +
                    "WHERE ep.user_id = ? " +
                    "ORDER BY e.event_date DESC";

            List<Event> events = dbOps.executeQuery(sql, new Object[]{userId}, this::mapResultSetToEvent);

            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            return events;
        } catch (SQLException e) {
            throw DatabaseException.readError("events_by_participant", userId, e);
        }
    }

    public List<Event> findUpcomingEventsWithinAMonth () {
        try {
            String sql = "SELECT * FROM events WHERE event_date > NOW() AND event_date < DATE_ADD(NOW(), INTERVAL 1 MONTH) ORDER BY event_date ASC";
            List<Event> events = dbOps.executeQuery(sql, null, this::mapResultSetToEvent);

            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            return events;

        } catch (SQLException e) {
            throw DatabaseException.readError("upcoming_events", "future", e);
        }


    }

    public List<Event> findUpcomingEvents() {
        try {
            String sql = "SELECT * FROM events WHERE event_date > NOW() ORDER BY event_date ASC";
            List<Event> events = dbOps.executeQuery(sql, null, this::mapResultSetToEvent);

            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            return events;
        } catch (SQLException e) {
            throw DatabaseException.readError("upcoming_events", "future", e);
        }
    }

    public Long save(Event event) {
        try {
            Long locationId = null;
            if (event.getLocation() != null) {
                locationId = locationDB.save(event.getLocation());
            }

            String sql = "INSERT INTO events (title, description, event_date, event_type, location_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

            Object[] params = new Object[]{
                    event.getTitle(),
                    event.getDescription(),
                    Timestamp.valueOf(event.getEventDate()),
                    event.getType().name(),
                    locationId
            };

            Long eventId = dbOps.executeInsertAndGetId(sql, params);

            if (event.getParticipants() != null && !event.getParticipants().isEmpty()) {
                saveParticipants(eventId, event.getParticipants());
            }

            if (event.getRelatedMemories() != null && !event.getRelatedMemories().isEmpty()) {
                saveRelatedMemories(eventId, event.getRelatedMemories());
            }

            return eventId;
        } catch (SQLException e) {
            throw DatabaseException.writeError("event", e);
        }
    }

    public boolean update(Event event) {
        try {
            Long locationId = null;
            if (event.getLocation() != null) {
                locationId = locationDB.save(event.getLocation());
            }

            String sql = "UPDATE events SET title = ?, description = ?, event_date = ?, " +
                    "event_type = ?, location_id = ? WHERE id = ?";

            Object[] params = new Object[]{
                    event.getTitle(),
                    event.getDescription(),
                    Timestamp.valueOf(event.getEventDate()),
                    event.getType().name(),
                    locationId,
                    event.getId()
            };

            int rowsAffected = dbOps.executeUpdate(sql, params);

            if (event.getParticipants() != null) {
                String deleteParticipantsSql = "DELETE FROM event_participants WHERE event_id = ?";
                dbOps.executeUpdate(deleteParticipantsSql, new Object[]{event.getId()});

                saveParticipants(event.getId(), event.getParticipants());
            }

            if (event.getRelatedMemories() != null) {
                String deleteMemoriesSql = "DELETE FROM event_memories WHERE event_id = ?";
                dbOps.executeUpdate(deleteMemoriesSql, new Object[]{event.getId()});

                saveRelatedMemories(event.getId(), event.getRelatedMemories());
            }

            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("event", event.getId(), e);
        }
    }

    public boolean deleteById(Long id) {
        try {
            String deleteParticipantsSql = "DELETE FROM event_participants WHERE event_id = ?";
            dbOps.executeUpdate(deleteParticipantsSql, new Object[]{id});

            String deleteMemoriesSql = "DELETE FROM event_memories WHERE event_id = ?";
            dbOps.executeUpdate(deleteMemoriesSql, new Object[]{id});

            String sql = "DELETE FROM events WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{id});

            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("event", id, e);
        }
    }

    public List<Event> findEventsByWeek(LocalDate weekStartDate) {
        try {
            // Trin 1: Konverter startdato til LocalDateTime (starter ved midnat)
            LocalDateTime startDateTime = weekStartDate.atStartOfDay();

            // Trin 2: Beregn slutdatoen (7 dage senere ved midnat)
            LocalDateTime endDateTime = weekStartDate.plusDays(7).atStartOfDay();

            // Trin 3: Forbered SQL-forespørgslen med datointervallet
            String sql = "SELECT * FROM events WHERE event_date >= ? AND event_date < ? ORDER BY event_date";

            // Trin 4: Udfør forespørgslen med start- og slutdato som parametre
            List<Event> events = dbOps.executeQuery(sql,
                    new Object[]{Timestamp.valueOf(startDateTime), Timestamp.valueOf(endDateTime)},
                    this::mapResultSetToEvent);

            // Trin 5: For hver begivenhed, indlæs deltagere og relaterede minder
            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            // Trin 6: Returner den fulde liste af begivenheder
            return events;
        } catch (SQLException e) {
            // Trin 7: Håndter eventuelle database-fejl
            throw DatabaseException.readError("events_by_week", weekStartDate.toString(), e);
        }
    }

    public List<Event> findEventsByMonth(int month, int year) {
        try {
            // Trin 1: Opret start- og slutdatoer for måneden
            LocalDateTime startDateTime = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime endDateTime = startDateTime.plusMonths(1);

            // Trin 2: Forbered SQL-forespørgslen med datointervallet
            String sql = "SELECT * FROM events WHERE event_date >= ? AND event_date < ? ORDER BY event_date";

            // Trin 3: Udfør forespørgslen med start- og slutdato som parametre
            List<Event> events = dbOps.executeQuery(sql,
                    new Object[]{Timestamp.valueOf(startDateTime), Timestamp.valueOf(endDateTime)},
                    this::mapResultSetToEvent);

            // Trin 4: For hver begivenhed, indlæs deltagere og relaterede minder
            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            // Trin 5: Returner den fulde liste af begivenheder
            return events;
        } catch (SQLException e) {
            // Trin 6: Håndter eventuelle database-fejl
            throw DatabaseException.readError("events_by_month", month + "/" + year, e);
        }
    }

    public List<Event> findEventsByYear(int year) {
        try {
            // Trin 1: Opret start- og slutdatoer for året
            LocalDateTime startDateTime = LocalDate.of(year, 1, 1).atStartOfDay();
            LocalDateTime endDateTime = LocalDate.of(year + 1, 1, 1).atStartOfDay();

            // Trin 2: Forbered SQL-forespørgslen med datointervallet
            String sql = "SELECT * FROM events WHERE event_date >= ? AND event_date < ? ORDER BY event_date";

            // Trin 3: Udfør forespørgslen med start- og slutdato som parametre
            List<Event> events = dbOps.executeQuery(sql,
                    new Object[]{Timestamp.valueOf(startDateTime), Timestamp.valueOf(endDateTime)},
                    this::mapResultSetToEvent);

            // Trin 4: For hver begivenhed, indlæs deltagere og relaterede minder
            for (Event event : events) {
                event.setParticipants(findParticipantsByEventId(event.getId()));
                event.setRelatedMemories(findRelatedMemoriesByEventId(event.getId()));
            }

            // Trin 5: Returner den fulde liste af begivenheder
            return events;
        } catch (SQLException e) {
            // Trin 6: Håndter eventuelle database-fejl
            throw DatabaseException.readError("events_by_year", String.valueOf(year), e);
        }
    }
}