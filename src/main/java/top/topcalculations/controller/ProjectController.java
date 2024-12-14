package top.topcalculations.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import top.topcalculations.model.Project;
import top.topcalculations.model.User;
import top.topcalculations.service.ProjectService;
import top.topcalculations.service.SubTaskService;
import top.topcalculations.service.TaskService;
import top.topcalculations.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/")  // Root URL for controller
public class ProjectController {
    private final ProjectService projectService;  // Service til at håndtere projekter
    private final UserService userService;  // Service til at håndtere brugere
    private final TaskService taskService;
    private final SubTaskService subTaskService;

    // Konstruktor til at injicere ProjectService og UserService afhængigheder
    public ProjectController(ProjectService projectService, UserService userService, TaskService taskService, SubTaskService subTaskService) {
        this.projectService = projectService;
        this.userService = userService;
        this.taskService = taskService;
        this.subTaskService = subTaskService;
    }

    // Vis index-siden
    @GetMapping("/")
    public String homePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }
        return "index"; // Thymeleaf will render index.html
    }

    // Vis formularen til at tilføje et projekt eller en opgave
    @GetMapping("/add")
    public String showAddForm(Model model, HttpSession session) {
        // Tjek om brugeren er logget ind, ellers send til login
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }

        List<Project> projects = projectService.getAllProjects();  // Hent alle hovedprojekter
        model.addAttribute("projects", projects);  // Tilføj projekter til model
        model.addAttribute("project", new Project());  // Tilføj et nyt tomt projekt til model
        return "add";  // Returner formularen til tilføjelse af projekt
    }

    @PostMapping("/add")
    public String submitAddForm(@ModelAttribute Project project, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        // Tjek om brugeren er logget ind, ellers omdiriger til login
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Omdiriger til login siden
        }

        // Hent brugeren fra sessionen og tilføj brugernavnet til modellen
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }

        // Hvis hovedprojektet ikke er valgt, behandl som et nyt projekt
        if (project.getMainProjectName() == null || project.getMainProjectName().isEmpty()) {
            // Indstil projectTaskName som taskProjectName og ryd taskProjectName
            project.setProjectTaskName(project.getTaskProjectName());  // Sæt projectTaskName som den indtastede taskProjectName
            project.setTaskProjectName(null);  // Ryd taskProjectName
            projectService.saveProject(project);  // Gem projektet
            redirectAttributes.addFlashAttribute("message", "Project added successfully.");  // Success meddelelse
        } else {
            // Hent hovedprojektet ved navn
            Project mainProject = projectService.getProjectByName(project.getMainProjectName());  // Få hovedprojektet efter navn

            // Tjek om hovedprojektet findes
            if (mainProject != null) {
                // Hent WBS (Work Breakdown Structure) for hovedprojektet
                String mainProjectWBS = mainProject.getWbs();  // Få WBS for hovedprojektet

                // Hent den højeste WBS indeks fra projekter og opgave tabeller
                int highestTaskIndex = projectService.getHighestWbsIndex(mainProjectWBS);

                // Generer en ny WBS for opgaven
                String newWBS = mainProjectWBS + "." + (highestTaskIndex + 1);

                // Sæt WBS for den nye opgave
                project.setWbs(newWBS);  // Sæt WBS for den nye opgave
                project.setTaskProjectName(project.getTaskProjectName());  // Sæt taskProjectName
                project.setProjectTaskName(mainProject.getProjectTaskName());  // Sæt projectTaskName fra hovedprojektet
                project.setResource_name(project.getResource_name());  // Sæt resource_name
                project.setId(project.getId());  // Sæt ID
                taskService.saveTask(project);  // Gem opgaven
                redirectAttributes.addFlashAttribute("message", "Task added successfully.");  // Success meddelelse
            } else {
                // Hvis hovedprojektet ikke findes, vis en fejlmeddelelse
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Main project not found.");  // Fejlmeddelelse
                return "redirect:/add";  // Omdiriger tilbage til formularen
            }
        }

        // Omdiriger tilbage til formularen for at tilføje et projekt/opgave
        return "redirect:/add";  // Omdiriger tilbage til formularen for at tilføje et projekt/opgave
    }

    // Vis formularen til at tilføje en underopgave
    @GetMapping("/addSub")
    public String showSubForm(Model model, HttpSession session) {
        // Tjek om brugeren er logget ind, ellers send til login
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }

        List<Project> projects = taskService.getAllTasks();  // Hent alle opgaver
        model.addAttribute("projects", projects);  // Tilføj opgaver til model
        model.addAttribute("project", new Project());  // Tilføj et nyt tomt projekt til model
        return "addSub";  // Returner formularen til tilføjelse af underopgave
    }

    @PostMapping("/addSub")
    public String submitSubForm(@ModelAttribute Project project, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        // Tjek om brugeren er logget ind, ellers send til login
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }

        if (project.getTaskProjectName() != null && !project.getTaskProjectName().isEmpty()) {  // Hvis taskProjectName er angivet
            Project mainTask = taskService.getTaskByName(project.getTaskProjectName());  // Hent hovedopgaven

            if (mainTask != null) {
                String mainTaskWBS = mainTask.getWbs();  // Hent WBS for hovedopgaven

                // Hent den højeste WBS-index for underopgaver
                int highestSubtaskIndex = subTaskService.getHighestWbsIndexForSubtasks(mainTaskWBS);

                // Generer en ny WBS for underopgaven
                String newWBS = mainTaskWBS + "." + (highestSubtaskIndex + 1);

                project.setWbs(newWBS);  // Sæt WBS for den nye underopgave
                project.setTaskProjectName(mainTask.getTaskProjectName());  // Sæt taskProjectName
                project.setProjectTaskName(mainTask.getProjectTaskName());  // Sæt projectTaskName
                project.setResource_name(project.getResource_name());
                project.setId(project.getId());

                subTaskService.saveSubTask(project);  // Gem underopgaven
                redirectAttributes.addFlashAttribute("messageSub", "Subtask added successfully.");  // Success-besked
            } else {
                redirectAttributes.addFlashAttribute("errorMessageSub", "Error: Main task not found.");  // Fejlbesked
                return "redirect:/addSub";  // Omdiriger tilbage til formularen
            }
        }

        return "redirect:/addSub";  // Redirect tilbage til formularen for underopgave
    }

    // Viser et view over alle projekter, tasks og subtasks
    @GetMapping("/view")
    public String view(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect to login page if the user is not logged in
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }

        List<Object> projects = projectService.getAll();  // Get all projects, tasks, and subtasks
        model.addAttribute("projects", projects);  // Add projects, tasks, and subtasks to the model

        // Calculate the total time to spend, excluding projects with status "done"
        double totalTimeToSpend = projects.stream()
                .filter(project -> !(project instanceof Project && "done".equals(((Project) project).getStatus())))
                .mapToDouble(project -> {
                    // Assuming project has a method `getTimeToSpend` that returns a numeric value
                    if (project instanceof Project) {
                        return ((Project) project).getTimeToSpend();
                    }
                    return 0;  // Return 0 if the project doesn't have a timeToSpend value
                })
                .sum();

        // Add the total time to spend to the model
        model.addAttribute("totalTimeToSpend", totalTimeToSpend);

        return "view";  // Return the view to display projects
    }

    // Vis et specifikt projekt ved ID
    @GetMapping("/view-project/{id}")
    public String viewProject(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }

        List<Project> projects = projectService.getProjectByID(id);  // Hent projekt efter ID
        model.addAttribute("projects", projects);  // Tilføj projekter til model
        return "view-project";  // Returner view til visning af underopgavedetaljer
    }

    @GetMapping("/edit-project/{id}")
    public String editProject(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());

            if ("Admin".equals(user.getRole())) {
                model.addAttribute("isAdmin", true); // This will be true if the user is Admin
            } else {
                model.addAttribute("isAdmin", false);
            }
        } else {
            model.addAttribute("username", "Guest");
            model.addAttribute("isAdmin", false); // Set isAdmin to false for guest users
        }

        List<Project> project = projectService.getProjectByID(id); // Henter projektet med det angivne ID
        model.addAttribute("project", project); // Tilføjer det hentede projekt til modellen
        return "edit-project"; // Returnerer viewet til at redigere projektet
    }

    @PostMapping("/update-project/{id}")
    public String updateProject(@PathVariable("id") int id, @ModelAttribute Project project, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        System.out.println("Opdaterer projekt med ID: " + id);
        System.out.println("Nyt projektnavn: " + project.getProjectTaskName());
        System.out.println("Ny varighed: " + project.getDuration());

        project.setId(id);  // Sætter ID for projektet
        projectService.updateProject(id, project);  // Opdater projektet

        return "redirect:/view-project/" + id;  // Redirect til visning af projektet
    }

    @PostMapping("/delete-project/{id}")
    public String deleteProject(@PathVariable("id") int id, @ModelAttribute Project project, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        System.out.println("Sletter projekt med ID: " + id);

        projectService.deleteProject(id);

        return "redirect:/view";
    }

    // Opdaterer en tasks status
    @PostMapping("/update-project-status/{id}/{status}")
    public String updateProjectStatus(@PathVariable("id") Long id,
                                      @PathVariable("status") String status, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // Redirect til login-siden
        }

        projectService.updateProjectStatus(id, status);
        return "redirect:/view-task/" + id;  // Redirect til task view efter opdatering af status
    }
}