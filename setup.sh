#!/bin/sh

# color
RED='\033[0;31m'
GREEN='\033[0;32m'
CYAL='\033[0;36m'
NC='\033[0m' # no color

REGISTRY_SERVER_URL=registry-server.tha.localdomain:443

# pull needed image
echo "${GREEN}BEGIN PULL & PUSH IMAGES...${NC}"

# postgres
echo "${CYAL}------- STEP 1: POSTGRES ALPINE${NC}"
docker pull postgres:14.1-alpine
docker tag postgres:14.1-alpine ${REGISTRY_SERVER_URL}/postgres:14.1-alpine
docker push ${REGISTRY_SERVER_URL}/postgres:14.1-alpine
docker image remove postgres:14.1-alpine

# nginx
echo "${CYAL}------- STEP 2: NGINX ALPINE${NC}"
docker pull nginx:1.21.4-alpine
docker tag nginx:1.21.4-alpine ${REGISTRY_SERVER_URL}/nginx:1.21.4-alpine
docker push ${REGISTRY_SERVER_URL}/nginx:1.21.4-alpine
docker image remove nginx:1.21.4-alpine

echo "${GREEN}FINISH PULL & PUSH IMAGES${NC}"

# e-contract microservices setup
BASE_DIR="$( cd "$( dirname "$0" )" && pwd )"
EC_APP=${BASE_DIR}"/application"
EC_DISCOVERY=$EC_APP"/ec-discovery-srv"
EC_CONFIG=$EC_APP"/ec-config-srv"
EC_GATEWAY=$EC_APP"/ec-gateway-srv"
EC_AUTH=$EC_APP"/ec-auth-srv"
EC_CUSTOMER=$EC_APP"/ec-customer-srv"
EC_CONTRACT=$EC_APP"/ec-contract-srv"
EC_BPMN=$EC_APP"/ec-bpmn-srv"
EC_FILE=$EC_APP"/ec-file-srv"
EC_NOTIFICATION=$EC_APP"/ec-notification-srv"


# remove all docker image
echo "${RED}BEGIN CLEAN OLDED SERVICES...${NC}"

docker image remove exec $(docker image ls | grep ${REGISTRY_SERVER_URL}\/ | awk '{print $3}')

echo "${RED}FINISH CLEAN OLDED SERVICES${NC}"

echo $BASE_DIR

echo "${GREEN}BEGIN BUILDING NEW IMAGES...${NC}"

# build discovery service image
echo "${CYAL}------- STEP 1: DISCOVERY IMAGE${NC}"

cd $EC_DISCOVERY

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-discovery-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-discovery-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-discovery-srv:1.0.0

# build config service image
echo "${CYAL}------- STEP 2: CONFIGURATION IMAGE${NC}"

cd $EC_CONFIG

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-config-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-config-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-config-srv:1.0.0

# build gateway service image
echo "${CYAL}------- STEP 3: GATEWAY IMAGE${NC}"

cd $EC_GATEWAY

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-gateway-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-gateway-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-gateway-srv:1.0.0

echo "${GREEN}LIST OF IMAGE${NC}"
# build auth service image
echo "${CYAL}------- STEP 4: AUTH IMAGE${NC}"

cd $EC_AUTH

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-auth-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-auth-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-auth-srv:1.0.0

echo "${GREEN}LIST OF IMAGE${NC}"

# build customer service image
echo "${CYAL}------- STEP 5: CUSTOMER IMAGE${NC}"

cd $EC_CUSTOMER

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-customer-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-customer-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-customer-srv:1.0.0

echo "${GREEN}LIST OF IMAGE${NC}"

# build contract service image
echo "${CYAL}------- STEP 6: CONTRACT IMAGE${NC}"

cd $EC_CONTRACT

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-contract-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-contract-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-contract-srv:1.0.0

echo "${GREEN}LIST OF IMAGE${NC}"

# build bpmn service image
echo "${CYAL}------- STEP 7: BPMN IMAGE${NC}"

cd $EC_BPMN

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-bpmn-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-bpmn-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-bpmn-srv:1.0.0

echo "${GREEN}LIST OF IMAGE${NC}"

# build file service image
echo "${CYAL}------- STEP 8: FILE IMAGE${NC}"

cd $EC_FILE

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-file-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-file-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-file-srv:1.0.0

echo "${GREEN}LIST OF IMAGE${NC}"

# build notification service image
echo "${CYAL}------- STEP 9: NOTIFICATION IMAGE${NC}"

cd $EC_NOTIFICATION

gradle clean
gradle bootJar

docker build --build-arg JAR_FILE=build/libs/\*.jar --tag ${REGISTRY_SERVER_URL}/ec-notification-srv:1.0.0 --label 1.0 .
docker push ${REGISTRY_SERVER_URL}/ec-notification-srv:1.0.0
docker image remove ${REGISTRY_SERVER_URL}/ec-notification-srv:1.0.0

echo "${GREEN}LIST OF IMAGE${NC}"

docker image ls | grep ${REGISTRY_SERVER_URL}\/

echo "${GREEN}SUCCESSFULLY${NC}"