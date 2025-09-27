<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cleverse AI Code Review</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .hero-section {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 100px 0;
        }
        .card-hover {
            transition: transform 0.3s ease;
        }
        .card-hover:hover {
            transform: translateY(-5px);
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
            </div>
        </nav>

    <!-- Hero Section -->
    <div class="hero-section">
        <div class="container text-center">
            <h1 class="display-4 mb-4">AI 기반 코드 리뷰 시스템</h1>
            <p class="lead mb-5">Git 저장소의 코드 변경사항을 AI로 분석하고 리뷰합니다</p>
            <a href="/git/projects" class="btn btn-light btn-lg">
                <i class="fas fa-rocket"></i> 시작하기
                                        </a>
                                    </div>
                                </div>

    <!-- Features Section -->
    <div class="container my-5">
                                        <div class="row">
            <div class="col-md-4 mb-4">
                <div class="card card-hover h-100">
                    <div class="card-body text-center">
                        <i class="fas fa-git-alt fa-3x text-primary mb-3"></i>
                        <h5 class="card-title">Git 통합</h5>
                        <p class="card-text">GitHub 저장소와 연동하여 실시간 코드 변경사항을 추적합니다.</p>
                                            </div>
                                        </div>
                                    </div>
            <div class="col-md-4 mb-4">
                <div class="card card-hover h-100">
                    <div class="card-body text-center">
                        <i class="fas fa-brain fa-3x text-success mb-3"></i>
                        <h5 class="card-title">AI 분석</h5>
                        <p class="card-text">로컬 LLM을 활용하여 코드 품질과 잠재적 문제점을 분석합니다.</p>
                                            </div>
                                        </div>
                                    </div>
            <div class="col-md-4 mb-4">
                <div class="card card-hover h-100">
                    <div class="card-body text-center">
                        <i class="fas fa-chart-line fa-3x text-warning mb-3"></i>
                        <h5 class="card-title">상세 리포트</h5>
                        <p class="card-text">변경사항에 대한 상세한 분석 리포트를 제공합니다.</p>
                    </div>
                </div>
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
