package eu.europa.ec.eudi.signer.rssp.csc.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/gitlab")
public class CopilotGitlabMergeController {

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.api.token}")
    private String gitlabApiToken;

    private final RestTemplate restTemplate;

    public CopilotGitlabMergeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/merge-requests/merge")
    public ResponseEntity<String> mergeMergeRequests(@RequestBody List<Integer> mergeRequestIds, @RequestParam int projectId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", gitlabApiToken);

        for (Integer mergeRequestId : mergeRequestIds) {
            String url = String.format("%s/projects/%d/merge_requests/%d/merge", gitlabApiUrl, projectId, mergeRequestId);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to merge merge request ID: " + mergeRequestId);
            }
        }

        return ResponseEntity.ok("All merge requests merged successfully");
    }

    private static final String FRENCH_MOBILE_NUMBER_REGEXV1 = "^(\\+33|0)[67]\\d{8}$";
    private static final Pattern patternV1 = Pattern.compile(FRENCH_MOBILE_NUMBER_REGEXV1);

    public static boolean isValidFrenchMobileNumberV1(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        Matcher matcher = patternV1.matcher(phoneNumber);
        return matcher.matches();
    }


    private static final String FRENCH_MOBILE_NUMBER_REGEX = "^(\\+33|0033|0)[67](\\d{8}|([ .-]?\\d{2}){4})$";
    private static final Pattern pattern = Pattern.compile(FRENCH_MOBILE_NUMBER_REGEX);

    public static boolean isValidFrenchMobileNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public static void main(String[] args) {
        // Test cases
        System.out.println(isValidFrenchMobileNumber("0612345678")); // true
        System.out.println(isValidFrenchMobileNumber("06.33.44.55.66")); // true
        System.out.println(isValidFrenchMobileNumber("06 33 44 55 66")); // true
        System.out.println(isValidFrenchMobileNumber("06-33-44-55-66")); // true
        System.out.println(isValidFrenchMobileNumber("+33712345678")); // true
        System.out.println(isValidFrenchMobileNumber("0033 6 12 34 56 78")); // true
        System.out.println(isValidFrenchMobileNumber("06 33 445 566")); // false
        System.out.println(isValidFrenchMobileNumber("0-6-3-3-4-4-5-5-6-6")); // false
        System.out.println(isValidFrenchMobileNumber("06.33-44.55 66")); // false
        System.out.println(isValidFrenchMobileNumber(null)); // false
    }

    // Auto completion

    /**
     * return true if mobile number is valid and from germany
     */
    public static boolean isValidGermanMobileNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return phoneNumber.matches("^(\\+49|0)([1-9][0-9]{2}|[1-9][0-9]{4,})$");
    }

    // Compute factorial of x
    public static int factorial(int x) {
        if (x == 0) {
            return 1;
        }
        return x * factorial(x - 1);
    }

    @GetMapping("/project/{projectId}/merge-requests/{mergeRequestId}")
    public ResponseEntity<String> getMergeRequest(@PathVariable int projectId, @PathVariable int mergeRequestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", gitlabApiToken);

        String url = String.format("%s/projects/%d/merge_requests/%d", gitlabApiUrl, projectId, mergeRequestId);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(response.getStatusCode()).body("Failed to get merge request ID: " + mergeRequestId);
        }

        return ResponseEntity.ok(response.getBody());
    }

}