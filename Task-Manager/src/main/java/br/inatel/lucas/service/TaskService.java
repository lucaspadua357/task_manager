package br.inatel.lucas.service;

import br.inatel.lucas.model.Task;
import br.inatel.lucas.repo.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    private final TaskRepository repo;

    public TaskService(TaskRepository repo) { this.repo = repo; }

    public void addTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank())
            throw new IllegalArgumentException("Título obrigatório");
        repo.save(task);
    }

    public List<Task> listDone() {
        return repo.findAll().stream().filter(Task::isDone).collect(Collectors.toList());
    }

    public List<Task> listPending() {
        return repo.findAll().stream().filter(t -> !t.isDone()).collect(Collectors.toList());
    }

    public void markDone(String id) {
        Task t = repo.findById(id).orElseThrow();
        t.markDone();
        repo.save(t);
    }

    public void remove(String id) {
        repo.delete(id);
    }
}