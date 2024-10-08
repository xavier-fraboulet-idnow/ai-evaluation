package eu.europa.ec.eudi.signer.rssp.csc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/gitlab")
public class CodiumGitlabMergeController {

    private final RestTemplate restTemplate;
    private final String gitlabApiUrl = "https://gitlab.com/api/v4";
    private final String privateToken = "YOUR_PRIVATE_TOKEN"; // Replace with your GitLab private token

    @Autowired
    public CodiumGitlabMergeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @PostMapping("/merge-requests/merge")
    public ResponseEntity<String> mergeMergeRequests(@RequestBody List<Integer> mergeRequestIds, @RequestParam int projectId) {
        for (Integer mergeRequestId : mergeRequestIds) {
            String url = String.format("%s/projects/%d/merge_requests/%d/merge", gitlabApiUrl, projectId, mergeRequestId);

            try {
                restTemplate.postForEntity(url, null, String.class);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to merge merge request ID: " + mergeRequestId);
            }
        }
        return ResponseEntity.ok("All merge requests merged successfully.");
    }

    public static boolean isValidFrenchMobileNumber(String number) {
        // Regular expression for French mobile numbers
        String regex = "^0[67]\\d{8}$";

        // Check if the number matches the regex
        return number.matches(regex);
    }

    // MEthod to validate a german phone number, it accepts space, dots and dash, and handles null
    public static boolean isValidGermanMobileNumber(String number) {
        if (number == null) {
            return false;
        }

        // Remove any whitespace from the number
        String cleanedNumber = number.replaceAll("\\s", "");

        // Regular expression for German mobile numbers
        String regex = "^\\+49\\d{10}$";

        // Check if the number matches the regex
        return cleanedNumber.matches(regex);
    }

}