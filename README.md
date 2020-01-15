# digdag-plugin-myjdbc

Digdag `myjdbc>` operator plugin to execute a query.

## Caution
This plugin is in development.
So, this plugin is not for general use for all databases.
If you want to use specific database, you should change sql in [this code](https://github.com/katsuyan/digdag-plugin-myjdbc/blob/master/src/main/java/com/github/katsuyan/digdag/plugin/myjdbc/MyjdbcConnection.java).

## configuration

```yaml
_export:
  plugin:
    repositories:
      - file:///Users/katsuya.tajima/src/github.com/katsuyan/digdag-plugin-myjdbc/build/repo
      # - https://jitpack.io
    dependencies:
      - com.github.katsuyan:digdag-plugin-myjdbc:0.1.1

  myjdbc:
    host: 127.0.0.1
    port: 5432
    user: digdag
    database: tmp
    driver_name: org.postgresql.Driver
    protocol: postgresql
    driver_path: postgresql-42.2.6.jar

+step1:
  myjdbc>: test.sql
  store_last_results: all

+step2:
  sh>: echo ${myjdbc.last_results}
```

Register db password into secrets.

local mode 

```
digdag secrets --local --set myjdbc.password
```

server mode 

```
digdag secrets --project <project> --set myjdbc.password
```
