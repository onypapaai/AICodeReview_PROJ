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
    <title>커밋 목록 - Cleverse AI Code Review</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .commit-item {
            transition: all 0.3s ease;
            cursor: pointer;
        }
        .commit-item:hover {
            background-color: #f8f9fa;
            transform: translateX(5px);
        }
        .commit-message {
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        .date-group {
            margin-bottom: 1.5rem;
        }
        .date-header {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 50%, #2c3e50 100%);
            color: white;
            padding: 16px 20px;
            border-radius: 8px;
            margin-bottom: 16px;
            box-shadow: 0 4px 16px rgba(44, 62, 80, 0.2);
            position: relative;
            overflow: hidden;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        .date-header:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 32px rgba(44, 62, 80, 0.35);
        }
        .date-header.collapsed {
            margin-bottom: 16px;
        }
        .date-header::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(45deg, rgba(255,255,255,0.1) 0%, transparent 100%);
            pointer-events: none;
        }
        .date-icon {
            width: 48px;
            height: 48px;
            background: rgba(255, 255, 255, 0.2);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            backdrop-filter: blur(10px);
        }
        .commit-count-badge {
            position: relative;
            z-index: 1;
        }
        .commits-for-date {
            padding-left: 0;
        }
        
        /* 커밋 카드 스타일 */
        .commit-card {
            background: white;
            border: 1px solid #e9ecef;
            border-radius: 8px;
            transition: all 0.3s ease;
            cursor: pointer;
            overflow: hidden;
            position: relative;
            margin-bottom: 6px;
        }
        .commit-card:first-child {
            margin-top: 2px;
        }
        .commit-card:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
            border-color: #667eea;
        }
        .commit-content {
            padding: 10px 12px;
        }
        .commits-for-date {
            transition: all 0.3s ease;
            overflow: hidden;
            padding-top: 8px;
        }
        .commits-for-date.collapsed {
            max-height: 0;
            opacity: 0;
            margin-bottom: 0;
        }
        .collapse-icon {
            transition: transform 0.3s ease;
        }
        .collapse-icon.rotated {
            transform: rotate(180deg);
        }
        .commit-header {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            margin-bottom: 8px;
        }
        .commit-avatar {
            width: 60px;
            height: 28px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }
        .avatar-text {
            color: white;
            font-weight: bold;
            font-size: 11px;
            line-height: 1.0;
            text-align: center;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .commit-info {
            flex: 1;
            min-width: 0;
        }
        .commit-title {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;
        }
        .commit-id {
            font-family: 'Courier New', monospace;
            font-weight: bold;
            color: #6c757d;
            font-size: 11px;
            background: #e9ecef;
            padding: 2px 6px;
            border-radius: 4px;
        }
        
        .commit-author {
            font-weight: 600;
            color: #495057;
            font-size: 13px;
            background: #f8f9fa;
            padding: 4px 8px;
            border-radius: 6px;
            border: 1px solid #e9ecef;
        }
        .commit-type-badge {
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 500;
        }
        .commit-message-section {
            margin: 8px 0;
            padding: 8px;
            border-radius: 6px;
            transition: background-color 0.2s ease;
        }
        
        .commit-message-section:hover {
            background-color: #f8f9fa;
        }
        
        .commit-message {
            font-size: 15px;
            font-weight: 600;
            color: #1a202c;
            line-height: 1.5;
            margin: 0;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            background: #f8f9fa;
            padding: 12px 16px;
            border-radius: 8px;
            border-left: 4px solid #4facfe;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        }
        .commit-actions {
            flex-shrink: 0;
        }
        .commit-meta {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }
        .meta-item {
            display: flex;
            align-items: center;
            gap: 3px;
            color: #6c757d;
            font-size: 10px;
        }
        .meta-item i {
            width: 16px;
            text-align: center;
        }
        
        /* 커스텀 색상 */
        .bg-purple {
            background-color: #6f42c1 !important;
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
        
        /* 반응형 디자인 */
        @media (max-width: 768px) {
            .commit-header {
                flex-direction: column;
                gap: 12px;
            }
            .commit-actions {
                align-self: stretch;
            }
            .commit-actions .btn {
                width: 100%;
            }
            .commit-meta {
                gap: 16px;
            }
            .date-header {
                padding: 20px;
            }
            .date-header .d-flex {
                flex-direction: column;
                gap: 16px;
                text-align: center;
            }
        }
    </style>
</head>
<body>
    <!-- Loading Progress Bar -->
    <div id="commits-loading" class="position-fixed top-50 start-50 translate-middle text-center" style="display: none; z-index: 9999;">
        <div class="bg-white p-4 rounded shadow-lg">
            <div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;">
                <span class="visually-hidden">Loading...</span>
            </div>
            <h5 class="text-primary">커밋 목록을 불러오는 중...</h5>
            <p class="text-muted mb-0">잠시만 기다려주세요.</p>
        </div>
    </div>

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

    <div class="container my-5" style="margin-bottom: 200px;">
        <div class="row">
            <div class="col-12">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/git/projects">프로젝트</a></li>
                        <li class="breadcrumb-item"><a href="/git/branches?projectName=<%= request.getAttribute("projectName") %>">브랜치</a></li>
                        <li class="breadcrumb-item active"><%= request.getAttribute("branchName") %></li>
                    </ol>
                </nav>
                
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h1 class="mb-0">
                        <i class="fas fa-history text-primary"></i> 커밋 목록
                    </h1>
                    <div class="d-flex gap-2">
                        <button id="selectAllBtn" class="btn btn-outline-secondary btn-sm" onclick="toggleSelectAll()">
                            <i class="fas fa-check-square me-1"></i> 전체 선택
                        </button>
                        <button id="aiAnalysisBtn" class="ai-analysis-btn" onclick="requestAIAnalysis()" disabled>
                            <i class="fas fa-robot"></i> AI 분석 요청
                        </button>
                    </div>
                </div>
                <div class="alert alert-info mb-4">
                    <i class="fas fa-info-circle me-2"></i>
                    <strong>원격에 푸시된 커밋만 표시됩니다.</strong> 로컬에서만 커밋한 내용은 제외됩니다.
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-12">
                <%
                    Map<String, List<Map<String, Object>>> commitsByDate = (Map<String, List<Map<String, Object>>>) request.getAttribute("commitsByDate");
                    if (commitsByDate != null && !commitsByDate.isEmpty()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        for (Map.Entry<String, List<Map<String, Object>>> dateEntry : commitsByDate.entrySet()) {
                            String dateKey = dateEntry.getKey();
                            List<Map<String, Object>> dayCommits = dateEntry.getValue();
                            
                            // 첫 번째 커밋에서 표시용 날짜 가져오기
                            String displayDate = dayCommits.get(0).get("displayDate").toString();
                %>
                <div class="date-group mb-4">
                    <div class="date-header mb-3" onclick="toggleDateGroup(this)">
                        <div class="d-flex align-items-center justify-content-between">
                            <div class="d-flex align-items-center">
                                <div class="date-icon me-3">
                                    <i class="fas fa-calendar-day"></i>
                                </div>
                                <div>
                                    <h4 class="mb-1 text-white fw-bold"><%= displayDate %></h4>
                                    <p class="mb-0 text-white-50 small">총 <%= dayCommits.size() %>개의 커밋</p>
                                </div>
                            </div>
                            <div class="d-flex align-items-center">
                                <div class="collapse-icon">
                                    <i class="fas fa-chevron-down text-white"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="commits-for-date">
                        <%
                            for (Map<String, Object> commit : dayCommits) {
                                Date commitDate = (Date) commit.get("date");
                                String formattedDate = commitDate != null ? sdf.format(commitDate) : "알 수 없음";
                        %>
                        <div class="commit-card" data-commit-id="<%= commit.get("id") %>">
                            <div class="commit-content">
                                <div class="commit-header">
                                    <div class="form-check me-3">
                                        <input class="form-check-input commit-checkbox" type="checkbox" 
                                               value="<%= commit.get("id") %>" 
                                               id="commit-<%= commit.get("id") %>"
                                               onchange="updateAIAnalysisButton()">
                                        <label class="form-check-label" for="commit-<%= commit.get("id") %>"></label>
                                    </div>
                                    <div class="commit-avatar" data-commit-id="<%= commit.get("id") %>" onclick="viewCommitDetail(this.dataset.commitId)">
                                        <span class="avatar-text"><%= commit.get("shortId") %></span>
                                    </div>
                                    <div class="commit-info" data-commit-id="<%= commit.get("id") %>" onclick="viewCommitDetail(this.dataset.commitId)">
                                        <div class="commit-title">
                                            <span class="commit-type-badge" id="commit-type-<%= commit.get("id") %>"></span>
                                        </div>
                                    </div>
                                    <div class="commit-actions">
                                        <button class="btn btn-outline-primary btn-sm" data-commit-id="<%= commit.get("id") %>" onclick="event.stopPropagation(); viewCommitDetail(this.dataset.commitId)">
                                            <i class="fas fa-eye me-1"></i> 상세보기
                                        </button>
                                    </div>
                                </div>
                                <div class="commit-message-section" data-commit-id="<%= commit.get("id") %>" onclick="viewCommitDetail(this.dataset.commitId)" style="cursor: pointer;">
                                    <p class="commit-message"><%= commit.get("message") %></p>
                                </div>
                                <div class="commit-meta">
                                    <div class="meta-item">
                                        <i class="fas fa-user"></i>
                                        <span><%= commit.get("author") %></span>
                                    </div>
                                    <div class="meta-item">
                                        <i class="fas fa-clock"></i>
                                        <span><%= formattedDate %></span>
                                    </div>
                                    <div class="meta-item">
                                        <i class="fas fa-file-code"></i>
                                        <span><%= commit.get("changedFilesCount") != null ? commit.get("changedFilesCount") + "개 파일" : "변경사항" %></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <%
                            }
                        %>
                    </div>
                </div>
                <%
                        }
                    } else {
                %>
                <div class="alert alert-info text-center">
                    <i class="fas fa-info-circle fa-2x mb-3"></i>
                    <h5>커밋이 없습니다</h5>
                    <p>이 브랜치에는 커밋이 없습니다.</p>
                </div>
                <%
                    }
                %>
            </div>
        </div>
    </div>


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


        function viewCommitDetail(commitId) {
            const projectName = '<%= request.getAttribute("projectName") %>';
            const branchName = '<%= request.getAttribute("branchName") %>';
            
            // 기존 로딩 상태 제거
            const existingLoading = document.getElementById('commit-detail-loading');
            if (existingLoading) {
                existingLoading.remove();
            }
            
            // 로딩 상태 표시
            const loadingDiv = document.createElement('div');
            loadingDiv.id = 'commit-detail-loading';
            loadingDiv.className = 'position-fixed top-50 start-50 translate-middle text-center';
            loadingDiv.style.zIndex = '9999';
            loadingDiv.innerHTML = 
                '<div class="bg-white p-4 rounded shadow-lg">' +
                    '<div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;">' +
                        '<span class="visually-hidden">Loading...</span>' +
                    '</div>' +
                    '<h5 class="text-primary">커밋 상세 정보를 불러오는 중...</h5>' +
                    '<p class="text-muted mb-0">커밋 정보를 로딩하고 있습니다.</p>' +
                '</div>';
            document.body.appendChild(loadingDiv);
            
            // 프로그래스바가 표시되도록 약간의 지연 후 페이지 이동
            setTimeout(() => {
                const url = '/git/commit-detail?projectName=' + encodeURIComponent(projectName) + 
                           '&branchName=' + encodeURIComponent(branchName) + 
                           '&commitId=' + commitId;
                window.location.href = url;
            }, 100);
        }
        
        // 커밋 목록 로딩 상태 제거 함수
        function removeLoadingStates() {
            // 모든 로딩 상태 제거
            const loadingElements = document.querySelectorAll('.position-fixed.top-50.start-50.translate-middle');
            loadingElements.forEach(element => {
                element.remove();
            });

            // 특정 ID로도 제거
            const commitsLoading = document.getElementById('commits-loading');
            if (commitsLoading) {
                commitsLoading.remove();
            }

            const commitDetailLoading = document.getElementById('commit-detail-loading');
            if (commitDetailLoading) {
                commitDetailLoading.remove();
            }

            const branchLoading = document.getElementById('branch-loading');
            if (branchLoading) {
                branchLoading.remove();
            }

            const compareLoading = document.getElementById('compare-loading');
            if (compareLoading) {
                compareLoading.remove();
            }

            const commitListLoading = document.getElementById('commit-list-loading');
            if (commitListLoading) {
                commitListLoading.remove();
            }
        }
        
        // 커밋 메시지 포맷팅 함수
        function formatCommitMessage(message) {
            if (!message) return message;
            
            // 커밋 메시지 타입별 아이콘과 스타일 추가
            let icon = '';
            let type = '';
            let bgClass = '';
            
            if (message.toLowerCase().includes('feat') || message.toLowerCase().includes('feature')) {
                icon = '✨';
                type = '새 기능';
                bgClass = 'bg-success';
            } else if (message.toLowerCase().includes('fix') || message.toLowerCase().includes('bug')) {
                icon = '🐛';
                type = '버그 수정';
                bgClass = 'bg-danger';
            } else if (message.toLowerCase().includes('refactor')) {
                icon = '🔧';
                type = '리팩토링';
                bgClass = 'bg-warning';
            } else if (message.toLowerCase().includes('docs') || message.toLowerCase().includes('doc')) {
                icon = '📝';
                type = '문서';
                bgClass = 'bg-info';
            } else if (message.toLowerCase().includes('style')) {
                icon = '💄';
                type = '스타일';
                bgClass = 'bg-secondary';
            } else if (message.toLowerCase().includes('test')) {
                icon = '🧪';
                type = '테스트';
                bgClass = 'bg-primary';
            } else if (message.toLowerCase().includes('chore')) {
                icon = '🔨';
                type = '기타';
                bgClass = 'bg-dark';
            } else if (message.toLowerCase().includes('merge')) {
                icon = '🔀';
                type = '병합';
                bgClass = 'bg-purple';
            } else {
                icon = '📦';
                type = '변경';
                bgClass = 'bg-secondary';
            }
            
            return {
                icon: icon,
                type: type,
                message: message,
                bgClass: bgClass
            };
        }
        
        // 커밋 카드 포맷팅
        function formatCommitCards() {
            document.querySelectorAll('.commit-card').forEach(card => {
                const messageElement = card.querySelector('.commit-message');
                const badgeElement = card.querySelector('.commit-type-badge');
                const commitId = card.getAttribute('data-commit-id');
                
                if (messageElement && badgeElement) {
                    const message = messageElement.textContent.trim();
                    const formatted = formatCommitMessage(message);
                    
                    // 메시지에 아이콘 추가
                    messageElement.innerHTML = formatted.icon + ' ' + formatted.message;
                    
                    // 배지 스타일 적용
                    badgeElement.innerHTML = formatted.type;
                    badgeElement.className = 'commit-type-badge ' + formatted.bgClass + ' text-white';
                }
            });
        }
        
        // 날짜 그룹 접기/펼치기 기능
        function toggleDateGroup(headerElement) {
            const dateGroup = headerElement.closest('.date-group');
            const commitsContainer = dateGroup.querySelector('.commits-for-date');
            const collapseIcon = headerElement.querySelector('.collapse-icon i');
            
            if (commitsContainer.classList.contains('collapsed')) {
                // 펼치기
                commitsContainer.classList.remove('collapsed');
                headerElement.classList.remove('collapsed');
                collapseIcon.classList.remove('rotated');
            } else {
                // 접기
                commitsContainer.classList.add('collapsed');
                headerElement.classList.add('collapsed');
                collapseIcon.classList.add('rotated');
            }
        }
        
        // 페이지 로드 시 커밋 카드 포맷팅
        document.addEventListener('DOMContentLoaded', formatCommitCards);
        
        // 즉시 로딩 상태 제거 (스크립트 로드 시)
        removeLoadingStates();
        
        // DOM 로드 시
        document.addEventListener('DOMContentLoaded', removeLoadingStates);
        
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
        
        // 페이지 로드 시 AI 분석 버튼 상태 업데이트
        document.addEventListener('DOMContentLoaded', function() {
            console.log('페이지 로드 완료, AI 분석 버튼 상태 업데이트');
            updateAIAnalysisButton();
        });
        
        // AI 분석 요청 관련 함수들
        function updateAIAnalysisButton() {
            console.log('updateAIAnalysisButton 함수 호출됨');
            
            const checkboxes = document.querySelectorAll('.commit-checkbox:checked');
            const aiAnalysisBtn = document.getElementById('aiAnalysisBtn');
            const selectAllBtn = document.getElementById('selectAllBtn');
            
            console.log('체크된 체크박스 수:', checkboxes.length);
            console.log('AI 분석 버튼 요소:', aiAnalysisBtn);
            
            if (checkboxes.length > 0) {
                aiAnalysisBtn.disabled = false;
                aiAnalysisBtn.innerHTML = '<i class="fas fa-robot"></i> AI 분석 요청 (' + checkboxes.length + ')';
                console.log('AI 분석 버튼 활성화됨');
            } else {
                aiAnalysisBtn.disabled = true;
                aiAnalysisBtn.innerHTML = '<i class="fas fa-robot"></i> AI 분석 요청';
                console.log('AI 분석 버튼 비활성화됨');
            }
            
            // 전체 선택 버튼 상태 업데이트
            const allCheckboxes = document.querySelectorAll('.commit-checkbox');
            if (checkboxes.length === allCheckboxes.length) {
                selectAllBtn.innerHTML = '<i class="fas fa-check-square me-1"></i> 전체 해제';
            } else {
                selectAllBtn.innerHTML = '<i class="fas fa-check-square me-1"></i> 전체 선택';
            }
        }
        
        function toggleSelectAll() {
            const allCheckboxes = document.querySelectorAll('.commit-checkbox');
            const selectAllBtn = document.getElementById('selectAllBtn');
            const checkedCheckboxes = document.querySelectorAll('.commit-checkbox:checked');
            
            if (checkedCheckboxes.length === allCheckboxes.length) {
                // 모두 선택된 상태면 모두 해제
                allCheckboxes.forEach(checkbox => checkbox.checked = false);
                selectAllBtn.innerHTML = '<i class="fas fa-check-square me-1"></i> 전체 선택';
            } else {
                // 일부만 선택된 상태면 모두 선택
                allCheckboxes.forEach(checkbox => checkbox.checked = true);
                selectAllBtn.innerHTML = '<i class="fas fa-check-square me-1"></i> 전체 해제';
            }
            
            updateAIAnalysisButton();
        }
        
        function requestAIAnalysis() {
            console.log('requestAIAnalysis 함수 호출됨');
            
            const selectedCommits = Array.from(document.querySelectorAll('.commit-checkbox:checked'))
                .map(checkbox => checkbox.value);
            
            console.log('선택된 커밋 수:', selectedCommits.length);
            console.log('선택된 커밋 ID들:', selectedCommits);
            
            if (selectedCommits.length === 0) {
                alert('분석할 커밋을 선택해주세요.');
                return;
            }
            
            // 브랜치 선택 모달 표시
            console.log('브랜치 선택 모달 표시 시작');
            showBranchSelectionModal(selectedCommits);
        }
        
        function showBranchSelectionModal(selectedCommits) {
            console.log('showBranchSelectionModal 함수 호출됨');
            console.log('선택된 커밋들:', selectedCommits);
            
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
            
            modal.innerHTML = 
                '<div class="modal-dialog modal-lg">' +
                    '<div class="modal-content">' +
                        '<div class="modal-header">' +
                            '<h5 class="modal-title" id="branchSelectionModalLabel">' +
                                '<i class="fas fa-code-branch me-2"></i>브랜치 선택 및 병합 분석' +
                            '</h5>' +
                            '<button type="button" class="btn-close" onclick="closeBranchSelectionModal()" aria-label="Close"></button>' +
                        '</div>' +
                        '<div class="modal-body">' +
                            '<div class="mb-3">' +
                                '<label class="form-label">현재 브랜치: <strong>' + currentBranch + '</strong></label>' +
                            '</div>' +
                            '<div class="mb-3">' +
                                '<label for="targetBranch" class="form-label">병합할 대상 브랜치를 선택하세요:</label>' +
                                '<select class="form-select" id="targetBranch">' +
                                    '<option value="">브랜치를 선택하세요...</option>' +
                                '</select>' +
                            '</div>' +
                            '<div class="mb-3">' +
                                '<label class="form-label">선택된 커밋 수: <span class="badge bg-primary">' + selectedCommits.length + '개</span></label>' +
                            '</div>' +
                            '<div class="alert alert-info">' +
                                '<i class="fas fa-info-circle me-2"></i>' +
                                '선택한 커밋들을 대상 브랜치에 병합했을 때의 잠재적 문제점과 개선안을 분석합니다.' +
                            '</div>' +
                        '</div>' +
                        '<div class="modal-footer">' +
                            '<button type="button" class="btn btn-secondary" onclick="closeBranchSelectionModal()">취소</button>' +
                            '<button type="button" class="btn btn-primary" onclick="startMergeAnalysis()">' +
                                '<i class="fas fa-robot me-1"></i>병합 분석 시작' +
                            '</button>' +
                        '</div>' +
                    '</div>' +
                '</div>';
            
            console.log('모달 HTML 생성 완료, DOM에 추가 중...');
            document.body.appendChild(modal);
            
            // 브랜치 목록 로드
            console.log('브랜치 목록 로드 시작...');
            loadBranches(projectName);
            
            // Bootstrap 모달 표시
            console.log('Bootstrap 모달 표시 중...');
            try {
                if (typeof bootstrap !== 'undefined') {
                    const bootstrapModal = new bootstrap.Modal(modal);
                    bootstrapModal.show();
                    console.log('Bootstrap 모달 표시 완료');
                } else {
                    // Bootstrap이 없는 경우 직접 표시
                    console.log('Bootstrap이 없음, 직접 모달 표시');
                    modal.style.display = 'block';
                    modal.classList.add('show');
                    document.body.classList.add('modal-open');
                    
                    // 배경 클릭 시 모달 닫기
                    const backdrop = document.createElement('div');
                    backdrop.className = 'modal-backdrop fade show';
                    backdrop.onclick = function() {
                        modal.style.display = 'none';
                        modal.classList.remove('show');
                        document.body.classList.remove('modal-open');
                        backdrop.remove();
                        modal.remove();
                    };
                    document.body.appendChild(backdrop);
                }
            } catch (error) {
                console.error('모달 표시 오류:', error);
                // 폴백: 직접 표시
                modal.style.display = 'block';
                modal.classList.add('show');
                document.body.classList.add('modal-open');
            }
            
            // 모달이 닫힐 때 DOM에서 제거
            modal.addEventListener('hidden.bs.modal', function () {
                modal.remove();
            });
        }
        
        function closeBranchSelectionModal() {
            console.log('브랜치 선택 모달 닫기');
            const modal = document.getElementById('branchSelectionModal');
            if (modal) {
                try {
                    if (typeof bootstrap !== 'undefined') {
                        const bootstrapModal = bootstrap.Modal.getInstance(modal);
                        if (bootstrapModal) {
                            bootstrapModal.hide();
                        }
                    } else {
                        // Bootstrap이 없는 경우 직접 닫기
                        modal.style.display = 'none';
                        modal.classList.remove('show');
                        document.body.classList.remove('modal-open');
                        const backdrop = document.querySelector('.modal-backdrop');
                        if (backdrop) {
                            backdrop.remove();
                        }
                        modal.remove();
                    }
                } catch (error) {
                    console.error('모달 닫기 오류:', error);
                    // 폴백: 직접 닫기
                    modal.style.display = 'none';
                    modal.classList.remove('show');
                    document.body.classList.remove('modal-open');
                    const backdrop = document.querySelector('.modal-backdrop');
                    if (backdrop) {
                        backdrop.remove();
                    }
                    modal.remove();
                }
            }
        }
        
        function loadBranches(projectName) {
            console.log('loadBranches 함수 호출됨, 프로젝트명:', projectName);
            
            const currentBranch = '<%= request.getAttribute("branchName") %>';
            console.log('현재 브랜치 (제외할 브랜치):', currentBranch);
            
            fetch('/git/branches?projectName=' + encodeURIComponent(projectName))
                .then(response => {
                    console.log('브랜치 목록 응답 상태:', response.status);
                    return response.text();
                })
                .then(html => {
                    console.log('브랜치 목록 HTML 받음, 길이:', html.length);
                    
                    // HTML에서 브랜치 목록 추출
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const branchItems = doc.querySelectorAll('.branch-card');
                    
                    console.log('찾은 브랜치 아이템 수:', branchItems.length);
                    
                    const select = document.getElementById('targetBranch');
                    if (!select) {
                        console.error('targetBranch select 요소를 찾을 수 없습니다');
                        return;
                    }
                    
                    select.innerHTML = '<option value="">브랜치를 선택하세요...</option>';
                    
                    branchItems.forEach((item, index) => {
                        // 브랜치 카드에서 브랜치 이름 추출 (.card-title 클래스 사용)
                        const branchNameElement = item.querySelector('.card-title');
                        if (branchNameElement) {
                            const branchName = branchNameElement.textContent.trim();
                            
                            // 현재 브랜치는 제외
                            if (branchName !== currentBranch) {
                                console.log('브랜치 추가:', branchName);
                                const option = document.createElement('option');
                                option.value = branchName;
                                option.textContent = branchName;
                                select.appendChild(option);
                            } else {
                                console.log('현재 브랜치 제외:', branchName);
                            }
                        }
                    });
                    
                    console.log('브랜치 목록 로드 완료');
                })
                .catch(error => {
                    console.error('브랜치 목록 로드 오류:', error);
                    alert('브랜치 목록을 불러오는 중 오류가 발생했습니다.');
                });
        }
        
        function startMergeAnalysis() {
            const targetBranch = document.getElementById('targetBranch').value;
            if (!targetBranch) {
                alert('대상 브랜치를 선택해주세요.');
                return;
            }
            
            const selectedCommits = Array.from(document.querySelectorAll('.commit-checkbox:checked'))
                .map(checkbox => checkbox.value);
            
            const projectName = '<%= request.getAttribute("projectName") %>';
            const currentBranch = '<%= request.getAttribute("branchName") %>';
            
            // 브랜치 선택 모달 닫기
            try {
                if (typeof bootstrap !== 'undefined') {
                    const branchModal = bootstrap.Modal.getInstance(document.getElementById('branchSelectionModal'));
                    if (branchModal) {
                        branchModal.hide();
                    }
                } else {
                    // Bootstrap이 없는 경우 직접 닫기
                    const modal = document.getElementById('branchSelectionModal');
                    if (modal) {
                        modal.style.display = 'none';
                        modal.classList.remove('show');
                        document.body.classList.remove('modal-open');
                        const backdrop = document.querySelector('.modal-backdrop');
                        if (backdrop) {
                            backdrop.remove();
                        }
                        modal.remove();
                    }
                }
            } catch (error) {
                console.error('모달 닫기 오류:', error);
            }
            
            // 로딩 상태 표시
            const loadingDiv = document.createElement('div');
            loadingDiv.id = 'merge-analysis-loading';
            loadingDiv.className = 'position-fixed top-50 start-50 translate-middle text-center';
            loadingDiv.style.zIndex = '9999';
            loadingDiv.innerHTML = 
                '<div class="bg-white p-4 rounded shadow-lg">' +
                    '<div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;">' +
                        '<span class="visually-hidden">Loading...</span>' +
                    '</div>' +
                    '<h5 class="text-primary">병합 분석 중...</h5>' +
                    '<p class="text-muted mb-0">선택된 ' + selectedCommits.length + '개 커밋을 ' + targetBranch + ' 브랜치에 병합 분석하고 있습니다.</p>' +
                '</div>';
            document.body.appendChild(loadingDiv);
            
            fetch('/git/merge-analysis', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    projectName: projectName,
                    currentBranch: currentBranch,
                    targetBranch: targetBranch,
                    commitIds: selectedCommits
                })
            })
            .then(response => response.json())
            .then(data => {
                // 로딩 상태 제거
                const loadingElement = document.getElementById('merge-analysis-loading');
                if (loadingElement) {
                    loadingElement.remove();
                }
                
                if (data.success) {
                    showMergeAnalysisModal(data.analysis, data.analyzedCommits, data.projectName, data.currentBranch, data.targetBranch);
                } else {
                    alert('병합 분석 중 오류가 발생했습니다: ' + data.message);
                }
            })
            .catch(error => {
                console.error('병합 분석 요청 오류:', error);
                // 로딩 상태 제거
                const loadingElement = document.getElementById('merge-analysis-loading');
                if (loadingElement) {
                    loadingElement.remove();
                }
                alert('병합 분석 요청 중 오류가 발생했습니다.');
            });
        }
        
        function showMergeAnalysisModal(analysis, analyzedCommits, projectName, currentBranch, targetBranch) {
            const modal = document.createElement('div');
            modal.className = 'modal fade';
            modal.id = 'mergeAnalysisModal';
            modal.setAttribute('tabindex', '-1');
            modal.setAttribute('aria-labelledby', 'mergeAnalysisModalLabel');
            modal.setAttribute('aria-hidden', 'true');
            
            modal.innerHTML = 
                '<div class="modal-dialog modal-xl">' +
                    '<div class="modal-content">' +
                        '<div class="modal-header">' +
                            '<h5 class="modal-title" id="mergeAnalysisModalLabel">' +
                                '<i class="fas fa-code-branch me-2"></i>병합 분석 결과' +
                            '</h5>' +
                            '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>' +
                        '</div>' +
                        '<div class="modal-body">' +
                            '<div class="row mb-3">' +
                                '<div class="col-md-6">' +
                                    '<div class="card">' +
                                        '<div class="card-header">' +
                                            '<h6 class="mb-0"><i class="fas fa-info-circle me-2"></i>분석 정보</h6>' +
                                        '</div>' +
                                        '<div class="card-body">' +
                                            '<p><strong>프로젝트:</strong> ' + projectName + '</p>' +
                                            '<p><strong>현재 브랜치:</strong> ' + currentBranch + '</p>' +
                                            '<p><strong>대상 브랜치:</strong> ' + targetBranch + '</p>' +
                                            '<p><strong>분석 커밋 수:</strong> ' + analyzedCommits + '개</p>' +
                                        '</div>' +
                                    '</div>' +
                                '</div>' +
                            '</div>' +
                            '<div class="analysis-content" style="white-space: pre-wrap; line-height: 1.6; font-family: \'Segoe UI\', Tahoma, Geneva, Verdana, sans-serif;">' +
                                analysis +
                            '</div>' +
                        '</div>' +
                        '<div class="modal-footer">' +
                            '<button type="button" class="btn btn-primary" onclick="copyAnalysisResult()">' +
                                '<i class="fas fa-copy me-1"></i> 결과 복사' +
                            '</button>' +
                            '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>' +
                        '</div>' +
                    '</div>' +
                '</div>';
            
            document.body.appendChild(modal);
            
            // Bootstrap 모달 표시
            try {
                if (typeof bootstrap !== 'undefined') {
                    const bootstrapModal = new bootstrap.Modal(modal);
                    bootstrapModal.show();
                    
                    // 모달이 닫힐 때 DOM에서 제거
                    modal.addEventListener('hidden.bs.modal', function () {
                        modal.remove();
                    });
                } else {
                    // Bootstrap이 없는 경우 직접 표시
                    modal.style.display = 'block';
                    modal.classList.add('show');
                    document.body.classList.add('modal-open');
                    
                    // 배경 클릭 시 모달 닫기
                    const backdrop = document.createElement('div');
                    backdrop.className = 'modal-backdrop fade show';
                    backdrop.onclick = function() {
                        modal.style.display = 'none';
                        modal.classList.remove('show');
                        document.body.classList.remove('modal-open');
                        backdrop.remove();
                        modal.remove();
                    };
                    document.body.appendChild(backdrop);
                    
                    // X 버튼과 닫기 버튼에 이벤트 리스너 추가
                    const closeButtons = modal.querySelectorAll('[data-bs-dismiss="modal"], .btn-close');
                    closeButtons.forEach(button => {
                        button.addEventListener('click', function() {
                            modal.style.display = 'none';
                            modal.classList.remove('show');
                            document.body.classList.remove('modal-open');
                            backdrop.remove();
                            modal.remove();
                        });
                    });
                }
            } catch (error) {
                console.error('모달 표시 오류:', error);
                // 폴백: 직접 표시
                modal.style.display = 'block';
                modal.classList.add('show');
                document.body.classList.add('modal-open');
                
                // 배경 클릭 시 모달 닫기
                const backdrop = document.createElement('div');
                backdrop.className = 'modal-backdrop fade show';
                backdrop.onclick = function() {
                    modal.style.display = 'none';
                    modal.classList.remove('show');
                    document.body.classList.remove('modal-open');
                    backdrop.remove();
                    modal.remove();
                };
                document.body.appendChild(backdrop);
                
                // X 버튼과 닫기 버튼에 이벤트 리스너 추가
                const closeButtons = modal.querySelectorAll('[data-bs-dismiss="modal"], .btn-close');
                closeButtons.forEach(button => {
                    button.addEventListener('click', function() {
                        modal.style.display = 'none';
                        modal.classList.remove('show');
                        document.body.classList.remove('modal-open');
                        backdrop.remove();
                        modal.remove();
                    });
                });
            }
        }
        
        function showAIAnalysisModal(analysis) {
            // 기존 모달이 있으면 제거
            const existingModal = document.getElementById('aiAnalysisModal');
            if (existingModal) {
                existingModal.remove();
            }
            
            // 모달 생성
            const modal = document.createElement('div');
            modal.id = 'aiAnalysisModal';
            modal.className = 'modal fade';
            modal.setAttribute('tabindex', '-1');
            modal.setAttribute('aria-labelledby', 'aiAnalysisModalLabel');
            modal.setAttribute('aria-hidden', 'true');
            modal.innerHTML = 
                '<div class="modal-dialog modal-lg">' +
                    '<div class="modal-content">' +
                        '<div class="modal-header bg-primary text-white">' +
                            '<h5 class="modal-title" id="aiAnalysisModalLabel">' +
                                '<i class="fas fa-robot me-2"></i>AI 코드 리뷰 분석 결과' +
                            '</h5>' +
                            '<button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>' +
                        '</div>' +
                        '<div class="modal-body" style="max-height: 70vh; overflow-y: auto;">' +
                            '<div class="analysis-content">' +
                                analysis +
                            '</div>' +
                        '</div>' +
                        '<div class="modal-footer">' +
                            '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>' +
                            '<button type="button" class="btn btn-primary" onclick="copyAnalysisResult()">' +
                                '<i class="fas fa-copy me-1"></i> 결과 복사' +
                            '</button>' +
                        '</div>' +
                    '</div>' +
                '</div>';
            
            document.body.appendChild(modal);
            
            // Bootstrap 모달 표시
            const bootstrapModal = new bootstrap.Modal(modal);
            bootstrapModal.show();
            
            // 모달이 닫힐 때 DOM에서 제거
            modal.addEventListener('hidden.bs.modal', function () {
                modal.remove();
            });
        }
        
        function copyAnalysisResult() {
            const analysisContent = document.querySelector('#aiAnalysisModal .analysis-content');
            if (analysisContent) {
                const text = analysisContent.textContent || analysisContent.innerText;
                navigator.clipboard.writeText(text).then(() => {
                    alert('분석 결과가 클립보드에 복사되었습니다.');
                }).catch(err => {
                    console.error('복사 실패:', err);
                    alert('복사에 실패했습니다.');
                });
            }
        }
    </script>
</body>
</html>
