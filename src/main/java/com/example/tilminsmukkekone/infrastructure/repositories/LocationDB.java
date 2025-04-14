package com.example.tilminsmukkekone.infrastructure.repositories;

import com.example.tilminsmukkekone.domain.classes.Location;
import com.example.tilminsmukkekone.infrastructure.util.DatabaseException;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


@Component
public class LocationDB {

    private final DatabaseOperations dbOps;

    public LocationDB(DatabaseOperations dbOps) {
        this.dbOps = dbOps;
    }

    private Location mapResultSetToLocation(ResultSet rs) {
        try {
            Location location = new Location();
            if (rs.getMetaData().getColumnCount() > 4) {
                location.setId(rs.getLong("id"));
            }
            location.setName(rs.getString("name"));
            location.setAddress(rs.getString("address"));
            location.setLatitude(rs.getDouble("latitude"));
            location.setLongitude(rs.getDouble("longitude"));
            return location;
        } catch (SQLException e) {
            throw new DatabaseException("Error mapping database row to Location object",
                    "mapping", "location", e);
        }
    }

    public Optional<Location> findById(Long id) {
        try {
            String sql = "SELECT * FROM locations WHERE id = ?";
            Location location = dbOps.executeQueryForSingleResult(sql, new Object[]{id}, this::mapResultSetToLocation);
            return Optional.ofNullable(location);
        } catch (SQLException e) {
            throw DatabaseException.readError("location", id, e);
        }
    }


    public List<Location> findAll() {
        try {
            String sql = "SELECT * FROM locations";
            return dbOps.executeQuery(sql, null, this::mapResultSetToLocation);
        } catch (SQLException e) {
            throw DatabaseException.createError("read_all", "location", e);
        }
    }

    public Long save(Location location) {
        try {
            String sql = "INSERT INTO locations (name, address, latitude, longitude) VALUES (?, ?, ?, ?)";

            Object[] params = {
                    location.getName(),
                    location.getAddress(),
                    location.getLatitude(),
                    location.getLongitude()
            };

            return dbOps.executeInsertAndGetId(sql, params);
        } catch (SQLException e) {
            throw DatabaseException.writeError("location", e);
        }
    }

    public boolean update(Location location) {
        try {
            String sql = "UPDATE locations SET name = ?, address = ?, latitude = ?, longitude = ? WHERE id = ?";
            Object[] params = new Object[]{
                    location.getName(),
                    location.getAddress(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getId() // Added the missing ID parameter
            };

            int rowsAffected = dbOps.executeUpdate(sql, params);
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("location", location.getId(), e);
        }
    }

    public boolean deleteById(Long id) {
        try {
            String sql = "DELETE FROM locations WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{id});
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("location", id, e);
        }
    }

    public List<Location> findByName(String name) {
        try {
            String sql = "SELECT * FROM locations WHERE name LIKE ?";
            return dbOps.executeQuery(sql, new Object[]{"%" + name + "%"}, this::mapResultSetToLocation);
        } catch (SQLException e) {
            throw DatabaseException.readError("locations_by_name", name, e);
        }
    }

    public List<Location> findByCoordinates(double latitude, double longitude, double radiusKm) {
        try {
            String sql = "SELECT *, (6371 * acos(cos(radians(?)) * cos(radians(latitude)) * " +
                    "cos(radians(longitude) - radians(?)) + sin(radians(?)) * sin(radians(latitude)))) " +
                    "AS distance FROM locations HAVING distance < ? ORDER BY distance";

            Object[] params = new Object[]{latitude, longitude, latitude, radiusKm};
            return dbOps.executeQuery(sql, params, this::mapResultSetToLocation);
        } catch (SQLException e) {
            throw DatabaseException.readError("locations_by_coordinates",
                    latitude + "," + longitude + " radius:" + radiusKm, e);
        }
    }

    public Optional<Location> findByEventId(Long eventId) {
        try {
            String sql = "SELECT l.* FROM locations l " +
                    "INNER JOIN events e ON l.id = e.location_id " +
                    "WHERE e.id = ?";

            Location location = dbOps.executeQueryForSingleResult(sql, new Object[]{eventId}, this::mapResultSetToLocation);
            return Optional.ofNullable(location);
        } catch (SQLException e) {
            throw DatabaseException.readError("location_by_event", eventId, e);
        }
    }
}