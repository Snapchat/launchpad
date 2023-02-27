# File Upload

## Note
In Launchpad, ```<FILENAME>``` is used to uniquely identity the files uploaded. If the same ```<FILENAME>``` is being used in another upload, the later file will overwrite the previous file. 

## Curl
```bash
curl -v -L -H "Authorization: Bearer <CAPI_TOKEN>" -T <FILEPATH> https://<LAUNCHPAD_URL>/v1/mpc/files/<FILENAME> | cat
```

## Python
```python
import requests

r = requests.put(
    'https:/<LAUNCHPAD_URL>/v1/mpc/files/<FILENAME>', 
    allow_redirects=False, 
    headers={'Authorization': 'Bearer <CAPI_TOKEN>'},
)

with open('<FILEPATH>','rb') as payload:
    r = requests.put(r.headers['Location'], data=payload)
```

## Direct Upload (Not Recommended)
Alternatively, you may upload the file directly to S3 under this path. Recursive folders are not supported. ```<snap-launchpad-bucket-name>``` is the S3 bucket that is associated with this Launchpad instance.
```
s3://<snap-launchpad-bucket-name>/files/<FILENAME>
```

# Trigger MPC Lift Job

## Note
The MPC job will consider the conversions from the start date of the experiment to ```<DATE_ID>```. ```<DATE_ID>``` needs to be in ```<yyyy-mm-dd>``` format.

## Curl
```bash
curl -v -H "Authorization: Bearer <CAPI_TOKEN>" -H "Content-Type: application/json" -d '{"experiment_id":"<EXPERIMENT_ID>","date_id":"<DATE_ID>","file_ids":["<FILENAME>", "<ANOTHER_FILENAME>"]}' https://<LAUNCHPAD_URL>/v1/mpc/jobs
```

## Python
```python
import requests

r = requests.post(
    'https://<LAUNCHPAD_URL>/v1/mpc/jobs',
    headers={
        'Content-Type': 'application/json', 
        'Authorization': 'Bearer <CAPI_TOKEN>',
    },
    json={
        'experiment_id': '<EXPERIMENT_ID>',
        'date_id': '<DATE_ID>',
        'file_ids':['<FILENAME>', '<ANOTHER_FILENAME>'],
    },
)
print(r.content)
```

## Output
```
{
    'run_id': '<RUN_ID>', // Debugging purpose for reference when contacting Snap
    'job_id': '<JOB_ID>', // Used to check job status
    'job_status': '<RUNNING | FAILED | SUCCEEDED>',
    'message': '<SOME_MESSAGE>'
}
```

# Trigger MPC Attribution Job

## Note
The MPC job will consider the conversions on the date to ```<DATE_ID>```. ```<DATE_ID>``` needs to be in ```<yyyy-mm-dd>``` format. 

<CLICK_DAYS> is the max number of days that we will attribute for a "click" exposure. 
- Valid Click Days:[0, 1d,7d,28d] 

<IMPRESSION_DAYS> is the max number of days that we will attribute for a "non-click" exposure.
- Valid Impression Days: [0, 1d, 7d]

## Curl
```bash
curl -v -H "Authorization: Bearer <CAPI_TOKEN>" -H "Content-Type: application/json" -d '{"conversion_ids":["<CONVERSION-ID>", "<ANOTHER_CONVERSION_ID>"],"date_id":"<DATE_ID>","click_days":<CLICK_DAYS>, "impression_days":<IMPRESSION_DAYS>, "file_ids":["<FILENAME>", "<ANOTHER_FILENAME>"]}' https://<LAUNCHPAD_URL>/v1/mpc/attribution/jobs
```

## Python
```python
import requests

r = requests.post(
    'https://<LAUNCHPAD_URL>/v1/mpc/jobs',
    headers={
        'Content-Type': 'application/json', 
        'Authorization': 'Bearer <CAPI_TOKEN>',
    },
    json={
        'conversion_ids': ["<CONVERSION-ID>", "<ANOTHER_CONVERSION_ID>"],
        'date_id': '<DATE_ID>',
        'click_days': <CLICK_DAYS>,
        'impression_days': <IMPRESSION_DAYS>,
        'file_ids':['<FILENAME>', '<ANOTHER_FILENAME>'],
    },
)
print(r.content)
```

## Output
```
{
    'run_id': '<RUN_ID>', // Debugging purpose for reference when contacting Snap
    'job_id': '<JOB_ID>', // Used to check job status
    'job_status': '<RUNNING | FAILED | SUCCEEDED>',
    'message': '<SOME_MESSAGE>'
}
```

# Check Job Status

## Curl
```bash
curl -v -H "Authorization: Bearer <CAPI_TOKEN>" https://<LAUNCHPAD_URL>/v1/mpc/jobs/<JOB_ID>
```

## Python
```python
import requests

r = requests.get('https://<LAUNCHPAD_URL>/v1/mpc/jobs/<JOB_ID>', headers={'Authorization': 'Bearer <CAPI_TOKEN>'})
print(r.content)
```

## Output
```
{
    'run_id': '<RUN_ID>', // Debugging purpose for reference when contacting Snap
    'job_id': '<JOB_ID>', // Used to check job status
    'job_status': '<RUNNING | FAILED | SUCCEEDED>',
    'message': '<SOME_MESSAGE>'
}
```
