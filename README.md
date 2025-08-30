# Shamir's Secret Sharing - Error Detection

This project implements an error detection algorithm for Shamir's Secret Sharing scheme. It can identify corrupted shares and recover the correct secret even when some shares contain errors.

## Overview

Shamir's Secret Sharing is a cryptographic algorithm that divides a secret into multiple shares, where a minimum threshold of shares (k) is required to reconstruct the original secret. This implementation adds error detection capabilities by analyzing the frequency of secrets computed from different combinations of shares.

## Features

- **Multi-base Support**: Handles shares encoded in different number bases (binary, decimal, hexadecimal, etc.)
- **Error Detection**: Identifies corrupted shares by analyzing which shares consistently produce incorrect results
- **Statistical Analysis**: Provides detailed statistics on share reliability and error rates
- **JSON Input Format**: Easy-to-use JSON configuration for test cases

## Algorithm

The error detection works by:

1. **Parsing Shares**: Converting shares from various bases to decimal values
2. **Combination Generation**: Creating all possible combinations of k shares from n total shares
3. **Secret Calculation**: Using Lagrange interpolation to compute the secret for each combination
4. **Frequency Analysis**: Identifying the most frequently occurring secret as the correct one
5. **Error Identification**: Analyzing which shares appear in incorrect combinations to identify corrupted shares

## Project Structure

```
├── ShamirErrorDetectionSimple.java  # Main implementation
├── test.json                        # Test case 1: No errors detected
├── test1.json                       # Test case 2: Error detected in share 2
├── result.md                        # Analysis results for test.json
├── result1.md                       # Analysis results for test1.json
└── README.md                        # This file
```

## Input Format

The program expects a JSON file with the following structure:

```json
{
    "keys": {
        "n": 4,     // Total number of shares
        "k": 3      // Minimum shares needed to reconstruct secret
    },
    "1": {
        "base": "10",   // Number base for this share
        "value": "4"    // Share value in the specified base
    },
    "2": {
        "base": "2",
        "value": "111"
    },
    // ... more shares
}
```

## Usage

### Compilation and Execution

```bash
# Compile the Java program
javac ShamirErrorDetectionSimple.java

# Run with default test file (test.json)
java ShamirErrorDetectionSimple

# To test with different files, modify the filename in main() method
```

### Example Test Cases

#### Test Case 1 (test.json)
- **Configuration**: n=4, k=3 (4 shares, need 3 to reconstruct)
- **Shares**: Mix of decimal and binary bases
- **Result**: No corrupted shares detected, secret = 3
- **Analysis**: All shares show 0% error rate

#### Test Case 2 (test1.json)
- **Configuration**: n=10, k=7 (10 shares, need 7 to reconstruct)
- **Shares**: Various bases (base 3, 6, 8, 15, 16)
- **Result**: Share 2 identified as corrupted (100% error rate)
- **Correct Secret**: 79836264049851

## Output Analysis

The program provides comprehensive analysis including:

### Secret Frequency Analysis
Lists all discovered secrets and their frequency of occurrence across different share combinations.

### Share Reliability Analysis
For each share, shows:
- **Total**: Number of combinations the share participated in
- **Correct**: Number of times it contributed to the correct secret
- **Incorrect**: Number of times it contributed to an incorrect secret
- **Correct %**: Percentage of correct contributions
- **Error Rate**: Percentage of incorrect contributions

### Error Detection Results
- Identifies shares with high error rates (>50%) as potentially corrupted
- Provides the most likely correct secret based on frequency analysis

## Key Classes

### `Share`
Represents a single share with its ID, base, original value, and converted decimal value.

### `Point`
Represents a point (x, y) used in Lagrange interpolation for secret reconstruction.

### `ShareAnalysis`
Tracks statistics for each share including correct/incorrect usage counts and error rates.

## Mathematical Foundation

The implementation uses:
- **Lagrange Interpolation**: For polynomial reconstruction from share points
- **Base Conversion**: Supports arbitrary number bases up to base 36
- **Statistical Analysis**: Frequency-based error detection
- **BigInteger/BigDecimal**: For handling large numbers with precision

## Limitations

- Currently designed for detecting single corrupted shares
- Requires a simple JSON parser (included in implementation)
- Error detection effectiveness depends on having sufficient share combinations

## Example Results

### No Errors Case (test.json)
```
Secret frequency analysis:
  1. Secret 3: appears in 4 combinations (100.00%)

Share analysis shows all shares with 0% error rate
Result: No wrong shares identified
```

### Error Detected Case (test1.json)
```
Secret frequency analysis:
  1. Secret 79836264049851: appears in 8 combinations (6.67%)

Share 2 identified with 100% error rate
Result: Share 2 corrupted, correct secret = 79836264049851
```

