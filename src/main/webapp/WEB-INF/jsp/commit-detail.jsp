<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>커밋 상세 정보 - Cleverse AI Code Review</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .commit-header {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 50%, #2c3e50 100%);
            color: white;
            padding: 30px 0;
            position: relative;
            overflow: hidden;
        }
        
        .commit-header::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(45deg, rgba(52, 152, 219, 0.1) 0%, rgba(155, 89, 182, 0.1) 100%);
            pointer-events: none;
        }
        
        .commit-header .container {
            position: relative;
            z-index: 1;
        }
        
        /* 접었다 펼쳤다 스타일 */
        .collapsible-header {
            cursor: pointer;
            user-select: none;
            transition: background-color 0.2s ease;
        }
        
        .collapsible-header:hover {
            background-color: rgba(0, 0, 0, 0.05);
        }
        
        .collapsible-content {
            display: none;
        }
        
        .collapsible-content.show {
            display: block;
        }
        
        .collapse-icon {
            transition: transform 0.2s ease;
        }
        
        .collapse-icon.rotated {
            transform: rotate(180deg);
        }
        .diff-container {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 0.375rem;
            max-height: 600px;
            overflow-y: auto;
        }
        .diff-content {
            font-family: 'Courier New', monospace;
            font-size: 13px;
            line-height: 1.4;
            white-space: pre-wrap;
            padding: 0;
        }
        
        /* GitHub 스타일 diff CSS */
        .diff-file-header {
            background-color: #f6f8fa;
            border-bottom: 1px solid #d0d7de;
            font-weight: 600;
        }
        
        .diff-git-header {
            background-color: #24292f;
            color: #f0f6fc;
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
        }
        
        .diff-index {
            background-color: #6c757d;
            color: white;
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
        }
        
        .diff-file-path {
            background-color: #0969da;
            color: white;
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
        }
        
        .diff-hunk-header {
            background-color: #fff3cd;
            color: #664d03;
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
        }
        
        .diff-line {
            display: flex;
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
            font-size: 12px;
            line-height: 1.45;
        }
        
        .diff-line-numbers {
            display: flex;
            width: 80px;
            flex-shrink: 0;
            background-color: #f6f8fa;
            border-right: 1px solid #d0d7de;
        }
        
        .diff-line-old {
            width: 50%;
            padding: 0 8px;
            text-align: right;
            color: #656d76;
            background-color: #f6f8fa;
            border-right: 1px solid #d0d7de;
        }
        
        .diff-line-new {
            width: 50%;
            padding: 0 8px;
            text-align: right;
            color: #656d76;
            background-color: #f6f8fa;
        }
        
        .diff-line-content {
            flex: 1;
            display: flex;
            padding: 0 8px;
        }
        
        .diff-line-prefix {
            width: 20px;
            text-align: center;
            color: #656d76;
            background-color: #f6f8fa;
            border-right: 1px solid #d0d7de;
            flex-shrink: 0;
        }
        
        .diff-line-text {
            flex: 1;
            padding-left: 8px;
            white-space: pre;
            overflow-x: auto;
            word-break: break-all;
        }
        
        .diff-line-added {
            background-color: #dbeafe;
        }
        
        .diff-line-added .diff-line-old {
            background-color: #f6f8fa;
        }
        
        .diff-line-added .diff-line-new {
            background-color: #dbeafe;
        }
        
        .diff-line-added .diff-line-prefix {
            background-color: #dbeafe;
            color: #1f883d;
            font-weight: bold;
        }
        
        .diff-line-added .diff-line-text {
            background-color: #dbeafe;
        }
        
        .diff-line-removed {
            background-color: #fef2f2;
        }
        
        .diff-line-removed .diff-line-old {
            background-color: #fef2f2;
        }
        
        .diff-line-removed .diff-line-new {
            background-color: #f6f8fa;
        }
        
        .diff-line-removed .diff-line-prefix {
            background-color: #fef2f2;
            color: #d1242f;
            font-weight: bold;
        }
        
        .diff-line-removed .diff-line-text {
            background-color: #fef2f2;
        }
        
        .diff-line-context {
            background-color: #ffffff;
        }
        
        .diff-line-context .diff-line-old,
        .diff-line-context .diff-line-new {
            background-color: #f6f8fa;
        }
        
        .diff-line-context .diff-line-prefix {
            background-color: #f6f8fa;
        }
        
        .diff-line-context .diff-line-text {
            background-color: #ffffff;
        }
        .diff-file-header {
            background-color: #e9ecef;
            padding: 10px 15px;
            margin: 10px 0;
            border-radius: 5px;
            font-weight: bold;
            color: #495057;
        }
        .diff-line-added {
            background-color: #d4edda;
            color: #155724;
        }
        .diff-line-removed {
            background-color: #f8d7da;
            color: #721c24;
        }
        .diff-line-context {
            background-color: #f8f9fa;
            color: #6c757d;
        }
        .file-change {
            padding: 8px 12px;
            margin: 4px 0;
            border-radius: 4px;
            font-family: 'Courier New', monospace;
        }
        .file-added {
            background-color: #d4edda;
            border-left: 4px solid #28a745;
        }
        .file-modified {
            background-color: #fff3cd;
            border-left: 4px solid #ffc107;
        }
        .file-deleted {
            background-color: #f8d7da;
            border-left: 4px solid #dc3545;
        }
               .file-renamed {
                   background-color: #d1ecf1;
                   border-left: 4px solid #17a2b8;
               }
               .file-item {
                   transition: all 0.2s ease;
                   border-left: 3px solid transparent;
               }
               .file-item:hover {
                   background-color: #f8f9fa;
                   border-left-color: #0d6efd;
               }
               .file-item.bg-primary {
                   background-color: #0d6efd !important;
                   color: white !important;
                   border-left-color: #0056b3;
               }
               .file-item .file-path {
                   word-break: break-all;
                   white-space: normal;
                   line-height: 1.4;
                   font-size: 14px;
               }
               .file-item .file-old-path {
                   word-break: break-all;
                   white-space: normal;
                   font-size: 12px;
                   margin-top: 4px;
               }
               .file-item .change-badge {
                   flex-shrink: 0;
                   margin-left: 8px;
               }
               .file-diff-header {
                   background-color: #f8f9fa;
                   border-bottom: 1px solid #dee2e6;
               }
               
               .date-group {
                   margin-bottom: 2rem;
               }
               .date-header {
                   background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                   color: white;
                   padding: 15px 20px;
                   border-radius: 8px;
                   margin-bottom: 20px;
               }
               .commits-for-date {
                   padding-left: 20px;
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

    <%
        Map<String, Object> commitDetail = (Map<String, Object>) request.getAttribute("commitDetail");
        String errorMessage = (String) request.getAttribute("errorMessage");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    %>

    <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
    <div class="container my-5">
        <div class="alert alert-danger">
            <i class="fas fa-exclamation-triangle"></i> <%= errorMessage %>
        </div>
    </div>
    <% } else { %>

    <!-- Commit Header -->
    <div class="commit-header">
        <div class="container">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="/git/projects" class="text-white">프로젝트</a></li>
                    <li class="breadcrumb-item"><a href="/git/branches?projectName=<%= request.getAttribute("projectName") %>" class="text-white">브랜치</a></li>
                    <li class="breadcrumb-item"><a href="/git/commits?projectName=<%= request.getAttribute("projectName") %>&branchName=<%= request.getAttribute("branchName") %>" class="text-white">커밋</a></li>
                    <li class="breadcrumb-item active text-white">상세</li>
                </ol>
            </nav>
            
            <!-- Navigation Buttons -->
            <div class="mb-3">
                <button onclick="goBackToCommits()" class="btn btn-outline-light btn-sm me-2">
                    <i class="fas fa-arrow-left"></i> 커밋 목록으로
                </button>
                <button class="btn btn-outline-light btn-sm" onclick="location.reload()">
                    <i class="fas fa-refresh"></i> 새로고침
                </button>
            </div>
            
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h1 class="mb-3">
                        <i class="fas fa-code-commit"></i> 커밋 상세 정보
                    </h1>
                    <h4 class="mb-0"><%= commitDetail.get("shortId") %></h4>
                </div>
                <div>
                    <button class="ai-analysis-btn" onclick="showBranchSelectionModal()">
                        <i class="fas fa-robot"></i>AI 분석 요청
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div class="container my-5">
        <!-- Commit Info -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header collapsible-header" onclick="toggleCollapse('commit-info')">
                        <h5 class="mb-0">
                            <i class="fas fa-info-circle text-primary"></i> 커밋 정보
                            <i class="fas fa-chevron-down collapse-icon float-end" id="commit-info-icon"></i>
                        </h5>
                    </div>
                    <div class="card-body collapsible-content" id="commit-info">
                        <div class="row">
                            <div class="col-md-6">
                               <%
                                   String shortMsg1 = (String) commitDetail.get("message");
                                   String fullMsg1 = (String) commitDetail.get("fullMessage");
                                   boolean isMsgSame1 = false;
                                   
                                   if (shortMsg1 != null && fullMsg1 != null) {
                                       // 공백과 줄바꿈을 제거하고 비교
                                       String cleanShort1 = shortMsg1.trim().replaceAll("\\s+", " ");
                                       String cleanFull1 = fullMsg1.trim().replaceAll("\\s+", " ");
                                       isMsgSame1 = cleanShort1.equals(cleanFull1);
                                   }
                               %>

                               <% if (isMsgSame1) { %>
                               <h6>커밋 메시지</h6>
                               <pre class="bg-light p-3 rounded"><%= fullMsg1 %></pre>
                               <% } else { %>
                               <h6>커밋 메시지 (요약)</h6>
                               <p class="text-muted"><%= shortMsg1 %></p>

                               <h6>전체 메시지</h6>
                               <pre class="bg-light p-3 rounded"><%= fullMsg1 %></pre>
                               <% } %>
                                
                                <h6>작성자</h6>
                                <p><i class="fas fa-user"></i> <%= commitDetail.get("author") %> 
                                   <small class="text-muted">(<%= commitDetail.get("email") %>)</small></p>
                            </div>
                            <div class="col-md-6">
                                <h6>커밋 ID</h6>
                                <p><code><%= commitDetail.get("id") %></code></p>
                                
                                <h6>작성일</h6>
                                <p><i class="fas fa-clock"></i> <%= sdf.format((Date) commitDetail.get("date")) %></p>
                                
                                <h6>부모 커밋 수</h6>
                                <p><%= commitDetail.get("parentCount") %>개</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Parent Commits -->
        <% 
            List<Map<String, String>> parents = (List<Map<String, String>>) commitDetail.get("parents");
            if (parents != null && !parents.isEmpty()) {
        %>
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header collapsible-header" onclick="toggleCollapse('parent-commits')">
                        <h5 class="mb-0">
                            <i class="fas fa-sitemap text-success"></i> 부모 커밋
                            <i class="fas fa-chevron-down collapse-icon float-end" id="parent-commits-icon"></i>
                        </h5>
                    </div>
                    <div class="card-body collapsible-content" id="parent-commits">
                        <% for (Map<String, String> parent : parents) { %>
                        <div class="d-flex align-items-center mb-2">
                            <i class="fas fa-code-commit text-muted me-2"></i>
                            <code><%= parent.get("shortId") %></code>
                            <span class="ms-2 text-muted"><%= parent.get("message") %></span>
                        </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
        <% } %>



        <!-- Changed Files List -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="fas fa-list text-info"></i> 변경된 파일 목록
                            <%
                                List<Map<String, String>> changedFiles = (List<Map<String, String>>) commitDetail.get("changedFiles");
                                int fileCount = changedFiles != null ? changedFiles.size() : 0;
                            %>
                            <span class="badge bg-primary ms-2"><%= fileCount %>개</span>
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="file-list-container" style="max-height: 300px; overflow-y: auto; overflow-x: hidden;">
                            <%
                                if (changedFiles != null && !changedFiles.isEmpty()) {
                                    for (int i = 0; i < changedFiles.size(); i++) {
                                        Map<String, String> file = changedFiles.get(i);
                                        String changeType = file.get("changeType");
                                        String cssClass = "";
                                        String icon = "";
                                        if (changeType.contains("ADD")) {
                                            cssClass = "file-added";
                                            icon = "fas fa-plus-circle text-success";
                                        } else if (changeType.contains("MODIFY")) {
                                            cssClass = "file-modified";
                                            icon = "fas fa-edit text-warning";
                                        } else if (changeType.contains("DELETE")) {
                                            cssClass = "file-deleted";
                                            icon = "fas fa-trash text-danger";
                                        } else if (changeType.contains("RENAME")) {
                                            cssClass = "file-renamed";
                                            icon = "fas fa-exchange-alt text-info";
                                        }
                            %>
                            <div class="file-item p-3 border-bottom <%= cssClass %>" onclick="showFileDiff('<%= i %>', event)" style="cursor: pointer;">
                                <div class="d-flex align-items-start">
                                    <i class="<%= icon %> me-3 mt-1" style="flex-shrink: 0;"></i>
                                    <div class="flex-grow-1" style="min-width: 0;">
                                        <div class="file-path fw-bold" title="<%= file.get("path") %>">
                                            <%= file.get("path") %>
                                        </div>
                                        <% if (file.get("oldPath") != null && !file.get("oldPath").equals(file.get("path"))) { %>
                                        <div class="file-old-path text-muted">
                                            이전: <%= file.get("oldPath") %>
                                        </div>
                                        <% } %>
                                    </div>
                                    <span class="change-badge badge bg-secondary"><%= file.get("changeType") %></span>
                                </div>
                            </div>
                            <%
                                    }
                                } else {
                            %>
                            <div class="text-center p-4">
                                <i class="fas fa-info-circle fa-2x text-muted mb-3"></i>
                                <p class="text-muted">변경된 파일이 없습니다.</p>
                            </div>
                            <%
                                }
                            %>
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
                       <div class="diff-container" style="height: 600px; overflow: hidden;">
                           <div id="diff-content" style="height: 100%; overflow-y: auto;">
                               <div class="text-center p-4">
                                   <i class="fas fa-hand-pointer fa-3x text-muted mb-3"></i>
                                   <h5 class="text-muted">파일을 선택하세요</h5>
                                   <p class="text-muted">위에서 파일을 클릭하면 변경사항을 확인할 수 있습니다.</p>
                               </div>
                           </div>
                       </div>
                    </div>
                </div>
            </div>
        </div>


    </div>

    <% } %>

    <!-- Footer -->
    <footer class="bg-dark text-light py-4 mt-5">
        <div class="container text-center">
            <p>&copy; 2024 Cleverse AI Code Review. All rights reserved.</p>
        </div>
    </footer>


    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 페이지 로드 시 기존 로딩 상태 제거
        function removeLoadingStates() {
            // 모든 로딩 상태 제거
            const loadingElements = document.querySelectorAll('.position-fixed.top-50.start-50.translate-middle');
            loadingElements.forEach(element => {
                element.remove();
            });
            
            // 특정 ID로도 제거
            const commitDetailLoading = document.getElementById('commit-detail-loading');
            if (commitDetailLoading) {
                commitDetailLoading.remove();
            }
            
            const branchLoading = document.getElementById('branch-loading');
            if (branchLoading) {
                branchLoading.remove();
            }
        }
        
        // 즉시 로딩 상태 제거 (스크립트 로드 시)
        removeLoadingStates();
        
        // DOM 로드 시
        document.addEventListener('DOMContentLoaded', removeLoadingStates);
        
        // 접었다 펼쳤다 함수
        function toggleCollapse(elementId) {
            const content = document.getElementById(elementId);
            const icon = document.getElementById(elementId + '-icon');
            
            if (content.classList.contains('show')) {
                content.classList.remove('show');
                icon.classList.remove('rotated');
            } else {
                content.classList.add('show');
                icon.classList.add('rotated');
            }
        }
        
        // 페이지 표시 시 (뒤로가기 포함)
        window.addEventListener('pageshow', function(event) {
            removeLoadingStates();
        });
        
        // 페이지 숨김 시
        window.addEventListener('pagehide', function(event) {
            removeLoadingStates();
        });
        
               // 페이지 포커스 시
               window.addEventListener('focus', removeLoadingStates);
               
               // 커밋 목록으로 돌아가기 함수
               function goBackToCommits() {
                   const projectName = '<%= request.getAttribute("projectName") %>';
                   const branchName = '<%= request.getAttribute("branchName") %>';
                   
                   // 기존 로딩 상태 제거
                   const existingLoading = document.getElementById('commit-list-loading');
                   if (existingLoading) {
                       existingLoading.remove();
                   }
                   
                   // 로딩 상태 표시
                   const loadingDiv = document.createElement('div');
                   loadingDiv.id = 'commit-list-loading';
                   loadingDiv.className = 'position-fixed top-50 start-50 translate-middle text-center';
                   loadingDiv.style.zIndex = '9999';
                   loadingDiv.innerHTML = `
                       <div class="bg-white p-4 rounded shadow-lg">
                           <div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;">
                               <span class="visually-hidden">Loading...</span>
                           </div>
                           <h5 class="text-primary">커밋 목록을 불러오는 중...</h5>
                           <p class="text-muted mb-0">잠시만 기다려주세요.</p>
                       </div>
                   `;
                   document.body.appendChild(loadingDiv);
                   
                   // 프로그래스바가 표시되도록 약간의 지연 후 페이지 이동
                   setTimeout(() => {
                       window.location.href = '/git/commits?projectName=' + encodeURIComponent(projectName) + 
                                            '&branchName=' + encodeURIComponent(branchName);
                   }, 100);
               }
               
               // Diff 텍스트 포맷팅 함수
               function formatDiffText(text) {
                   if (!text || typeof text !== 'string') {
                       return '변경 내용을 불러올 수 없습니다.';
                   }
                   
                   // 수동으로 라인 분할
                   let lines = [];
                   let currentLine = '';
                   for (let i = 0; i < text.length; i++) {
                       let char = text.charAt(i);
                       if (char === '\n') {
                           lines.push(currentLine);
                           currentLine = '';
                       } else {
                           currentLine += char;
                       }
                   }
                   // 마지막 라인 추가
                   if (currentLine.length > 0) {
                       lines.push(currentLine);
                   }
                   
                   let result = '';
                   let oldLineNum = 0;
                   let newLineNum = 0;
                   let inHunk = false;
                   let processedLines = 0;
                   
                   for (let i = 0; i < lines.length; i++) {
                       let line = lines[i];
                       
                       // 파일 헤더 처리 (제거)
                       if (line.startsWith('=== 파일:')) {
                           continue;
                       }
                       
                       // 변경 타입, 경로 정보 처리
                       if (line.startsWith('변경 타입:') || line.startsWith('이전 경로:') || line.startsWith('새 경로:') || line.startsWith('---')) {
                           continue;
                       }
                       
                       // diff 헤더 처리 (제거)
                       if (line.startsWith('diff --git')) {
                           continue;
                       }
                       
                       // index 라인 처리 (제거)
                       if (line.startsWith('index ')) {
                           continue;
                       }
                       
                       // 파일 경로 헤더 처리 (간소화)
                       if (line.startsWith('--- a/') || line.startsWith('+++ b/')) {
                           continue;
                       }
                       
                       // hunk 헤더 처리 (@@ ... @@)
                       if (line.startsWith('@@')) {
                           inHunk = true;
                           let match = line.match(/@@ -(\d+),?(\d*) \+(\d+),?(\d*) @@/);
                           if (match) {
                               oldLineNum = parseInt(match[1]) - 1;
                               newLineNum = parseInt(match[3]) - 1;
                           }
                           result += '<div class="diff-hunk-header p-2 bg-warning text-dark font-monospace small fw-bold">' + line + '</div>';
                           continue;
                       }
                       
                       // 실제 코드 라인 처리
                       if (inHunk) {
                           if (line.length > 0) {
                               processedLines++;
                               let lineClass = '';
                               let lineColor = '';
                               let prefix = '';
                               let displayLine = '';
                               
                               if (line.startsWith('+')) {
                                   newLineNum++;
                                   lineClass = 'diff-line-added';
                                   lineColor = '#dbeafe';
                                   prefix = '+';
                                   displayLine = line.substring(1);
                               } else if (line.startsWith('-')) {
                                   oldLineNum++;
                                   lineClass = 'diff-line-removed';
                                   lineColor = '#fef2f2';
                                   prefix = '-';
                                   displayLine = line.substring(1);
                               } else {
                                   oldLineNum++;
                                   newLineNum++;
                                   lineClass = 'diff-line-context';
                                   lineColor = '#ffffff';
                                   prefix = ' ';
                                   displayLine = line;
                               }
                           
                               let oldLineDisplay = line.startsWith('+') ? '' : oldLineNum;
                               let newLineDisplay = line.startsWith('-') ? '' : newLineNum;
                               
                               result += '<div class="diff-line ' + lineClass + '" style="display: flex; background-color: ' + lineColor + '; border-bottom: 1px solid #e1e4e8; min-height: 20px;">' +
                                   '<div class="diff-line-numbers" style="width: 80px; display: flex; background-color: #f6f8fa; border-right: 1px solid #d0d7de;">' +
                                       '<span class="diff-line-old" style="width: 50%; padding: 0 8px; text-align: right; color: #656d76; line-height: 20px;">' + oldLineDisplay + '</span>' +
                                       '<span class="diff-line-new" style="width: 50%; padding: 0 8px; text-align: right; color: #656d76; line-height: 20px;">' + newLineDisplay + '</span>' +
                                   '</div>' +
                                   '<div class="diff-line-content" style="flex: 1; display: flex; padding: 0 8px; align-items: center;">' +
                                       '<span class="diff-line-prefix" style="width: 20px; text-align: center; color: ' + (line.startsWith('+') ? '#1f883d' : line.startsWith('-') ? '#d1242f' : '#656d76') + '; font-weight: bold; line-height: 20px;">' + prefix + '</span>' +
                                       '<span class="diff-line-text" style="flex: 1; padding-left: 8px; white-space: pre; font-family: monospace; line-height: 20px; color: #24292f;">' + displayLine + '</span>' +
                                   '</div>' +
                               '</div>';
                           } else {
                               // 빈 라인
                               result += '<div class="diff-line diff-line-context" style="display: flex; background-color: #ffffff; border-bottom: 1px solid #e1e4e8; min-height: 20px;">' +
                                   '<div class="diff-line-numbers" style="width: 80px; display: flex; background-color: #f6f8fa; border-right: 1px solid #d0d7de;">' +
                                       '<span class="diff-line-old" style="width: 50%; padding: 0 8px; text-align: right; color: #656d76; line-height: 20px;"></span>' +
                                       '<span class="diff-line-new" style="width: 50%; padding: 0 8px; text-align: right; color: #656d76; line-height: 20px;"></span>' +
                                   '</div>' +
                                   '<div class="diff-line-content" style="flex: 1; display: flex; padding: 0 8px; align-items: center;">' +
                                       '<span class="diff-line-prefix" style="width: 20px; text-align: center; color: #656d76; line-height: 20px;"> </span>' +
                                       '<span class="diff-line-text" style="flex: 1; padding-left: 8px; white-space: pre; font-family: monospace; line-height: 20px; color: #24292f;"></span>' +
                                   '</div>' +
                               '</div>';
                           }
                       }
                   }
                   
                   return result;
               }
               
               // Diff 내용 포맷팅 함수 (기존 DOM 요소용)
               function formatDiffContent() {
                   const diffContent = document.querySelector('.diff-content');
                   if (diffContent && diffContent.textContent.trim() !== '') {
                       let content = diffContent.textContent;
                       diffContent.innerHTML = formatDiffText(content);
                   }
               }
        
               // 페이지 로드 시 diff 포맷팅 실행
               document.addEventListener('DOMContentLoaded', formatDiffContent);
               
               // 파일 diff 표시 함수
               function showFileDiff(fileIndex, event) {
                   
                   // fileIndex가 문자열인 경우 정수로 변환
                   const actualFileIndex = parseInt(fileIndex);
                   
                   // 이벤트 전파 방지
                   if (event) {
                       event.stopPropagation();
                   }
                   
                   // 클릭된 파일 아이템 하이라이트
                   document.querySelectorAll('.file-item').forEach(item => {
                       item.classList.remove('bg-primary', 'text-white');
                   });
                   
                   if (event && event.currentTarget) {
                       event.currentTarget.classList.add('bg-primary', 'text-white');
                   }
                   
                   // 로딩 상태 표시
                   const diffContent = document.getElementById('diff-content');
                   diffContent.innerHTML = `
                       <div class="text-center p-4">
                           <div class="spinner-border text-primary mb-3" role="status">
                               <span class="visually-hidden">Loading...</span>
                           </div>
                           <h5 class="text-primary">파일 diff를 불러오는 중...</h5>
                           <p class="text-muted">잠시만 기다려주세요.</p>
                       </div>
                   `;
                   
                   // 유효한 인덱스인지 확인
                   if (isNaN(actualFileIndex) || actualFileIndex < 0) {
                       displayError('잘못된 파일 인덱스입니다: ' + fileIndex);
                       return;
                   }
                   
                   // AJAX로 파일 diff 데이터 가져오기
                   const url = '/git/file-diff?commitId=<%= commitDetail.get("id") %>&fileIndexStr=' + actualFileIndex;
                   
                   fetch(url)
                       .then(response => {
                           if (!response.ok) {
                               throw new Error('HTTP ' + response.status + ': ' + response.statusText);
                           }
                           return response.json();
                       })
                       .then(data => {
                           if (data.success && data.data) {
                               displayFileDiff(data.data);
                           } else {
                               displayError(data.message || '파일 diff 데이터를 불러올 수 없습니다.');
                           }
                       })
                       .catch(error => {
                           displayError('파일 diff를 불러오는 중 오류가 발생했습니다: ' + error.message);
                       });
               }
               
               // 파일 diff 표시 함수
               function displayFileDiff(fileDiff) {
                   const diffContent = document.getElementById('diff-content');
                   
                   if (fileDiff && fileDiff.path) {
                       // 파일 정보 헤더
                       let oldPathInfo = '';
                       if (fileDiff.oldPath != null && fileDiff.oldPath != fileDiff.path) {
                           oldPathInfo = '<small class="text-muted ms-2">이전: ' + fileDiff.oldPath + '</small>';
                       }
                       
                       // diff 내용 처리
                       let diffText = '변경 내용을 불러올 수 없습니다.';
                       if (fileDiff.diffContent && fileDiff.diffContent !== 'undefined' && fileDiff.diffContent !== 'null') {
                           if (typeof fileDiff.diffContent === 'string') {
                               diffText = fileDiff.diffContent;
                           } else {
                               diffText = String(fileDiff.diffContent);
                           }
                       }
                       
                       // 로딩 표시
                       diffContent.innerHTML = '<div class="text-center p-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">로딩중...</span></div><div class="mt-2">파일 내용을 불러오는 중...</div></div>';

                       // 1초 후에 실제 diff 내용 표시
                       setTimeout(() => {
                           console.log('=== setTimeout 시작 ===');
                           let formattedDiff = formatDiffText(diffText);
                           console.log('formattedDiff 길이:', formattedDiff.length);
                           console.log('formattedDiff 미리보기:', formattedDiff.substring(0, 200) + '...');
                           
                           let actualDiffContent = 
                               '<div class="file-diff-container">' +
                                   '<div class="file-diff-header p-3 bg-primary text-white rounded-top">' +
                                       '<div class="d-flex align-items-center justify-content-between">' +
                                           '<h6 class="mb-0"><i class="fas fa-file-code me-2"></i>' + fileDiff.path + '</h6>' +
                                           '<div class="d-flex align-items-center">' +
                                               '<span class="badge bg-light text-dark me-2">' + fileDiff.changeType + '</span>' +
                                               oldPathInfo +
                                           '</div>' +
                                       '</div>' +
                                   '</div>' +
                                   '<div class="file-diff-content" style="max-height: 500px; overflow-y: auto; background: white;">' +
                                       formattedDiff +
                                   '</div>' +
                               '</div>';
                           
                           console.log('actualDiffContent 길이:', actualDiffContent.length);
                           console.log('DOM에 삽입 전 diffContent 요소:', diffContent);
                           
                           diffContent.innerHTML = actualDiffContent;
                           
                           console.log('DOM에 삽입 후 diffContent.innerHTML 길이:', diffContent.innerHTML.length);
                           console.log('=== setTimeout 완료 ===');
                       }, 1000);
                   } else {
                       displayError('파일 정보를 불러올 수 없습니다.');
                   }
               }
               
               // 오류 표시 함수
               function displayError(message) {
                   const diffContent = document.getElementById('diff-content');
                   diffContent.innerHTML = `
                       <div class="text-center p-4">
                           <i class="fas fa-exclamation-triangle fa-2x text-warning mb-3"></i>
                           <h5 class="text-warning">오류 발생</h5>
                           <p class="text-muted">${message}</p>
                       </div>
                   `;
               }

               // 브랜치 선택 모달 표시 함수
               function showBranchSelectionModal() {
                   console.log('브랜치 선택 모달 표시 시작');
                   
                   const projectName = '<%= request.getAttribute("projectName") %>';
                   const currentBranch = '<%= request.getAttribute("branchName") %>';
                   
                   console.log('프로젝트명:', projectName);
                   console.log('현재 브랜치:', currentBranch);
                   
                   const modal = document.createElement('div');
                   modal.className = 'modal fade';
                   modal.id = 'branchSelectionModal';
                   modal.setAttribute('tabindex', '-1');
                   modal.setAttribute('aria-labelledby', 'branchSelectionModalLabel');
                   modal.setAttribute('aria-hidden', 'true');
                   
                   modal.innerHTML = `
                       <div class="modal-dialog modal-lg">
                           <div class="modal-content">
                               <div class="modal-header">
                                   <h5 class="modal-title" id="branchSelectionModalLabel">
                                       <i class="fas fa-code-branch me-2"></i>AI 분석을 위한 브랜치 선택
                                   </h5>
                                   <button type="button" class="btn-close" onclick="closeBranchSelectionModal()" aria-label="Close"></button>
                               </div>
                               <div class="modal-body">
                                   <div class="mb-3">
                                       <label for="targetBranch" class="form-label">분석 대상 브랜치 선택</label>
                                       <select class="form-select" id="targetBranch" required>
                                           <option value="">브랜치를 선택하세요...</option>
                                       </select>
                                   </div>
                                   <div class="alert alert-info">
                                       <i class="fas fa-info-circle me-2"></i>
                                       선택한 커밋을 대상 브랜치에 병합할 때의 잠재적 문제점과 개선사항을 분석합니다.
                                   </div>
                               </div>
                               <div class="modal-footer">
                                   <button type="button" class="btn btn-secondary" onclick="closeBranchSelectionModal()">취소</button>
                                   <button type="button" class="btn btn-primary" onclick="startMergeAnalysis()">
                                       <i class="fas fa-robot me-2"></i>AI 분석 시작
                                   </button>
                               </div>
                           </div>
                       </div>
                   `;
                   
                   document.body.appendChild(modal);
                   
                   // 브랜치 목록 로드
                   loadBranches(projectName);
                   
                   // 모달 표시
                   try {
                       const bootstrapModal = new bootstrap.Modal(modal);
                       bootstrapModal.show();
                   } catch (e) {
                       console.log('Bootstrap 모달 사용 불가, 수동으로 표시');
                       modal.style.display = 'block';
                       modal.classList.add('show');
                       document.body.classList.add('modal-open');
                       
                       // 배경 클릭으로 닫기
                       const backdrop = document.createElement('div');
                       backdrop.className = 'modal-backdrop fade show';
                       backdrop.onclick = closeBranchSelectionModal;
                       document.body.appendChild(backdrop);
                   }
               }
               
               // 브랜치 목록 로드 함수
               function loadBranches(projectName) {
                   console.log('브랜치 목록 로드 시작:', projectName);
                   
                   fetch('/git/branches?projectName=' + encodeURIComponent(projectName))
                       .then(response => response.text())
                       .then(html => {
                           console.log('브랜치 목록 응답 받음');
                           
                           const parser = new DOMParser();
                           const doc = parser.parseFromString(html, 'text/html');
                           const branchCards = doc.querySelectorAll('.branch-card');
                           
                           console.log('찾은 브랜치 아이템 수:', branchCards.length);
                           
                           const targetBranchSelect = document.getElementById('targetBranch');
                           const currentBranch = '<%= request.getAttribute("branchName") %>';
                           
                           // 기존 옵션 제거 (첫 번째 옵션 제외)
                           while (targetBranchSelect.children.length > 1) {
                               targetBranchSelect.removeChild(targetBranchSelect.lastChild);
                           }
                           
                           branchCards.forEach(card => {
                               const branchNameElement = card.querySelector('.card-title');
                               if (branchNameElement) {
                                   const branchName = branchNameElement.textContent.trim();
                                   console.log('브랜치 발견:', branchName);
                                   
                                   // 현재 브랜치는 제외
                                   if (branchName !== currentBranch) {
                                       const option = document.createElement('option');
                                       option.value = branchName;
                                       option.textContent = branchName;
                                       targetBranchSelect.appendChild(option);
                                   }
                               }
                           });
                           
                           console.log('브랜치 옵션 추가 완료');
                       })
                       .catch(error => {
                           console.error('브랜치 목록 로드 실패:', error);
                           const targetBranchSelect = document.getElementById('targetBranch');
                           const option = document.createElement('option');
                           option.value = '';
                           option.textContent = '브랜치 로드 실패';
                           targetBranchSelect.appendChild(option);
                       });
               }
               
               // 브랜치 선택 모달 닫기 함수
               function closeBranchSelectionModal() {
                   try {
                       const modal = document.getElementById('branchSelectionModal');
                       if (modal) {
                           const bootstrapModal = bootstrap.Modal.getInstance(modal);
                           if (bootstrapModal) {
                               bootstrapModal.hide();
                           } else {
                               modal.style.display = 'none';
                               modal.classList.remove('show');
                               document.body.classList.remove('modal-open');
                               
                               // 배경 제거
                               const backdrop = document.querySelector('.modal-backdrop');
                               if (backdrop) {
                                   backdrop.remove();
                               }
                           }
                           modal.remove();
                       }
                   } catch (e) {
                       console.log('모달 닫기 중 오류:', e);
                   }
               }
               
               // 병합 분석 시작 함수
               function startMergeAnalysis() {
                   const targetBranch = document.getElementById('targetBranch').value;
                   
                   if (!targetBranch) {
                       alert('브랜치를 선택해주세요.');
                       return;
                   }
                   
                   console.log('병합 분석 시작:', targetBranch);
                   
                   // 모달 닫기
                   closeBranchSelectionModal();
                   
                   // 로딩 표시
                   const projectName = '<%= request.getAttribute("projectName") %>';
                   const branchName = '<%= request.getAttribute("branchName") %>';
                   const commitId = '<%= commitDetail.get("id") %>';
                   
                   const aiAnalysisResult = document.getElementById('aiAnalysisResult');
                   aiAnalysisResult.innerHTML = '<div class="text-center p-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">로딩중...</span></div><div class="mt-2">AI 분석 중...</div></div>';
                   
                   // 팝업 표시
                   document.getElementById('aiPopupOverlay').style.display = 'flex';
                   
                   // 병합 분석 요청
                   fetch('/git/merge-analysis', {
                       method: 'POST',
                       headers: {
                           'Content-Type': 'application/json',
                       },
                       body: JSON.stringify({
                           projectName: projectName,
                           sourceBranch: branchName,
                           targetBranch: targetBranch,
                           commitIds: [commitId]
                       })
                   })
                   .then(response => response.json())
                   .then(data => {
                       if (data.success) {
                           aiAnalysisResult.innerHTML = data.analysis;
                       } else {
                           aiAnalysisResult.innerHTML = '<div class="alert alert-danger"><i class="fas fa-exclamation-triangle"></i> ' + data.message + '</div>';
                       }
                   })
                   .catch(error => {
                       console.error('병합 분석 요청 오류:', error);
                       aiAnalysisResult.innerHTML = '<div class="alert alert-danger"><i class="fas fa-exclamation-triangle"></i> 병합 분석 요청 중 오류가 발생했습니다.</div>';
                   });
               }

               // AI 분석 요청 함수 (기존 단일 커밋 분석용)
               function requestAIAnalysis() {
                   const projectName = '<%= request.getAttribute("projectName") %>';
                   const branchName = '<%= request.getAttribute("branchName") %>';
                   const commitId = '<%= commitDetail.get("id") %>';
                   
                   // 로딩 표시
                   const aiAnalysisResult = document.getElementById('aiAnalysisResult');
                   aiAnalysisResult.innerHTML = '<div class="text-center p-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">로딩중...</span></div><div class="mt-2">AI 분석 중...</div></div>';
                   
                   // 팝업 표시
                   document.getElementById('aiPopupOverlay').style.display = 'flex';
                   
                   // AI 분석 요청
                   fetch('/git/ai-analysis', {
                       method: 'POST',
                       headers: {
                           'Content-Type': 'application/json',
                       },
                       body: JSON.stringify({
                           projectName: projectName,
                           branchName: branchName,
                           commitIds: [commitId]
                       })
                   })
                   .then(response => response.json())
                   .then(data => {
                       if (data.success) {
                           aiAnalysisResult.innerHTML = data.analysis;
                       } else {
                           aiAnalysisResult.innerHTML = '<div class="alert alert-danger"><i class="fas fa-exclamation-triangle"></i> ' + data.message + '</div>';
                       }
                   })
                   .catch(error => {
                       console.error('AI 분석 요청 오류:', error);
                       aiAnalysisResult.innerHTML = '<div class="alert alert-danger"><i class="fas fa-exclamation-triangle"></i> AI 분석 요청 중 오류가 발생했습니다.</div>';
                   });
               }

               // AI 팝업 닫기 함수
               function closeAIPopup() {
                   const popup = document.getElementById('aiPopupOverlay');
                   if (popup) {
                       popup.style.display = 'none';
                   }
               }
               
               // AI 팝업 배경 클릭 시 닫기
               document.addEventListener('DOMContentLoaded', function() {
                   const popupOverlay = document.getElementById('aiPopupOverlay');
                   if (popupOverlay) {
                       popupOverlay.addEventListener('click', function(e) {
                           if (e.target === popupOverlay) {
                               closeAIPopup();
                           }
                       });
                   }
               });

               // AI 분석 결과 복사 함수
               function copyAnalysisResult() {
                   const analysisText = document.getElementById('aiAnalysisResult').textContent;
                   navigator.clipboard.writeText(analysisText).then(() => {
                       alert('AI 분석 결과가 클립보드에 복사되었습니다.');
                   }).catch(err => {
                       console.error('복사 실패:', err);
                       alert('복사에 실패했습니다.');
                   });
               }
               
    </script>

    <!-- AI 분석 결과 팝업 -->
    <div id="aiPopupOverlay" class="ai-popup-overlay" style="display: none;">
        <div class="ai-popup-content">
            <div class="ai-popup-header">
                <h5><i class="fas fa-robot me-2"></i>AI 분석 결과</h5>
                <button type="button" class="ai-popup-close" onclick="closeAIPopup()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="ai-popup-body">
                <div id="aiAnalysisResult" class="ai-analysis-content">
                    <!-- AI 분석 결과가 여기에 표시됩니다 -->
                </div>
            </div>
            <div class="ai-popup-footer">
                <button class="btn btn-outline-secondary btn-sm" onclick="copyAnalysisResult()">
                    <i class="fas fa-copy me-1"></i> 결과 복사
                </button>
                <button class="btn btn-secondary btn-sm" onclick="closeAIPopup()">
                    닫기
                </button>
            </div>
        </div>
    </div>

    <style>
        .ai-popup-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 9999;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        
        .ai-popup-content {
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            max-width: 800px;
            width: 90%;
            max-height: 80vh;
            display: flex;
            flex-direction: column;
            position: relative;
            z-index: 10000;
        }
        
        .ai-popup-header {
            padding: 20px;
            border-bottom: 1px solid #dee2e6;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 8px 8px 0 0;
        }
        
        .ai-popup-header h5 {
            margin: 0;
            font-weight: 600;
        }
        
        .ai-popup-close {
            background: none;
            border: none;
            color: white;
            font-size: 18px;
            cursor: pointer;
            padding: 5px;
            border-radius: 4px;
            transition: background-color 0.2s;
        }
        
        .ai-popup-close:hover {
            background-color: rgba(255, 255, 255, 0.2);
        }
        
        .ai-popup-body {
            padding: 20px;
            flex: 1;
            overflow-y: auto;
        }
        
        .ai-popup-footer {
            padding: 15px 20px;
            border-top: 1px solid #dee2e6;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            background-color: #f8f9fa;
            border-radius: 0 0 8px 8px;
        }
        
        .ai-analysis-content {
            white-space: pre-wrap;
            line-height: 1.6;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        .ai-analysis-content h1, .ai-analysis-content h2, .ai-analysis-content h3 {
            color: #2c3e50;
            margin-top: 20px;
            margin-bottom: 10px;
        }
        
        .ai-analysis-content h1:first-child {
            margin-top: 0;
        }
        
        .ai-analysis-content ul, .ai-analysis-content ol {
            padding-left: 20px;
        }
        
        .ai-analysis-content code {
            background-color: #f8f9fa;
            padding: 2px 4px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
        }
        
        .ai-analysis-content blockquote {
            border-left: 4px solid #007bff;
            padding-left: 15px;
            margin: 15px 0;
            color: #6c757d;
        }
        
        /* AI 분석 버튼 스타일 */
        .ai-analysis-btn {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 50%, #4facfe 100%);
            border: none;
            border-radius: 25px;
            padding: 12px 24px;
            font-weight: 600;
            font-size: 14px;
            color: white;
            box-shadow: 0 4px 15px rgba(79, 172, 254, 0.5);
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .ai-analysis-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(79, 172, 254, 0.7);
            background: linear-gradient(135deg, #2196f3 0%, #00bcd4 50%, #2196f3 100%);
        }
        
        .ai-analysis-btn:active {
            transform: translateY(0);
            box-shadow: 0 4px 15px rgba(79, 172, 254, 0.5);
        }
        
        .ai-analysis-btn:disabled {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            box-shadow: 0 2px 8px rgba(79, 172, 254, 0.2);
            transform: none;
            cursor: not-allowed;
            color: #90a4ae;
        }
        
        .ai-analysis-btn:disabled:hover {
            transform: none;
            box-shadow: 0 2px 8px rgba(79, 172, 254, 0.2);
        }
        
        .ai-analysis-btn::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            transition: left 0.5s;
        }
        
        .ai-analysis-btn:hover::before {
            left: 100%;
        }
        
        .ai-analysis-btn i {
            margin-right: 8px;
            animation: pulse 2s infinite;
        }
        
        @keyframes pulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.1); }
            100% { transform: scale(1); }
        }
        
        .ai-analysis-btn:disabled i {
            animation: none;
        }
    </style>
</body>
</html>

