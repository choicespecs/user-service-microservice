# User Service Microservice
just a sample for learning

./build.sh

podman compose up --build

curl -u guest:guest -X POST \
  http://localhost:15672/api/exchanges/%2F/user.exchange/publish \
  -H 'Content-Type: application/json' \
  -d '{
    "routing_key": "user.created",
    "payload": "{\"action\":\"CREATE\",\"user\":{\"username\":\"john\",\"email\":\"john@example.com\",\"phone\":\"1234567890\",\"firstName\":\"John\",\"lastName\":\"Doe\"}}",
    "payload_encoding": "string",
    "properties": { "content_type": "application/json" }
  }'


curl -u guest:guest -X POST http://localhost:15672/api/exchanges/%2F/user.exchange/publish \
  -H 'Content-Type: application/json' \
  -d '{
    "routing_key": "user.delete",
    "payload": "{\"action\":\"DELETE\",\"email\":\"john@example.com\"}",
    "payload_encoding": "string",
    "properties": { "content_type": "application/json" }
  }'