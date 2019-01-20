#! /bin/sh

echo "--- REQUESTING ALL JSON USERS ------------"
curl -X GET \
     -v "http://localhost:8080/userJAXRS/rest/users"
echo " "
echo "-------------------------------------------------------------------------------------------------"

echo "--- REQUESTING ALL JSON USERS--------"
curl -X GET \
     -H "Accept: application/json" \
     -v "http://localhost:8080/userJAXRS/rest/users"
echo " "
echo "-------------------------------------------------------------------------------------------------"
