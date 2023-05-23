#!/bin/bash

get_names() {
    curl http://localhost:8081/subjects
}

# results=$(get_names)

readarray my_array < <(get_names | jq -r '.[]')

# debug print my_array values
for i in "${!my_array[@]}"; do
  subject=${my_array[$i]}
  subject=${subject//$'\n'/}
  echo "Will download schema for subject ${my_array[$i]}"
  echo "Will curl http://localhost:8081/subjects/$subject/versions/latest/schema"
  curl http://localhost:8081/subjects/$subject/versions/latest/schema > src/main/avro/$subject.avsc
done
