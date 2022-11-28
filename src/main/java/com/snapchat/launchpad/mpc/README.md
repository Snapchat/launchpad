# File Upload

## Curl
```bash
curl -v -L -H "Authorization: Bearer <CAPI_TOKEN>" -T <FILE_PATH> https://<LAUNCHPAD_URL>/v1/mpc/files/<FILENAME> | cat
```

## Python
```python
import requests

r = requests.put(
    'https:/<LAUNCHPAD_URL>/v1/mpc/files/<FILENAME>', 
    allow_redirects=False, 
    headers={'Authorization': 'Bearer <CAPI_TOKEN>'},
)

with open('<FILE_PATH>','rb') as payload:
    r = requests.put(r.headers['Location'], data=payload)
```

# Trigger MPC Job

## Curl
```bash
curl -v -H "Authorization: Bearer <CAPI_TOKEN>" -H "Content-Type: application/json" -d '{"experiment_id":"<EXPERIMENT_ID>","date_id":"<DATE_ID>","file_ids":["<FILENAME>"]}' https://<LAUNCHPAD_URL>/v1/mpc/jobs
```

## Python
```python
import requests

r = requests.post(
    'https://<LAUNCHPAD_URL>/v1/mpc/jobs',
    headers={'Content-Type': 'application/json'},
    data={
        'experiment_id': '<EXPERIMENT_ID>',
        'date_id': '<DATE_ID>',
        'file_ids':['<FILENAME>'],
    },
)
```
