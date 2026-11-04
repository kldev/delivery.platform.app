```text
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 


     execution: local
        script: .\flow.js
        output: -

     scenarios: (100.00%) 1 scenario, 50 max VUs, 2m30s max duration (incl. graceful stop):
              * delivery_flow: 50 iterations shared among 50 VUs (maxDuration: 2m0s, gracefulStop: 30s)
 THRESHOLDS 

    checks
    ✓ 'rate>0.95' rate=100.00%

    flow_success
    ✓ 'count>0' count=50

    http_req_failed
    ✓ 'rate<0.05' rate=0.00%


  █ TOTAL RESULTS 

    checks_total.......: 1588    191.91558/s
    checks_succeeded...: 100.00% 1588 out of 1588
    checks_failed......: 0.00%   0 out of 1588

    ✓ get drivers - status 200
    ✓ get drivers - has list
    ✓ create delivery - status 200/201
    ✓ create delivery - has id
    ✓ confirm delivery - status 204
    ✓ get pending payments - status 200
    ✓ payment created
    ✓ payment has id
    ✓ accept payment - status 204
    ✓ get assigned deliveries - status 200
    ✓ get assigned deliveries - has list
    ✓ delivery assigned
    ✓ assigned delivery has id
    ✓ assigned delivery has correct id
    ✓ assigned delivery has ASSIGNED status
    ✓ pickup delivery - status 204
    ✓ start delivery - status 204
    ✓ complete delivery - status 204
    ✓ get deliveries - status 200
    ✓ get payments - status 200
    ✓ get ledger entries - status 200
    ✓ complete payment - status 200
    ✓ get reconciliation entries - status 200

    CUSTOM
    flow_success...................: 50     6.042682/s

    HTTP
    http_req_duration..............: avg=56.74ms min=1.56ms med=22.43ms max=327.15ms p(90)=230.1ms p(95)=247.83ms
      { expected_response:true }...: avg=56.74ms min=1.56ms med=22.43ms max=327.15ms p(90)=230.1ms p(95)=247.83ms
    http_req_failed................: 0.00%  0 out of 994
    http_reqs......................: 994    120.128518/s

    EXECUTION
    iteration_duration.............: avg=7.99s   min=7.56s  med=8.07s   max=8.09s    p(90)=8.09s   p(95)=8.09s   
    iterations.....................: 50     6.042682/s
    vus............................: 43     min=43       max=50
    vus_max........................: 50     min=50       max=50

    NETWORK
    data_received..................: 18 MB  2.2 MB/s
    data_sent......................: 183 kB 22 kB/s




running (0m08.3s), 00/50 VUs, 50 complete and 0 interrupted iterations
delivery_flow ✓ [======================================] 50 VUs  0m08.1s/2m0s  50/50 shared iters
```