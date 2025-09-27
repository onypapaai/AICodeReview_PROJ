<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>브랜치 선택 - Cleverse AI Code Review</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .branch-card {
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            cursor: pointer;
        }
        .branch-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 20px rgba(0,0,0,0.1);
        }
        .branch-type {
            font-size: 0.8em;
        }
        
        /* 검색 결과 스타일 개선 (2025-01-27) */
        .search-results-preview {
            max-height: 250px;
            overflow-y: auto;
            overflow-x: hidden; /* 가로스크롤 방지 */
        }
        
        .search-result-item {
            border-left: 3px solid #e9ecef;
            padding: 8px 12px;
            margin-bottom: 8px;
            border-radius: 4px;
            transition: all 0.2s ease;
            word-wrap: break-word;
            overflow-wrap: break-word;
            white-space: normal;
        }
        
        .search-content {
            font-size: 0.9em;
            color: #495057;
            margin-top: 4px;
            word-break: break-word;
            overflow-wrap: break-word;
            white-space: normal;
            line-height: 1.4;
        }
        
        .search-result-item:hover {
            border-left-color: #007bff;
            background-color: #f8f9fa;
            transform: translateX(2px);
        }
        
        /* 커밋 검색 결과 스타일 */
        .commit-result {
            background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
            border-left-color: #2196f3;
        }
        
        .commit-result:hover {
            background: linear-gradient(135deg, #bbdefb 0%, #e1bee7 100%);
        }
        
        /* 소스 검색 결과 스타일 */
        .source-result {
            background: linear-gradient(135deg, #e8f5e8 0%, #f1f8e9 100%);
            border-left-color: #4caf50;
        }
        
        .source-result:hover {
            background: linear-gradient(135deg, #c8e6c9 0%, #dcedc8 100%);
        }
        
        /* 브랜치명 검색 결과 스타일 */
        .branch-name-result {
            background: linear-gradient(135deg, #fff3e0 0%, #fce4ec 100%);
            border-left-color: #ff9800;
        }
        
        .branch-name-result:hover {
            background: linear-gradient(135deg, #ffe0b2 0%, #f8bbd9 100%);
        }
        
        /* 검색 결과 카드 개선 */
        .search-result-card {
            border: 1px solid #e9ecef;
            border-radius: 8px;
            transition: all 0.3s ease;
        }
        
        .search-result-card:hover {
            border-color: #007bff;
            box-shadow: 0 4px 12px rgba(0,123,255,0.15);
        }
        
        /* 검색 초기화 후 브랜치 목록 개선 (2025-01-27) */
        .branch-list-improved {
            animation: fadeInUp 0.5s ease-out;
        }
        
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        /* 브랜치 목록 그리드 레이아웃 강제 적용 (로컬/원격 분리) (2025-01-27) */
        #localBranchList,
        #remoteBranchList {
            display: flex !important;
            flex-wrap: wrap !important;
            margin: 0 -15px !important;
        }
        
        #localBranchList .col-md-6,
        #localBranchList .col-lg-4,
        #remoteBranchList .col-md-6,
        #remoteBranchList .col-lg-4 {
            padding: 0 15px !important;
            margin-bottom: 1.5rem !important;
        }
        
        /* 기본 그리드 레이아웃 */
        #branchList .col-md-6 {
            flex: 0 0 50% !important;
            max-width: 50% !important;
        }
        
        #branchList .col-lg-4 {
            flex: 0 0 33.333333% !important;
            max-width: 33.333333% !important;
        }
        
        /* 반응형 브랜치 카드 */
        @media (max-width: 768px) {
            #branchList .col-md-6,
            #branchList .col-lg-4 {
                flex: 0 0 100% !important;
                max-width: 100% !important;
            }
        }
        
        @media (min-width: 769px) and (max-width: 991px) {
            #branchList .col-lg-4 {
                flex: 0 0 50% !important;
                max-width: 50% !important;
            }
        }
        
        /* 브랜치 카드 내부 스타일 개선 */
        #branchList .branch-card {
            height: 100% !important;
            min-height: 200px !important;
        }
        
        #branchList .card-body {
            display: flex !important;
            flex-direction: column !important;
            justify-content: space-between !important;
        }
        
        /* 검색 결과 헤더 개선 */
        .search-result-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 8px 8px 0 0;
        }
        
        /* 결과 타입별 아이콘 */
        .result-type-icon {
            width: 20px;
            height: 20px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            font-size: 0.8em;
        }
        
        .commit-icon {
            background-color: #2196f3;
            color: white;
        }
        
        .source-icon {
            background-color: #4caf50;
            color: white;
        }
        
        .branch-icon {
            background-color: #ff9800;
            color: white;
        }
        
        /* 브랜치 섹션 헤더 스타일 (2025-01-27) */
        .branch-section-header {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .branch-section-header h3 {
            color: #495057;
            font-weight: 600;
        }
        
        .branch-section-header .text-muted {
            color: #6c757d !important;
        }
        
        .branch-count-badge {
            font-size: 1rem;
            padding: 0.5rem 1rem;
            border-radius: 20px;
        }
        
        /* 로컬 브랜치 섹션 */
        .local-branch-section {
            border-left: 4px solid #28a745;
        }
        
        .local-branch-section .branch-section-header {
            background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
            border-color: #28a745;
        }
        
        /* 원격 브랜치 섹션 */
        .remote-branch-section {
            border-left: 4px solid #17a2b8;
        }
        
        .remote-branch-section .branch-section-header {
            background: linear-gradient(135deg, #d1ecf1 0%, #bee5eb 100%);
            border-color: #17a2b8;
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
                        <li class="breadcrumb-item active"><%= request.getAttribute("projectName") %></li>
                    </ol>
                </nav>
                
                <h1 class="mb-4">
                    <i class="fas fa-code-branch text-primary"></i> 브랜치 선택
                </h1>
                <p class="text-muted mb-3">분석할 브랜치를 선택하세요</p>
                
                <!-- 검색 기능 -->
                <div class="card mb-4">
                    <div class="card-body">
                        <h5 class="card-title">
                            <i class="fas fa-search me-2"></i>브랜치 검색
                        </h5>
                        <p class="card-text text-muted">커밋 메시지나 소스 코드에서 특정 텍스트를 검색하여 관련 브랜치를 찾아보세요.</p>
                        <div class="row">
                            <div class="col-md-8">
                                <div class="input-group">
                                    <input type="text" class="form-control" id="searchInput" placeholder="검색할 텍스트를 입력하세요 (예: '테스트', 'bug', 'feature')">
                                    <button class="btn btn-primary" type="button" onclick="searchBranches()">
                                        <i class="fas fa-search me-1"></i>검색
                                    </button>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="searchCommits" checked>
                                    <label class="form-check-label" for="searchCommits">
                                        커밋 메시지 검색
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="searchSource" checked>
                                    <label class="form-check-label" for="searchSource">
                                        소스 코드 검색
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i> 
                    <strong>자동 동기화:</strong> 브랜치 선택 시 원격 저장소에서 최신 코드를 자동으로 pull 받습니다.
                </div>
            </div>
        </div>

        <!-- 검색 결과 영역 개선 (2025-01-27) -->
        <div id="searchResults" class="row" style="display: none;">
            <div class="col-12">
                <div class="card search-result-card">
                    <div class="card-header search-result-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">
                            <i class="fas fa-search me-2"></i>검색 결과
                            <span id="searchResultCount" class="badge bg-light text-dark ms-2">0개</span>
                        </h5>
                        <button class="btn btn-sm btn-light" onclick="clearSearch()">
                            <i class="fas fa-times me-1"></i>검색 초기화
                        </button>
                    </div>
                    <div class="card-body">
                        <div id="searchResultsContent">
                            <!-- 검색 결과가 여기에 표시됩니다 -->
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 브랜치 목록 - 로컬/원격 브랜치 분리 표시 (2025-01-27) -->
        <div id="branchList">
            <%
                List<Map<String, String>> branches = (List<Map<String, String>>) request.getAttribute("branches");
                if (branches != null && !branches.isEmpty()) {
                    // 로컬 브랜치와 원격 브랜치 분리
                    List<Map<String, String>> localBranches = new ArrayList<>();
                    List<Map<String, String>> remoteBranches = new ArrayList<>();
                    
                    for (Map<String, String> branch : branches) {
                        boolean isRemote = Boolean.parseBoolean(branch.get("isRemote"));
                        if (isRemote) {
                            remoteBranches.add(branch);
                        } else {
                            localBranches.add(branch);
                        }
                    }
            %>
            
            <!-- 로컬 브랜치 섹션 -->
            <% if (!localBranches.isEmpty()) { %>
            <div class="mb-5 local-branch-section">
                <div class="d-flex align-items-center mb-4 branch-section-header">
                    <i class="fas fa-laptop-code fa-2x text-success me-3"></i>
                    <div>
                        <h3 class="mb-1">로컬 브랜치</h3>
                        <p class="text-muted mb-0">현재 로컬 저장소에 있는 브랜치들</p>
                    </div>
                    <span class="badge bg-success branch-count-badge ms-auto"><%= localBranches.size() %>개</span>
                </div>
                <div class="row" id="localBranchList">
                    <% for (Map<String, String> branch : localBranches) { %>
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card branch-card h-100" onclick="selectBranch('<%= branch.get("name") %>')">
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-3">
                            <i class="fas fa-code-branch fa-2x text-success me-3"></i>
                            <div>
                                <h5 class="card-title mb-0"><%= branch.get("name") %></h5>
                                        <span class="badge bg-success branch-type">
                                            <i class="fas fa-laptop-code me-1"></i>로컬
                                        </span>
                                    </div>
                                </div>
                                <p class="card-text text-muted">
                                    로컬 브랜치를 선택하여 커밋 목록을 확인하세요.
                                </p>
                                <div class="d-flex justify-content-between align-items-center">
                                    <small class="text-muted">
                                        <i class="fas fa-git-alt"></i> 로컬 브랜치
                                    </small>
                                    <i class="fas fa-chevron-right text-primary"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <% } %>
                </div>
            </div>
            <% } %>
            
            <!-- 원격 브랜치 섹션 -->
            <% if (!remoteBranches.isEmpty()) { %>
            <div class="mb-5 remote-branch-section">
                <div class="d-flex align-items-center mb-4 branch-section-header">
                    <i class="fas fa-cloud fa-2x text-info me-3"></i>
                    <div>
                        <h3 class="mb-1">원격 브랜치</h3>
                        <p class="text-muted mb-0">GitHub 원격 저장소에 있는 브랜치들</p>
                    </div>
                    <span class="badge bg-info branch-count-badge ms-auto"><%= remoteBranches.size() %>개</span>
                </div>
                <div class="row" id="remoteBranchList">
                    <% for (Map<String, String> branch : remoteBranches) { %>
                    <div class="col-md-6 col-lg-4 mb-4">
                        <div class="card branch-card h-100" onclick="selectBranch('<%= branch.get("name") %>')">
                            <div class="card-body">
                                <div class="d-flex align-items-center mb-3">
                                    <i class="fas fa-code-branch fa-2x text-info me-3"></i>
                                    <div>
                                        <h5 class="card-title mb-0"><%= branch.get("name") %></h5>
                                        <span class="badge bg-info branch-type">
                                            <i class="fas fa-cloud me-1"></i>원격
                                </span>
                            </div>
                        </div>
                        <p class="card-text text-muted">
                                    원격 브랜치를 선택하여 커밋 목록을 확인하세요.
                        </p>
                        <div class="d-flex justify-content-between align-items-center">
                            <small class="text-muted">
                                        <i class="fas fa-git-alt"></i> 원격 브랜치
                            </small>
                            <i class="fas fa-chevron-right text-primary"></i>
                        </div>
                    </div>
                </div>
            </div>
                    <% } %>
                </div>
            </div>
            <% } %>
            
            <!-- 브랜치가 없는 경우 -->
            <% if (localBranches.isEmpty() && remoteBranches.isEmpty()) { %>
            <div class="col-12">
                <div class="alert alert-warning text-center">
                    <i class="fas fa-exclamation-triangle fa-2x mb-3"></i>
                    <h5>브랜치를 찾을 수 없습니다</h5>
                    <p>Git 저장소를 확인하거나 새로고침해주세요.</p>
                    <button class="btn btn-primary" onclick="location.reload()">
                        <i class="fas fa-refresh"></i> 새로고침
                    </button>
                </div>
            </div>
            <% } %>
            
            <%
                } else {
            %>
            <div class="col-12">
                <div class="alert alert-warning text-center">
                    <i class="fas fa-exclamation-triangle fa-2x mb-3"></i>
                    <h5>브랜치를 찾을 수 없습니다</h5>
                    <p>Git 저장소를 확인하거나 새로고침해주세요.</p>
                    <button class="btn btn-primary" onclick="location.reload()">
                        <i class="fas fa-refresh"></i> 새로고침
                    </button>
                </div>
            </div>
            <%
                }
            %>
        </div>
    </div>

    <script>
        // 페이지 로드 시 기존 로딩 상태 제거 및 브랜치 목록 초기화 (2025-01-27)
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
        
        // 브랜치 목록 레이아웃 초기화 (로컬/원격 분리) (2025-01-27)
        function initializeBranchLayout() {
            const branchList = document.getElementById('branchList');
            if (branchList) {
                // 로컬 브랜치 섹션 초기화
                const localBranchList = document.getElementById('localBranchList');
                if (localBranchList) {
                    initializeBranchSection(localBranchList);
                }
                
                // 원격 브랜치 섹션 초기화
                const remoteBranchList = document.getElementById('remoteBranchList');
                if (remoteBranchList) {
                    initializeBranchSection(remoteBranchList);
                }
            }
        }
        
        // 브랜치 섹션별 레이아웃 초기화
        function initializeBranchSection(section) {
            if (section) {
                // 섹션 스타일 강제 적용
                section.style.display = 'flex';
                section.style.flexWrap = 'wrap';
                section.style.margin = '0 -15px';
                
                const branchCards = section.querySelectorAll('.col-md-6, .col-lg-4');
                branchCards.forEach(card => {
                    card.style.padding = '0 15px';
                    card.style.marginBottom = '1.5rem';
                    card.style.display = 'block';
                    
                    // 화면 크기에 따른 반응형 레이아웃 적용
                    if (window.innerWidth <= 768) {
                        card.style.flex = '0 0 100%';
                        card.style.maxWidth = '100%';
                    } else if (window.innerWidth <= 991) {
                        card.style.flex = '0 0 50%';
                        card.style.maxWidth = '50%';
                    } else {
                        card.style.flex = '0 0 33.333333%';
                        card.style.maxWidth = '33.333333%';
                    }
                });
            }
        }
        
        // 즉시 로딩 상태 제거 (스크립트 로드 시)
        removeLoadingStates();
        
        // DOM 로드 시
        document.addEventListener('DOMContentLoaded', function() {
            removeLoadingStates();
            initializeBranchLayout();
        });
        
        // 페이지 표시 시 (뒤로가기 포함)
        window.addEventListener('pageshow', function(event) {
            removeLoadingStates();
            initializeBranchLayout();
        });
        
        // 윈도우 리사이즈 시 레이아웃 재조정
        window.addEventListener('resize', function() {
            initializeBranchLayout();
        });

        // 브랜치 검색 함수
        async function searchBranches() {
            const searchText = document.getElementById('searchInput').value.trim();
            if (!searchText) {
                alert('검색할 텍스트를 입력해주세요.');
                return;
            }

            const searchCommits = document.getElementById('searchCommits').checked;
            const searchSource = document.getElementById('searchSource').checked;
            
            if (!searchCommits && !searchSource) {
                alert('검색할 항목을 선택해주세요.');
                return;
            }

            try {
                // 로딩 표시
                showSearchLoading();
                
                const projectName = '<%= request.getAttribute("projectName") %>';
                const response = await fetch('/git/search-branches?projectName=' + encodeURIComponent(projectName) + '&searchText=' + encodeURIComponent(searchText));
                
                if (!response.ok) {
                    throw new Error('검색 요청 실패');
                }
                
                const results = await response.json();
                console.log('검색 결과:', results);
                console.log('결과 개수:', results.length);
                displaySearchResults(results, searchText);
                
            } catch (error) {
                console.error('검색 오류:', error);
                alert('검색 중 오류가 발생했습니다: ' + error.message);
                hideSearchLoading();
            }
        }

        // 검색 결과 표시 개선 (2025-01-27)
        function displaySearchResults(results, searchText) {
            const searchResults = document.getElementById('searchResults');
            const searchResultsContent = document.getElementById('searchResultsContent');
            const branchList = document.getElementById('branchList');
            const searchResultCount = document.getElementById('searchResultCount');
            
            // 결과 개수 업데이트
            searchResultCount.textContent = results.length + '개';
            
            if (results.length === 0) {
                searchResultsContent.innerHTML = 
                    '<div class="alert alert-info text-center">' +
                        '<i class="fas fa-info-circle fa-2x mb-3"></i>' +
                        '<h5>검색 결과가 없습니다</h5>' +
                        '<p>"' + searchText + '"에 대한 검색 결과를 찾을 수 없습니다.</p>' +
                    '</div>';
            } else {
                // 결과를 브랜치별로 그룹화
                const groupedResults = groupResultsByBranch(results);
                
                let html = '<div class="row">';
                for (const [branchName, branchResults] of Object.entries(groupedResults)) {
                    // 결과 타입별 통계
                    const commitCount = branchResults.filter(r => r.matchType === 'commit').length;
                    const sourceCount = branchResults.filter(r => r.matchType === 'source').length;
                    const branchNameCount = branchResults.filter(r => r.matchType === 'branch_name').length;
                    
                    html += '<div class="col-md-6 col-lg-4 mb-4">' +
                        '<div class="card branch-card h-100" onclick="selectBranch(\'' + branchName + '\')">' +
                            '<div class="card-body">' +
                                '<div class="d-flex align-items-center mb-3">' +
                                    '<i class="fas fa-code-branch fa-2x text-primary me-3"></i>' +
                                    '<div>' +
                                        '<h5 class="card-title mb-0">' + branchName + '</h5>' +
                                        '<div class="d-flex gap-2">' +
                                            '<span class="badge bg-primary">검색 결과</span>' +
                                            (commitCount > 0 ? '<span class="badge bg-info">커밋 ' + commitCount + '</span>' : '') +
                                            (sourceCount > 0 ? '<span class="badge bg-success">소스 ' + sourceCount + '</span>' : '') +
                                            (branchNameCount > 0 ? '<span class="badge bg-warning">브랜치명</span>' : '') +
                                        '</div>' +
                                    '</div>' +
                                '</div>' +
                                '<div class="search-results-preview">' +
                                    branchResults.slice(0, 3).map(result => {
                                        const resultClass = getResultClass(result.matchType);
                                        const iconClass = getResultIcon(result.matchType);
                                        const typeText = getResultTypeText(result.matchType);
                                        const content = getResultContent(result);
                                        
                                        return '<div class="search-result-item ' + resultClass + ' mb-2">' +
                                            '<div class="d-flex align-items-center mb-1">' +
                                                '<span class="result-type-icon ' + iconClass + ' me-2">' +
                                                    '<i class="fas fa-' + getResultIconName(result.matchType) + '"></i>' +
                                                '</span>' +
                                                '<small class="text-muted fw-bold">' + typeText + '</small>' +
                                            '</div>' +
                                            '<div class="search-content">' + content + '</div>' +
                                        '</div>';
                                    }).join('') +
                                    (branchResults.length > 3 ? '<div class="text-center mt-2"><small class="text-muted">+ ' + (branchResults.length - 3) + '개 더...</small></div>' : '') +
                                '</div>' +
                                '<div class="d-flex justify-content-between align-items-center mt-3">' +
                                    '<small class="text-muted">' +
                                        '<i class="fas fa-search"></i> 총 ' + branchResults.length + '개 결과' +
                                    '</small>' +
                                    '<i class="fas fa-chevron-right text-primary"></i>' +
                                '</div>' +
                            '</div>' +
                        '</div>' +
                    '</div>';
                }
                html += '</div>';
                searchResultsContent.innerHTML = html;
            }
            
            // 검색 결과 영역 표시, 브랜치 목록 숨기기
            searchResults.style.display = 'block';
            branchList.style.display = 'none';
            hideSearchLoading();
        }
        
        // 결과 타입별 CSS 클래스 반환
        function getResultClass(matchType) {
            switch(matchType) {
                case 'commit': return 'commit-result';
                case 'source': return 'source-result';
                case 'branch_name': return 'branch-name-result';
                default: return '';
            }
        }
        
        // 결과 타입별 아이콘 클래스 반환
        function getResultIcon(matchType) {
            switch(matchType) {
                case 'commit': return 'commit-icon';
                case 'source': return 'source-icon';
                case 'branch_name': return 'branch-icon';
                default: return '';
            }
        }
        
        // 결과 타입별 아이콘 이름 반환
        function getResultIconName(matchType) {
            switch(matchType) {
                case 'commit': return 'comment';
                case 'source': return 'code';
                case 'branch_name': return 'code-branch';
                default: return 'file';
            }
        }
        
        // 결과 타입별 텍스트 반환
        function getResultTypeText(matchType) {
            switch(matchType) {
                case 'commit': return '커밋 메시지';
                case 'source': return '소스 코드';
                case 'branch_name': return '브랜치명';
                default: return '기타';
            }
        }
        
        // 결과 내용 반환
        function getResultContent(result) {
            if (result.matchType === 'commit') {
                const message = result.commitMessage || result.matchedContent || '';
                return message.substring(0, 120) + (message.length > 120 ? '...' : '');
            } else if (result.matchType === 'source') {
                const fileName = result.fileName || '';
                const content = result.matchedContent || '';
                const lineNumber = result.lineNumber || 0;
                return '<strong>' + fileName + '</strong> (라인 ' + lineNumber + '): ' + 
                       content.substring(0, 100) + (content.length > 100 ? '...' : '');
            } else if (result.matchType === 'branch_name') {
                return '브랜치명: <strong>' + result.matchedContent + '</strong>';
            }
            return result.matchedContent || '';
        }

        // 결과를 브랜치별로 그룹화
        function groupResultsByBranch(results) {
            const grouped = {};
            results.forEach(result => {
                if (!grouped[result.branchName]) {
                    grouped[result.branchName] = [];
                }
                grouped[result.branchName].push(result);
            });
            return grouped;
        }

        // 검색 초기화 개선 (2025-01-27)
        function clearSearch() {
            // 검색 입력 필드 초기화
            document.getElementById('searchInput').value = '';
            
            // 검색 결과 영역 숨기기
            document.getElementById('searchResults').style.display = 'none';
            
            // 브랜치 목록 표시 및 레이아웃 강제 재구성
            const branchList = document.getElementById('branchList');
            branchList.style.display = 'block';
            branchList.classList.add('branch-list-improved');
            
            // 로컬/원격 브랜치 섹션별로 레이아웃 재구성
            const localBranchList = document.getElementById('localBranchList');
            const remoteBranchList = document.getElementById('remoteBranchList');
            
            if (localBranchList) {
                initializeBranchSection(localBranchList);
            }
            
            if (remoteBranchList) {
                initializeBranchSection(remoteBranchList);
            }
            
            // 결과 개수 초기화
            const searchResultCount = document.getElementById('searchResultCount');
            if (searchResultCount) {
                searchResultCount.textContent = '0개';
            }
            
            // 검색 옵션 초기화
            document.getElementById('searchCommits').checked = true;
            document.getElementById('searchSource').checked = true;
            
            // 레이아웃 재계산을 위한 강제 리플로우
            branchList.offsetHeight;
            
            // 애니메이션 클래스 제거 (다음 검색을 위해)
            setTimeout(() => {
                branchList.classList.remove('branch-list-improved');
            }, 500);
        }

        // 검색 로딩 표시
        function showSearchLoading() {
            const searchResultsContent = document.getElementById('searchResultsContent');
            searchResultsContent.innerHTML = `
                <div class="text-center">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">검색 중...</span>
                    </div>
                    <p class="mt-2">검색 중입니다...</p>
                </div>
            `;
            document.getElementById('searchResults').style.display = 'block';
            document.getElementById('branchList').style.display = 'none';
        }

        // 검색 로딩 숨기기
        function hideSearchLoading() {
            // 로딩 상태는 displaySearchResults에서 처리됨
        }

        // Enter 키로 검색
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBranches();
            }
        });
        
        // 페이지 숨김 시
        window.addEventListener('pagehide', function(event) {
            removeLoadingStates();
        });
        
        // 페이지 포커스 시
        window.addEventListener('focus', removeLoadingStates);
        
        function selectBranch(branchName) {
            const projectName = '<%= request.getAttribute("projectName") %>';
            
            // 기존 로딩 상태 제거
            const existingLoading = document.getElementById('branch-loading');
            if (existingLoading) {
                existingLoading.remove();
            }
            
            // 로딩 상태 표시
            const loadingDiv = document.createElement('div');
            loadingDiv.id = 'branch-loading';
            loadingDiv.className = 'position-fixed top-50 start-50 translate-middle text-center';
            loadingDiv.style.zIndex = '9999';
            loadingDiv.innerHTML = `
                <div class="bg-white p-4 rounded shadow-lg">
                    <div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <h5 class="text-primary">커밋 목록을 불러오는 중...</h5>
                    <p class="text-muted mb-0">브랜치를 체크아웃하고 최신 코드를 가져오고 있습니다.</p>
                </div>
            `;
            document.body.appendChild(loadingDiv);
            
            // 프로그래스바가 표시되도록 약간의 지연 후 페이지 이동
            setTimeout(() => {
                window.location.href = '/git/commits?projectName=' + encodeURIComponent(projectName) + 
                                     '&branchName=' + encodeURIComponent(branchName);
            }, 100);
        }
    </script>
</body>
</html>
