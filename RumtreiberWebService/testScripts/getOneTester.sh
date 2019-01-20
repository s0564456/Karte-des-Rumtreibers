#! /bin/sh

echo "--- REQUESTING USER 1 IN JSON--------"
curl -X GET \
     -H "Accept: application/json" \
     -v "http://localhost:8080/userJAXRS/rest/users/1"
echo " "
echo "-------------------------------------------------------------------------------------------------"

echo "--- REQUESTING USER 2 --------"
curl -X GET \
     -H "Accept: application/xml" \
     -v "http://localhost:8080/userJAXRS/rest/users/2"
echo " "
echo "-------------------------------------------------------------------------------------------------"


echo "--- REQUESTING USER 22 --------"
curl -X GET \
     -v "http://localhost:8080/userJAXRS/rest/users/22"
echo " "
echo "-------------------------------------------------------------------------------------------------"

echo "--- REQUESTING CONTACT with id NEW --------"
curl -X GET \
     -H "Accept: application/json" \
     -v "http://localhost:8080/userJAXRS/rest/users/NEW"
echo " "
echo "-------------------------------------------------------------------------------------------------"