# Anbarinoo

A simple warehouse and financial application to control your business and products

## Features

- [x] Registration and saving your profile
- [x] Managing Products (Save,Update,Delete)
- [x] Managing Categories to categorize your products
- [x] Financial stuff like registering sell or buy records
- [x] Calculating your profit and loss based on sells and buys
- [x] Exporting reports in an excel file (under development)

### Some notes for developers:

- Used testcontainers so make sure to configure it in your machine by doing these steps:
- [x] install docker
- [x] pull postgres:13.1-alpine
- [x] run this: ```$ echo testcontainers.reuse.enable=true  > ~/.testcontainers.properties```

- Deploy and actuator folders are useless for now
- By running controller tests, rest_apis_docs folder will be generated in the root of project, presenting documentations
  for apis
- Used java 10 (var) and java 16 (record) features, make sure use jdk-16+(recommended 17)
- For tests, you may run one by one if encountered fails