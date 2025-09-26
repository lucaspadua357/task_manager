package br.inatel.lucas.repo;

import br.inatel.lucas.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    void save(Task task);
    Optional<Task> findById(String id);
    List<Task> findAll();
    void delete(String id);
}