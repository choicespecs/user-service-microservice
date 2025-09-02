# User Service Microservice
just a sample for learning

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


  curl -u guest:guest -X POST \
  http://localhost:15672/api/exchanges/%2F/user.exchange/publish \
  -H 'Content-Type: application/json' \
  -d '{
    "routing_key": "user.updated",
    "payload": "{\"action\":\"UPDATE\",\"user\":{\"username\":\"john\",\"email\":\"jack@example.com\"}}",
    "payload_encoding": "string",
    "properties": { "content_type": "application/json" }
  }'

curl -u guest:guest -X POST \
  http://localhost:15672/api/exchanges/%2F/user.exchange/publish \
  -H 'Content-Type: application/json' \
  -d '{
    "routing_key": "user.get",
    "payload": "{\"action\":\"GET\",\"user\":{\"email\":\"john@example.com\"}}",
    "payload_encoding": "string",
    "properties": {
      "content_type": "application/json",
      "headers": {
        "x-request-id": "req-12345"
      }
    }
  }'



curl -u guest:guest -X POST \
  http://localhost:15672/api/exchanges/%2F/user.exchange/publish \
  -H 'Content-Type: application/json' \
  -d '{
    "routing_key": "user.search",
    "payload": "{\"action\":\"SEARCH\",\"q\":\"john\",\"page\":0,\"size\":10,\"sortBy\":\"username\",\"sortDir\":\"asc\",\"includeDeleted\":false}",
    "payload_encoding": "string",
    "properties": {
      "content_type": "application/json",
      "headers": {
        "x-request-id": "req-search-001"
      }
    }
  }'


curl -u guest:guest -X POST \
  http://localhost:15672/api/exchanges/%2F/user.exchange/publish \
  -H 'Content-Type: application/json' \
  -d '{
    "routing_key": "user.search",
    "payload": "{\"action\":\"SEARCH\",\"user\":{\"email\":\"john@example.com\"},\"page\":0,\"size\":10,\"sortBy\":\"username\",\"sortDir\":\"asc\",\"includeDeleted\":false}",
    "payload_encoding": "string",
    "properties": {
      "content_type": "application/json",
      "headers": {
        "x-request-id": "req-search-001"
      }
    }
  }'



 we can debug events using this 

# create
curl -u guest:guest -X PUT http://localhost:15672/api/queues/%2F/debug.all -H 'Content-Type: application/json' -d '{"durable":false,"auto_delete":true}'
# bind to everything on your exchange
curl -u guest:guest -X POST http://localhost:15672/api/bindings/%2F/e/user.exchange/q/debug.all -H 'Content-Type: application/json' -d '{"routing_key":"#"}'
# fetch
curl -u guest:guest -X POST http://localhost:15672/api/queues/%2F/debug.all/get -H 'Content-Type: application/json' -d '{"count":50,"ackmode":"ack_requeue_false","encoding":"auto"}'