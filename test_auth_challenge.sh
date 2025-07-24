#!/bin/bash

echo "Testing authentication challenge with StatusPages..."

# Start the server in the background
cd server
./gradlew run &
SERVER_PID=$!

# Wait for server to start
echo "Waiting for server to start..."
sleep 10

# Test with invalid JWT token
echo "Testing with invalid JWT token..."
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
  -H "Authorization: Bearer invalid_token" \
  http://localhost:8080/api/products)

echo "Response:"
echo "$RESPONSE"

# Test with no JWT token
echo -e "\nTesting with no JWT token..."
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
  http://localhost:8080/api/products)

echo "Response:"
echo "$RESPONSE"

# Clean up
echo -e "\nStopping server..."
kill $SERVER_PID
wait $SERVER_PID 2>/dev/null

echo "Test completed."