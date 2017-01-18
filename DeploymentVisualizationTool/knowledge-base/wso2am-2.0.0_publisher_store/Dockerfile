FROM docker.wso2.com/wso2am:2.0.0

COPY wso2am-2.0.0_publisher_store/carbon/ /mnt/wso2-artifacts

COPY artifacts/mysql-connector-java-5.1.34-bin.jar /mnt/wso2-artifacts/repository/components/lib/

COPY wso2am-2.0.0_publisher_store/carbon/repository/components/api-publisher/configuration /mnt/wso2am-2.0.0/repository/components/api-publisher/

USER root