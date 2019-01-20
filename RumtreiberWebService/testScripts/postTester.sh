#! /bin/sh

echo "--- POSTING A JSON CONTACT ------------------"
curl -X POST \
     -H "Content-Type: application/json" \
     -H "Accept: text/plain" \
     -d "@oneUser.json" \
     -v "http://localhost:8080/userJAXRS/rest/users"
echo " "
echo "-------------------------------------------------------------------------------------------------"
