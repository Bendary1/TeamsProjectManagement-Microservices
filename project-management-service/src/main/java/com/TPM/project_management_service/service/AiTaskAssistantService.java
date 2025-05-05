package com.TPM.project_management_service.service;

import com.TPM.project_management_service.dto.TaskResponse;
import com.TPM.project_management_service.model.Task;
import com.TPM.project_management_service.model.TaskPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTaskAssistantService {

    @Value("${spring.ai.openai.chat.base-url}")
    private String geminiBaseUrl;

    @Value("${spring.ai.openai.chat.completions-path}")
    private String completionsPath;

    @Value("${spring.ai.openai.chat.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    private final RestTemplate restTemplate;

    public String generateTaskPlan(Task task) {
        log.info("Generating AI task plan for task: {}", task.getId());
        try {
            // Build the URL without query parameters
            String apiUrl = geminiBaseUrl + completionsPath;
            
            // Format task details for prompt
            String taskDescription = task.getDescription() != null ? task.getDescription() : "No description provided";
            String priorityLevel = task.getPriority().toString();
            String deadlineStr = task.getDeadline() != null ? 
                task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
                "No deadline set";
            
            // Create prompt
            String prompt = String.format(
                "You are an AI assistant for project management. Create a personalized task plan for the following task:\n\n" +
                "Task: %s\n" +
                "Description: %s\n" +
                "Priority: %s\n" +
                "Deadline: %s\n" +
                "Estimated Hours: %s\n\n" +
                "Based on this information, provide a detailed plan including:\n" +
                "1. A recommended time schedule with milestones\n" +
                "2. Suggestions for breaking down the task into smaller steps\n" +
                "3. Best practices for approaching this type of task\n" +
                "4. Tips for managing time efficiently given the priority and deadline\n" +
                "Format the response in a clear, concise way that's easy to follow.",
                task.getTitle(),
                taskDescription,
                priorityLevel,
                deadlineStr,
                task.getEstimatedHours() != null ? task.getEstimatedHours() + " hours" : "Not specified"
            );

            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Use Authorization header with Bearer token format
            headers.set("Authorization", "Bearer " + apiKey);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            requestBody.put("model", model);
            requestBody.put("messages", new Object[]{message});
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.7);
            
            // Make API call
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                // Handle the choices as a List instead of an array
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");
                    return (String) messageResponse.get("content");
                }
            }
            
            // Log the actual response to help with debugging
            log.info("API Response structure: {}", response);
            
            return "Unable to generate AI task plan at this time.";
        } catch (Exception e) {
            log.error("Error generating AI task plan: {}", e.getMessage(), e);
            return "Error generating AI task plan: " + e.getMessage();
        }
    }

    public String generateTaskPlan(TaskResponse taskResponse) {
        Task task = new Task();
        task.setId(taskResponse.getId());
        task.setTitle(taskResponse.getTitle());
        task.setDescription(taskResponse.getDescription());
        task.setPriority(taskResponse.getPriority());
        task.setDeadline(taskResponse.getDeadline());
        task.setEstimatedHours(taskResponse.getEstimatedHours());
        
        return generateTaskPlan(task);
    }
} 