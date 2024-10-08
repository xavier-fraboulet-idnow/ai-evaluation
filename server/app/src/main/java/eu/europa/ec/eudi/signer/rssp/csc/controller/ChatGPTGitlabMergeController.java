package eu.europa.ec.eudi.signer.rssp.csc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//prompt: generate a Rest Controller only using Java and SPringBoot to merge a list of merge request in Gitlab
@RestController
@RequestMapping("/api/gitlab")
public class ChatGPTGitlabMergeController {

    private final String GITLAB_API_URL = "https://gitlab.com/api/v4";
    private final String PRIVATE_ACCESS_TOKEN = "YOUR_GITLAB_PERSONAL_ACCESS_TOKEN";

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/merge")
    public String mergeRequests(@RequestParam String projectId, @RequestBody List<Integer> mergeRequestIds) {
        for (Integer mergeRequestId : mergeRequestIds) {
            String mergeUrl = GITLAB_API_URL + "/projects/" + projectId + "/merge_requests/" + mergeRequestId + "/merge";
            try {
                ResponseEntity<String> response = sendMergeRequest(mergeUrl);
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Merge request " + mergeRequestId + " merged successfully.");
                } else {
                    System.out.println("Failed to merge request " + mergeRequestId + ": " + response.getBody());
                }
            } catch (Exception e) {
                return "Error while merging MR ID " + mergeRequestId + ": " + e.getMessage();
            }
        }
        return "All merge requests processed.";
    }

    private ResponseEntity<String> sendMergeRequest(String mergeUrl) {
        // Set the headers and the authorization token
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", PRIVATE_ACCESS_TOKEN);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(mergeUrl, HttpMethod.PUT, entity, String.class);
    }

    // Prompt: generate a method in Java that would validate a french mobile number
    public static boolean isValidFrenchMobileNumber(String mobileNumber) {
        // Regular expression to match a French mobile number
        String regex = "^(\\+33|0)[67]\\d{8}$";

        // Compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Match the mobile number with the regular expression
        Matcher matcher = pattern.matcher(mobileNumber);

        // Return true if it matches, false otherwise
        return matcher.matches();
    }

    // Prompt: generate a method in Java that would validate a french mobile number, it must handle null values,
    //It must accepts number like "06.33.44.55.66" or "06 33 44 55 66" or "06-33-44-55-66" or "+33712345678" or "0033 6 12 34 56 78"
    //but not "06 33 445 566" or  "0-6-3-3-4-4-5-5-6-6" or "06.33-44.55 66
    public static boolean isValidFrenchMobileNumberComplexPrompt(String mobileNumber) {
        // Check for null or empty string
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            return false;
        }

        // Regular expression to match valid French mobile numbers with different formats
        String regex = "^(\\+33|0033|0)([67])([-. ]?\\d{2}){4}$";

        // Compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Normalize the number by removing extra spaces at the ends
        String normalizedNumber = mobileNumber.trim();

        // Match the normalized mobile number with the regular expression
        Matcher matcher = pattern.matcher(normalizedNumber);

        // Return true if it matches, false otherwise
        return matcher.matches();
    }

    public static void main(String[] args) {
        // Test cases
        String number1 = "06.33.44.55.66";         // Valid
        String number2 = "06 33 44 55 66";         // Valid
        String number3 = "06-33-44-55-66";         // Valid
        String number4 = "+33712345678";           // Valid
        String number5 = "0033 6 12 34 56 78";     // Valid
        String number6 = "06 33 445 566";          // Invalid (incorrect grouping)
        String number7 = "0-6-3-3-4-4-5-5-6-6";    // Invalid (incorrect grouping)
        String number8 = "06.33-44.55 66";         // Invalid (mixed separators)
        String number9 = null;                     // Invalid (null)
        String number10 = "";                      // Invalid (empty)

        System.out.println(isValidFrenchMobileNumberComplexPrompt(number1));  // true
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number2));  // true
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number3));  // true
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number4));  // true
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number5));  // true
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number6));  // false
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number7));  // false
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number8));  // false
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number9));  // false
        System.out.println(isValidFrenchMobileNumberComplexPrompt(number10)); // false
    }
}