package eu.europa.ec.eudi.signer.rssp.csc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/gitlab")
public class AmazonMergeController {

    private final RestTemplate restTemplate;
    private final String gitlabApiUrl;
    private final String privateToken;

    @Autowired
    public AmazonMergeController(RestTemplate restTemplate,
                                 @Value("${gitlab.api.url}") String gitlabApiUrl,
                                 @Value("${gitlab.private.token}") String privateToken) {
        this.restTemplate = restTemplate;
        this.gitlabApiUrl = gitlabApiUrl;
        this.privateToken = privateToken;
    }

    @PostMapping("/merge-requests/merge")
    public ResponseEntity<String> mergeMergeRequests(@RequestBody List<Integer> mergeRequestIds,
                                                     @RequestParam int projectId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", privateToken);

        for (Integer mergeRequestId : mergeRequestIds) {
            String url = String.format("%s/projects/%d/merge_requests/%d/merge", gitlabApiUrl, projectId, mergeRequestId);

            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        new HttpEntity<>(null, headers),
                        String.class
                );

                if (response.getStatusCode() != HttpStatus.OK) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to merge merge request ID: " + mergeRequestId);
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error merging merge request ID: " + mergeRequestId + ". Error: " + e.getMessage());
            }
        }

        return ResponseEntity.ok("All merge requests merged successfully.");
    }


    private static final String FRENCH_MOBILE_REGEX = "^(?:(?:\\+|00)33|0)\\s*[67](?:[\\s.-]*\\d{2}){4}$";
    private static final Pattern pattern = Pattern.compile(FRENCH_MOBILE_REGEX);

    /**
     * Validates a French mobile phone number.
     *
     * @param phoneNumber The phone number to validate
     * @return true if the number is a valid French mobile number, false otherwise
     */
    public static boolean isValidFrenchMobileNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        // Remove any whitespace from the number
        String cleanedNumber = phoneNumber.replaceAll("\\s", "");

        // Check if the number matches the pattern
        Matcher matcher = pattern.matcher(cleanedNumber);
        if (!matcher.matches()) {
            return false;
        }

        return true;
    }


    private static final String FRENCH_MOBILE_REGEXV2 =
            "^(?:(?:\\+|00)33|0)\\s*[67](?:(?:[\\s.-]*\\d{2}){4}|\\d{2}(?:[\\s.-]*\\d{2}){3})$";

    private static final Pattern patternV2 = Pattern.compile(FRENCH_MOBILE_REGEXV2);

    public static boolean isValidFrenchMobileNumberV2(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        // Remove all whitespace from the number
        String normalizedNumber = phoneNumber.replaceAll("\\s", "");

        // Match the normalized mobile number with the regular expression
        Matcher matcher = patternV2.matcher(normalizedNumber);

        // Return true if it matches, false otherwise
        return matcher.matches();
    }

    public static void main(String[] args) {
        // Test cases
        String[] testNumbers = {
                "06.33.44.55.66",         // Valid
                "06 33 44 55 66",         // Valid
                "06-33-44-55-66",         // Valid
                "+33712345678",           // Valid
                "0033 6 12 34 56 78",     // Valid
                "06 33 445 566",          // Invalid (incorrect grouping)
                "0-6-3-3-4-4-5-5-6-6",    // Invalid (incorrect grouping)
                "06.33-44.55 66",         // Invalid (mixed separators)
                null,                     // Invalid (null)
                ""                        // Invalid (empty)
        };

        for (String number : testNumbers) {
            System.out.println(number + " is " + (isValidFrenchMobileNumberV2(number) ? "valid" : "invalid"));
        }
    }

}