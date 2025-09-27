<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>커밋 비교 - Cleverse AI Code Review</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .diff-container {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 0.375rem;
            max-height: 600px;
            overflow-y: auto;
        }
        .diff-content {
            font-family: 'Courier New', monospace;
            font-size: 14px;
            line-height: 1.5;
            white-space: pre-wrap;
            padding: 20px;
        }
        .commit-info {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .ai-analysis-section {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
            color: white;
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
                <a class="nav-link" href="/git/projects">프로젝트</a>
            </div>
        </div>
    </nav>

    <div class="container my-5">
        <div class="row">
            <div class="col-12">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/git/projects">프로젝트</a></li>
                        <li class="breadcrumb-item"><a href="/git/branches?projectName=<%= request.getAttribute("projectName") %>">브랜치</a></li>
                        <li class="breadcrumb-item"><a href="/git/commits?projectName=<%= request.getAttribute("projectName") %>&branchName=<%= request.getAttribute("branchName") %>">커밋</a></li>
                        <li class="breadcrumb-item active">비교</li>
                    </ol>
                </nav>
                
                <h1 class="mb-4">
                    <i class="fas fa-balance-scale text-primary"></i> 커밋 비교 결과
                </h1>
            </div>
        </div>

        <!-- Commit Info -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card commit-info">
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <h6><i class="fas fa-code-commit"></i> 커밋 1</h6>
                                <p class="mb-0"><%= request.getAttribute("commit1") %></p>
                            </div>
                            <div class="col-md-6">
                                <h6><i class="fas fa-code-commit"></i> 커밋 2</h6>
                                <p class="mb-0"><%= request.getAttribute("commit2") %></p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Diff Results -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="fas fa-file-diff text-primary"></i> 변경사항 (Diff)
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="diff-container">
                            <div class="diff-content"><%= request.getAttribute("diffResult") %></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- AI Analysis Section (Future Feature) -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card ai-analysis-section">
                    <div class="card-body text-center">
                        <i class="fas fa-brain fa-3x mb-3"></i>
                        <h5>AI 코드 분석</h5>
                        <p class="mb-3">로컬 LLM을 활용한 코드 품질 분석 기능이 곧 추가됩니다.</p>
                        <button class="btn btn-light" disabled>
                            <i class="fas fa-cog fa-spin"></i> 분석 준비 중...
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Action Buttons -->
        <div class="row">
            <div class="col-12 text-center">
                <a href="/git/commits?projectName=<%= request.getAttribute("projectName") %>&branchName=<%= request.getAttribute("branchName") %>" 
                   class="btn btn-primary me-2">
                    <i class="fas fa-arrow-left"></i> 커밋 목록으로
                </a>
                <button class="btn btn-success" onclick="location.reload()">
                    <i class="fas fa-refresh"></i> 새로고침
                </button>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="bg-dark text-light py-4 mt-5">
        <div class="container text-center">
            <p>&copy; 2024 Cleverse AI Code Review. All rights reserved.</p>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
