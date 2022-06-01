# shardingsphere整合组件工程,在shardingsphere的5.0.0-alpha组件基础上实现个性化需求,目前支持的数据有oracle,mysql,dm,人大金仓
# 使用方式说明:
# 1.需要使用分表分库的服务pom.xml增加GAV坐标如下:
#       <dependency>
#			<artifactId>nontax3-sharding-integrate</artifactId>
#			<groupId>com.bosssoft.nontax3.sharding</groupId>
#			<version>xxx</version>
#		</dependency>
#
# 2.对应微服务开启分表分库功能:
#   外部化配置文件添加 spring.shardingsphere.enabled=true（支持properties,yaml等）
#
# 3.按照分表分库规则添加对应规则,以yaml文件配置为示例,具体如下:
#spring:
#  shardingsphere:
#    enabled: true
#    # 展示修改以后的sql语句
#    props:
#      sql-show: true
#    datasource:
#      common:
#        driver-class-name: oracle.jdbc.driver.OracleDriver
#        driver-class-name: dm.jdbc.driver.DmDriver
#        driver-class-name: com.mysql.jdbc.Driver
#        type: com.alibaba.druid.pool.DruidDataSource
#        initial-size: 5
#        min-idle: 1
#        max-active: 500
#        max-wait: 6000
#        validation-query: select 1 FROM DUAL
#        test-while-idle: true
#        test-on-borrow: false
#        test-on-return: false
#        pool-prepared-statements: true
#        max-open-prepared-statements: 20
#        max-pool-prepared-statement-per-connection-size: 20
#        connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
#        use-global-data-source-stat: true
#        filters: stat
#        filter:
#          slf4j:
#           enabled: true
#            result-set-log-error-enabled: false
#            result-set-log-enabled: false
#            connection-log-enabled: false
#            statement-log-enabled: true
#            statement-executable-sql-log-enable: true
#      names: db0
#      ORACLE
#      db0:
#        url: jdbc:oracle:thin:@172.18.166.172:1521/orcl
#        username: JLPJ0402
#        password: Bstdb@2021#

#      DM
#      db0:
#        url: jdbc:dm://172.18.166.148:5236
#        username: SHPJ1031
#        password: 1234567890

#      MYSQL
#      db0:
#        url: jdbc:mysql://172.18.166.235:3306/gaoxiao?prepStmtCacheSize=517&cachePrepStmts=true&autoReconnect=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true
#        username: root
#        password: root
#    rules:
#      sharding:
#        key-generators:
          # 此处必须要配置，否则会导致报错，因为shardingsphere-jdbc-core-spring-boot-starter需要加载此项配置，官网的demo例子有错
#          snowflake:
#            type: SNOWFLAKE
#            props:
#              worker-id: 123
#        defaultDatabaseStrategy:
#          none:
#        broadcastTables:
#          AFA_USER
#        bindingTables:
#          - UNE_CBILL,UNE_CBILL_ITEM
#          - UNE_CBILL,UNE_CBILL_ASSIST
#        tables:
#          une_cbill:
#            actualDataNodes: db0.une_cbill_$->{2021..2022}$->{['01','02','03','04','05','06','07','08','09','10','11','12']}
#            databaseStrategy:
#              standard:
#                shardingColumn: fversion
#                shardingAlgorithmName: database-hash
#            tableStrategy:
#              standard:
#                shardingColumn: fdate
#                shardingAlgorithmName: table-month
#          une_cbill_item:
#            actualDataNodes: db0.une_cbill_item_$->{2021..2022}$->{['01','02','03','04','05','06','07','08','09','10','11','12']}
#            databaseStrategy:
#              standard:
#                shardingColumn: fversion
#                shardingAlgorithmName: database-hash
#            tableStrategy:
#              standard:
#                shardingColumn: fdate
#                shardingAlgorithmName: table-month
#          une_cbill_assist:
#            actualDataNodes: db0.une_cbill_assist_$->{2021..2022}$->{['01','02','03','04','05','06','07','08','09','10','11','12']}
#            tableStrategy:
#              standard:
#                shardingColumn: fdate
#                shardingAlgorithmName: table-month
#        shardingAlgorithms:
#          table-month:
#            type: DATE
#            props:
#              date-pattern: 'yyyy-MM-dd'
#              date-lower: '2020-12-01'
#              date-upper: '2999-12-01'
#              sharding-suffix-pattern: yyyyMM
#              date-interval-unit: MONTHS
#          table-year:
#            type: DATE
#            props:
#              date-pattern: 'yyyy-MM-dd'
#              date-lower: '2020-12-01'
#              date-upper: '2999-12-01'
#              sharding-suffix-pattern: yyyy
#              date-interval-unit: YEARS
#          table-hash:
#            type: HASH_MOD
#            props:
#              sharding-count: '100'
#          database-hash:
#            type: MOD
#            props:
#              sharding-count: '1'
