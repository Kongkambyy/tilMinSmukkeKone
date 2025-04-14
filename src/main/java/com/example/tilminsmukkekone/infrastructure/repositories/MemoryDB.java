package com.example.tilminsmukkekone.infrastructure.repositories;

import com.example.tilminsmukkekone.domain.classes.Memory;
import com.example.tilminsmukkekone.domain.classes.User;
import com.example.tilminsmukkekone.domain.enums.MemoryType;
import com.example.tilminsmukkekone.infrastructure.util.DatabaseException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MemoryDB {

    private final DatabaseOperations dbOps;
    private final UserDB userDB;

    public MemoryDB(DataSource dataSource, UserDB userDB) {
        this.dbOps = new DatabaseOperations(dataSource);
        this.userDB = userDB;
    }


    private Memory mapResultSetToMemory(ResultSet rs) {
        try {
            Memory memory = new Memory();
            memory.setId(rs.getLong("id"));
            memory.setTitle(rs.getString("title"));
            memory.setDescription(rs.getString("description"));
            memory.setDateOfEvent(rs.getTimestamp("date_of_event").toLocalDateTime());
            memory.setLocation(rs.getString("location"));
            memory.setMemoryType(MemoryType.valueOf(rs.getString("memory_type")));

            Long creatorId = rs.getLong("creator_id");
            Optional<User> creator = userDB.findById(creatorId);
            creator.ifPresent(memory::setCreator);

            return memory;
        } catch (SQLException e) {
            throw new DatabaseException("Error mapping database row to Memory object",
                    "mapping", "memory", e);
        }
    }

    private List<String> findImagePathsByMemoryId(Long memoryId) {
        try {
            String sql = "SELECT image_path FROM memory_images WHERE memory_id = ?";
            return dbOps.executeQuery(sql, new Object[]{memoryId}, rs -> {
                try {
                    return rs.getString("image_path");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw DatabaseException.readError("memory_images", memoryId, e);
        }
    }

    public Optional<Memory> findById(Long id) {
        try {
            String sql = "SELECT * FROM memories WHERE id = ?";
            Memory memory = dbOps.executeQueryForSingleResult(sql, new Object[]{id}, this::mapResultSetToMemory);

            if (memory != null) {
                memory.setImagePaths(findImagePathsByMemoryId(id));
            }

            return Optional.ofNullable(memory);
        } catch (SQLException e) {
            throw DatabaseException.readError("memory", id, e);
        }
    }

    public List<Memory> findAll() {
        try {
            String sql = "SELECT * FROM memories ORDER BY date_of_event DESC";
            List<Memory> memories = dbOps.executeQuery(sql, null, this::mapResultSetToMemory);

            for (Memory memory : memories) {
                memory.setImagePaths(findImagePathsByMemoryId(memory.getId()));
            }

            return memories;
        } catch (SQLException e) {
            throw DatabaseException.createError("read_all", "memory", e);
        }
    }

    private void saveImagePaths(Long memoryId, List<String> imagePaths) {
        try {
            String sql = "INSERT INTO memory_images (memory_id, image_path) VALUES (?, ?)";
            List<Object[]> batchParams = new ArrayList<>();

            for (String path : imagePaths) {
                batchParams.add(new Object[]{memoryId, path});
            }

            dbOps.executeBatch(sql, batchParams);
        } catch (SQLException e) {
            throw DatabaseException.writeError("memory_images", e);
        }
    }

    public Long save(Memory memory) {
        try {
            String sql = "INSERT INTO memories (title, description, date_of_event, location, " +
                    "memory_type, creator_id) VALUES (?, ?, ?, ?, ?, ?)";

            Object[] params = new Object[]{
                    memory.getTitle(),
                    memory.getDescription(),
                    Timestamp.valueOf(memory.getDateOfEvent()),
                    memory.getLocation(),
                    memory.getMemoryType().name(),
                    memory.getCreator().getId()
            };

            Long memoryId = dbOps.executeInsertAndGetId(sql, params);

            if (memory.getImagePaths() != null && !memory.getImagePaths().isEmpty()) {
                saveImagePaths(memoryId, memory.getImagePaths());
            }

            return memoryId;
        } catch (SQLException e) {
            throw DatabaseException.writeError("memory", e);
        }
    }

    public boolean update(Memory memory) {
        try {
            String sql = "UPDATE memories SET title = ?, description = ?, date_of_event = ?, " +
                    "location = ?, memory_type = ?, creator_id = ? WHERE id = ?";

            Object[] params = new Object[]{
                    memory.getTitle(),
                    memory.getDescription(),
                    Timestamp.valueOf(memory.getDateOfEvent()),
                    memory.getLocation(),
                    memory.getMemoryType().name(),
                    memory.getCreator().getId(),
                    memory.getId()
            };

            int rowsAffected = dbOps.executeUpdate(sql, params);

            if (memory.getImagePaths() != null) {
                String deleteImagesSql = "DELETE FROM memory_images WHERE memory_id = ?";
                dbOps.executeUpdate(deleteImagesSql, new Object[]{memory.getId()});

                saveImagePaths(memory.getId(), memory.getImagePaths());
            }

            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.updateError("memory", memory.getId(), e);
        }
    }

    public boolean deleteById(Long id) {
        try {
            String deleteImagesSql = "DELETE FROM memory_images WHERE memory_id = ?";
            dbOps.executeUpdate(deleteImagesSql, new Object[]{id});

            String deleteCommentsSql = "DELETE FROM comments WHERE memory_id = ?";
            dbOps.executeUpdate(deleteCommentsSql, new Object[]{id});

            String deleteEventMemoriesSql = "DELETE FROM event_memories WHERE memory_id = ?";
            dbOps.executeUpdate(deleteEventMemoriesSql, new Object[]{id});

            String sql = "DELETE FROM memories WHERE id = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{id});

            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("memory", id, e);
        }
    }

    public List<Memory> findByCreatorId(Long creatorId) {
        try {
            String sql = "SELECT * FROM memories WHERE creator_id = ? ORDER BY date_of_event DESC";
            List<Memory> memories = dbOps.executeQuery(sql, new Object[]{creatorId}, this::mapResultSetToMemory);

            // Load images for each memory
            for (Memory memory : memories) {
                memory.setImagePaths(findImagePathsByMemoryId(memory.getId()));
            }

            return memories;
        } catch (SQLException e) {
            throw DatabaseException.readError("memories_by_creator", creatorId, e);
        }
    }

    public List<Memory> findByMemoryType(MemoryType memoryType) {
        try {
            String sql = "SELECT * FROM memories WHERE memory_type = ? ORDER BY date_of_event DESC";
            List<Memory> memories = dbOps.executeQuery(sql, new Object[]{memoryType.name()}, this::mapResultSetToMemory);

            for (Memory memory : memories) {
                memory.setImagePaths(findImagePathsByMemoryId(memory.getId()));
            }

            return memories;
        } catch (SQLException e) {
            throw DatabaseException.readError("memories_by_type", memoryType, e);
        }
    }

    public boolean addImagePath(Long memoryId, String imagePath) {
        try {
            String sql = "INSERT INTO memory_images (memory_id, image_path) VALUES (?, ?)";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{memoryId, imagePath});
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.writeError("memory_image", e);
        }
    }

    public boolean removeImagePath(Long memoryId, String imagePath) {
        try {
            String sql = "DELETE FROM memory_images WHERE memory_id = ? AND image_path = ?";
            int rowsAffected = dbOps.executeUpdate(sql, new Object[]{memoryId, imagePath});
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw DatabaseException.deleteError("memory_image", memoryId + ":" + imagePath, e);
        }
    }

    public int countMemories() {
        try {
            String sql = "SELECT COUNT(*) FROM memories";
            Integer count = dbOps.executeQueryForSingleResult(sql, null, rs -> {
                try {
                    return rs.getInt(1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return count != null ? count : 0;
        } catch (SQLException e) {
            throw DatabaseException.createError("count", "memory", e);
        }
    }

    public List<Memory> findByEventId(Long eventId) {
        try {
            String sql = "SELECT m.* FROM memories m " +
                    "INNER JOIN event_memories em ON m.id = em.memory_id " +
                    "WHERE em.event_id = ? " +
                    "ORDER BY m.date_of_event DESC";

            List<Memory> memories = dbOps.executeQuery(sql, new Object[]{eventId}, this::mapResultSetToMemory);

            for (Memory memory : memories) {
                memory.setImagePaths(findImagePathsByMemoryId(memory.getId()));
            }

            return memories;
        } catch (SQLException e) {
            throw DatabaseException.readError("memories_by_event", eventId, e);
        }
    }
}