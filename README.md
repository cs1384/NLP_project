# NLP_project

gogogo

# Raw Data

https://www.dropbox.com/sh/hm8c4aj36upyd2j/AABWOjkntvqXVHWGTCWpTQMma?dl=0

```
770672122  <###> Toy Story 3 <###> Animation;Kids & Family;Science Fiction & Fantasy;Comedy <###> 0.8 <###> amazing animation movies!
```

# Pool Server
```
httpRequest: http://127.0.0.1:20001/three?q=movie+name
response.body: json -> {"status":"success", "evaluation":"good"}

httpRequest: http://127.0.0.1:20001/seven?q=movie+name
response.body: json -> {"status":"success", "evaluation":"terrible"}
```
# Genre Server
```
httpRequest: http://127.0.0.1:20002/three?q=movie+name
response.body: json -> {"status":"success", "evaluation":"good"}

httpRequest: http://127.0.0.1:20002/seven?q=movie+name
response.body: json -> {"status":"success", "evaluation":"terrible"}
```


