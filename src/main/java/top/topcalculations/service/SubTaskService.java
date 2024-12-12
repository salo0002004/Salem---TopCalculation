package top.topcalculations.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.topcalculations.model.Project;
import top.topcalculations.repository.SubTaskRepository;
import top.topcalculations.repository.TaskRepository;

import java.util.List;

@Service
public class SubTaskService {
    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private TaskRepository taskRepository;

    // Gemmer en underopgave i databasen
    public void saveSubTask(Project project) {
        subTaskRepository.saveSubTask(project); // Kald til repository-metode for at gemme underopgaven
    }

    // Opdaterer en underopgave i databasen
    public void updateSubTask(int id, Project subTask, String oldSubTaskName) {
        subTaskRepository.updateSubTask(id, subTask, oldSubTaskName); // Kald til repository-metode for at opdatere underopgaven
    }

    // Opdaterer en underopgaves status i databasen
    public void updateSubTaskStatus(Long id, String status) {
        subTaskRepository.updateSubTaskStatusByID(id, status);
    }
    // Sletter en underopgave i databasen
    public void deleteSubTask(int id) {
        subTaskRepository.deleteSubTask(id);
    }
    // Henter underopgaver baseret på ID fra databasen
    public List<Project> getSubTaskByID(Long id) {
        return subTaskRepository.findSubTaskByID(id); // Kald til repository-metode for at hente underopgave baseret på ID
    }
    public int getHighestWbsIndexForSubtasks(String mainTaskWBS) {
        // Get the highest WBS index from tasks and subtasks tables
        int highestIndexFromTasks = taskRepository.getHighestWbsIndexFromTasks(mainTaskWBS);
        int highestIndexFromSubTasks = subTaskRepository.getHighestWbsIndexFromSubTasks(mainTaskWBS);
        // Return the highest of the two indexes
        return Math.max(highestIndexFromTasks, highestIndexFromSubTasks);
    }
}