// Java Program to check if a String is a valid IPv4 address
    public class ValidateIPv4 {

        public static boolean isValidIPv4(String ip) {
            // Step 1: Separate the given string into an array of strings using the dot as delimiter
            String[] parts = ip.split("\\.");

            // Step 2: Check if there are exactly 4 parts
            if (parts.length != 4) {
                return false;
            }

            // Step 3: Check each part for valid number
            for (String part : parts) {
                try {
                    // Step 4: Convert each part into a number
                    int num = Integer.parseInt(part);

                    // Step 5: Check whether the number lies in between 0 to 255
                    if (num < 0 || num > 255) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // If parsing fails, it's not a valid number
                    return false;
                }
            }

            // If all checks passed, return true
            return true;
        }
    }
