package com.cleverse.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class GitHubService {
    
    private final WebClient webClient;
    private final String GITHUB_API_BASE_URL = "https://api.github.com";
//    private final String GITHUB_USERNAME = "juldae719";
    private final String GITHUB_USERNAME = "onypapaai";
    
    @Value("${github.token:}")
    private String githubToken;
    
    public GitHubService(@Value("${github.token:}") String githubToken) {
        this.githubToken = githubToken;
        
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(GITHUB_API_BASE_URL)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)); // 10MB 버퍼 크기로 증가 (2025-01-27)
        
        // 토큰이 있으면 인증 헤더 추가
        if (githubToken != null && !githubToken.isEmpty()) {
            builder.defaultHeader("Authorization", "token " + githubToken);
            System.out.println("GitHub API 인증 토큰이 설정되었습니다.");
        } else {
            System.out.println("GitHub API 인증 토큰이 없습니다. Rate Limiting이 적용됩니다.");
        }
        
        this.webClient = builder.build();
    }
    
    public List<Map<String, String>> getUserRepositories() {
        try {
            System.out.println("GitHub API 호출 시작: " + GITHUB_USERNAME + "의 저장소 목록");
            
            JsonNode response = webClient.get()
                    .uri("/users/{username}/repos", GITHUB_USERNAME)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.isArray()) {
                List<Map<String, String>> repositories = new ArrayList<>();
                
                for (JsonNode repo : response) {
                    Map<String, String> repoInfo = new java.util.HashMap<>();
                    repoInfo.put("name", repo.get("name").asText());
                    repoInfo.put("fullName", repo.get("full_name").asText());
                    repoInfo.put("url", repo.get("html_url").asText());
                    repoInfo.put("cloneUrl", repo.get("clone_url").asText());
                    repoInfo.put("description", repo.has("description") && !repo.get("description").isNull() 
                            ? repo.get("description").asText() : "설명 없음");
                    repoInfo.put("language", repo.has("language") && !repo.get("language").isNull() 
                            ? repo.get("language").asText() : "Unknown");
                    repoInfo.put("stars", String.valueOf(repo.get("stargazers_count").asInt()));
                    repoInfo.put("forks", String.valueOf(repo.get("forks_count").asInt()));
                    repoInfo.put("updatedAt", repo.get("updated_at").asText());
                    repoInfo.put("private", String.valueOf(repo.get("private").asBoolean()));
                    
                    repositories.add(repoInfo);
                    System.out.println("저장소 추가: " + repo.get("name").asText());
                }
                
                System.out.println("총 " + repositories.size() + "개의 저장소를 가져왔습니다.");
                return repositories;
            }
            
        } catch (WebClientResponseException e) {
            System.err.println("GitHub API 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("GitHub API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 오류 발생 시 기본 저장소 반환
        List<Map<String, String>> defaultRepos = new ArrayList<>();
        Map<String, String> defaultRepo = new java.util.HashMap<>();
        defaultRepo.put("name", "AICodeReview_PROJ");
        defaultRepo.put("fullName", "onypapaai/AICodeReview_PROJ");
        defaultRepo.put("url", "https://github.com/onypapaai/AICodeReview_PROJ");
        defaultRepo.put("cloneUrl", "https://github.com/onypapaai/AICodeReview_PROJ.git");
        defaultRepo.put("description", "AI Code Review Application");
        defaultRepo.put("language", "Java");
        defaultRepo.put("stars", "0");
        defaultRepo.put("forks", "0");
        defaultRepo.put("updatedAt", "2025-04-28T09:50:00Z");
        defaultRepo.put("private", "false");
        defaultRepos.add(defaultRepo);
//        defaultRepo.put("name", "AICodeReview");
//        defaultRepo.put("fullName", "juldae719/AICodeReview");
//        defaultRepo.put("url", "https://github.com/juldae719/AICodeReview");
//        defaultRepo.put("cloneUrl", "https://github.com/juldae719/AICodeReview.git");
//        defaultRepo.put("description", "AI Code Review Application");
//        defaultRepo.put("language", "Java");
//        defaultRepo.put("stars", "0");
//        defaultRepo.put("forks", "0");
//        defaultRepo.put("updatedAt", "2025-04-28T09:50:00Z");
//        defaultRepo.put("private", "false");
//        defaultRepos.add(defaultRepo);
        
        return defaultRepos;
    }
    
    /**
     * GitHub API를 통해 원격 저장소의 브랜치 목록을 가져옵니다.
     * 로컬 PC 용량 문제를 해결하기 위해 원격에서 직접 검색합니다.
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @return 브랜치 목록
     */
    public List<Map<String, String>> getRepositoryBranches(String owner, String repo) {
        try {
            System.out.println("GitHub API 호출 시작: " + owner + "/" + repo + "의 브랜치 목록");
            
            JsonNode response = webClient.get()
                    .uri("/repos/{owner}/{repo}/branches", owner, repo)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.isArray()) {
                List<Map<String, String>> branches = new ArrayList<>();
                
                for (JsonNode branch : response) {
                    Map<String, String> branchInfo = new java.util.HashMap<>();
                    branchInfo.put("name", branch.get("name").asText());
                    branchInfo.put("sha", branch.get("commit").get("sha").asText());
                    branchInfo.put("url", branch.get("commit").get("url").asText());
                    branchInfo.put("protected", String.valueOf(branch.get("protected").asBoolean()));
                    
                    branches.add(branchInfo);
                    System.out.println("브랜치 추가: " + branch.get("name").asText());
                }
                
                System.out.println("총 " + branches.size() + "개의 브랜치를 가져왔습니다.");
                return branches;
            }
            
        } catch (WebClientResponseException e) {
            System.err.println("GitHub API 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("GitHub API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new ArrayList<>();
    }
    
    /**
     * GitHub API를 통해 원격 저장소에서 브랜치를 검색합니다.
     * 브랜치명, 커밋 메시지, 소스 코드 내용에서 검색이 가능하도록 개선되었습니다.
     * 검색 성능 최적화 및 로깅 개선 (2025-01-27)
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @param searchText 검색할 텍스트
     * @return 검색 결과
     */
    public List<Map<String, String>> searchBranches(String owner, String repo, String searchText) {
        try {
            System.out.println("=== GitHub API 브랜치 검색 시작 ===");
            System.out.println("저장소: " + owner + "/" + repo);
            System.out.println("검색어: '" + searchText + "'");
            System.out.println("검색 시간: " + java.time.LocalDateTime.now());
            
            List<Map<String, String>> allBranches = getRepositoryBranches(owner, repo);
            List<Map<String, String>> searchResults = new ArrayList<>();
            
            System.out.println("총 " + allBranches.size() + "개 브랜치에서 검색 시작");
            
            int branchCount = 0;
            for (Map<String, String> branch : allBranches) {
                branchCount++;
                String branchName = branch.get("name");
                System.out.println("[" + branchCount + "/" + allBranches.size() + "] 브랜치 검색: " + branchName);
                
                // 1. 브랜치명 자체에서 검색 (대소문자 구분 없음)
                if (branchName != null && branchName.toLowerCase().contains(searchText.toLowerCase())) {
                    Map<String, String> result = new java.util.HashMap<>();
                    result.put("branchName", branchName);
                    result.put("sha", branch.get("sha"));
                    result.put("url", branch.get("url"));
                    result.put("protected", branch.get("protected"));
                    result.put("matchType", "branch_name");
                    result.put("matchedContent", branchName);
                    result.put("fileName", "");
                    result.put("lineNumber", "0");
                    
                    searchResults.add(result);
                    System.out.println("✓ 브랜치명 매치: " + branchName);
                }
                
                // 2. 커밋 메시지에서 검색
                int commitMatches = searchResults.size();
                searchCommitsInBranch(owner, repo, branchName, searchText, searchResults);
                int newCommitMatches = searchResults.size() - commitMatches;
                if (newCommitMatches > 0) {
                    System.out.println("✓ 커밋 메시지에서 " + newCommitMatches + "개 매치");
                }
                
                // 3. 소스 코드 내용에서 검색 (GitHub Search API 사용으로 안전한 검색)
                int sourceMatches = searchResults.size();
                searchSourceInBranchSafe(owner, repo, branchName, searchText, searchResults);
                int newSourceMatches = searchResults.size() - sourceMatches;
                if (newSourceMatches > 0) {
                    System.out.println("✓ 소스 코드에서 " + newSourceMatches + "개 매치");
                }
                
                // 전체 매치 수가 너무 많으면 조기 종료
                if (searchResults.size() > 100) {
                    System.out.println("검색 결과가 100개를 초과하여 조기 종료");
                    break;
                }
            }
            
            System.out.println("=== 브랜치 검색 완료 ===");
            System.out.println("원본 결과 개수: " + searchResults.size());
            
            // 검색 결과 최적화 (중복 제거 및 정렬)
            List<Map<String, String>> optimizedResults = optimizeSearchResults(searchResults);
            System.out.println("최적화된 결과 개수: " + optimizedResults.size());
            System.out.println("검색 완료 시간: " + java.time.LocalDateTime.now());
            
            return optimizedResults;
            
        } catch (Exception e) {
            System.err.println("브랜치 검색 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * GitHub API를 통해 특정 브랜치의 커밋 메시지에서 검색합니다.
     * Rate Limiting 방지를 위한 최적화 적용 (2025-01-27)
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @param branchName 브랜치명
     * @param searchText 검색할 텍스트
     * @param searchResults 검색 결과를 저장할 리스트
     */
    private void searchCommitsInBranch(String owner, String repo, String branchName, String searchText, List<Map<String, String>> searchResults) {
        try {
            System.out.println("커밋 메시지 검색 시작: " + branchName + " 브랜치에서 '" + searchText + "' 검색");
            
            // Rate Limiting 방지를 위한 지연 추가
            Thread.sleep(100);
            
            // GitHub API를 통해 커밋 목록 가져오기 (최대 10개로 제한)
            JsonNode response = webClient.get()
                    .uri("/repos/{owner}/{repo}/commits?sha={branch}&per_page=10", owner, repo, branchName)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.isArray()) {
                for (JsonNode commit : response) {
                    String commitMessage = commit.get("commit").get("message").asText();
                    String commitSha = commit.get("sha").asText();
                    
                    if (commitMessage != null && commitMessage.toLowerCase().contains(searchText.toLowerCase())) {
                        Map<String, String> result = new java.util.HashMap<>();
                        result.put("branchName", branchName);
                        result.put("sha", commitSha);
                        result.put("url", commit.get("html_url").asText());
                        result.put("protected", "false");
                        result.put("matchType", "commit");
                        result.put("matchedContent", commitMessage);
                        result.put("fileName", "");
                        result.put("lineNumber", "0");
                        
                        searchResults.add(result);
                        System.out.println("커밋 메시지 매치: " + branchName + " - " + commitMessage.substring(0, Math.min(50, commitMessage.length())));
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("커밋 메시지 검색 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * GitHub Search API를 사용한 안전한 소스 코드 검색
     * DataBufferLimitException을 완전히 방지하는 안전한 검색 방식 (2025-01-27)
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @param branchName 브랜치명
     * @param searchText 검색할 텍스트
     * @param searchResults 검색 결과를 저장할 리스트
     */
    private void searchSourceInBranchSafe(String owner, String repo, String branchName, String searchText, List<Map<String, String>> searchResults) {
        try {
            System.out.println("안전한 소스 코드 검색 시작: " + branchName + " 브랜치에서 '" + searchText + "' 검색");
            
            // GitHub Search API를 사용하여 코드 검색 (DataBufferLimitException 방지)
            String query = searchText + " repo:" + owner + "/" + repo;
            
            // Rate Limiting 방지를 위한 지연
            Thread.sleep(500);
            
            JsonNode response = webClient.get()
                    .uri("/search/code?q={query}", query)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.has("items")) {
                JsonNode items = response.get("items");
                int matchCount = 0;
                int maxMatches = 8; // 검색 결과를 8개로 제한
                
                for (JsonNode item : items) {
                    if (matchCount >= maxMatches) break;
                    
                    String fileName = item.get("name").asText();
                    String filePath = item.get("path").asText();
                    String htmlUrl = item.get("html_url").asText();
                    String sha = item.get("sha").asText();
                    
                    // 소스 파일만 검색 (Java, JSP, XML)
                    if (isTextFile(fileName)) {
                        try {
                            // Rate Limiting 방지를 위한 추가 지연
            Thread.sleep(200);
                            
                            // 파일 내용 가져오기
                            JsonNode fileContent = webClient.get()
                                    .uri("/repos/{owner}/{repo}/contents/{path}?ref={branch}", owner, repo, filePath, branchName)
                                    .retrieve()
                                    .bodyToMono(JsonNode.class)
                                    .block();
                            
                            if (fileContent != null && fileContent.has("content")) {
                                // GitHub API 응답에 따른 안전한 디코딩
                                try {
                                    String encoding = fileContent.has("encoding") ? fileContent.get("encoding").asText() : "base64";
                                    String content;
                                    
                                    if ("base64".equals(encoding)) {
                                        String base64Content = fileContent.get("content").asText();
                                        
                                        // Base64 패딩 추가 (필요한 경우)
                                        while (base64Content.length() % 4 != 0) {
                                            base64Content += "=";
                                        }
                                        
                                        content = new String(java.util.Base64.getDecoder().decode(base64Content));
                                    } else {
                                        // base64가 아닌 경우 직접 텍스트로 처리
                                        content = fileContent.get("content").asText();
                                    }
                                    
                                    // 파일 내용에서 검색 (대소문자 구분 없음)
                                    String[] lines = content.split("\n");
                                    
                                    for (int i = 0; i < lines.length; i++) {
                                        if (lines[i].toLowerCase().contains(searchText.toLowerCase())) {
                                            Map<String, String> result = new java.util.HashMap<>();
                                            result.put("branchName", branchName);
                                            result.put("sha", sha);
                                            result.put("url", htmlUrl);
                                            result.put("protected", "false");
                                            result.put("matchType", "source");
                                            result.put("matchedContent", lines[i].trim());
                                            result.put("fileName", filePath);
                                            result.put("lineNumber", String.valueOf(i + 1));
                                            
                                            searchResults.add(result);
                                            matchCount++;
                                            
                                            System.out.println("안전한 소스 코드 매치: " + branchName + " - " + filePath + ":" + (i + 1) + " - " + lines[i].trim());
                                            
                                            if (matchCount >= maxMatches) break;
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    System.err.println("Base64 디코딩 오류: " + filePath + " - " + e.getMessage());
                                    continue;
                                }
                            }
                        } catch (WebClientResponseException e) {
                            if (e.getStatusCode().value() == 403) {
                                System.err.println("Rate Limiting 발생. 잠시 대기 후 재시도...");
                                Thread.sleep(2000); // 2초 대기
                            } else {
                                System.err.println("파일 검색 중 API 오류: " + filePath + " - " + e.getStatusCode());
                            }
                        } catch (Exception e) {
                            System.err.println("파일 검색 중 오류: " + filePath + " - " + e.getMessage());
                            continue;
                        }
                    }
                }
                
                System.out.println("안전한 소스 코드 검색 완료: " + matchCount + "개 매치");
            }
            
        } catch (Exception e) {
            System.err.println("안전한 소스 코드 검색 중 오류: " + e.getMessage());
            // GitHub Search API가 실패하면 폴백 방식 사용
            searchSourceInBranchFallback(owner, repo, branchName, searchText, searchResults);
        }
        
        // GitHub Search API 결과가 없으면 직접 파일 검색 시도
        if (searchResults.isEmpty()) {
            System.out.println("GitHub Search API 결과가 없어 직접 파일 검색을 시도합니다.");
            searchDirectFiles(owner, repo, branchName, searchText, searchResults);
        }
    }
    
    /**
     * 소스 파일에서 직접 검색 (Java, JSP, XML 파일만 - DataBufferLimitException 방지를 위한 안전한 방식)
     */
    private void searchDirectFiles(String owner, String repo, String branchName, String searchText, List<Map<String, String>> searchResults) {
        try {
            System.out.println("소스 파일 검색 시작: " + branchName + " 브랜치에서 '" + searchText + "' 검색 (Java, JSP, XML)");
            
            // GitHub API를 통해 파일 트리 가져오기 (recursive=1로 모든 파일 검색)
            JsonNode response = webClient.get()
                    .uri("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1", owner, repo, branchName)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.has("tree")) {
                JsonNode tree = response.get("tree");
                int matchCount = 0;
                int maxMatches = 50; // 검색 결과를 50개로 제한
                int fileCount = 0;
                int totalFiles = tree.size();
                
                System.out.println("전체 파일 수: " + totalFiles + "개 (모든 소스 파일 검색)");
                
                for (JsonNode file : tree) {
                    if (matchCount >= maxMatches) {
                        System.out.println("매치 수 제한에 도달: " + maxMatches + "개");
                        break;
                    }
                    
                    String fileName = file.get("path").asText();
                    String fileType = file.get("type").asText();
                    long fileSize = file.has("size") ? file.get("size").asLong() : 0;
                    
                    // 소스 파일만 검색 (Java, JSP, XML)
                    if ("blob".equals(fileType) && fileSize < 1024 * 1024 && isTextFile(fileName)) { // 1MB 미만 소스 파일만
                        fileCount++;
                        System.out.println("검색 중인 파일 [" + fileCount + "]: " + fileName + " (크기: " + fileSize + " bytes)");
                        try {
                            Thread.sleep(200); // Rate Limiting 방지
                            
                            JsonNode fileContent = webClient.get()
                                    .uri("/repos/{owner}/{repo}/contents/{path}?ref={branch}", owner, repo, fileName, branchName)
                                    .retrieve()
                                    .bodyToMono(JsonNode.class)
                                    .block();
                            
                            if (fileContent != null && fileContent.has("content")) {
                                try {
                                    String base64Content = fileContent.get("content").asText();
                                    System.out.println("파일 내용 디버깅: " + fileName + " - Base64 길이: " + base64Content.length());
                                    
                                    // Base64 패딩 추가 (필요한 경우)
                                    while (base64Content.length() % 4 != 0) {
                                        base64Content += "=";
                                    }
                                    
                                    String content = new String(java.util.Base64.getDecoder().decode(base64Content));
                                String[] lines = content.split("\n");
                                    
                                for (int i = 0; i < lines.length; i++) {
                                    if (lines[i].toLowerCase().contains(searchText.toLowerCase())) {
                                        Map<String, String> result = new java.util.HashMap<>();
                                        result.put("branchName", branchName);
                                        result.put("sha", fileContent.get("sha").asText());
                                        result.put("url", fileContent.get("html_url").asText());
                                        result.put("protected", "false");
                                        result.put("matchType", "source");
                                        result.put("matchedContent", lines[i].trim());
                                        result.put("fileName", fileName);
                                        result.put("lineNumber", String.valueOf(i + 1));
                                        
                                        searchResults.add(result);
                                            matchCount++;
                                            
                                            System.out.println("전체 파일 검색 매치: " + branchName + " - " + fileName + ":" + (i + 1) + " - " + lines[i].trim());
                                            
                                            if (matchCount >= maxMatches) break;
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    System.err.println("Base64 디코딩 오류: " + fileName + " - " + e.getMessage());
                                    continue;
                                }
                            }
                        } catch (WebClientResponseException e) {
                            if (e.getStatusCode().value() == 403) {
                                System.err.println("Rate Limiting 발생. 잠시 대기...");
                                Thread.sleep(2000);
                            } else {
                                System.err.println("파일 검색 중 API 오류: " + fileName + " - " + e.getStatusCode());
                            }
                        } catch (Exception e) {
                            System.err.println("파일 검색 중 오류: " + fileName + " - " + e.getMessage());
                            continue;
                        }
                    }
                }
                
                System.out.println("소스 파일 검색 완료: " + fileCount + "개 파일 검색, " + matchCount + "개 매치");
            }
            
        } catch (Exception e) {
            System.err.println("소스 파일 검색 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 소스 파일인지 확인 (Java, JSP, XML 파일만 검색 - JS 제외)
     */
    private boolean isTextFile(String fileName) {
        String[] sourceExtensions = {
            ".java", // Java 소스 파일
            ".jsp", ".jspx", ".jspf", // JSP 파일
            ".xml" // XML 파일
        };
        
        String lowerFileName = fileName.toLowerCase();
        for (String ext : sourceExtensions) {
            if (lowerFileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * GitHub API를 통해 특정 브랜치의 소스 코드 내용에서 검색합니다.
     * DataBufferLimitException 완전 방지를 위한 안전한 검색 (2025-01-27)
     * 
     * @param owner 저장소 소유자
     * @param repo 저장소 이름
     * @param branchName 브랜치명
     * @param searchText 검색할 텍스트
     * @param searchResults 검색 결과를 저장할 리스트
     */
    private void searchSourceInBranch(String owner, String repo, String branchName, String searchText, List<Map<String, String>> searchResults) {
        try {
            System.out.println("소스 코드 검색 시작: " + branchName + " 브랜치에서 '" + searchText + "' 검색");
            
            // DataBufferLimitException을 완전히 방지하기 위해 GitHub Search API 사용
            String query = searchText + " repo:" + owner + "/" + repo;
            
            // Rate Limiting 방지를 위한 지연 추가
            Thread.sleep(500);
            
            JsonNode response = webClient.get()
                    .uri("/search/code?q={query}", query)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.has("items")) {
                JsonNode items = response.get("items");
                int matchCount = 0;
                int maxMatches = 5; // 검색 결과를 5개로 제한하여 안전성 확보
                
                for (JsonNode item : items) {
                    if (matchCount >= maxMatches) break;
                    
                    String fileName = item.get("name").asText();
                    String filePath = item.get("path").asText();
                    String htmlUrl = item.get("html_url").asText();
                    String sha = item.get("sha").asText();
                    
                    // 소스 파일만 검색
                    if (isSourceFile(fileName)) {
                        try {
                            // Rate Limiting 방지를 위한 추가 지연
                            Thread.sleep(300);
                            
                            // 파일 내용 가져오기
                            JsonNode fileContent = webClient.get()
                                    .uri("/repos/{owner}/{repo}/contents/{path}?ref={branch}", owner, repo, filePath, branchName)
                                    .retrieve()
                                    .bodyToMono(JsonNode.class)
                                    .block();
                            
                            if (fileContent != null && fileContent.has("content")) {
                                // GitHub API 응답에 따른 안전한 디코딩
                                try {
                                    String encoding = fileContent.has("encoding") ? fileContent.get("encoding").asText() : "base64";
                                    String content;
                                    
                                    if ("base64".equals(encoding)) {
                                        String base64Content = fileContent.get("content").asText();
                                        
                                        // Base64 패딩 추가 (필요한 경우)
                                        while (base64Content.length() % 4 != 0) {
                                            base64Content += "=";
                                        }
                                        
                                        content = new String(java.util.Base64.getDecoder().decode(base64Content));
                                    } else {
                                        // base64가 아닌 경우 직접 텍스트로 처리
                                        content = fileContent.get("content").asText();
                                    }
                                    
                                    // 파일 내용에서 검색 (대소문자 구분 없음)
                                    String[] lines = content.split("\n");
                                    
                                    for (int i = 0; i < lines.length; i++) {
                                        if (lines[i].toLowerCase().contains(searchText.toLowerCase())) {
                                            Map<String, String> result = new java.util.HashMap<>();
                                            result.put("branchName", branchName);
                                            result.put("sha", sha);
                                            result.put("url", htmlUrl);
                                            result.put("protected", "false");
                                            result.put("matchType", "source");
                                            result.put("matchedContent", lines[i].trim());
                                            result.put("fileName", filePath);
                                            result.put("lineNumber", String.valueOf(i + 1));
                                            
                                            searchResults.add(result);
                                            matchCount++;
                                            
                                            System.out.println("소스 코드 매치: " + branchName + " - " + filePath + ":" + (i + 1) + " - " + lines[i].trim());
                                            
                                            if (matchCount >= maxMatches) break;
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    System.err.println("Base64 디코딩 오류: " + filePath + " - " + e.getMessage());
                                    continue;
                                }
                            }
                        } catch (WebClientResponseException e) {
                            if (e.getStatusCode().value() == 403) {
                                System.err.println("Rate Limiting 발생. 잠시 대기 후 재시도...");
                                Thread.sleep(3000); // 3초 대기
                            } else {
                                System.err.println("파일 검색 중 API 오류: " + filePath + " - " + e.getStatusCode());
                            }
                        } catch (Exception e) {
                            System.err.println("파일 검색 중 오류: " + filePath + " - " + e.getMessage());
                            continue;
                        }
                    }
                }
                
                System.out.println("소스 코드 검색 완료: " + matchCount + "개 매치");
            }
            
        } catch (Exception e) {
            System.err.println("소스 코드 검색 중 오류: " + e.getMessage());
            // GitHub Search API가 실패하면 폴백 방식 사용
            searchSourceInBranchFallback(owner, repo, branchName, searchText, searchResults);
        }
    }
    
    /**
     * GitHub Search API 실패 시 사용하는 폴백 검색 방법
     * DataBufferLimitException 완전 방지를 위한 안전한 검색 (2025-01-27)
     */
    private void searchSourceInBranchFallback(String owner, String repo, String branchName, String searchText, List<Map<String, String>> searchResults) {
        try {
            System.out.println("폴백 소스 코드 검색 시작: " + branchName + " 브랜치에서 '" + searchText + "' 검색");
            
            // 주요 소스 파일들만 직접 검색 (DataBufferLimitException 방지)
            String[] commonFiles = {
                "src/main/java/com/cleverse/ai/controller/GitController.java",
                "src/main/java/com/cleverse/ai/service/GitHubService.java",
                "src/main/java/com/cleverse/ai/CleverseAiCodeReviewApplication.java",
                "src/main/resources/application.properties",
                "pom.xml"
            };
            
            int matchCount = 0;
            int maxMatches = 5; // 폴백에서는 더 적은 수로 제한
            
            for (String filePath : commonFiles) {
                if (matchCount >= maxMatches) break;
                
                try {
                    // Rate Limiting 방지를 위한 지연
                    Thread.sleep(400);
                    
                    // 파일 내용 가져오기
                    JsonNode fileContent = webClient.get()
                            .uri("/repos/{owner}/{repo}/contents/{path}?ref={branch}", owner, repo, filePath, branchName)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .block();
                    
                    if (fileContent != null && fileContent.has("content")) {
                        // Base64 디코딩 (안전한 디코딩)
                        try {
                            String content = new String(java.util.Base64.getDecoder().decode(fileContent.get("content").asText()));
                            
                            // 파일 내용에서 검색
                            String[] lines = content.split("\n");
                            
                            for (int i = 0; i < lines.length; i++) {
                                if (lines[i].toLowerCase().contains(searchText.toLowerCase())) {
                                    Map<String, String> result = new java.util.HashMap<>();
                                    result.put("branchName", branchName);
                                    result.put("sha", fileContent.get("sha").asText());
                                    result.put("url", fileContent.get("html_url").asText());
                                    result.put("protected", "false");
                                    result.put("matchType", "source");
                                    result.put("matchedContent", lines[i].trim());
                                    result.put("fileName", filePath);
                                    result.put("lineNumber", String.valueOf(i + 1));
                                    
                                    searchResults.add(result);
                                    matchCount++;
                                    
                                    System.out.println("폴백 소스 코드 매치: " + branchName + " - " + filePath + ":" + (i + 1) + " - " + lines[i].trim());
                                    
                                    if (matchCount >= maxMatches) break;
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            System.err.println("폴백 Base64 디코딩 오류: " + filePath + " - " + e.getMessage());
                            continue;
                        }
                    }
                } catch (WebClientResponseException e) {
                    if (e.getStatusCode().value() == 403) {
                        System.err.println("폴백 Rate Limiting 발생. 잠시 대기...");
                        Thread.sleep(2000);
                    } else {
                        System.err.println("폴백 파일 검색 중 API 오류: " + filePath + " - " + e.getStatusCode());
                    }
                } catch (Exception e) {
                    System.err.println("폴백 파일 검색 중 오류: " + filePath + " - " + e.getMessage());
                    continue;
                }
            }
            
            System.out.println("폴백 소스 코드 검색 완료: " + matchCount + "개 매치");
            
        } catch (Exception e) {
            System.err.println("폴백 소스 코드 검색 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 소스 파일인지 확인합니다.
     * 더 많은 파일 형식을 지원하도록 개선 (2025-01-27)
     * 
     * @param fileName 파일명
     * @return 소스 파일 여부
     */
    private boolean isSourceFile(String fileName) {
        return isTextFile(fileName);
    }
    
    /**
     * 검색 결과를 정렬하고 중복을 제거합니다.
     * 검색 품질 향상을 위한 유틸리티 메서드 (2025-01-27)
     * 
     * @param searchResults 검색 결과 리스트
     * @return 정렬되고 중복이 제거된 검색 결과
     */
    private List<Map<String, String>> optimizeSearchResults(List<Map<String, String>> searchResults) {
        // 중복 제거 (같은 파일의 같은 라인은 하나만 유지)
        Map<String, Map<String, String>> uniqueResults = new java.util.LinkedHashMap<>();
        
        for (Map<String, String> result : searchResults) {
            String key = result.get("branchName") + ":" + result.get("fileName") + ":" + result.get("lineNumber");
            if (!uniqueResults.containsKey(key)) {
                uniqueResults.put(key, result);
            }
        }
        
        List<Map<String, String>> optimizedResults = new ArrayList<>(uniqueResults.values());
        
        // 매치 타입별로 정렬 (branch_name > commit > source)
        optimizedResults.sort((a, b) -> {
            String typeA = a.get("matchType");
            String typeB = b.get("matchType");
            
            int priorityA = getMatchTypePriority(typeA);
            int priorityB = getMatchTypePriority(typeB);
            
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            
            // 같은 타입이면 브랜치명으로 정렬
            return a.get("branchName").compareTo(b.get("branchName"));
        });
        
        return optimizedResults;
    }
    
    /**
     * 매치 타입의 우선순위를 반환합니다.
     * 
     * @param matchType 매치 타입
     * @return 우선순위 (낮을수록 높은 우선순위)
     */
    private int getMatchTypePriority(String matchType) {
        switch (matchType) {
            case "branch_name": return 1;
            case "commit": return 2;
            case "source": return 3;
            default: return 4;
        }
    }
}
