package de.uni.scrumboard.data;

public record BacklogAssignee(int backlogId,int assigneeId,String username, String email, String role )
    {
 public boolean isProductOwner() {
        return "Product Owner".equals(role);
    }
    
    public boolean isScrumMaster() {
        return "Scrum Master".equals(role);
    }
    
    public boolean isDeveloper() {
        return "Developer".equals(role);
    }
    }


