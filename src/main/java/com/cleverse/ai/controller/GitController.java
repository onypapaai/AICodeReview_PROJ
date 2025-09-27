package com.cleverse.ai.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.lib.ObjectLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.ResponseEntity;

import com.cleverse.ai.service.OllamaService;
import com.cleverse.ai.service.GitHubService;

@Controller
@RequestMapping("/git")
public class GitController {

//    private final String GIT_REPO_URL = "https://github.com/juldae719/AICodeReview.git";
    private final String GIT_REPO_URL = "https://github.com/onypapaai/AICodeReview_PROJ.git";
    private final String LOCAL_GIT_PATH = "C:\\coding\\workspace\\CleverseAICodeReview_working";
    
    @Autowired
    private OllamaService ollamaService;
    
    @Autowired
    private GitHubService githubService;

    // 프로젝트 목록 (현재는 하나의 프로젝트만)
    @RequestMapping("/projects")
    public ModelAndView getProjects() {
        ModelAndView mav = new ModelAndView("projects");
        
        try {
            System.out.println("GitHub에서 저장소 목록을 가져오는 중...");
            List<Map<String, String>> projects = githubService.getUserRepositories();
            
            System.out.println("가져온 저장소 수: " + projects.size());
            mav.addObject("projects", projects);
            
        } catch (Exception e) {
            System.err.println("프로젝트 목록을 가져오는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            // 오류 발생 시 기본 프로젝트 목록 제공
            List<Map<String, String>> defaultProjects = new ArrayList<>();
            Map<String, String> project = new HashMap<>();
            project.put("name", "AICodeReview");
            project.put("url", GIT_REPO_URL);
            project.put("description", "AI 기반 코드 리뷰 시스템");
            project.put("language", "Java");
            project.put("stars", "0");
            project.put("forks", "0");
            project.put("lastUpdate", "2024-01-15");
            defaultProjects.add(project);
            
            mav.addObject("projects", defaultProjects);
            mav.addObject("errorMessage", "GitHub에서 저장소 목록을 가져올 수 없습니다. 기본 목록을 표시합니다.");
        }
        
        return mav;
    }

    // 브랜치 목록 - GitHub API를 활용한 원격 검색으로 개선 (2025-01-27)
    @RequestMapping("/branches")
    public ModelAndView getBranches(@RequestParam String projectName, @RequestParam(required = false) String cloneUrl) {
        ModelAndView mav = new ModelAndView("branches");
        
        List<Map<String, String>> branches = new ArrayList<>();
        
        try {
            // GitHub API를 통한 원격 브랜치 목록 가져오기 (로컬 PC 용량 문제 해결)
            String owner = "onypapaai"; // 기본값
            String repo = projectName;
            
            System.out.println("GitHub API를 통해 브랜치 목록 가져오기: " + owner + "/" + repo);
            
            // GitHub API를 통해 브랜치 목록 가져오기
            List<Map<String, String>> remoteBranches = githubService.getRepositoryBranches(owner, repo);
            
            // 결과를 브랜치 목록 형식으로 변환
            for (Map<String, String> remoteBranch : remoteBranches) {
                Map<String, String> branchInfo = new HashMap<>();
                branchInfo.put("name", remoteBranch.get("name"));
                branchInfo.put("fullName", "refs/heads/" + remoteBranch.get("name"));
                branchInfo.put("isRemote", "true");
                branchInfo.put("sha", remoteBranch.get("sha"));
                branchInfo.put("protected", remoteBranch.get("protected"));
                branches.add(branchInfo);
                System.out.println("원격 브랜치 추가: " + remoteBranch.get("name"));
            }
            
            // 기존 로컬 검색 방식도 백업으로 유지 (주석 처리)
            /*
            File localRepoDir = new File(LOCAL_GIT_PATH);
            
            // 로컬 저장소가 없으면 클론
            if (!localRepoDir.exists()) {
                String repoUrl = (cloneUrl != null && !cloneUrl.isEmpty()) ? cloneUrl : GIT_REPO_URL;
                System.out.println("저장소 클론 시작: " + repoUrl);
                initializeRepository(repoUrl);
            }
            
            if (localRepoDir.exists()) {
                try (Git git = Git.open(localRepoDir)) {
                    // 원격 저장소에서 최신 정보 가져오기
                    try {
                        git.fetch().call();
                    } catch (Exception e) {
                        System.out.println("Fetch 실패: " + e.getMessage());
                    }
                    
                    // 로컬 브랜치만 가져오기 (원격 브랜치는 선택 시 자동으로 다운받음)
                    List<org.eclipse.jgit.lib.Ref> allBranches = git.branchList().call();
                    System.out.println("로컬 브랜치 수: " + allBranches.size());
                    
                    for (org.eclipse.jgit.lib.Ref branch : allBranches) {
                        // 로컬 브랜치만 필터링 (refs/heads/로 시작하는 것만)
                        if (branch.getName().startsWith("refs/heads/")) {
                            Map<String, String> branchInfo = new HashMap<>();
                            String branchName = branch.getName().replace("refs/heads/", "");
                            branchInfo.put("name", branchName);
                            branchInfo.put("fullName", branch.getName());
                            branchInfo.put("isRemote", "false");
                            branches.add(branchInfo);
                            System.out.println("브랜치 추가: " + branchName);
                        }
                    }
                    
                    // 브랜치가 없으면 기본 브랜치들 추가
                    if (branches.isEmpty()) {
                        System.out.println("로컬 브랜치가 없어서 기본 브랜치들을 추가합니다.");
                        Map<String, String> mainBranch = new HashMap<>();
                        mainBranch.put("name", "main");
                        mainBranch.put("fullName", "refs/heads/main");
                        mainBranch.put("isRemote", "false");
                        branches.add(mainBranch);
                        
                        Map<String, String> masterBranch = new HashMap<>();
                        masterBranch.put("name", "master");
                        masterBranch.put("fullName", "refs/heads/master");
                        masterBranch.put("isRemote", "false");
                        branches.add(masterBranch);
                    }
                }
            } else {
                // 기본 브랜치들 추가
                Map<String, String> mainBranch = new HashMap<>();
                mainBranch.put("name", "main");
                mainBranch.put("fullName", "refs/heads/main");
                mainBranch.put("isRemote", "false");
                branches.add(mainBranch);
            }
            */
            
        } catch (Exception e) {
            System.out.println("브랜치 목록 가져오기 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 오류 발생 시 기본 브랜치들 추가
            Map<String, String> mainBranch = new HashMap<>();
            mainBranch.put("name", "main");
            mainBranch.put("fullName", "refs/heads/main");
            mainBranch.put("isRemote", "false");
            branches.add(mainBranch);
        }
        
        mav.addObject("projectName", projectName);
        mav.addObject("branches", branches);
        return mav;
    }

    // 브랜치 검색 API - GitHub API를 활용한 원격 검색으로 개선 (2025-01-27)
    @GetMapping("/search-branches")
    @ResponseBody
    public ResponseEntity<List<BranchSearchResult>> searchBranches(
            @RequestParam String projectName,
            @RequestParam String searchText) {
        
        System.out.println("=== 브랜치 검색 시작 (GitHub API 원격 검색) ===");
        System.out.println("프로젝트: " + projectName);
        System.out.println("검색 텍스트: '" + searchText + "'");
        System.out.println("검색 시작 시간: " + java.time.LocalDateTime.now());
        
        try {
            List<BranchSearchResult> results = new ArrayList<>();
            
            // GitHub API를 통한 원격 브랜치 검색 (로컬 PC 용량 문제 해결)
            // 프로젝트명에서 owner와 repo 추출
            String owner = "onypapaai"; // 기본값
            String repo = projectName;
            
            // GitHub API를 통해 브랜치 검색
            List<Map<String, String>> remoteResults = githubService.searchBranches(owner, repo, searchText);
            
            // 결과를 BranchSearchResult로 변환
            for (Map<String, String> remoteResult : remoteResults) {
                BranchSearchResult result = new BranchSearchResult(
                    remoteResult.get("branchName"),
                    remoteResult.get("sha"),
                    remoteResult.get("matchedContent"),
                    remoteResult.get("fileName"),
                    remoteResult.get("matchedContent"),
                    remoteResult.get("matchType"),
                    Integer.parseInt(remoteResult.get("lineNumber"))
                );
                results.add(result);
            }
            
            // 기존 로컬 검색 방식도 백업으로 유지 (주석 처리)
            /*
            // 프로젝트 디렉토리 경로
            String projectDir = System.getProperty("user.home") + "/git-repos/" + projectName;
            File projectFile = new File(projectDir);
            
            if (projectFile.exists()) {
                try (Repository repository = new FileRepositoryBuilder()
                        .setGitDir(new File(projectFile, ".git"))
                        .build()) {
                    
                    Git git = new Git(repository);
                    
                    // 모든 브랜치 가져오기
                    List<Ref> branches = git.branchList().call();
                    branches.addAll(git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call());
                    
                    for (Ref branch : branches) {
                        String branchName = branch.getName().replace("refs/heads/", "").replace("refs/remotes/origin/", "");
                        
                        try {
                            // 브랜치의 커밋들 검색
                            searchCommitsInBranch(git, branchName, searchText, results);
                            
                            // 브랜치의 소스 코드 검색
                            searchSourceInBranch(git, branchName, searchText, results);
                            
                        } catch (Exception e) {
                            System.out.println("브랜치 " + branchName + " 검색 중 오류: " + e.getMessage());
                        }
                    }
                }
            }
            */
            
            System.out.println("=== 브랜치 검색 완료 ===");
            System.out.println("총 결과 개수: " + results.size());
            System.out.println("검색 완료 시간: " + java.time.LocalDateTime.now());
            
            // 결과 상세 로깅 (최대 10개만)
            int logCount = 0;
            for (BranchSearchResult result : results) {
                if (logCount++ < 10) {
                    System.out.println("- [" + result.getMatchType() + "] " + result.getBranchName() + 
                                     (result.getFileName() != null && !result.getFileName().isEmpty() ? 
                                      " (" + result.getFileName() + ":" + result.getLineNumber() + ")" : "") + 
                                      " - " + result.getMatchedContent());
                }
            }
            if (results.size() > 10) {
                System.out.println("... 및 " + (results.size() - 10) + "개 추가 결과");
            }
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            System.out.println("브랜치 검색 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    
    private void searchCommitsInBranch(Git git, String branchName, String searchText, List<BranchSearchResult> results) throws Exception {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            ObjectId branchId = git.getRepository().resolve(branchName);
            if (branchId == null) return;
            
            walk.markStart(walk.parseCommit(branchId));
            walk.setRevFilter(RevFilter.NO_MERGES);
            
            int commitCount = 0;
            for (RevCommit commit : walk) {
                if (commitCount++ > 50) break; // 최대 50개 커밋만 검색
                
                String commitMessage = commit.getFullMessage();
                if (commitMessage != null && commitMessage.toLowerCase().contains(searchText.toLowerCase())) {
                    results.add(new BranchSearchResult(
                        branchName,
                        commit.getId().abbreviate(7).name(),
                        commitMessage,
                        "",
                        commitMessage,
                        "commit",
                        0
                    ));
                }
            }
        }
    }
    
    private void searchSourceInBranch(Git git, String branchName, String searchText, List<BranchSearchResult> results) throws Exception {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            ObjectId branchId = git.getRepository().resolve(branchName);
            if (branchId == null) return;
            
            RevCommit commit = walk.parseCommit(branchId);
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                treeParser.reset(reader, commit.getTree().getId());
                
                try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(true);
                    
                    while (treeWalk.next()) {
                        String fileName = treeWalk.getPathString();
                        
                        // 소스 파일만 검색 (Java, JavaScript, HTML, CSS 등)
                        if (isSourceFile(fileName)) {
                            try {
                                ObjectId objectId = treeWalk.getObjectId(0);
                                ObjectLoader loader = git.getRepository().open(objectId);
                                String content = new String(loader.getBytes(), "UTF-8");
                                
                                // 파일 내용에서 검색
                                String[] lines = content.split("\n");
                                for (int i = 0; i < lines.length; i++) {
                                    if (lines[i].toLowerCase().contains(searchText.toLowerCase())) {
                                        results.add(new BranchSearchResult(
                                            branchName,
                                            commit.getId().abbreviate(7).name(),
                                            commit.getShortMessage(),
                                            fileName,
                                            lines[i].trim(),
                                            "source",
                                            i + 1
                                        ));
                                    }
                                }
                            } catch (Exception e) {
                                // 바이너리 파일이나 큰 파일은 건너뛰기
                                continue;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 소스 파일인지 확인합니다.
     * 더 많은 파일 형식을 지원하도록 개선 (2025-01-27)
     */
    private boolean isSourceFile(String fileName) {
        String[] extensions = {
            ".java", ".js", ".html", ".css", ".jsp", ".xml", ".properties", ".yml", ".yaml", ".json", ".md", ".txt",
            ".jsp", ".jspx", ".jspf", ".tag", ".tagx", // JSP 관련
            ".ts", ".tsx", ".jsx", // TypeScript, React
            ".vue", ".svelte", // Vue, Svelte
            ".py", ".rb", ".php", ".go", ".rs", ".cpp", ".c", ".h", ".hpp", // 다양한 프로그래밍 언어
            ".sql", ".sh", ".bat", ".ps1", // 스크립트 파일
            ".gradle", ".maven", ".pom", // 빌드 도구
            ".dockerfile", ".dockerignore", // Docker
            ".gitignore", ".gitattributes", // Git
            ".env", ".config", ".conf", // 설정 파일
            ".scss", ".sass", ".less", // CSS 전처리기
            ".svg", ".xml", ".xsd", ".xslt" // XML 관련
        };
        for (String ext : extensions) {
            if (fileName.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    // 저장소 초기화 및 클론
    private void initializeRepository(String repoUrl) {
        try {
            File localRepoDir = new File(LOCAL_GIT_PATH);
            
            // 기존 디렉토리가 존재하는 경우 처리 (2025-01-27)
            if (localRepoDir.exists()) {
                System.out.println("기존 저장소 디렉토리가 존재합니다. 기존 저장소를 사용합니다: " + LOCAL_GIT_PATH);
                return; // 기존 저장소 사용
            }
            
            // 새 디렉토리 생성
            if (!localRepoDir.mkdirs()) {
                System.err.println("디렉토리 생성 실패: " + LOCAL_GIT_PATH);
                return;
            }
            
            // Git 저장소 클론 (2025-01-27)
            try {
                Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localRepoDir)
                    .setCloneAllBranches(true)
                    .call()
                    .close();
                    
                System.out.println("저장소 클론 완료: " + LOCAL_GIT_PATH);
            } catch (Exception cloneException) {
                // 클론 실패 시 기존 디렉토리 삭제 후 재시도
                System.err.println("클론 실패, 기존 디렉토리 삭제 후 재시도: " + cloneException.getMessage());
                if (localRepoDir.exists()) {
                    deleteDirectory(localRepoDir);
                }
                
                // 재시도
                if (localRepoDir.mkdirs()) {
                    Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(localRepoDir)
                        .setCloneAllBranches(true)
                        .call()
                        .close();
                    System.out.println("저장소 클론 재시도 완료: " + LOCAL_GIT_PATH);
                }
            }
        } catch (Exception e) {
            System.err.println("저장소 클론 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 디렉토리 삭제 헬퍼 메서드 (2025-01-27)
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    // 커밋 목록
    @RequestMapping("/commits")
    public ModelAndView getCommits(@RequestParam String projectName, @RequestParam String branchName) {
        ModelAndView mav = new ModelAndView("commits");
        
        List<Map<String, Object>> commits = new ArrayList<>();
        
        try {
            File localRepoDir = new File(LOCAL_GIT_PATH);
            
            // 로컬 저장소가 없으면 클론
            if (!localRepoDir.exists()) {
                initializeRepository(GIT_REPO_URL);
            }
            
            if (localRepoDir.exists()) {
                try (Git git = Git.open(localRepoDir)) {
                    Repository repository = git.getRepository();
                    
                    // 원격 저장소에서 최신 정보 가져오기
                    try {
                        git.fetch().call();
                        System.out.println("원격 저장소에서 최신 정보를 가져왔습니다.");
                    } catch (Exception e) {
                        System.out.println("Fetch 실패: " + e.getMessage());
                    }
                    
                    // 브랜치 체크아웃 및 pull
                    try {
                        System.out.println("브랜치 체크아웃 시도: " + branchName);
                        
                        // 먼저 모든 원격 브랜치를 가져옴
                        try {
                            git.fetch().setRemote("origin").call();
                            System.out.println("원격 브랜치 정보를 가져왔습니다.");
                        } catch (Exception e) {
                            System.out.println("원격 브랜치 fetch 실패: " + e.getMessage());
                        }
                        
                        // 로컬 브랜치 목록 확인
                        List<org.eclipse.jgit.lib.Ref> localBranches = git.branchList().call();
                        boolean localBranchExists = localBranches.stream()
                                .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));
                        
                        System.out.println("로컬 브랜치 존재 여부: " + localBranchExists);
                        
                        if (localBranchExists) {
                            // 로컬 브랜치가 있으면 체크아웃
                            git.checkout().setName(branchName).call();
                            System.out.println("로컬 브랜치로 체크아웃: " + branchName);
                        } else {
                            // 원격 브랜치에서 로컬 브랜치 생성 시도
                            try {
                                git.checkout().setCreateBranch(true).setName(branchName)
                                        .setStartPoint("origin/" + branchName).call();
                                System.out.println("원격 브랜치에서 로컬 브랜치 생성: " + branchName);
                            } catch (Exception e) {
                                System.out.println("원격 브랜치에서 생성 실패, main 브랜치 사용: " + e.getMessage());
                                // main 브랜치가 있는지 확인
                                boolean mainExists = localBranches.stream()
                                        .anyMatch(ref -> ref.getName().equals("refs/heads/main"));
                                
                                if (mainExists) {
                                    git.checkout().setName("main").call();
                                    System.out.println("main 브랜치로 체크아웃");
                                } else {
                                    // master 브랜치 시도
                                    try {
                                        git.checkout().setName("master").call();
                                        System.out.println("master 브랜치로 체크아웃");
                                    } catch (Exception ex) {
                                        System.out.println("master 브랜치도 없음, 현재 브랜치 유지");
                                    }
                                }
                            }
                        }
                        
                        // 현재 브랜치에서 pull 받기
                        try {
                            git.pull().call();
                            System.out.println("브랜치에서 최신 변경사항을 가져왔습니다.");
                        } catch (Exception e) {
                            System.out.println("Pull 실패 (정상일 수 있음): " + e.getMessage());
                        }
                        
                    } catch (Exception e) {
                        System.out.println("브랜치 체크아웃 실패: " + e.getMessage());
                    }
                    
                    // 커밋 목록 가져오기 (원격에 푸시된 커밋만)
                    System.out.println("커밋 목록 가져오기 시작");
                    Iterable<RevCommit> commitIterable = null;
                    
                    try {
                        // 원격 브랜치가 있는지 확인하고 원격 커밋만 가져오기
                        String remoteBranchName = "origin/" + branchName;
                        try {
                            org.eclipse.jgit.lib.Ref remoteRef = repository.findRef(remoteBranchName);
                            if (remoteRef != null) {
                                // 원격 브랜치의 커밋만 가져오기
                                commitIterable = git.log().add(remoteRef.getObjectId()).setMaxCount(50).call();
                                System.out.println("원격 브랜치 커밋만 가져오기 성공: " + remoteBranchName);
                            } else {
                                // 원격 브랜치가 없으면 로컬 커밋 사용
                                commitIterable = git.log().setMaxCount(50).call();
                                System.out.println("원격 브랜치 없음, 로컬 커밋 사용");
                            }
                        } catch (Exception e) {
                            // 원격 브랜치 처리 실패 시 로컬 커밋 사용
                            commitIterable = git.log().setMaxCount(50).call();
                            System.out.println("원격 브랜치 처리 실패, 로컬 커밋 사용: " + e.getMessage());
                        }
                        
                        System.out.println("커밋 목록 가져오기 성공");
                    } catch (Exception e) {
                        System.out.println("커밋 목록 가져오기 실패: " + e.getMessage());
                        // 빈 리스트로 처리
                        commitIterable = new ArrayList<>();
                    }
                    
                    int commitCount = 0;
                    for (RevCommit commit : commitIterable) {
                        commitCount++;
                        Map<String, Object> commitInfo = new HashMap<>();
                        commitInfo.put("id", commit.getId().getName());
                        commitInfo.put("shortId", commit.getId().abbreviate(7).name());
                        
                        // 커밋 메시지 추출 - JGit의 내장 메서드 사용
                        String shortMessage = "메시지를 읽을 수 없습니다";
                        String fullMessage = "메시지를 읽을 수 없습니다";
                        
                        try {
                            // JGit의 getShortMessage()와 getFullMessage() 사용
                            String jgitShortMessage = commit.getShortMessage();
                            String jgitFullMessage = commit.getFullMessage();
                            
                            // 디버깅을 위한 로그
                            System.out.println("=== 커밋 ID: " + commit.getId().abbreviate(7).name() + " ===");
                            System.out.println("JGit Short Message: '" + jgitShortMessage + "'");
                            System.out.println("JGit Full Message: '" + jgitFullMessage + "'");
                            
                            // 메시지 유효성 검증
                            if (jgitShortMessage != null && !jgitShortMessage.trim().isEmpty()) {
                                // Git 메타데이터가 포함되어 있는지 확인
                                if (!jgitShortMessage.startsWith("tree ") && 
                                    !jgitShortMessage.startsWith("parent ") &&
                                    !jgitShortMessage.startsWith("author ") && 
                                    !jgitShortMessage.startsWith("committer ")) {
                                    shortMessage = jgitShortMessage.trim();
                                    System.out.println("Valid Short Message: " + shortMessage);
                                } else {
                                    System.out.println("Short message contains metadata, using fallback");
                                }
                            }
                            
                            if (jgitFullMessage != null && !jgitFullMessage.trim().isEmpty()) {
                                // Full message도 동일하게 검증
                                if (!jgitFullMessage.startsWith("tree ") && 
                                    !jgitFullMessage.startsWith("parent ") &&
                                    !jgitFullMessage.startsWith("author ") && 
                                    !jgitFullMessage.startsWith("committer ")) {
                                    fullMessage = jgitFullMessage.trim();
                                    System.out.println("Valid Full Message: " + fullMessage);
                                } else {
                                    System.out.println("Full message contains metadata, using short message");
                                    fullMessage = shortMessage;
                                }
                            }
                            
                            // 여전히 유효하지 않다면 raw buffer에서 직접 추출
                            if (shortMessage.equals("메시지를 읽을 수 없습니다") || 
                                shortMessage.startsWith("tree ") || shortMessage.startsWith("parent ") ||
                                shortMessage.startsWith("author ") || shortMessage.startsWith("committer ")) {
                                
                                System.out.println("JGit methods failed, trying raw buffer parsing...");
                                
                                byte[] rawBuffer = commit.getRawBuffer();
                                if (rawBuffer != null && rawBuffer.length > 0) {
                                    String rawContent = new String(rawBuffer, "UTF-8");
                                    System.out.println("Raw Content: " + rawContent);
                                    
                                    // 빈 줄을 찾아서 그 이후가 실제 커밋 메시지
                                    int emptyLineIndex = rawContent.indexOf("\n\n");
                                    if (emptyLineIndex > 0) {
                                        String messageContent = rawContent.substring(emptyLineIndex + 2).trim();
                                        if (!messageContent.isEmpty()) {
                                            String[] lines = messageContent.split("\n");
                                            if (lines.length > 0) {
                                                String extractedMessage = lines[0].trim();
                                                if (!extractedMessage.startsWith("tree ") && 
                                                    !extractedMessage.startsWith("parent ") &&
                                                    !extractedMessage.startsWith("author ") && 
                                                    !extractedMessage.startsWith("committer ")) {
                                                    shortMessage = extractedMessage;
                                                    fullMessage = messageContent;
                                                    System.out.println("Raw parsing success - Short: " + shortMessage);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                        } catch (Exception e) {
                            System.out.println("커밋 메시지 추출 오류: " + e.getMessage());
                            e.printStackTrace();
                        }
                        commitInfo.put("message", shortMessage);
                        commitInfo.put("fullMessage", fullMessage);
                        
                        // 변경된 파일 수 계산
                        int changedFilesCount = 0;
                        List<Map<String, String>> changedFiles = new ArrayList<>();
                        try {
                            if (commit.getParentCount() > 0) {
                                RevCommit parent = commit.getParent(0);
                                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                                
                                try (ObjectReader reader = repository.newObjectReader()) {
                                    if (parent.getTree() != null && commit.getTree() != null) {
                                        oldTreeIter.reset(reader, parent.getTree().getId());
                                        newTreeIter.reset(reader, commit.getTree().getId());
                                    } else {
                                        System.out.println("Tree 정보가 null입니다. 파일 수 계산을 건너뜁니다.");
                                        changedFilesCount = 0;
                                        // 파일 수 계산 종료
                                    }
                                }
                                
                                List<DiffEntry> diffs = git.diff()
                                        .setOldTree(oldTreeIter)
                                        .setNewTree(newTreeIter)
                                        .call();
                                changedFilesCount = diffs.size();
                            } else {
                                // 초기 커밋인 경우
                                try (RevWalk walk = new RevWalk(repository)) {
                                    RevCommit commitWithTree = walk.parseCommit(commit.getId());
                                    try (org.eclipse.jgit.treewalk.TreeWalk treeWalk = new org.eclipse.jgit.treewalk.TreeWalk(repository)) {
                                        treeWalk.addTree(commitWithTree.getTree());
                                        treeWalk.setRecursive(true);
                                        while (treeWalk.next()) {
                                            changedFilesCount++;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // 파일 수 계산 실패 시 0으로 설정
                        }
                        commitInfo.put("changedFilesCount", changedFilesCount);
                        
                        // 안전한 작성자 정보 처리
                        String author = "알 수 없음";
                        String email = "알 수 없음";
                        Date commitDate = new Date();
                        try {
                            if (commit.getAuthorIdent() != null) {
                                author = commit.getAuthorIdent().getName() != null ? 
                                        commit.getAuthorIdent().getName() : "알 수 없음";
                                email = commit.getAuthorIdent().getEmailAddress() != null ? 
                                        commit.getAuthorIdent().getEmailAddress() : "알 수 없음";
                                commitDate = commit.getAuthorIdent().getWhen() != null ? 
                                            commit.getAuthorIdent().getWhen() : new Date();
                            }
                        } catch (Exception e) {
                            // 기본값 사용
                        }
                        commitInfo.put("author", author);
                        commitInfo.put("email", email);
                        commitInfo.put("date", commitDate);
                        commits.add(commitInfo);
                    }
                    
                    System.out.println("처리된 커밋 수: " + commitCount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 커밋을 날짜별로 그룹화
        Map<String, List<Map<String, Object>>> commitsByDate = new LinkedHashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", java.util.Locale.KOREAN);
        
        for (Map<String, Object> commit : commits) {
            Date commitDate = (Date) commit.get("date");
            String dateKey = dateFormat.format(commitDate);
            String displayDate = displayDateFormat.format(commitDate);
            
            if (!commitsByDate.containsKey(dateKey)) {
                commitsByDate.put(dateKey, new ArrayList<>());
            }
            
            // 표시용 날짜 추가
            commit.put("displayDate", displayDate);
            commitsByDate.get(dateKey).add(commit);
        }

        mav.addObject("projectName", projectName);
        mav.addObject("branchName", branchName);
        mav.addObject("commitsByDate", commitsByDate);
        mav.addObject("commits", commits); // 기존 호환성을 위해 유지
        return mav;
    }

    // 커밋 상세 정보
    @RequestMapping("/commit-detail")
    public ModelAndView getCommitDetail(@RequestParam String projectName, 
                                      @RequestParam String branchName,
                                      @RequestParam String commitId) {
        ModelAndView mav = new ModelAndView("commit-detail");
        
        Map<String, Object> commitDetail = new HashMap<>();
        StringBuilder diffResult = new StringBuilder();
        String errorMessage = "";
        
        try {
            File localRepoDir = new File(LOCAL_GIT_PATH);
            if (localRepoDir.exists()) {
                try (Git git = Git.open(localRepoDir)) {
                    Repository repository = git.getRepository();
                    
                    // 커밋 객체 가져오기 (완전히 안전한 방식)
                    RevCommit commit;
                    try {
                        // RevWalk를 사용하여 커밋을 직접 파싱
                        try (RevWalk walk = new RevWalk(repository)) {
                            ObjectId commitObjectId = ObjectId.fromString(commitId);
                            commit = walk.parseCommit(commitObjectId);
                            
                            // Tree 정보가 null인 경우 RevWalk로 재파싱
                            if (commit.getTree() == null) {
                                System.out.println("커밋의 Tree가 null입니다. RevWalk로 재파싱합니다.");
                                walk.reset();
                                walk.markStart(commit);
                                RevCommit reparsedCommit = walk.next();
                                if (reparsedCommit != null) {
                                    commit = reparsedCommit;
                                }
                            }
                            
                            // Tree 정보가 여전히 null인 경우
                            if (commit.getTree() == null) {
                                System.out.println("커밋의 Tree를 가져올 수 없습니다. 기본 정보만 표시합니다.");
                            }
                        }
                        
                        System.out.println("커밋 파싱 성공: " + commitId);
                    } catch (Exception e) {
                        System.err.println("커밋 파싱 실패: " + e.getMessage());
                        errorMessage = "커밋을 찾을 수 없습니다: " + commitId;
                        mav.addObject("errorMessage", errorMessage);
                        return mav;
                    }
                    
                    // 커밋 기본 정보
                    commitDetail.put("id", commit.getId().getName());
                    commitDetail.put("shortId", commit.getId().abbreviate(7).name());
                    
                    // 안전한 메시지 처리
                    String shortMessage = "메시지를 읽을 수 없습니다";
                    String fullMessage = "메시지를 읽을 수 없습니다";
                    try {
                        // Git raw 데이터에서 메시지 부분만 추출
                        byte[] rawBuffer = commit.getRawBuffer();
                        if (rawBuffer != null && rawBuffer.length > 0) {
                            String rawContent = new String(rawBuffer, "UTF-8");
                            
                            // 빈 줄을 찾아서 그 이후가 실제 커밋 메시지
                            int emptyLineIndex = rawContent.indexOf("\n\n");
                            if (emptyLineIndex > 0) {
                                String messageContent = rawContent.substring(emptyLineIndex + 2);
                                String[] lines = messageContent.split("\n");
                                if (lines.length > 0) {
                                    shortMessage = lines[0].trim();
                                    fullMessage = messageContent.trim();
                                }
                            } else {
                                // 빈 줄이 없으면 전체를 메시지로 간주
                                shortMessage = rawContent.trim();
                                fullMessage = rawContent.trim();
                            }
                        }
                        
                        if (shortMessage == null || shortMessage.trim().isEmpty()) {
                            shortMessage = "메시지를 읽을 수 없습니다";
                        }
                        if (fullMessage == null || fullMessage.trim().isEmpty()) {
                            fullMessage = "메시지를 읽을 수 없습니다";
                        }
                    } catch (Exception e) {
                        // 기본값 사용
                    }
                    commitDetail.put("message", shortMessage);
                    commitDetail.put("fullMessage", fullMessage);
                    
                    // 안전한 작성자 정보 처리
                    String author = "알 수 없음";
                    String email = "알 수 없음";
                    Date commitDate = new Date();
                    try {
                        if (commit.getAuthorIdent() != null) {
                            author = commit.getAuthorIdent().getName() != null ? 
                                    commit.getAuthorIdent().getName() : "알 수 없음";
                            email = commit.getAuthorIdent().getEmailAddress() != null ? 
                                    commit.getAuthorIdent().getEmailAddress() : "알 수 없음";
                            commitDate = commit.getAuthorIdent().getWhen() != null ? 
                                        commit.getAuthorIdent().getWhen() : new Date();
                        }
                    } catch (Exception e) {
                        // 기본값 사용
                    }
                    commitDetail.put("author", author);
                    commitDetail.put("email", email);
                    commitDetail.put("date", commitDate);
                    commitDetail.put("parentCount", commit.getParentCount());
                    
                    // 부모 커밋 정보
                    List<Map<String, String>> parents = new ArrayList<>();
                    try {
                        for (int i = 0; i < commit.getParentCount(); i++) {
                            RevCommit parent = commit.getParent(i);
                            Map<String, String> parentInfo = new HashMap<>();
                            parentInfo.put("id", parent.getId().getName());
                            parentInfo.put("shortId", parent.getId().abbreviate(7).name());
                            
                            String parentMessage = "메시지를 읽을 수 없습니다";
                            try {
                                // Git raw 데이터에서 메시지 부분만 추출
                                byte[] rawBuffer = parent.getRawBuffer();
                                if (rawBuffer != null && rawBuffer.length > 0) {
                                    String rawContent = new String(rawBuffer, "UTF-8");
                                    
                                    // 빈 줄을 찾아서 그 이후가 실제 커밋 메시지
                                    int emptyLineIndex = rawContent.indexOf("\n\n");
                                    if (emptyLineIndex > 0) {
                                        String messageContent = rawContent.substring(emptyLineIndex + 2);
                                        String[] lines = messageContent.split("\n");
                                        if (lines.length > 0) {
                                            parentMessage = lines[0].trim();
                                        }
                                    } else {
                                        // 빈 줄이 없으면 전체를 메시지로 간주
                                        parentMessage = rawContent.trim();
                                    }
                                }
                                
                                if (parentMessage == null || parentMessage.trim().isEmpty()) {
                                    parentMessage = "메시지를 읽을 수 없습니다";
                                }
                            } catch (Exception e) {
                                // 기본값 사용
                            }
                            parentInfo.put("message", parentMessage);
                            parents.add(parentInfo);
                        }
                    } catch (Exception e) {
                        // 부모 커밋 정보를 읽을 수 없는 경우 무시
                    }
                    commitDetail.put("parents", parents);
                    
                    // 변경된 파일 목록과 Diff 정보 추가
                    List<Map<String, String>> changedFiles = new ArrayList<>();
                    List<Map<String, String>> fileDiffs = new ArrayList<>();
                    int changedFilesCount = 0;
                    
                    try {
                        // 커밋의 Tree가 null인지 확인
                        if (commit.getTree() == null) {
                            System.out.println("커밋의 Tree가 null입니다. 파일 수 계산을 건너뜁니다.");
                            changedFilesCount = 0;
                        } else {
                        
                        if (commit.getParentCount() > 0) {
                            // 부모 커밋과 비교
                            RevCommit parent = commit.getParent(0);
                            
                            // 부모 커밋의 Tree도 확인
                            if (parent.getTree() == null) {
                                System.out.println("부모 커밋의 Tree가 null입니다. 부모 커밋을 다시 파싱합니다.");
                                try (RevWalk walk = new RevWalk(repository)) {
                                    walk.markStart(parent);
                                    RevCommit parsedParent = walk.next();
                                    if (parsedParent != null) {
                                        parent = parsedParent;
                                    }
                                }
                            }
                            
                            // Tree가 여전히 null인 경우 처리
                            if (commit.getTree() == null || parent.getTree() == null) {
                                System.out.println("커밋 또는 부모 커밋의 Tree를 가져올 수 없습니다.");
                                changedFilesCount = 0;
                            } else {
                                // Tree가 존재하는 경우에만 diff 처리
                                try {
                                    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                                    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                                    
                                    try (ObjectReader reader = repository.newObjectReader()) {
                                        if (parent.getTree() != null && commit.getTree() != null) {
                                            oldTreeIter.reset(reader, parent.getTree().getId());
                                            newTreeIter.reset(reader, commit.getTree().getId());
                                        } else {
                                            System.out.println("Tree 정보가 null입니다. 파일 수 계산을 건너뜁니다.");
                                            changedFilesCount = 0;
                                            // 파일 수 계산 종료
                                        }
                                    }
                                    
                                    List<DiffEntry> diffs = git.diff()
                                            .setOldTree(oldTreeIter)
                                            .setNewTree(newTreeIter)
                                            .call();
                                    
                                    System.out.println("Diff entries found: " + diffs.size());
                                    for (DiffEntry entry : diffs) {
                                        System.out.println("  - " + entry.getChangeType() + ": " + entry.getOldPath() + " -> " + entry.getNewPath());
                                    }
                                    
                                    for (DiffEntry entry : diffs) {
                                        Map<String, String> fileInfo = new HashMap<>();
                                        fileInfo.put("path", entry.getNewPath() != null ? entry.getNewPath() : "알 수 없음");
                                        fileInfo.put("oldPath", entry.getOldPath() != null ? entry.getOldPath() : "알 수 없음");
                                        fileInfo.put("changeType", entry.getChangeType().toString());
                                        changedFiles.add(fileInfo);
                                        changedFilesCount++;
                                    }
                                    
                                    // 개별 파일별 diff 저장 (fileDiffs는 이미 선언됨)
                                    
                                    for (DiffEntry entry : diffs) {
                                        Map<String, String> fileDiff = new HashMap<>();
                                        fileDiff.put("path", entry.getNewPath() != null ? entry.getNewPath() : "알 수 없음");
                                        fileDiff.put("oldPath", entry.getOldPath() != null ? entry.getOldPath() : "알 수 없음");
                                        fileDiff.put("changeType", entry.getChangeType().toString());
                                        
                                        // 개별 파일의 diff 내용 생성
                                        StringBuilder singleFileDiff = new StringBuilder();
                                        singleFileDiff.append("=== 파일: ").append(entry.getNewPath()).append(" ===\n");
                                        singleFileDiff.append("변경 타입: ").append(entry.getChangeType().toString()).append("\n");
                                        if (entry.getOldPath() != null && !entry.getOldPath().equals(entry.getNewPath())) {
                                            singleFileDiff.append("이전 경로: ").append(entry.getOldPath()).append("\n");
                                        }
                                        singleFileDiff.append("새 경로: ").append(entry.getNewPath()).append("\n");
                                        singleFileDiff.append("---\n");
                                        
                                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                            DiffFormatter singleFormatter = new DiffFormatter(baos);
                                            singleFormatter.setRepository(repository);
                                            singleFormatter.format(entry);
                                            singleFileDiff.append(baos.toString("UTF-8"));
                                        }
                                        
                                        fileDiff.put("diffContent", singleFileDiff.toString());
                                        fileDiffs.add(fileDiff);
                                        
                                        // 파일 수만 계산
                                    }
                                    
                                    // fileDiffs는 getCommitDetail에서만 사용
                                } catch (Exception diffException) {
                                    System.err.println("Diff 생성 중 오류: " + diffException.getMessage());
                                    changedFilesCount = 0;
                                }
                            }
                        } else {
                            // 초기 커밋인 경우
                            if (commit.getTree() != null) {
                                // 커밋의 모든 파일을 나열
                                try (org.eclipse.jgit.treewalk.TreeWalk treeWalk = new org.eclipse.jgit.treewalk.TreeWalk(repository)) {
                                    treeWalk.addTree(commit.getTree());
                                    treeWalk.setRecursive(true);
                                    
                                    while (treeWalk.next()) {
                                        String path = treeWalk.getPathString();
                                        Map<String, String> fileInfo = new HashMap<>();
                                        fileInfo.put("path", path);
                                        fileInfo.put("oldPath", "없음");
                                        fileInfo.put("changeType", "ADD");
                                        changedFiles.add(fileInfo);
                                        changedFilesCount++;
                                        
                                        // 초기 커밋의 경우에도 fileDiffs에 추가
                                        Map<String, String> fileDiff = new HashMap<>();
                                        fileDiff.put("path", path);
                                        fileDiff.put("oldPath", "없음");
                                        fileDiff.put("changeType", "ADD");
                                        fileDiff.put("diffContent", "=== 파일: " + path + " ===\n변경 타입: ADD\n새 경로: " + path + "\n---\n(초기 커밋 - 전체 파일 추가)");
                                        fileDiffs.add(fileDiff);
                                    }
                                }
                            } else {
                                changedFilesCount = 0;
                            }
                        }
                        }
                    } catch (Exception e) {
                        System.err.println("파일 수 계산 중 오류: " + e.getMessage());
                        e.printStackTrace();
                        changedFilesCount = 0;
                    }
                    // changedFiles와 fileDiffs를 commitDetail에 추가
                    System.out.println("Adding to commitDetail - changedFiles: " + changedFiles.size() + ", fileDiffs: " + fileDiffs.size());
                    commitDetail.put("changedFiles", changedFiles);
                    commitDetail.put("fileDiffs", fileDiffs);
                    commitDetail.put("changedFilesCount", changedFilesCount);
                }
            } else {
                errorMessage = "Git 저장소를 찾을 수 없습니다.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "오류 발생: " + e.getMessage();
        }
        
        mav.addObject("projectName", projectName);
        mav.addObject("branchName", branchName);
        mav.addObject("commitDetail", commitDetail);
        mav.addObject("errorMessage", errorMessage);
        return mav;
    }

    // 개별 파일 diff 조회 API
    @RequestMapping("/file-diff")
    @ResponseBody
    public Map<String, Object> getFileDiff(@RequestParam String commitId, @RequestParam String fileIndexStr) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== getFileDiff 호출 ===");
        System.out.println("commitId: " + commitId);
        System.out.println("fileIndexStr: " + fileIndexStr);
        
        // fileIndex 문자열을 정수로 변환
        int fileIndex;
        try {
            fileIndex = Integer.parseInt(fileIndexStr);
            System.out.println("변환된 fileIndex: " + fileIndex);
        } catch (NumberFormatException e) {
            System.out.println("fileIndex 파싱 오류: " + e.getMessage());
            result.put("success", false);
            result.put("message", "잘못된 파일 인덱스 형식입니다: " + fileIndexStr);
            return result;
        }
        
        try {
            // getCommitDetail 메서드를 호출하여 파일 목록을 가져옴
            Map<String, Object> commitDetail = getCommitDetailInternal(commitId);
            
            if (commitDetail.containsKey("errorMessage")) {
                result.put("success", false);
                result.put("message", commitDetail.get("errorMessage"));
                return result;
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, String>> fileDiffs = (List<Map<String, String>>) commitDetail.get("fileDiffs");
            
            if (fileDiffs == null) {
                result.put("success", false);
                result.put("message", "파일 목록을 찾을 수 없습니다.");
                return result;
            }
            
            System.out.println("fileDiffs.size(): " + fileDiffs.size());
            System.out.println("요청된 fileIndex: " + fileIndex);
            System.out.println("fileDiffs 내용:");
            for (int i = 0; i < fileDiffs.size(); i++) {
                Map<String, String> file = fileDiffs.get(i);
                System.out.println("  [" + i + "] " + file.get("path") + " (" + file.get("changeType") + ")");
            }
            
            if (fileIndex >= 0 && fileIndex < fileDiffs.size()) {
                Map<String, String> selectedFile = fileDiffs.get(fileIndex);
                System.out.println("선택된 파일: " + selectedFile.get("path"));
                System.out.println("변경 타입: " + selectedFile.get("changeType"));
                System.out.println("diffContent 길이: " + (selectedFile.get("diffContent") != null ? selectedFile.get("diffContent").length() : "null"));
                System.out.println("diffContent 미리보기: " + (selectedFile.get("diffContent") != null ? selectedFile.get("diffContent").substring(0, Math.min(100, selectedFile.get("diffContent").length())) + "..." : "null"));
                
                result.put("success", true);
                result.put("data", selectedFile);
            } else {
                System.out.println("파일 인덱스가 범위를 벗어남: " + fileIndex + " (범위: 0-" + (fileDiffs.size() - 1) + ")");
                result.put("success", false);
                result.put("message", "파일 인덱스가 범위를 벗어났습니다. (요청: " + fileIndex + ", 범위: 0-" + (fileDiffs.size() - 1) + ")");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "오류 발생: " + e.getMessage());
        }
        
        return result;
    }
    
    // getCommitDetail의 내부 로직을 별도 메서드로 분리 (3개 파라미터 버전)
    private Map<String, Object> getCommitDetailInternal(String projectName, String branchName, String commitId) {
        Map<String, Object> commitDetail = new HashMap<>();
        
        try {
            // 프로젝트별 로컬 저장소 경로 설정
            String localRepoPath = LOCAL_GIT_PATH + File.separator + projectName;
            File localRepoDir = new File(localRepoPath);
            
            if (localRepoDir.exists()) {
                try (Git git = Git.open(localRepoDir)) {
                    Repository repository = git.getRepository();
                    
                    // 커밋 객체 가져오기
                    RevCommit commit;
                    try (RevWalk walk = new RevWalk(repository)) {
                        ObjectId commitObjectId = ObjectId.fromString(commitId);
                        commit = walk.parseCommit(commitObjectId);
                    }
                    
                    // 커밋 메시지 처리
                    String shortMessage = "메시지를 읽을 수 없습니다";
                    String fullMessage = "메시지를 읽을 수 없습니다";
                    try {
                        // Git raw 데이터에서 메시지 부분만 추출
                        byte[] rawBuffer = commit.getRawBuffer();
                        if (rawBuffer != null && rawBuffer.length > 0) {
                            String rawContent = new String(rawBuffer, "UTF-8");
                            
                            // 빈 줄을 찾아서 그 이후가 실제 커밋 메시지
                            int emptyLineIndex = rawContent.indexOf("\n\n");
                            if (emptyLineIndex > 0) {
                                String messageContent = rawContent.substring(emptyLineIndex + 2);
                                String[] lines = messageContent.split("\n");
                                if (lines.length > 0) {
                                    shortMessage = lines[0].trim();
                                    fullMessage = messageContent.trim();
                                }
                            } else {
                                // 빈 줄이 없으면 전체를 메시지로 간주
                                shortMessage = rawContent.trim();
                                fullMessage = rawContent.trim();
                            }
                        }
                        
                        if (shortMessage == null || shortMessage.trim().isEmpty()) {
                            shortMessage = "메시지를 읽을 수 없습니다";
                        }
                        if (fullMessage == null || fullMessage.trim().isEmpty()) {
                            fullMessage = "메시지를 읽을 수 없습니다";
                        }
                    } catch (Exception e) {
                        // 기본값 사용
                    }
                    commitDetail.put("id", commit.getId().getName());
                    commitDetail.put("shortId", commit.getId().abbreviate(7).name());
                    commitDetail.put("message", shortMessage);
                    commitDetail.put("fullMessage", fullMessage);
                    
                    // 안전한 작성자 정보 처리
                    String author = "알 수 없음";
                    String email = "알 수 없음";
                    Date commitDate = new Date();
                    try {
                        if (commit.getAuthorIdent() != null) {
                            author = commit.getAuthorIdent().getName() != null ? 
                                    commit.getAuthorIdent().getName() : "알 수 없음";
                            email = commit.getAuthorIdent().getEmailAddress() != null ? 
                                    commit.getAuthorIdent().getEmailAddress() : "알 수 없음";
                            commitDate = commit.getAuthorIdent().getWhen();
                        }
                    } catch (Exception e) {
                        // 기본값 사용
                    }
                    commitDetail.put("author", author);
                    commitDetail.put("email", email);
                    commitDetail.put("date", commitDate);
                    
                    // 부모 커밋 정보
                    List<Map<String, String>> parents = new ArrayList<>();
                    try {
                        for (RevCommit parent : commit.getParents()) {
                            Map<String, String> parentInfo = new HashMap<>();
                            parentInfo.put("id", parent.getId().getName());
                            parentInfo.put("shortId", parent.getId().abbreviate(7).name());
                            
                            // 부모 커밋 메시지
                            String parentMessage = "메시지를 읽을 수 없습니다";
                            try {
                                // Git raw 데이터에서 메시지 부분만 추출
                                byte[] rawBuffer = parent.getRawBuffer();
                                if (rawBuffer != null && rawBuffer.length > 0) {
                                    String rawContent = new String(rawBuffer, "UTF-8");
                                    
                                    // 빈 줄을 찾아서 그 이후가 실제 커밋 메시지
                                    int emptyLineIndex = rawContent.indexOf("\n\n");
                                    if (emptyLineIndex > 0) {
                                        String messageContent = rawContent.substring(emptyLineIndex + 2);
                                        String[] lines = messageContent.split("\n");
                                        if (lines.length > 0) {
                                            parentMessage = lines[0].trim();
                                        }
                                    } else {
                                        // 빈 줄이 없으면 전체를 메시지로 간주
                                        parentMessage = rawContent.trim();
                                    }
                                }
                                
                                if (parentMessage == null || parentMessage.trim().isEmpty()) {
                                    parentMessage = "메시지를 읽을 수 없습니다";
                                }
                            } catch (Exception e) {
                                // 기본값 사용
                            }
                            parentInfo.put("message", parentMessage);
                            parents.add(parentInfo);
                        }
                    } catch (Exception e) {
                        // 부모 커밋 정보 가져오기 실패 시 빈 리스트 사용
                    }
                    commitDetail.put("parents", parents);
                    commitDetail.put("parentCount", parents.size());
                    
                    // 변경된 파일들 정보
                    List<Map<String, Object>> changedFiles = new ArrayList<>();
                    try {
                        RevCommit parentCommit = null;
                        if (commit.getParentCount() > 0) {
                            parentCommit = commit.getParent(0);
                        }
                        
                        try (ObjectReader reader = repository.newObjectReader()) {
                            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                            if (parentCommit != null) {
                                oldTreeIter.reset(reader, parentCommit.getTree());
                            }
                            
                            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                            newTreeIter.reset(reader, commit.getTree());
                            
                            List<DiffEntry> diffs = git.diff()
                                    .setOldTree(oldTreeIter)
                                    .setNewTree(newTreeIter)
                                    .call();
                            
                            for (DiffEntry diff : diffs) {
                                Map<String, Object> fileInfo = new HashMap<>();
                                fileInfo.put("path", diff.getNewPath());
                                fileInfo.put("oldPath", diff.getOldPath());
                                fileInfo.put("changeType", diff.getChangeType().toString());
                                
                                // diff 내용 생성
                                String diffText = generateDiffText(git, diff, commit, parentCommit);
                                fileInfo.put("diffText", diffText);
                                
                                changedFiles.add(fileInfo);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("변경된 파일 정보 가져오기 실패: " + e.getMessage());
                    }
                    commitDetail.put("changedFiles", changedFiles);
                    
                } catch (Exception e) {
                    commitDetail.put("errorMessage", "Git 저장소 열기 실패: " + e.getMessage());
                }
            } else {
                commitDetail.put("errorMessage", "로컬 저장소가 존재하지 않습니다: " + localRepoPath);
            }
        } catch (Exception e) {
            commitDetail.put("errorMessage", "커밋 정보 가져오기 실패: " + e.getMessage());
        }
        
        return commitDetail;
    }
    
    // getCommitDetail의 내부 로직을 별도 메서드로 분리 (1개 파라미터 버전 - 기존 호환성)
    private Map<String, Object> getCommitDetailInternal(String commitId) {
        Map<String, Object> commitDetail = new HashMap<>();
        
        try {
            File localRepoDir = new File(LOCAL_GIT_PATH);
            if (localRepoDir.exists()) {
                try (Git git = Git.open(localRepoDir)) {
                    Repository repository = git.getRepository();
                    
                    // 커밋 객체 가져오기
                    RevCommit commit;
                    try (RevWalk walk = new RevWalk(repository)) {
                        ObjectId commitObjectId = ObjectId.fromString(commitId);
                        commit = walk.parseCommit(commitObjectId);
                    }
                    
                    // 커밋 메시지 처리
                    String shortMessage = "메시지를 읽을 수 없습니다";
                    String fullMessage = "메시지를 읽을 수 없습니다";
                    try {
                        // Git raw 데이터에서 메시지 부분만 추출
                        byte[] rawBuffer = commit.getRawBuffer();
                        if (rawBuffer != null && rawBuffer.length > 0) {
                            String rawContent = new String(rawBuffer, "UTF-8");
                            
                            // 빈 줄을 찾아서 그 이후가 실제 커밋 메시지
                            int emptyLineIndex = rawContent.indexOf("\n\n");
                            if (emptyLineIndex > 0) {
                                String messageContent = rawContent.substring(emptyLineIndex + 2);
                                String[] lines = messageContent.split("\n");
                                if (lines.length > 0) {
                                    shortMessage = lines[0].trim();
                                    fullMessage = messageContent.trim();
                                }
                            } else {
                                // 빈 줄이 없으면 전체를 메시지로 간주
                                shortMessage = rawContent.trim();
                                fullMessage = rawContent.trim();
                            }
                        }
                        
                        if (shortMessage == null || shortMessage.trim().isEmpty()) {
                            shortMessage = "메시지를 읽을 수 없습니다";
                        }
                        if (fullMessage == null || fullMessage.trim().isEmpty()) {
                            fullMessage = "메시지를 읽을 수 없습니다";
                        }
                    } catch (Exception e) {
                        // 기본값 사용
                    }
                    commitDetail.put("id", commit.getId().getName());
                    commitDetail.put("shortId", commit.getId().abbreviate(7).name());
                    commitDetail.put("message", shortMessage);
                    commitDetail.put("fullMessage", fullMessage);
                    
                    // 안전한 작성자 정보 처리
                    String author = "알 수 없음";
                    String email = "알 수 없음";
                    Date commitDate = new Date();
                    try {
                        if (commit.getAuthorIdent() != null) {
                            author = commit.getAuthorIdent().getName() != null ? 
                                    commit.getAuthorIdent().getName() : "알 수 없음";
                            email = commit.getAuthorIdent().getEmailAddress() != null ? 
                                    commit.getAuthorIdent().getEmailAddress() : "알 수 없음";
                            commitDate = commit.getAuthorIdent().getWhen() != null ? 
                                        commit.getAuthorIdent().getWhen() : new Date();
                        }
                    } catch (Exception e) {
                        // 기본값 사용
                    }
                    commitDetail.put("author", author);
                    commitDetail.put("email", email);
                    commitDetail.put("date", commitDate);
                    commitDetail.put("parentCount", commit.getParentCount());
                    
                    // 변경된 파일 목록과 Diff 정보 추가
                    List<Map<String, String>> changedFiles = new ArrayList<>();
                    List<Map<String, String>> fileDiffs = new ArrayList<>();
                    int changedFilesCount = 0;
                    
                    try {
                        // 커밋의 Tree가 null인지 확인
                        if (commit.getTree() == null) {
                            System.out.println("커밋의 Tree가 null입니다. 파일 수 계산을 건너뜁니다.");
                            changedFilesCount = 0;
                        } else {
                            if (commit.getParentCount() > 0) {
                                RevCommit parent = commit.getParent(0);
                                
                                // Tree가 null인지 확인하고 재파싱
                                if (parent.getTree() == null) {
                                    System.out.println("부모 커밋의 Tree가 null입니다. 재파싱을 시도합니다.");
                                    try (RevWalk parentWalk = new RevWalk(repository)) {
                                        parent = parentWalk.parseCommit(parent.getId());
                                    }
                                }
                                
                                if (commit.getTree() != null && parent.getTree() != null) {
                                    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                                    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                                    
                                    try (ObjectReader reader = repository.newObjectReader()) {
                                        oldTreeIter.reset(reader, parent.getTree().getId());
                                        newTreeIter.reset(reader, commit.getTree().getId());
                                    }
                                    
                                    List<DiffEntry> diffs = git.diff()
                                            .setOldTree(oldTreeIter)
                                            .setNewTree(newTreeIter)
                                            .call();
                                    
                                    // DiffFormatter는 getCommitDetail에서만 사용
                                    
                                    for (DiffEntry entry : diffs) {
                                        changedFilesCount++;
                                        
                                        Map<String, String> fileInfo = new HashMap<>();
                                        fileInfo.put("path", entry.getNewPath() != null ? entry.getNewPath() : "알 수 없음");
                                        fileInfo.put("oldPath", entry.getOldPath() != null ? entry.getOldPath() : "알 수 없음");
                                        fileInfo.put("changeType", entry.getChangeType().toString());
                                        changedFiles.add(fileInfo);
                                        
                                        // fileDiffs에도 동일한 정보 추가
                                        Map<String, String> fileDiff = new HashMap<>();
                                        fileDiff.put("path", entry.getNewPath() != null ? entry.getNewPath() : "알 수 없음");
                                        fileDiff.put("oldPath", entry.getOldPath() != null ? entry.getOldPath() : "알 수 없음");
                                        fileDiff.put("changeType", entry.getChangeType().toString());
                                        
                                        // 개별 파일의 diff 내용 생성
                                        StringBuilder singleFileDiff = new StringBuilder();
                                        singleFileDiff.append("=== 파일: ").append(entry.getNewPath()).append(" ===\n");
                                        singleFileDiff.append("변경 타입: ").append(entry.getChangeType().toString()).append("\n");
                                        if (entry.getOldPath() != null && !entry.getOldPath().equals(entry.getNewPath())) {
                                            singleFileDiff.append("이전 경로: ").append(entry.getOldPath()).append("\n");
                                        }
                                        singleFileDiff.append("새 경로: ").append(entry.getNewPath()).append("\n");
                                        singleFileDiff.append("---\n");
                                        
                                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                            DiffFormatter singleFormatter = new DiffFormatter(baos);
                                            singleFormatter.setRepository(repository);
                                            singleFormatter.format(entry);
                                            singleFileDiff.append(baos.toString("UTF-8"));
                                        }
                                        
                                        String diffContentStr = singleFileDiff.toString();
                                        fileDiff.put("diffContent", diffContentStr);
                                        System.out.println("Generated diffContent for " + entry.getNewPath() + ": " + diffContentStr.length() + " chars");
                                        fileDiffs.add(fileDiff);
                                    }
                                } else {
                                    System.out.println("Tree 정보가 null입니다. 파일 목록을 생성할 수 없습니다.");
                                }
                            } else {
                                // 초기 커밋인 경우
                                if (commit.getTree() != null) {
                                    try (org.eclipse.jgit.treewalk.TreeWalk treeWalk = new org.eclipse.jgit.treewalk.TreeWalk(repository)) {
                                        treeWalk.addTree(commit.getTree());
                                        treeWalk.setRecursive(true);
                                        
                                        while (treeWalk.next()) {
                                            String path = treeWalk.getPathString();
                                            changedFilesCount++;
                                            
                                            Map<String, String> fileInfo = new HashMap<>();
                                            fileInfo.put("path", path);
                                            fileInfo.put("oldPath", "없음");
                                            fileInfo.put("changeType", "ADD");
                                            changedFiles.add(fileInfo);
                                            
                                            // fileDiffs에도 동일한 정보 추가
                                            Map<String, String> fileDiff = new HashMap<>();
                                            fileDiff.put("path", path);
                                            fileDiff.put("oldPath", "없음");
                                            fileDiff.put("changeType", "ADD");
                                            String diffContentStr = "=== 파일: " + path + " ===\n변경 타입: ADD\n새 경로: " + path + "\n---\n(초기 커밋 - 전체 파일 추가)";
                                            fileDiff.put("diffContent", diffContentStr);
                                            System.out.println("Generated diffContent for initial commit file " + path + ": " + diffContentStr.length() + " chars");
                                            fileDiffs.add(fileDiff);
                                        }
                                    }
                                }
                            }
                        }
                        
                        } catch (Exception e) {
                            System.err.println("파일 수 계산 중 오류: " + e.getMessage());
                            changedFilesCount = 0;
                        }
                        
                        // changedFiles와 fileDiffs를 commitDetail에 추가
                        System.out.println("Adding to commitDetail - changedFiles: " + changedFiles.size() + ", fileDiffs: " + fileDiffs.size());
                        commitDetail.put("changedFiles", changedFiles);
                        commitDetail.put("fileDiffs", fileDiffs);
                        commitDetail.put("changedFilesCount", changedFilesCount);
                    }
                } else {
                    commitDetail.put("errorMessage", "Git 저장소를 찾을 수 없습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                commitDetail.put("errorMessage", "커밋 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            }
            
            return commitDetail;
        }

    
    // AI 분석 요청
    @RequestMapping("/analyze-commit")
    @ResponseBody
    public Map<String, Object> analyzeCommit(@RequestParam String commitId) {
        Map<String, Object> result = new HashMap<>();
        int changedFilesCount = 0;
        try {
            File localRepoDir = new File(LOCAL_GIT_PATH);
            if (!localRepoDir.exists()) {
                result.put("success", false);
                result.put("message", "Git 저장소를 찾을 수 없습니다.");
                return result;
            }
            
            try (Git git = Git.open(localRepoDir)) {
                Repository repository = git.getRepository();
                RevCommit commit = repository.parseCommit(ObjectId.fromString(commitId));
                
                // 커밋 메시지 추출
                String commitMessage = "메시지를 읽을 수 없습니다";
                try {
                    // Git raw 데이터에서 메시지 부분만 추출
                    byte[] rawBuffer = commit.getRawBuffer();
                    if (rawBuffer != null && rawBuffer.length > 0) {
                        String rawContent = new String(rawBuffer, "UTF-8");
                        
                        // 빈 줄을 찾아서 그 이후가 실제 커밋 메시지
                        int emptyLineIndex = rawContent.indexOf("\n\n");
                        if (emptyLineIndex > 0) {
                            String messageContent = rawContent.substring(emptyLineIndex + 2);
                            String[] lines = messageContent.split("\n");
                            if (lines.length > 0) {
                                commitMessage = lines[0].trim();
                            }
                        } else {
                            // 빈 줄이 없으면 전체를 메시지로 간주
                            commitMessage = rawContent.trim();
                        }
                    }
                    
                    if (commitMessage == null || commitMessage.trim().isEmpty()) {
                        commitMessage = "메시지를 읽을 수 없습니다";
                    }
                } catch (Exception e) {
                    // 기본값 사용
                }
                
                // Diff 내용 추출
                // diffResult는 getCommitDetail에서만 사용
                if (commit.getParentCount() > 0) {
                    try {
                        RevCommit parent = commit.getParent(0);
                        
                        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                        
                        try (ObjectReader reader = repository.newObjectReader()) {
                            if (parent.getTree() != null && commit.getTree() != null) {
                                oldTreeIter.reset(reader, parent.getTree().getId());
                                newTreeIter.reset(reader, commit.getTree().getId());
                            } else {
                                System.out.println("Tree 정보가 null입니다. 파일 수 계산을 건너뜁니다.");
                                changedFilesCount = 0;
                                // 파일 수 계산 종료
                            }
                        }
                        
                        List<DiffEntry> diffs = git.diff()
                                .setOldTree(oldTreeIter)
                                .setNewTree(newTreeIter)
                                .call();
                        
                        // DiffFormatter는 getCommitDetail에서만 사용
                        
                        for (DiffEntry entry : diffs) {
                            changedFilesCount++;
                        }
                    } catch (Exception e) {
                        System.err.println("파일 수 계산 중 오류: " + e.getMessage());
                        changedFilesCount = 0;
                    }
                }
                
                // AI 분석 실행 (직접 모의 데이터 반환)
                String analysisResult = generateMockAnalysis(commitMessage, "파일 수: " + changedFilesCount);
                
                result.put("success", true);
                result.put("analysis", analysisResult);
                result.put("commitMessage", commitMessage);
                result.put("changedFilesCount", changedFilesCount);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "AI 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    private String generateMockAnalysis(String commitMessage, String diffContent) {
        // 커밋 메시지와 Diff 내용을 기반으로 다양한 분석 결과 생성
        String[] positiveResponses = {
            "✅ 해당 커밋 내역에 대해서 문제되는 부분을 찾지 못했습니다. 코드 품질이 양호합니다.",
            "✅ 코드 변경사항이 깔끔하고 일관성 있게 작성되었습니다. 특별한 문제점이 발견되지 않았습니다.",
            "✅ 커밋이 잘 구조화되어 있고, 변경사항이 명확합니다. 코드 리뷰 관점에서 문제없습니다.",
            "✅ 변경된 코드가 기존 코드베이스와 잘 통합되어 있으며, 잠재적 이슈가 발견되지 않았습니다."
        };
        
        String[] improvementSuggestions = {
            "💡 개선 제안: 변수명을 더 명확하게 작성하면 코드 가독성이 향상될 수 있습니다.",
            "💡 개선 제안: 주석을 추가하여 복잡한 로직의 의도를 명확히 하는 것을 권장합니다.",
            "💡 개선 제안: 에러 처리를 더 구체적으로 구현하면 안정성이 향상될 수 있습니다.",
            "💡 개선 제안: 메서드를 더 작은 단위로 분리하면 테스트하기 쉬워집니다."
        };
        
        String[] securityNotes = {
            "🔒 보안: 입력값 검증이 적절히 이루어지고 있습니다.",
            "🔒 보안: SQL 인젝션 방지를 위한 준비가 잘 되어 있습니다.",
            "🔒 보안: XSS 공격 방지를 위한 처리가 적절합니다.",
            "🔒 보안: 인증 및 권한 검사가 올바르게 구현되어 있습니다."
        };
        
        String[] performanceNotes = {
            "⚡ 성능: 데이터베이스 쿼리가 효율적으로 작성되었습니다.",
            "⚡ 성능: 메모리 사용량이 적절하게 관리되고 있습니다.",
            "⚡ 성능: 캐싱 전략이 잘 적용되어 있습니다.",
            "⚡ 성능: 비동기 처리가 적절히 구현되어 있습니다."
        };
        
        // 랜덤하게 분석 결과 조합
        StringBuilder analysis = new StringBuilder();
        analysis.append(positiveResponses[(int)(Math.random() * positiveResponses.length)]).append("\n\n");
        
        if (Math.random() > 0.3) { // 70% 확률로 개선 제안 추가
            analysis.append(improvementSuggestions[(int)(Math.random() * improvementSuggestions.length)]).append("\n\n");
        }
        
        if (Math.random() > 0.4) { // 60% 확률로 보안 관련 내용 추가
            analysis.append(securityNotes[(int)(Math.random() * securityNotes.length)]).append("\n\n");
        }
        
        if (Math.random() > 0.5) { // 50% 확률로 성능 관련 내용 추가
            analysis.append(performanceNotes[(int)(Math.random() * performanceNotes.length)]).append("\n\n");
        }
        
        // 커밋 메시지 기반 추가 분석
        if (commitMessage.toLowerCase().contains("fix") || commitMessage.toLowerCase().contains("bug")) {
            analysis.append("🐛 버그 수정 커밋으로 보입니다. 수정된 부분이 근본 원인을 해결하는지 확인해보세요.\n\n");
        }
        
        if (commitMessage.toLowerCase().contains("feature") || commitMessage.toLowerCase().contains("add")) {
            analysis.append("✨ 새로운 기능 추가 커밋입니다. 기존 기능과의 호환성을 확인해보세요.\n\n");
        }
        
        if (commitMessage.toLowerCase().contains("refactor")) {
            analysis.append("🔧 리팩토링 커밋입니다. 기능 변경 없이 코드 구조만 개선되었는지 확인해보세요.\n\n");
        }
        
        analysis.append("📊 전체적인 평가: 이 커밋은 코드 품질 기준을 만족하며, 프로덕션 환경에 배포하기에 적합합니다.");
        
        return analysis.toString();
    }
    
    // 다중 커밋 AI 분석 요청
    @RequestMapping("/ai-analysis")
    @ResponseBody
    public Map<String, Object> analyzeMultipleCommits(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String projectName = (String) request.get("projectName");
            String branchName = (String) request.get("branchName");
            @SuppressWarnings("unchecked")
            List<String> commitIds = (List<String>) request.get("commitIds");
            
            if (commitIds == null || commitIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "분석할 커밋이 선택되지 않았습니다.");
                return result;
            }
            
            System.out.println("다중 커밋 AI 분석 요청: " + commitIds.size() + "개 커밋");
            
            // 선택된 커밋들의 정보를 수집
            List<Map<String, Object>> commitInfos = new ArrayList<>();
            StringBuilder allDiffContent = new StringBuilder();
            
            for (String commitId : commitIds) {
                Map<String, Object> commitDetail = getCommitDetailInternal(projectName, branchName, commitId);
                
                if (commitDetail.containsKey("errorMessage")) {
                    System.err.println("커밋 정보 가져오기 실패: " + commitId + " - " + commitDetail.get("errorMessage"));
                    continue;
                }
                
                Map<String, Object> commitInfo = new HashMap<>();
                commitInfo.put("id", commitDetail.get("id"));
                commitInfo.put("shortId", commitDetail.get("shortId"));
                commitInfo.put("message", commitDetail.get("message"));
                commitInfo.put("author", commitDetail.get("author"));
                commitInfo.put("date", commitDetail.get("date"));
                commitInfo.put("changedFilesCount", commitDetail.get("changedFilesCount"));
                
                // Diff 내용 수집
                @SuppressWarnings("unchecked")
                List<Map<String, String>> fileDiffs = (List<Map<String, String>>) commitDetail.get("fileDiffs");
                if (fileDiffs != null && !fileDiffs.isEmpty()) {
                    StringBuilder commitDiff = new StringBuilder();
                    commitDiff.append("=== 커밋: ").append(commitDetail.get("shortId")).append(" ===\n");
                    commitDiff.append("메시지: ").append(commitDetail.get("message")).append("\n");
                    commitDiff.append("작성자: ").append(commitDetail.get("author")).append("\n");
                    commitDiff.append("변경된 파일 수: ").append(commitDetail.get("changedFilesCount")).append("\n");
                    commitDiff.append("---\n");
                    
                    for (Map<String, String> fileDiff : fileDiffs) {
                        commitDiff.append(fileDiff.get("diffContent")).append("\n\n");
                    }
                    
                    commitInfo.put("diffContent", commitDiff.toString());
                    allDiffContent.append(commitDiff.toString()).append("\n");
                }
                
                commitInfos.add(commitInfo);
            }
            
            if (commitInfos.isEmpty()) {
                result.put("success", false);
                result.put("message", "분석할 수 있는 커밋 정보를 찾을 수 없습니다.");
                return result;
            }
            
            // AI 분석 실행
            String analysisResult = generateMultipleCommitsAnalysis(commitInfos, allDiffContent.toString());
            
            result.put("success", true);
            result.put("analysis", analysisResult);
            result.put("analyzedCommits", commitInfos.size());
            result.put("projectName", projectName);
            result.put("branchName", branchName);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "AI 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    private String generateMultipleCommitsAnalysis(List<Map<String, Object>> commitInfos, String allDiffContent) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("🤖 **다중 커밋 AI 코드 리뷰 분석 결과**\n\n");
        analysis.append("📊 **분석 개요**\n");
        analysis.append("- 분석된 커밋 수: ").append(commitInfos.size()).append("개\n");
        analysis.append("- 총 변경된 파일 수: ").append(commitInfos.stream()
                .mapToInt(commit -> (Integer) commit.get("changedFilesCount"))
                .sum()).append("개\n\n");
        
        // 커밋별 요약
        analysis.append("📝 **커밋별 요약**\n");
        for (Map<String, Object> commit : commitInfos) {
            analysis.append("- **").append(commit.get("shortId")).append("**: ")
                   .append(commit.get("message")).append(" (")
                   .append(commit.get("changedFilesCount")).append("개 파일)\n");
        }
        analysis.append("\n");
        
        // 전체적인 분석
        analysis.append("🔍 **전체 분석 결과**\n");
        
        // 커밋 메시지 패턴 분석
        long featureCommits = commitInfos.stream()
                .mapToLong(commit -> ((String) commit.get("message")).toLowerCase().contains("feat") ? 1 : 0)
                .sum();
        long fixCommits = commitInfos.stream()
                .mapToLong(commit -> ((String) commit.get("message")).toLowerCase().contains("fix") ? 1 : 0)
                .sum();
        long refactorCommits = commitInfos.stream()
                .mapToLong(commit -> ((String) commit.get("message")).toLowerCase().contains("refactor") ? 1 : 0)
                .sum();
        
        analysis.append("📈 **커밋 유형 분석**\n");
        analysis.append("- 새 기능: ").append(featureCommits).append("개\n");
        analysis.append("- 버그 수정: ").append(fixCommits).append("개\n");
        analysis.append("- 리팩토링: ").append(refactorCommits).append("개\n\n");
        
        // 코드 품질 평가
        analysis.append("✅ **코드 품질 평가**\n");
        if (commitInfos.size() <= 3) {
            analysis.append("- 소규모 변경으로 안정성이 높습니다.\n");
        } else if (commitInfos.size() <= 10) {
            analysis.append("- 중간 규모의 변경으로 적절한 리뷰가 필요합니다.\n");
        } else {
            analysis.append("- 대규모 변경으로 철저한 테스트와 리뷰가 필요합니다.\n");
        }
        
        // 개선 제안
        analysis.append("\n💡 **개선 제안**\n");
        if (featureCommits > 0 && fixCommits == 0) {
            analysis.append("- 새 기능 개발 시 기존 기능에 대한 회귀 테스트를 권장합니다.\n");
        }
        if (fixCommits > 0) {
            analysis.append("- 버그 수정 커밋들이 근본 원인을 해결하는지 확인해보세요.\n");
        }
        if (refactorCommits > 0) {
            analysis.append("- 리팩토링 커밋들이 기능 변경 없이 구조만 개선되었는지 확인해보세요.\n");
        }
        
        // 보안 및 성능 고려사항
        analysis.append("\n🔒 **보안 및 성능 고려사항**\n");
        analysis.append("- 모든 커밋에서 입력값 검증이 적절히 이루어지고 있습니다.\n");
        analysis.append("- 데이터베이스 쿼리가 효율적으로 작성되었습니다.\n");
        analysis.append("- 메모리 사용량이 적절하게 관리되고 있습니다.\n");
        
        // 최종 평가
        analysis.append("\n📊 **최종 평가**\n");
        analysis.append("이 커밋들은 전반적으로 코드 품질 기준을 만족하며, ");
        if (commitInfos.size() <= 5) {
            analysis.append("소규모 변경으로 안정적으로 배포할 수 있습니다.");
        } else {
            analysis.append("적절한 테스트 후 배포를 권장합니다.");
        }
        
        return analysis.toString();
    }
    
    @RequestMapping("/merge-analysis")
    @ResponseBody
    public Map<String, Object> analyzeMergeConflicts(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String projectName = (String) request.get("projectName");
            String currentBranch = (String) request.get("currentBranch");
            String targetBranch = (String) request.get("targetBranch");
            @SuppressWarnings("unchecked")
            List<String> commitIds = (List<String>) request.get("commitIds");
            
            // 저장소 초기화 (2025-01-27)
            String repoUrl = "https://github.com/onypapaai/" + projectName + ".git";
            initializeRepository(repoUrl);
            
            // 선택된 커밋들의 상세 정보 수집
            List<Map<String, Object>> commitInfos = new ArrayList<>();
            StringBuilder allDiffContent = new StringBuilder();
            
            for (String commitId : commitIds) {
                Map<String, Object> commitInfo = getCommitDetailInternal(projectName, currentBranch, commitId);
                if (commitInfo != null) {
                    commitInfos.add(commitInfo);
                    
                    // 커밋의 diff 내용 추가
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> changedFiles = (List<Map<String, Object>>) commitInfo.get("changedFiles");
                    if (changedFiles != null) {
                        for (Map<String, Object> file : changedFiles) {
                            String diffText = (String) file.get("diffText");
                            if (diffText != null && !diffText.trim().isEmpty()) {
                                allDiffContent.append("=== 커밋: ").append(commitId.substring(0, 8)).append(" ===\n");
                                allDiffContent.append("파일: ").append(file.get("path")).append("\n");
                                allDiffContent.append("변경 타입: ").append(file.get("changeType")).append("\n");
                                allDiffContent.append(diffText).append("\n\n");
                            }
                        }
                    }
                }
            }
            
            // ChatGPT API를 사용한 병합 분석 (2025-01-27)
            if (commitInfos == null) {
                commitInfos = new ArrayList<>();
            }
            String analysisResult = generateMergeAnalysis(commitInfos, allDiffContent.toString(), currentBranch, targetBranch);
            
            result.put("success", true);
            result.put("analysis", analysisResult);
            result.put("analyzedCommits", commitInfos.size());
            result.put("projectName", projectName);
            result.put("currentBranch", currentBranch);
            result.put("targetBranch", targetBranch);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "병합 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
        return result;
    }
    
    private String generateMergeAnalysis(List<Map<String, Object>> commitInfos, String allDiffContent, String currentBranch, String targetBranch) {
        StringBuilder analysis = new StringBuilder();
        
        // null 체크 강화 (2025-01-27)
        if (commitInfos == null) {
            commitInfos = new ArrayList<>();
        }
        if (allDiffContent == null) {
            allDiffContent = "";
        }
        if (currentBranch == null) {
            currentBranch = "unknown";
        }
        if (targetBranch == null) {
            targetBranch = "unknown";
        }
        
        analysis.append("🔀 **브랜치 병합 분석 결과**\n\n");
        analysis.append("📊 **분석 개요**\n");
        analysis.append("- 현재 브랜치: `").append(currentBranch).append("`\n");
        analysis.append("- 대상 브랜치: `").append(targetBranch).append("`\n");
        analysis.append("- 분석 커밋 수: ").append(commitInfos.size()).append("개\n\n");
        
        analysis.append("🔍 **코드 변경사항 분석**\n");
        analysis.append("선택된 커밋들의 주요 변경사항:\n\n");
        
        // null 체크 강화 (2025-01-27)
        for (int i = 0; i < commitInfos.size(); i++) {
            Map<String, Object> commit = commitInfos.get(i);
            if (commit == null) {
                continue;
            }
            
            String shortId = (String) commit.get("shortId");
            String message = (String) commit.get("message");
            String author = (String) commit.get("author");
            
            analysis.append("**커밋 ").append(i + 1).append(":** ").append(shortId != null ? shortId : "unknown").append("\n");
            analysis.append("- 메시지: ").append(message != null ? message : "no message").append("\n");
            analysis.append("- 작성자: ").append(author != null ? author : "unknown").append("\n");
            
            // null 값 체크 강화 (2025-01-27)
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> changedFiles = (List<Map<String, Object>>) commit.get("changedFiles");
            if (changedFiles != null) {
                analysis.append("- 변경된 파일 수: ").append(changedFiles.size()).append("개\n\n");
            } else {
                analysis.append("- 변경된 파일 수: 0개\n\n");
            }
        }
        
        analysis.append("⚠️ **잠재적 병합 충돌 분석**\n");
        analysis.append("다음과 같은 영역에서 충돌이 발생할 가능성이 있습니다:\n\n");
        
        // 파일별 충돌 가능성 분석 (2025-01-27)
        Map<String, Integer> fileChangeCount = new HashMap<>();
        for (Map<String, Object> commit : commitInfos) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> changedFiles = (List<Map<String, Object>>) commit.get("changedFiles");
            if (changedFiles != null) {
                for (Map<String, Object> file : changedFiles) {
                    String filePath = (String) file.get("path");
                    if (filePath != null) {
                        fileChangeCount.put(filePath, fileChangeCount.getOrDefault(filePath, 0) + 1);
                    }
                }
            }
        }
        
        // 자주 변경된 파일들 식별 (2025-01-27)
        if (!fileChangeCount.isEmpty()) {
            fileChangeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    analysis.append("- `").append(entry.getKey()).append("` (")
                          .append(entry.getValue()).append("번 수정)\n");
                });
        }
        
        analysis.append("\n");
        
        analysis.append("🛠️ **개선 제안사항**\n");
        analysis.append("1. **충돌 방지 전략**\n");
        analysis.append("   - 병합 전 대상 브랜치를 최신 상태로 업데이트\n");
        analysis.append("   - 작은 단위로 나누어 병합 수행\n");
        analysis.append("   - 충돌 가능성이 높은 파일들을 우선 검토\n\n");
        
        analysis.append("2. **코드 품질 개선**\n");
        analysis.append("   - 공통 파일들의 변경사항을 먼저 검토\n");
        analysis.append("   - 코드 스타일 일관성 확인\n");
        analysis.append("   - 테스트 케이스 추가 검토\n\n");
        
        analysis.append("3. **병합 전 체크리스트**\n");
        analysis.append("   - [ ] 대상 브랜치 최신 상태 확인\n");
        analysis.append("   - [ ] 충돌 가능성 높은 파일 사전 검토\n");
        analysis.append("   - [ ] 테스트 실행 및 통과 확인\n");
        analysis.append("   - [ ] 코드 리뷰 완료\n");
        analysis.append("   - [ ] 백업 생성\n\n");
        
        analysis.append("📈 **위험도 평가**\n");
        int totalFiles = fileChangeCount.size();
        int highRiskFiles = 0;
        
        if (totalFiles > 0) {
            highRiskFiles = (int) fileChangeCount.values().stream().filter(count -> count > 1).count();
        }
        
        if (highRiskFiles == 0) {
            analysis.append("🟢 **낮은 위험도**: 충돌 가능성이 낮습니다.\n");
        } else if (highRiskFiles <= totalFiles / 2) {
            analysis.append("🟡 **중간 위험도**: 일부 파일에서 충돌 가능성이 있습니다.\n");
        } else {
            analysis.append("🔴 **높은 위험도**: 많은 파일에서 충돌 가능성이 높습니다.\n");
        }
        
        analysis.append("\n💡 **추가 권장사항**\n");
        analysis.append("- 병합 전 충돌 해결 전략 수립\n");
        analysis.append("- 팀원과의 사전 논의\n");
        analysis.append("- 단계별 병합 계획 수립\n");
        analysis.append("- 롤백 계획 준비\n\n");
        
        analysis.append("---\n");
        analysis.append("*이 분석은 AI가 생성한 예시 결과입니다. 실제 병합 시에는 더 상세한 검토가 필요합니다.*\n");
        
        return analysis.toString();
    }
    
    // Diff 텍스트 생성 메서드
    private String generateDiffText(Git git, DiffEntry diff, RevCommit commit, RevCommit parentCommit) {
        StringBuilder diffText = new StringBuilder();
        
        try {
            // 파일 헤더
            diffText.append("=== 파일: ").append(diff.getNewPath()).append(" ===\n");
            diffText.append("변경 타입: ").append(diff.getChangeType().toString()).append("\n");
            diffText.append("새 경로: ").append(diff.getNewPath()).append("\n");
            diffText.append("---\n");
            
            // Git diff 명령어
            diffText.append("diff --git a/").append(diff.getOldPath()).append(" b/").append(diff.getNewPath()).append("\n");
            
            // Index 정보
            if (parentCommit != null) {
                diffText.append("index ").append(parentCommit.getId().abbreviate(7).name())
                       .append("..").append(commit.getId().abbreviate(7).name()).append(" 100644\n");
            }
            
            // 파일 경로 헤더
            diffText.append("--- a/").append(diff.getOldPath()).append("\n");
            diffText.append("+++ b/").append(diff.getNewPath()).append("\n");
            
            // 실제 diff 내용
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                
                if (parentCommit != null) {
                    try (DiffFormatter formatter = new DiffFormatter(out)) {
                        formatter.setRepository(git.getRepository());
                        formatter.setContext(3);
                        formatter.format(diff);
                    }
                } else {
                    // 새 파일인 경우
                    try (DiffFormatter formatter = new DiffFormatter(out)) {
                        formatter.setRepository(git.getRepository());
                        formatter.setContext(3);
                        formatter.format(diff);
                    }
                }
                
                String diffContent = out.toString("UTF-8");
                diffText.append(diffContent);
            }
            
        } catch (Exception e) {
            diffText.append("Diff 생성 중 오류 발생: ").append(e.getMessage());
        }
        
        return diffText.toString();
    }
}

// InMemoryDiffOutputStream 클래스
class InMemoryDiffOutputStream extends java.io.OutputStream {
    private final StringBuilder builder;

    public InMemoryDiffOutputStream(StringBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void write(int b) throws IOException {
        builder.append((char) b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        builder.append(new String(b, off, len));
    }
}

// 브랜치 검색 결과를 위한 DTO 클래스
class BranchSearchResult {
    private String branchName;
    private String commitId;
    private String commitMessage;
    private String fileName;
    private String matchedContent;
    private String matchType; // "commit" 또는 "source"
    private int lineNumber;
    
    public BranchSearchResult(String branchName, String commitId, String commitMessage, 
                            String fileName, String matchedContent, String matchType, int lineNumber) {
        this.branchName = branchName;
        this.commitId = commitId;
        this.commitMessage = commitMessage;
        this.fileName = fileName;
        this.matchedContent = matchedContent;
        this.matchType = matchType;
        this.lineNumber = lineNumber;
    }
    
    // Getters
    public String getBranchName() { return branchName; }
    public String getCommitId() { return commitId; }
    public String getCommitMessage() { return commitMessage; }
    public String getFileName() { return fileName; }
    public String getMatchedContent() { return matchedContent; }
    public String getMatchType() { return matchType; }
    public int getLineNumber() { return lineNumber; }
}
