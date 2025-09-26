package br.inatel.lucas;

import br.inatel.lucas.model.Task;
import br.inatel.lucas.repo.TaskRepository;
import br.inatel.lucas.service.TaskService;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Test
    void addTask_ok() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService svc = new TaskService(repo);
        Task task = new Task("Estudar Java", "Fazer exercícios");

        svc.addTask(task);

        verify(repo).save(task);
    }

    @Test
    void addTaskWithoutTitleThrowsException() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService svc = new TaskService(repo);

        assertThatThrownBy(() -> svc.addTask(new Task("", "desc")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markDone_ok() {
        TaskRepository repo = mock(TaskRepository.class);
        Task t = new Task("Pagar conta", "Até sexta");
        when(repo.findById("1")).thenReturn(Optional.of(t));

        TaskService svc = new TaskService(repo);
        svc.markDone("1");

        assertThat(t.isDone()).isTrue();
        verify(repo).save(t);
    }

    @Test
    void addTaskWithEmptyDescriptionIsAllowed() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService svc = new TaskService(repo);
        Task task = new Task("Study", "");

        svc.addTask(task);

        verify(repo).save(task);
    }

    @Test
    void listPendingWhenNoTasksReturnsEmptyList() {
        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findAll()).thenReturn(Collections.emptyList());
        TaskService svc = new TaskService(repo);

        List<Task> result = svc.listPending();

        assertThat(result).isEmpty();
    }

    @Test
    void listPendingReturnsOnlyPendingTasks() {
        Task pending = new Task("Pending", "task");
        Task done = new Task("Done", "task");
        done.markDone();

        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findAll()).thenReturn(List.of(pending, done));
        TaskService svc = new TaskService(repo);

        List<Task> result = svc.listPending();

        assertThat(result).containsExactly(pending);
    }

    @Test
    void listDoneReturnsOnlyCompletedTasks() {
        Task done = new Task("Completed", "task");
        done.markDone();

        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findAll()).thenReturn(List.of(done));
        TaskService svc = new TaskService(repo);

        List<Task> result = svc.listDone();

        assertThat(result).containsExactly(done);
    }

    @Test
    void listDoneWhenNoCompletedTasksReturnsEmptyList() {
        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findAll()).thenReturn(Collections.emptyList());
        TaskService svc = new TaskService(repo);

        List<Task> result = svc.listDone();

        assertThat(result).isEmpty();
    }

    @Test
    void markDoneWhenTaskDoesNotExistThrowsException() {
        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findById("X")).thenReturn(Optional.empty());
        TaskService svc = new TaskService(repo);

        assertThatThrownBy(() -> svc.markDone("X"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void markDoneWhenAlreadyCompletedKeepsAsCompleted() {
        Task task = new Task("Task", "desc");
        task.markDone();

        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findById("1")).thenReturn(Optional.of(task));
        TaskService svc = new TaskService(repo);

        svc.markDone("1");

        assertThat(task.isDone()).isTrue();
        verify(repo).save(task);
    }

    @Test
    void removeExistingTaskCallsDelete() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService svc = new TaskService(repo);

        svc.remove("1");

        verify(repo).delete("1");
    }

    @Test
    void addTaskWithDuplicateTitlesIsAllowed() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService svc = new TaskService(repo);

        Task t1 = new Task("SameTitle", "desc1");
        Task t2 = new Task("SameTitle", "desc2");

        svc.addTask(t1);
        svc.addTask(t2);

        verify(repo, times(2)).save(any(Task.class));
    }

    @Test
    void listAllReturnsAllTasks() {
        Task t1 = new Task("T1", "desc1");
        Task t2 = new Task("T2", "desc2");

        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findAll()).thenReturn(List.of(t1, t2));
        TaskService svc = new TaskService(repo);

        List<Task> result = repo.findAll();

        assertThat(result).hasSize(2).containsExactly(t1, t2);
    }

    @Test
    void addTaskWhenRepositoryFailsPropagatesException() {
        TaskRepository repo = mock(TaskRepository.class);
        doThrow(new RuntimeException("DB error")).when(repo).save(any(Task.class));
        TaskService svc = new TaskService(repo);

        assertThatThrownBy(() -> svc.addTask(new Task("Title", "desc")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    void markDoneMultipleCallsKeepsTaskCompleted() {
        Task task = new Task("Task", "desc");
        TaskRepository repo = mock(TaskRepository.class);
        when(repo.findById("1")).thenReturn(Optional.of(task));
        TaskService svc = new TaskService(repo);

        svc.markDone("1");
        svc.markDone("1");

        assertThat(task.isDone()).isTrue();
        verify(repo, times(2)).save(task);
    }

    @Test
    void editTaskTitleUpdatesCorrectly() {
        Task task = new Task("Old", "desc");
        task.setTitle("New");

        assertThat(task.getTitle()).isEqualTo("New");
    }

    @Test
    void editTaskDescriptionUpdatesCorrectly() {
        Task task = new Task("Title", "Old desc");
        task.setDescription("New desc");

        assertThat(task.getDescription()).isEqualTo("New desc");
    }

    @Test
    void editTaskTitleWithEmptyThrowsException() {
        Task task = new Task("Old", "desc");

        assertThatThrownBy(() -> task.setTitle(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void taskInitiallyIsNotDone() {
        Task task = new Task("Initial", "desc");

        assertThat(task.isDone()).isFalse();
    }

    @Test
    void addTaskTrimsTitleBeforeSaving() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService svc = new TaskService(repo);

        Task task = new Task("   Trim me   ", "desc");
        svc.addTask(task);

        assertThat(task.getTitle()).isEqualTo("Trim me");
        verify(repo).save(task);
    }

    @Test
    void removeTaskThatDoesNotExistDoesNotThrow() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService svc = new TaskService(repo);

        // assume repository silently ignores if ID does not exist
        assertThatCode(() -> svc.remove("non-existing-id"))
                .doesNotThrowAnyException();

        verify(repo).delete("non-existing-id");
    }

    @Test
    void taskIdIsUniqueForEachTask() {
        Task t1 = new Task("Task1", "desc1");
        Task t2 = new Task("Task2", "desc2");

        assertThat(t1.getId()).isNotEqualTo(t2.getId());
        assertThat(t1.getId()).isNotBlank();
        assertThat(t2.getId()).isNotBlank();
    }
}
