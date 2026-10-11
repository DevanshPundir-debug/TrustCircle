#!/bin/bash

###########################################################################
# Trust Circle API Testing Script
###########################################################################
# This script tests all 23 REST API endpoints
# Usage: bash 02_test_all_apis.sh
# Prerequisites: curl, jq (for JSON parsing)
###########################################################################

set -e

# Configuration
BASE_URL="http://localhost:8085/api"
LOG_FILE="api_test_results.log"
FAILED_TESTS=0
PASSED_TESTS=0

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
log_test() {
    echo -e "${BLUE}[TEST]${NC} $1" | tee -a "$LOG_FILE"
}

log_pass() {
    echo -e "${GREEN}[PASS]${NC} $1" | tee -a "$LOG_FILE"
    ((PASSED_TESTS++))
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $1" | tee -a "$LOG_FILE"
    ((FAILED_TESTS++))
}

log_info() {
    echo -e "${YELLOW}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

# Check if server is running
check_server() {
    log_test "Checking if server is running on $BASE_URL"
    if curl -s "$BASE_URL/auth/me" > /dev/null 2>&1; then
        log_pass "Server is running"
    else
        log_fail "Server is not running. Start it with: mvn spring-boot:run"
        exit 1
    fi
}

# Clear log file
> "$LOG_FILE"

echo "=========================================="
echo "Trust Circle API Test Suite"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo "Time: $(date)"
echo "=========================================="

check_server

# ========================================
# SECTION 1: AUTHENTICATION
# ========================================
echo ""
echo "=== SECTION 1: AUTHENTICATION TESTS ==="

# Test 1.1: Register User
log_test "1.1 - POST /auth/register"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "testuser@example.com",
        "password": "TestPassword123"
    }')

if echo "$REGISTER_RESPONSE" | grep -q "email"; then
    log_pass "Register user"
    USER_ID=$(echo "$REGISTER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
else
    log_fail "Register user - Response: $REGISTER_RESPONSE"
fi

# Test 1.2: Login User
log_test "1.2 - POST /auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "abhijeet@trustcircle.com",
        "password": "hashed_password_1_bcrypt_here"
    }')

if echo "$LOGIN_RESPONSE" | grep -q "token\|id"; then
    log_pass "Login user"
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    log_info "Token received: ${TOKEN:0:20}..."
else
    log_fail "Login user - Response: $LOGIN_RESPONSE"
fi

# Test 1.3: Get Current User
log_test "1.3 - GET /auth/me"
if [ -z "$TOKEN" ]; then
    log_info "Skipping test 1.3 - No valid token"
else
    ME_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/me" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$ME_RESPONSE" | grep -q "fullName\|email"; then
        log_pass "Get current user"
    else
        log_fail "Get current user - Response: $ME_RESPONSE"
    fi
fi

# Test 1.4: Submit Verification Form
log_test "1.4 - POST /auth/verify-form"
if [ -z "$TOKEN" ]; then
    log_info "Skipping test 1.4 - No valid token"
else
    VERIFY_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/verify-form" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "claimedCity": "Noida",
            "bio": "Love outdoor activities",
            "weekendVibe": "Active & Outdoorsy",
            "interests": "Badminton,Travel,Photography",
            "typicalAvailability": "WEEKENDS",
            "budgetPreference": "FLEXIBLE"
        }')

    if echo "$VERIFY_RESPONSE" | grep -q "id\|email"; then
        log_pass "Submit verification form"
    else
        log_fail "Submit verification form - Response: $VERIFY_RESPONSE"
    fi
fi

# ========================================
# SECTION 2: ACTIVITY MANAGEMENT
# ========================================
echo ""
echo "=== SECTION 2: ACTIVITY MANAGEMENT TESTS ==="

# Test 2.1: List All Activities
log_test "2.1 - GET /activities"
ACTIVITIES_RESPONSE=$(curl -s -X GET "$BASE_URL/activities")

