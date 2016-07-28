FROM greyrocksoftware/javastandalone

RUN apt-get update

RUN apt-get install git -y

ADD run_tests.sh /opt/

ENTRYPOINT ["/opt/run_tests.sh"]

