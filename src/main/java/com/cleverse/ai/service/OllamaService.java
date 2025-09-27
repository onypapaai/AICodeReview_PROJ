package com.cleverse.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {
    
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    @Value("${ollama.model:llama3.2}")
    private String modelName;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String analyzeCommit(String commitMessage, String diffContent) {
        try {
            // 프롬프트 생성
            String prompt = createAnalysisPrompt(commitMessage, diffContent);
            
            // 실제 Ollama API 호출 (주석 처리)
            /*
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = ollamaBaseUrl + "/api/generate";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.get("response").asText();
            } else {
                return "AI 분석 요청 중 오류가 발생했습니다. (HTTP " + response.getStatusCode() + ")";
            }
            */
            
            // 모의 AI 분석 결과 반환
            return generateMockAnalysis(commitMessage, diffContent);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
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
    
    private String createAnalysisPrompt(String commitMessage, String diffContent) {
        return String.format("""
            당신은 경험 많은 소프트웨어 개발자이자 코드 리뷰어입니다. 
            다음 커밋에 대해 코드 품질, 잠재적 문제점, 개선사항을 분석해주세요.
            
            커밋 메시지: %s
            
            변경사항 (Diff):
            %s
            
            다음 관점에서 분석해주세요:
            1. 코드 품질 및 가독성
            2. 잠재적 버그나 보안 문제
            3. 성능상 이슈
            4. 코드 스타일 및 컨벤션
            5. 테스트 커버리지 고려사항
            6. 전반적인 개선 제안
            
            분석 결과를 한국어로 명확하고 구체적으로 작성해주세요.
            """, commitMessage, diffContent);
    }
    
    public boolean isOllamaAvailable() {
        // 모의 데이터 사용을 위해 항상 true 반환
        return true;
        
        // 실제 Ollama 서비스 확인 (주석 처리)
        /*
        try {
            String url = ollamaBaseUrl + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
        */
    }
}
