#!/bin/bash

BASE_URL="http://localhost:8081/api/v1"
TIMESTAMP=$(date +%s)
EMAIL="comprehensive_${TIMESTAMP}@example.com"
PASSWORD="password123"

echo "=== 1. Auth: Signup ==="
SIGNUP_RES=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Comprehensive Tester\", \"email\": \"${EMAIL}\", \"password\": \"${PASSWORD}\"}")
echo "Signup Result: $SIGNUP_RES"
echo ""

echo "=== 2. Auth: Login ==="
LOGIN_RES=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"${EMAIL}\", \"password\": \"${PASSWORD}\"}")
echo "Login Result: $LOGIN_RES"
TOKEN=$(echo "$LOGIN_RES" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Extracted Token: $TOKEN"
echo ""

if [ -z "$TOKEN" ]; then
  echo "Error: Signup/Login failed, no token obtained. Exiting."
  exit 1
fi

HEADER="Authorization: Bearer $TOKEN"

echo "=== 3. Users: Get Profile ==="
curl -s -X GET "${BASE_URL}/users/me" -H "$HEADER"
echo -e "\n"

echo "=== 4. Users: Update Profile ==="
curl -s -X PUT "${BASE_URL}/users/me" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"name": "Comprehensive Tester Edited", "height": 180, "currentWeight": 75.5, "dailyCalorieGoal": 2200}'
echo -e "\n"

echo "=== 5. Users: Log Weight ==="
curl -s -X POST "${BASE_URL}/users/weight" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"weight": 76.2}'
echo -e "\n"

echo "=== 6. Water: Log Water ==="
curl -s -X POST "${BASE_URL}/water/log" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"amountMl": 600}'
echo -e "\n"

echo "=== 7. Meals: Log Meal ==="
curl -s -X POST "${BASE_URL}/meals" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"mealType": "LUNCH", "totalCalories": 550, "totalProtein": 30, "totalCarbs": 60, "totalFat": 15, "foodItems": [{"name": "Chicken Rice", "calories": 400.0, "protein": 25.0, "carbs": 50.0, "fat": 10.0, "servingSize": "1 plate"}]}'
echo -e "\n"

echo "=== 8. Fasting: Start Fast ==="
curl -s -X POST "${BASE_URL}/fasting/start" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"goalHours": 16}'
echo -e "\n"

echo "=== 9. Fasting: End Fast ==="
curl -s -X POST "${BASE_URL}/fasting/end" -H "$HEADER"
echo -e "\n"

echo "=== 10. Fasting: Get History ==="
curl -s -X GET "${BASE_URL}/fasting/history" -H "$HEADER"
echo -e "\n"

echo "=== 11. Foods: Search ==="
curl -s -X GET "${BASE_URL}/foods/search?q=Apple" -H "$HEADER"
echo -e "\n"

echo "=== 12. Foods: Details ==="
curl -s -X GET "${BASE_URL}/foods/123" -H "$HEADER"
echo -e "\n"

echo "=== 13. Foods: Recent ==="
curl -s -X GET "${BASE_URL}/foods/recent" -H "$HEADER"
echo -e "\n"

echo "=== 14. Foods: Favorites ==="
curl -s -X GET "${BASE_URL}/foods/favorites" -H "$HEADER"
echo -e "\n"

echo "=== 15. Foods: Add Favorite ==="
curl -s -X POST "${BASE_URL}/foods/favorites" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"foodId": 123}'
echo -e "\n"

echo "=== 16. Analytics: Daily ==="
curl -s -X GET "${BASE_URL}/analytics/daily" -H "$HEADER"
echo -e "\n"

echo "=== 17. AI: Meal Suggestions ==="
curl -s -X POST "${BASE_URL}/ai/meal-suggestions" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"goal": "FAT_LOSS"}'
echo -e "\n"

echo "=== 18. AI: Analyze Text ==="
curl -s -X POST "${BASE_URL}/ai/analyze-text" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d '{"text": "I ate 2 eggs and a slice of toast for breakfast"}'
echo -e "\n"

echo "=== 19. AI: Image Detection via Upload ==="
SAMOSA_IMAGE="/Users/akashsingh/IdeaProjects/CalorieTrackorApplication/android/screenshot.png"
UPLOAD_RES=$(curl -s -X POST "${BASE_URL}/media/upload" \
  -H "$HEADER" \
  -F "file=@${SAMOSA_IMAGE}")
IMAGE_URL=$(echo "$UPLOAD_RES" | grep -o '"imageUrl":"[^"]*' | cut -d'"' -f4)
echo "Uploaded Image URL/Data URI: ${IMAGE_URL:0:80}..."

DETECTION_RES=$(curl -s -X POST "${BASE_URL}/ai/detect-food" \
  -H "$HEADER" \
  -H "Content-Type: application/json" \
  -d "{
    \"imageUrl\": \"${IMAGE_URL}\"
  }")
echo "Detection Result: $DETECTION_RES"
echo ""

echo "=== Done Comprehensive Tests ==="
