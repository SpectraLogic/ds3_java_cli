FROM greyrocksoftware/javastandalone7:latest

RUN apt-get update

RUN apt-get install git -y

ADD run_tests.sh /opt/

ENTRYPOINT ["/opt/run_tests.sh"]