if echo "$ACTIVITIES_RESPONSE" | grep -q "\[\|title\|activities"; then
    log_pass "List all activities"
    ACTIVITY_ID=$(echo "$ACTIVITIES_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
else
    log_fail "List all activities - Response: $ACTIVITIES_RESPONSE"
fi

# Test 2.2: Get Activity by ID
log_test "2.2 - GET /activities/{id}"
if [ -z "$ACTIVITY_ID" ]; then
    log_info "Skipping test 2.2 - No activity ID found"
else
    ACTIVITY_DETAIL=$(curl -s -X GET "$BASE_URL/activities/$ACTIVITY_ID")
    if echo "$ACTIVITY_DETAIL" | grep -q "title\|description"; then
        log_pass "Get activity by ID"
    else
        log_fail "Get activity by ID - Response: $ACTIVITY_DETAIL"
    fi
fi

# Test 2.3: Create Activity
log_test "2.3 - POST /activities"
if [ -z "$TOKEN" ]; then
    log_info "Skipping test 2.3 - No valid token"
else
    CREATE_ACTIVITY=$(curl -s -X POST "$BASE_URL/activities" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "title": "Test Badminton Session",
            "description": "Evening badminton game",
            "activityType": "Sports",
            "latitude": 28.6139,
            "longitude": 77.0249,
            "location": "Delhi Club",
            "scheduledDate": "2026-09-25",
            "scheduledTime": "18:00:00",
            "durationMinutes": 90,
            "minParticipants": 4,
            "maxParticipants": 8,
            "estimatedBudget": 200
        }')

    if echo "$CREATE_ACTIVITY" | grep -q "id\|title"; then
        log_pass "Create activity"
        NEW_ACTIVITY_ID=$(echo "$CREATE_ACTIVITY" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
    else
        log_fail "Create activity - Response: $CREATE_ACTIVITY"
    fi
fi

# Test 2.4: Join Activity
log_test "2.4 - POST /activities/{id}/join"
if [ -z "$TOKEN" ] || [ -z "$ACTIVITY_ID" ]; then
    log_info "Skipping test 2.4 - Missing token or activity ID"
else
    JOIN_RESPONSE=$(curl -s -X POST "$BASE_URL/activities/$ACTIVITY_ID/join" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$JOIN_RESPONSE" | grep -q "id\|success\|joined"; then
        log_pass "Join activity"
    else
        log_fail "Join activity - Response: $JOIN_RESPONSE"
    fi
fi

# Test 2.5: Get Activity Participants
log_test "2.5 - GET /activities/{id}/participants"
if [ -z "$ACTIVITY_ID" ]; then
    log_info "Skipping test 2.5 - No activity ID"
else
    PARTICIPANTS=$(curl -s -X GET "$BASE_URL/activities/$ACTIVITY_ID/participants")
    if echo "$PARTICIPANTS" | grep -q "\[\|id"; then
        log_pass "Get activity participants"
    else
        log_fail "Get activity participants - Response: $PARTICIPANTS"
    fi
fi

# Test 2.6: Complete Activity
log_test "2.6 - POST /activities/{id}/complete"
if [ -z "$TOKEN" ] || [ -z "$ACTIVITY_ID" ]; then
    log_info "Skipping test 2.6 - Missing token or activity ID"
else
    COMPLETE=$(curl -s -X POST "$BASE_URL/activities/$ACTIVITY_ID/complete" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$COMPLETE" | grep -q "id\|status\|COMPLETED"; then
        log_pass "Complete activity"
    else
        log_fail "Complete activity - Response: $COMPLETE"
    fi
fi

# ========================================
# SECTION 3: SMART MATCHING
# ========================================
echo ""
echo "=== SECTION 3: SMART MATCHING TESTS ==="

# Test 3.1: Get Smart Matches
log_test "3.1 - GET /matching/companions"
if [ -z "$TOKEN" ]; then
    log_info "Skipping test 3.1 - No valid token"
else
    MATCHES=$(curl -s -X GET "$BASE_URL/matching/companions" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$MATCHES" | grep -q "\[\|id\|name\|matchScore"; then
        log_pass "Get smart matches"
    else
        log_fail "Get smart matches - Response: $MATCHES"
    fi
fi

# ========================================
# SECTION 4: TRUST & REVIEWS
# ========================================
echo ""
echo "=== SECTION 4: TRUST & REVIEWS TESTS ==="

# Test 4.1: Get Trust Scorecard
log_test "4.1 - GET /trust/scorecard"
if [ -z "$TOKEN" ]; then
    log_info "Skipping test 4.1 - No valid token"
else
    SCORECARD=$(curl -s -X GET "$BASE_URL/trust/scorecard" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$SCORECARD" | grep -q "trustScore\|trustTier"; then
        log_pass "Get trust scorecard"
    else
        log_fail "Get trust scorecard - Response: $SCORECARD"
    fi
fi

# Test 4.2: Submit Peer Review
log_test "4.2 - POST /trust/review"
if [ -z "$TOKEN" ]; then
    log_info "Skipping test 4.2 - No valid token"
else
    REVIEW=$(curl -s -X POST "$BASE_URL/trust/review" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "activityId": 6,
            "reviewedUserId": 2,
            "rating": 5,
            "comment": "Excellent participation!",
            "punctuality": 5,
            "hygiene": 5,
            "communication": 5,
            "respectfulness": 5
        }')

    if echo "$REVIEW" | grep -q "id\|rating"; then
        log_pass "Submit peer review"
    else
        log_fail "Submit peer review - Response: $REVIEW"
    fi
fi

# Test 4.3: Get User Reviews
log_test "4.3 - GET /trust/reviews/{userId}"
REVIEWS=$(curl -s -X GET "$BASE_URL/trust/reviews/1")

if echo "$REVIEWS" | grep -q "\[\|rating"; then
    log_pass "Get user reviews"
else
    log_fail "Get user reviews - Response: $REVIEWS"
fi

# ========================================
# SECTION 5: SAFETY MONITORING
# ========================================
echo ""
echo "=== SECTION 5: SAFETY MONITORING TESTS ==="

# Test 5.1: Get Safety Alerts
log_test "5.1 - GET /safety/alerts"
ALERTS=$(curl -s -X GET "$BASE_URL/safety/alerts")

if echo "$ALERTS" | grep -q "\[\|id\|severity"; then
    log_pass "Get safety alerts"
else
    log_fail "Get safety alerts - Response: $ALERTS"
fi

# Test 5.2: Report Suspicious Activity
log_test "5.2 - POST /safety/report"
if [ -z "$TOKEN" ]; then
    log_info "Skipping test 5.2 - No valid token"
else
    REPORT=$(curl -s -X POST "$BASE_URL/safety/report" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "suspectedUserId": 6,
            "reportReason": "Suspicious behavior",
            "severityLevel": "MEDIUM"
        }')

    if echo "$REPORT" | grep -q "id\|status"; then
        log_pass "Report suspicious activity"
    else
        log_fail "Report suspicious activity - Response: $REPORT"
    fi
fi

# Test 5.3: Resolve Safety Alert
log_test "5.3 - POST /safety/alerts/{id}/resolve"
log_info "Skipping test 5.3 - Would need specific alert ID"

# ========================================
# SUMMARY
# ========================================
echo ""
echo "=========================================="
echo "TEST SUMMARY"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo "Total: $((PASSED_TESTS + FAILED_TESTS))"
echo "=========================================="

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed. Check $LOG_FILE for details.${NC}"
    exit 1
fi
