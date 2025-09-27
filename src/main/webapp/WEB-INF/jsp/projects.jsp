<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>프로젝트 목록 - Cleverse AI Code Review</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .project-card {
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            cursor: pointer;
        }
        .project-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.15);
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">
                <i class="fas fa-code"></i> Cleverse AI Code Review
            </a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="/">홈</a>
            </div>
        </div>
    </nav>

    <div class="container my-5">
        <div class="row">
            <div class="col-12">
                <h1 class="mb-4">
                    <i class="fas fa-folder-open text-primary"></i> 프로젝트 목록
                </h1>
                <p class="text-muted mb-3">GitHub 저장소에서 분석할 프로젝트를 선택하세요</p>
                <p class="text-info mb-5">
                    <i class="fas fa-info-circle"></i> 
                    <a href="https://github.com/juldae719?tab=repositories" target="_blank" class="text-decoration-none">
                        @onypapaai의 GitHub 저장소
                    </a>에서 동적으로 가져온 목록입니다.
                </p>
            </div>
        </div>
        
        <% 
            String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isEmpty()) {
        %>
        <div class="alert alert-warning alert-dismissible fade show" role="alert">
            <i class="fas fa-exclamation-triangle"></i> <%= errorMessage %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <div class="row">
            <%
                List<Map<String, String>> projects = (List<Map<String, String>>) request.getAttribute("projects");
                if (projects != null && !projects.isEmpty()) {
                    for (Map<String, String> project : projects) {
            %>
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card project-card h-100" onclick="selectProject('<%= project.get("name") %>', '<%= project.get("cloneUrl") %>')">
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-3">
                            <i class="fas fa-project-diagram fa-2x text-primary me-3"></i>
                            <div class="flex-grow-1">
                                <h5 class="card-title mb-0"><%= project.get("name") %></h5>
                                <small class="text-muted">@<%= project.get("fullName") %></small>
                            </div>
                        </div>
                        <p class="card-text text-muted mb-3"><%= project.get("description") %></p>
                        
                        <div class="row mb-3">
                            <div class="col-6">
                                <small class="text-muted">
                                    <i class="fas fa-code"></i> <%= project.get("language") %>
                                </small>
                            </div>
                            <div class="col-6">
                                <small class="text-muted">
                                    <i class="fas fa-lock" style="<%= "true".equals(project.get("private")) ? "" : "display: none;" %>"></i>
                                    <i class="fas fa-unlock" style="<%= "false".equals(project.get("private")) ? "" : "display: none;" %>"></i>
                                    <%= "true".equals(project.get("private")) ? "Private" : "Public" %>
                                </small>
                            </div>
                        </div>
                        
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <div class="d-flex gap-3">
                                <small class="text-muted">
                                    <i class="fas fa-star text-warning"></i> <%= project.get("stars") %>
                                </small>
                                <small class="text-muted">
                                    <i class="fas fa-code-branch text-info"></i> <%= project.get("forks") %>
                                </small>
                            </div>
                        </div>
                        
                        <div class="d-flex justify-content-between align-items-center">
                            <small class="text-muted">
                                <i class="fas fa-clock"></i> 
                                <%= project.get("updatedAt") != null ? 
                                    project.get("updatedAt").substring(0, 10) : "Unknown" %>
                            </small>
                            <i class="fas fa-chevron-right text-primary"></i>
                        </div>
                    </div>
                </div>
            </div>
            <%
                    }
                } else {
            %>
            <div class="col-12">
                <div class="alert alert-info text-center">
                    <i class="fas fa-info-circle fa-2x mb-3"></i>
                    <h5>프로젝트가 없습니다</h5>
                    <p>GitHub에서 프로젝트를 가져오는 중입니다...</p>
                </div>
            </div>
            <%
                }
            %>
        </div>
    </div>

    <script>
        function selectProject(projectName, cloneUrl) {
            console.log('Selected project:', projectName);
            console.log('Clone URL:', cloneUrl);
            window.location.href = '/git/branches?projectName=' + encodeURIComponent(projectName) + '&cloneUrl=' + encodeURIComponent(cloneUrl);
        }
    </script>
</body>
</html>

