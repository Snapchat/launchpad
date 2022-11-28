# File Upload

## Curl
```bash
curl -v -L -H "Authorization: Bearer <CAPI TOKEN>" -T <FILE_PATH> https://<LAUNCHPAD_URL>/v1/mpc/files/<FILE_NAME> | cat
```

## Python
```python
import requests

r = requests.put(
    'https:/<LAUNCHPAD_URL>/v1/mpc/files/<FILE_NAME>', 
    allow_redirects=False, 
    headers={'Authorization': 'Bearer <CAPI TOKEN>'},
)

with open('<FILE_PATH>','rb') as payload:
    r = requests.put(r.headers['Location'], data=payload)
```
